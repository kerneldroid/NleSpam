package com.nlespam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.nlespam.ui.NleSpamViewModel

data class UuidEntry(
    val uuid16: String,
    val name: String,
    val category: String,
)

private val KNOWN_UUIDS = listOf(
    UuidEntry("0x1800", "Generic Access", "GATT Service"),
    UuidEntry("0x1801", "Generic Attribute", "GATT Service"),
    UuidEntry("0x1802", "Immediate Alert", "GATT Service"),
    UuidEntry("0x1803", "Link Loss", "GATT Service"),
    UuidEntry("0x1804", "Tx Power", "GATT Service"),
    UuidEntry("0x1805", "Current Time", "GATT Service"),
    UuidEntry("0x1808", "Glucose", "Health"),
    UuidEntry("0x180A", "Device Information", "GATT Service"),
    UuidEntry("0x180D", "Heart Rate", "Health"),
    UuidEntry("0x180F", "Battery Service", "GATT Service"),
    UuidEntry("0x1810", "Blood Pressure", "Health"),
    UuidEntry("0x1812", "Human Interface Device", "HID"),
    UuidEntry("0x1813", "Scan Parameters", "GATT Service"),
    UuidEntry("0x1816", "Cycling Speed and Cadence", "Fitness"),
    UuidEntry("0x1818", "Cycling Power", "Fitness"),
    UuidEntry("0x1819", "Location and Navigation", "Fitness"),
    UuidEntry("0x181A", "Environmental Sensing", "Sensor"),
    UuidEntry("0x181C", "User Data", "GATT Service"),
    UuidEntry("0x181D", "Weight Scale", "Health"),
    UuidEntry("0x1822", "Pulse Oximeter", "Health"),
    UuidEntry("0x1826", "Fitness Machine", "Fitness"),
    UuidEntry("0x1827", "Mesh Provisioning", "Mesh"),
    UuidEntry("0x1828", "Mesh Proxy", "Mesh"),
    UuidEntry("0x183A", "Insulin Delivery", "Health"),
    UuidEntry("0x2A00", "Device Name", "Characteristic"),
    UuidEntry("0x2A01", "Appearance", "Characteristic"),
    UuidEntry("0x2A02", "Peripheral Privacy Flag", "Characteristic"),
    UuidEntry("0x2A04", "Peripheral Preferred Connection", "Characteristic"),
    UuidEntry("0x2A05", "Service Changed", "Characteristic"),
    UuidEntry("0x2A06", "Alert Level", "Characteristic"),
    UuidEntry("0x2A07", "Tx Power Level", "Characteristic"),
    UuidEntry("0x2A19", "Battery Level", "Characteristic"),
    UuidEntry("0x2A23", "System ID", "Characteristic"),
    UuidEntry("0x2A24", "Model Number String", "Characteristic"),
    UuidEntry("0x2A25", "Serial Number String", "Characteristic"),
    UuidEntry("0x2A26", "Firmware Revision", "Characteristic"),
    UuidEntry("0x2A27", "Hardware Revision", "Characteristic"),
    UuidEntry("0x2A28", "Software Revision", "Characteristic"),
    UuidEntry("0x2A29", "Manufacturer Name", "Characteristic"),
    UuidEntry("0x2A37", "Heart Rate Measurement", "Characteristic"),
    UuidEntry("0x2A38", "Body Sensor Location", "Characteristic"),
    UuidEntry("0xFE2C", "Google Fast Pair / Quick Share", "Vendor"),
    UuidEntry("0xFD6F", "Apple Exposure Notification", "Vendor"),
    UuidEntry("0xFE07", "Apple Handoff", "Vendor"),
    UuidEntry("0xFEAA", "Eddystone (Google)", "Vendor"),
    UuidEntry("0xFFF0", "Xiaomi Mi Band", "Vendor"),
    UuidEntry("0xFEE7", "Tencent", "Vendor"),
    UuidEntry("0xFE9F", "Google", "Vendor"),
    UuidEntry("0xFEBB", "Adafruit", "Vendor"),
    UuidEntry("0xFFE0", "HM-10 BLE Module", "Vendor"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UuidDatabaseScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categories = remember { KNOWN_UUIDS.map { it.category }.distinct().sorted() }

    val filtered = remember(searchQuery, selectedCategory) {
        KNOWN_UUIDS.filter { entry ->
            val matchesSearch = searchQuery.isBlank() ||
                    entry.name.contains(searchQuery, ignoreCase = true) ||
                    entry.uuid16.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || entry.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UUID Database") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search UUID or name") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(Modifier.height(8.dp))

            // Category filter chips
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SegmentedButton(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    shape = SegmentedButtonDefaults.itemShape(0, categories.size + 1),
                ) { Text("All", style = MaterialTheme.typography.labelSmall, maxLines = 1) }

                categories.forEachIndexed { idx, cat ->
                    SegmentedButton(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        shape = SegmentedButtonDefaults.itemShape(idx + 1, categories.size + 1),
                    ) { Text(cat, style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "${filtered.size} entries",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(filtered) { entry ->
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
                            Text(
                                text = entry.uuid16,
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(72.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(entry.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
