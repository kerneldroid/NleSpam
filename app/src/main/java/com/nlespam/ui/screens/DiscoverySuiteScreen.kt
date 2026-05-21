package com.nlespam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlespam.ui.NleSpamViewModel
import com.nlespam.ui.components.SpamCategoryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverySuiteScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discovery Suite", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpamCategoryCard(
                title = "BLE Device Scanner",
                subtitle = "Scan & browse all nearby BLE devices",
                icon = Icons.AutoMirrored.Filled.BluetoothSearching,
                deviceCount = 0,
                isActive = false,
                onClick = { viewModel.navigateTo(com.nlespam.ui.navigation.Routes.BLE_SCANNER) },
            )

            SpamCategoryCard(
                title = "Signal Monitor",
                subtitle = "Real-time RSSI strength meter for BLE devices",
                icon = Icons.Default.SignalCellularAlt,
                deviceCount = 0,
                isActive = false,
                onClick = { viewModel.navigateTo(com.nlespam.ui.navigation.Routes.SIGNAL_MONITOR) },
            )

            SpamCategoryCard(
                title = "RSSI Distance Calculator",
                subtitle = "Estimate distance to BLE devices by RSSI",
                icon = Icons.Default.Straighten,
                deviceCount = 0,
                isActive = false,
                onClick = { viewModel.navigateTo(com.nlespam.ui.navigation.Routes.RSSI_DISTANCE) },
            )
        }
    }
}
