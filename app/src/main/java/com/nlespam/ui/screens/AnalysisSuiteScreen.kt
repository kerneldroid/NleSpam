package com.nlespam.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlespam.ui.NleSpamViewModel
import com.nlespam.ui.components.SpamCategoryCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisSuiteScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Suite", fontWeight = FontWeight.Bold) },
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
                title = "Payload Inspector",
                subtitle = "Inspect raw BLE advertisement payloads",
                icon = Icons.Default.Code,
                deviceCount = 0,
                isActive = false,
                onClick = { viewModel.navigateTo(com.nlespam.ui.navigation.Routes.PAYLOAD_INSPECTOR) },
            )

            SpamCategoryCard(
                title = "Advertisement Decoder",
                subtitle = "Decode raw BLE ads into human-readable data",
                icon = Icons.Default.DataObject,
                deviceCount = 0,
                isActive = false,
                onClick = { viewModel.navigateTo(com.nlespam.ui.navigation.Routes.AD_DECODER) },
            )

            SpamCategoryCard(
                title = "UUID Database",
                subtitle = "Searchable BLE service & characteristic UUIDs",
                icon = Icons.Default.Search,
                deviceCount = 0,
                isActive = false,
                onClick = { viewModel.navigateTo(com.nlespam.ui.navigation.Routes.UUID_DATABASE) },
            )
        }
    }
}
