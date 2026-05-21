package com.nlespam.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.SendAndArchive
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlespam.ui.NleSpamViewModel
import kotlinx.coroutines.*
import java.io.InputStream
import java.util.UUID

private val OPP_UUID: UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb")

data class DiscoveredDevice(
    val address: String,
    val name: String?,
    val rssi: Int,
    val sendStatus: SendStatus = SendStatus.PENDING,
)

enum class SendStatus {
    PENDING, SENDING, SUCCESS, FAILED
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothFileSenderScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val btManager = remember { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    val btAdapter = remember { btManager.adapter }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<Long>(0L) }

    var isScanning by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var discoveredDevices by remember { mutableStateOf(listOf<DiscoveredDevice>()) }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIdx >= 0) selectedFileName = c.getString(nameIdx)
                    if (sizeIdx >= 0) selectedFileSize = c.getLong(sizeIdx)
                }
            }
        }
    }

    // BT Classic discovery receiver
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()

                        device?.let { d ->
                            val addr = d.address
                            if (discoveredDevices.none { it.address == addr }) {
                                discoveredDevices = discoveredDevices + DiscoveredDevice(
                                    address = addr,
                                    name = try { d.name } catch (_: SecurityException) { null },
                                    rssi = rssi,
                                )
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        isScanning = false
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        onDispose {
            try { btAdapter?.cancelDiscovery() } catch (_: SecurityException) { }
            try { context.unregisterReceiver(receiver) } catch (_: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth File Blast") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Scan toggle
                    FilledTonalIconButton(
                        onClick = {
                            if (isScanning) {
                                try { btAdapter?.cancelDiscovery() } catch (_: SecurityException) { }
                                isScanning = false
                            } else {
                                discoveredDevices = emptyList()
                                try { btAdapter?.startDiscovery() } catch (_: SecurityException) { }
                                isScanning = true
                            }
                        },
                    ) {
                        Icon(
                            if (isScanning) Icons.Default.Stop else Icons.AutoMirrored.Filled.BluetoothSearching,
                            if (isScanning) "Stop" else "Scan"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            // File selection card
            val cardColor by animateColorAsState(
                targetValue = if (selectedFileUri != null) MaterialTheme.colorScheme.primaryContainer
                              else MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "fileCardColor",
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = cardColor,
                onClick = { filePicker.launch("*/*") },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (selectedFileUri != null) Icons.Default.Description else Icons.Default.FileOpen,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = if (selectedFileUri != null) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedFileName ?: "Tap to select a file",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (selectedFileUri != null) formatFileSize(selectedFileSize) else "Photo, ZIP, APK, document...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Quick file type row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = { filePicker.launch("image/*") }, Modifier.weight(1f)) {
                    Icon(Icons.Default.Image, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Photo", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(onClick = { filePicker.launch("application/zip") }, Modifier.weight(1f)) {
                    Icon(Icons.Default.FolderZip, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Archive", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(onClick = { filePicker.launch("application/vnd.android.package-archive") }, Modifier.weight(1f)) {
                    Icon(Icons.Default.Android, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("APK", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Send to all button
            Button(
                onClick = {
                    val uri = selectedFileUri ?: return@Button
                    if (discoveredDevices.isEmpty()) {
                        Toast.makeText(context, "No devices discovered. Scan first!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSending = true
                    // Stop discovery before connecting
                    try { btAdapter?.cancelDiscovery() } catch (_: SecurityException) { }
                    isScanning = false

                    scope.launch(Dispatchers.IO) {
                        val devices = discoveredDevices.toList()
                        for (device in devices) {
                            // Update status to SENDING
                            withContext(Dispatchers.Main) {
                                discoveredDevices = discoveredDevices.map {
                                    if (it.address == device.address) it.copy(sendStatus = SendStatus.SENDING) else it
                                }
                            }

                            val status = try {
                                val btDevice = btAdapter?.getRemoteDevice(device.address)
                                if (btDevice != null) {
                                    sendFileToDevice(context, btDevice, uri)
                                    SendStatus.SUCCESS
                                } else {
                                    SendStatus.FAILED
                                }
                            } catch (_: Exception) {
                                SendStatus.FAILED
                            }

                            withContext(Dispatchers.Main) {
                                discoveredDevices = discoveredDevices.map {
                                    if (it.address == device.address) it.copy(sendStatus = status) else it
                                }
                            }
                        }

                        withContext(Dispatchers.Main) {
                            isSending = false
                            val successCount = discoveredDevices.count { it.sendStatus == SendStatus.SUCCESS }
                            Toast.makeText(context, "Sent to $successCount/${devices.size} devices", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedFileUri != null && discoveredDevices.isNotEmpty() && !isSending,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.SendAndArchive, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isSending) "Sending to ${discoveredDevices.size} devices..."
                           else "Blast to All (${discoveredDevices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Discovered devices list
            Text(
                text = if (isScanning) "Scanning..." else "Discovered Devices (${discoveredDevices.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(4.dp))

            if (discoveredDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Filled.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (isScanning) "Looking for Bluetooth devices..." else "Tap scan to find devices nearby",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(discoveredDevices) { device ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Status icon
                                val statusIcon = when (device.sendStatus) {
                                    SendStatus.PENDING -> Icons.Default.Bluetooth
                                    SendStatus.SENDING -> Icons.Default.Upload
                                    SendStatus.SUCCESS -> Icons.Default.CheckCircle
                                    SendStatus.FAILED -> Icons.Default.Error
                                }
                                val statusColor by animateColorAsState(
                                    targetValue = when (device.sendStatus) {
                                        SendStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                                        SendStatus.SENDING -> MaterialTheme.colorScheme.primary
                                        SendStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                                        SendStatus.FAILED -> MaterialTheme.colorScheme.error
                                    },
                                    label = "statusColor"
                                )
                                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(24.dp))

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name ?: "Unknown Device",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = device.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Text(
                                    text = "${device.rssi} dBm",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun sendFileToDevice(context: Context, device: BluetoothDevice, fileUri: Uri): Boolean {
    var socket: BluetoothSocket? = null
    var inputStream: InputStream? = null
    return try {
        socket = device.createRfcommSocketToServiceRecord(OPP_UUID)
        socket.connect()

        inputStream = context.contentResolver.openInputStream(fileUri)
        val outputStream = socket.outputStream

        // OBEX-like raw file push: just stream the bytes over the RFCOMM channel
        // Real OBEX has headers but many devices accept raw streams on OPP UUID
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1 && bytesRead > 0) {
            outputStream.write(buffer, 0, bytesRead)
            outputStream.flush()
        }

        true
    } catch (_: Exception) {
        false
    } finally {
        try { inputStream?.close() } catch (_: Exception) { }
        try { socket?.close() } catch (_: Exception) { }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
    }
}
