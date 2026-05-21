package com.nlespam.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import com.nlespam.helpers.HexUtils
import com.nlespam.models.*
import com.nlespam.ui.NleSpamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBleScreen(viewModel: NleSpamViewModel) {
    val isRunning by viewModel.engine.isRunning.collectAsState()
    val packetsSentState = viewModel.engine.packetsSent.collectAsState()

    var serviceUuidHex by remember { mutableStateOf("0000fe2c-0000-1000-8000-00805f9b34fb") }
    var serviceDataHex by remember { mutableStateOf("DAE096") }
    var manufacturerIdStr by remember { mutableStateOf("76") }
    var manufacturerDataHex by remember { mutableStateOf("") }
    var deviceLabel by remember { mutableStateOf("Custom Device") }
    var useServiceData by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun buildAndStart() {
        errorMsg = null
        try {
            val adSet = if (useServiceData) {
                val uuid = android.os.ParcelUuid.fromString(serviceUuidHex.trim())
                val data = HexUtils.decodeHex(serviceDataHex.trim().replace(" ", ""))
                AdvertisementSet(
                    title = deviceLabel,
                    target = AdvertisementTarget.ANDROID,
                    type = SpamType.FAST_PAIR,
                    serviceData = ServiceData(uuid, data),
                    includeTxPower = true,
                )
            } else {
                val mfId = manufacturerIdStr.trim().toInt()
                val data = HexUtils.decodeHex(manufacturerDataHex.trim().replace(" ", ""))
                AdvertisementSet(
                    title = deviceLabel,
                    target = AdvertisementTarget.ANDROID,
                    type = SpamType.FAST_PAIR,
                    manufacturerData = ManufacturerData(mfId, data),
                )
            }
            viewModel.engine.start(listOf(adSet))
        } catch (e: Exception) {
            errorMsg = "Error: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom BLE") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Mode toggle
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Payload Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = useServiceData,
                            onClick = { useServiceData = true },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) { Text("Service Data") }
                        SegmentedButton(
                            selected = !useServiceData,
                            onClick = { useServiceData = false },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) { Text("Manufacturer") }
                    }
                }
            }

            // Label
            OutlinedTextField(
                value = deviceLabel,
                onValueChange = { deviceLabel = it },
                label = { Text("Device Label") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, null) },
            )

            if (useServiceData) {
                // Service UUID
                OutlinedTextField(
                    value = serviceUuidHex,
                    onValueChange = { serviceUuidHex = it },
                    label = { Text("Service UUID") },
                    placeholder = { Text("0000fe2c-0000-1000-8000-00805f9b34fb") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    supportingText = { Text("Fast Pair: 0000fe2c…  Samsung: 0000fd5a…") },
                )
                // Service Data HEX
                OutlinedTextField(
                    value = serviceDataHex,
                    onValueChange = { serviceDataHex = it.uppercase() },
                    label = { Text("Service Data (HEX)") },
                    placeholder = { Text("e.g. DAE096") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                )
            } else {
                // Manufacturer ID
                OutlinedTextField(
                    value = manufacturerIdStr,
                    onValueChange = { manufacturerIdStr = it },
                    label = { Text("Manufacturer ID (decimal)") },
                    placeholder = { Text("76 = Apple, 117 = Samsung, 6 = Microsoft") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = { Text("76=Apple  117=Samsung  6=Microsoft  255=Typo") },
                )
                // Manufacturer Data HEX
                OutlinedTextField(
                    value = manufacturerDataHex,
                    onValueChange = { manufacturerDataHex = it.uppercase() },
                    label = { Text("Manufacturer Data (HEX)") },
                    placeholder = { Text("e.g. 071907010B00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    singleLine = true,
                )
            }

            // Error
            errorMsg?.let { err ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Stats when running
            AnimatedVisibility(
                visible = isRunning,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 550f)),
                exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 550f)),
            ) {
                CustomBleStats(
                    packetsSentProvider = { packetsSentState.value }
                )
            }

            // Start / Stop
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            val squishScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else if (isRunning) 1.04f else 1f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "squish",
            )

            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            Button(
                onClick = { if (isRunning) viewModel.stopSpam() else buildAndStart() },
                modifier = Modifier.fillMaxWidth().height(56.dp).scale(squishScale),
                interactionSource = interactionSource,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.WifiTethering,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isRunning) "STOP BROADCAST" else "START BROADCAST",
                    fontWeight = FontWeight.Bold,
                )
            }

            // Quick presets
            Text("Quick Presets", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            val presets = listOf(
                Triple("Fast Pair (Google)", "0000fe2c-0000-1000-8000-00805f9b34fb", "DAE096"),
                Triple("Fast Pair (Pixel Buds Pro)", "0000fe2c-0000-1000-8000-00805f9b34fb", "9ADB11"),
                Triple("Fast Pair (Bose NC700)", "0000fe2c-0000-1000-8000-00805f9b34fb", "CD8256"),
            )

            presets.forEach { (label, uuid, data) ->
                OutlinedCard(
                    onClick = {
                        deviceLabel = label
                        serviceUuidHex = uuid
                        serviceDataHex = data
                        useServiceData = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(data, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun CustomBleStats(packetsSentProvider: () -> Long) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Broadcasting", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("${packetsSentProvider()} pkts", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
