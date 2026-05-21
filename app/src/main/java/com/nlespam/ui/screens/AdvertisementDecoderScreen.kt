package com.nlespam.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlespam.ui.NleSpamViewModel

data class DecodedAd(
    val address: String,
    val deviceName: String?,
    val rssi: Int,
    val flags: String?,
    val serviceUuids: List<String>,
    val manufacturerData: List<Pair<Int, String>>,
    val serviceData: List<Pair<String, String>>,
    val txPowerLevel: Int?,
    val rawHex: String,
    val timestamp: Long,
)

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisementDecoderScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val btManager = remember { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    val scanner = remember { btManager.adapter?.bluetoothLeScanner }

    var isScanning by remember { mutableStateOf(false) }
    var decodedAds by remember { mutableStateOf(listOf<DecodedAd>()) }
    var scanCallback by remember { mutableStateOf<ScanCallback?>(null) }
    var expandedAddress by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            scanCallback?.let { cb ->
                try { scanner?.stopScan(cb) } catch (_: SecurityException) { }
            }
        }
    }

    fun startScan() {
        val sc = scanner ?: return
        isScanning = true
        decodedAds = emptyList()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val record = result.scanRecord ?: return
                val addr = result.device?.address ?: return
                val name = try { result.device?.name } catch (_: SecurityException) { null }

                // Decode flags
                val flags = record.advertiseFlags.let { f ->
                    if (f >= 0) {
                        buildString {
                            if (f and 0x01 != 0) append("LE Limited Disc, ")
                            if (f and 0x02 != 0) append("LE General Disc, ")
                            if (f and 0x04 != 0) append("BR/EDR Not Supported, ")
                            if (f and 0x08 != 0) append("LE+BR/EDR Controller, ")
                            if (f and 0x10 != 0) append("LE+BR/EDR Host, ")
                        }.trimEnd(',', ' ').ifEmpty { "0x${f.toString(16)}" }
                    } else null
                }

                // Service UUIDs
                val serviceUuids = record.serviceUuids?.map { it.toString() } ?: emptyList()

                // Manufacturer data
                val mfData = mutableListOf<Pair<Int, String>>()
                val mfSparse = record.manufacturerSpecificData
                if (mfSparse != null) {
                    for (i in 0 until mfSparse.size()) {
                        val id = mfSparse.keyAt(i)
                        val data = mfSparse.valueAt(i)
                        mfData.add(id to (data?.joinToString("") { "%02X".format(it) } ?: ""))
                    }
                }

                // Service data
                val svcData = record.serviceData?.map { (uuid, data) ->
                    uuid.toString() to (data?.joinToString("") { "%02X".format(it) } ?: "")
                } ?: emptyList()

                // Raw hex
                val rawHex = record.bytes?.joinToString("") { "%02X".format(it) } ?: ""

                val decoded = DecodedAd(
                    address = addr,
                    deviceName = name ?: record.deviceName,
                    rssi = result.rssi,
                    flags = flags,
                    serviceUuids = serviceUuids,
                    manufacturerData = mfData,
                    serviceData = svcData,
                    txPowerLevel = record.txPowerLevel.takeIf { it != Int.MIN_VALUE },
                    rawHex = rawHex,
                    timestamp = System.currentTimeMillis(),
                )

                // Replace or add (by address), cap at 50
                val current = decodedAds.toMutableList()
                val idx = current.indexOfFirst { it.address == addr }
                if (idx >= 0) current[idx] = decoded
                else { current.add(0, decoded); if (current.size > 50) current.removeLast() }
                decodedAds = current
            }
        }

        scanCallback = cb
        try { sc.startScan(null, settings, cb) } catch (_: SecurityException) { isScanning = false }
    }

    fun stopScan() {
        isScanning = false
        scanCallback?.let { cb ->
            try { scanner?.stopScan(cb) } catch (_: SecurityException) { }
        }
        scanCallback = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ad Decoder") },
                navigationIcon = {
                    IconButton(onClick = { stopScan(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { if (isScanning) stopScan() else startScan() },
                    ) {
                        Icon(
                            if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            if (isScanning) "Stop" else "Scan"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        if (decodedAds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DataObject,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (isScanning) "Capturing advertisements..." else "Tap ▶ to capture BLE ads",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(decodedAds) { ad ->
                    val isExpanded = expandedAddress == ad.address
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        onClick = {
                            expandedAddress = if (isExpanded) null else ad.address
                        },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            // Header row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Bluetooth,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        ad.deviceName ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        ad.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    "${ad.rssi} dBm",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            // Expanded details
                            if (isExpanded) {
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(8.dp))

                                ad.flags?.let {
                                    DecodedRow("Flags", it)
                                }
                                ad.txPowerLevel?.let {
                                    DecodedRow("TX Power", "$it dBm")
                                }
                                if (ad.serviceUuids.isNotEmpty()) {
                                    DecodedRow("Service UUIDs", ad.serviceUuids.joinToString("\n"))
                                }
                                ad.manufacturerData.forEach { (id, hex) ->
                                    val vendorName = when (id) {
                                        76 -> "Apple (0x004C)"
                                        6 -> "Microsoft (0x0006)"
                                        117 -> "Samsung (0x0075)"
                                        224 -> "Google (0x00E0)"
                                        else -> "0x${"%04X".format(id)}"
                                    }
                                    DecodedRow("Manufacturer ($vendorName)", hex)
                                }
                                ad.serviceData.forEach { (uuid, hex) ->
                                    DecodedRow("Service Data ($uuid)", hex)
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Raw Payload",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(2.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(
                                        ad.rawHex.chunked(2).joinToString(" "),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .horizontalScroll(rememberScrollState()),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecodedRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(120.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}
