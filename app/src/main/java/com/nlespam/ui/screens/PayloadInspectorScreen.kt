package com.nlespam.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlespam.models.SpamRadarEntry
import com.nlespam.ui.NleSpamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayloadInspectorScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    val isScanning by viewModel.engine.isScanning.collectAsState()
    val scanResults by viewModel.engine.scanResults.collectAsState()
    var selectedEntry by remember { mutableStateOf<SpamRadarEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payload Inspector") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedEntry != null) selectedEntry = null else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (selectedEntry == null) {
                        FilledTonalIconButton(
                            onClick = {
                                if (isScanning) viewModel.stopRadar() else viewModel.startRadar()
                            },
                        ) {
                            Icon(
                                if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                if (isScanning) "Stop" else "Scan"
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        if (selectedEntry != null) {
            // Detail view — hex dump
            val entry = selectedEntry!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            ) {
                // Header info
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = entry.deviceAddress,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                ) {
                                    Text(
                                        text = entry.detectedType,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                ) {
                                    Text(
                                        text = "RSSI: ${entry.rssi} dBm",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                    )
                                }
                            }
                            if (entry.manufacturerId != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Manufacturer ID: 0x${"%04X".format(entry.manufacturerId)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }

                // Hex dump
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Raw Payload (HEX)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(Modifier.height(12.dp))

                            val hex = entry.rawPayloadHex
                            if (hex.isNotEmpty()) {
                                // Format as hex rows of 16 bytes
                                val bytes = hex.chunked(2)
                                val rows = bytes.chunked(16)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        rows.forEachIndexed { rowIdx, row ->
                                            val offset = "%04X".format(rowIdx * 16)
                                            val hexPart = row.joinToString(" ")
                                            val asciiPart = row.map { byteHex ->
                                                val b = byteHex.toIntOrNull(16) ?: 0
                                                if (b in 32..126) b.toChar() else '.'
                                            }.joinToString("")

                                            Text(
                                                text = "$offset  $hexPart  |$asciiPart|",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No payload data available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Payload length info
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Payload Size",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${entry.rawPayloadHex.length / 2} bytes (${entry.rawPayloadHex.length} hex chars)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Device picker list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                if (scanResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (isScanning) "Scanning for devices..." else "Start a scan to inspect payloads",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Tap a device to inspect its payload",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                    ) {
                        itemsIndexed(scanResults) { index, entry ->
                            val cornerRadius = 24.dp
                            val connectionRadius = 4.dp
                            val isFirst = index == 0
                            val isLast = index == scanResults.lastIndex

                            val shape = when {
                                isFirst && isLast -> RoundedCornerShape(cornerRadius)
                                isFirst -> RoundedCornerShape(
                                    topStart = cornerRadius, topEnd = cornerRadius,
                                    bottomStart = connectionRadius, bottomEnd = connectionRadius
                                )
                                isLast -> RoundedCornerShape(
                                    topStart = connectionRadius, topEnd = connectionRadius,
                                    bottomStart = cornerRadius, bottomEnd = cornerRadius
                                )
                                else -> RoundedCornerShape(connectionRadius)
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = shape,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                onClick = { selectedEntry = entry },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.deviceAddress,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                        Text(
                                            text = "${entry.detectedType} • ${entry.rawPayloadHex.length / 2} bytes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Inspect",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
