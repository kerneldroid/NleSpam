package com.nlespam.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.automirrored.filled.SendAndArchive
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nlespam.models.SpamType
import com.nlespam.ui.NleSpamViewModel
import com.nlespam.ui.components.SpamCategoryCard
import com.nlespam.ui.theme.*
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    viewModel: NleSpamViewModel,
    onNavigateToFastPair: () -> Unit,
    onNavigateToApple: () -> Unit,
    onNavigateToSamsung: () -> Unit,
    onNavigateToSwiftPair: () -> Unit,
    onNavigateToLovespouse: () -> Unit,
    onNavigateToMixAll: () -> Unit,
    onNavigateToIBeaconFlood: () -> Unit,
    onNavigateToChromecastSpam: () -> Unit,
    onNavigateToAirTagClone: () -> Unit,
    onNavigateToMixSpam: () -> Unit,
    
    onNavigateToPacketLogger: () -> Unit,
    onNavigateToUuidDatabase: () -> Unit,
    onNavigateToAdDecoder: () -> Unit,
    onNavigateToBtFileSender: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val isRunning by viewModel.engine.isRunning.collectAsState()
    val activeType by viewModel.activeSpamType.collectAsState()
    
    val fastPairSets by viewModel.fastPairSets.collectAsState()
    val appleDeviceSets by viewModel.appleDeviceSets.collectAsState()
    val appleActionSets by viewModel.appleActionSets.collectAsState()
    val samsungBudsSets by viewModel.samsungBudsSets.collectAsState()
    val samsungWatchSets by viewModel.samsungWatchSets.collectAsState()
    val swiftPairSets by viewModel.swiftPairSets.collectAsState()
    val lovespouseSets by viewModel.lovespouseSets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "NleSpam",
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        DashboardStatusText(viewModel.engine.isRunning)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isRunning) {
                LiveStatsCard(
                    packetsFlow = viewModel.engine.packetsSent,
                    activeType = activeType,
                    onStop = { viewModel.stopSpam() }
                )
                Spacer(Modifier.height(4.dp))
            }

            Text(
                text = "Attack Vectors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp),
            )

            SpamCategoryCard(
                title = "Mix All Spam",
                subtitle = "Multi-protocol flood",
                icon = Icons.Default.Shuffle,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.MIXED_ALL,
                onClick = onNavigateToMixAll,
            )

            SpamCategoryCard(
                title = "Mix Spam",
                subtitle = "Fast Pair + Swift Pair + Samsung",
                icon = Icons.Default.Merge,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.MIX_SPAM,
                onClick = onNavigateToMixSpam,
            )

            SpamCategoryCard(
                title = "Google Fast Pair",
                subtitle = "Android devices",
                icon = Icons.Default.Bluetooth,
                deviceCount = fastPairSets.size,
                isActive = isRunning && activeType == SpamType.FAST_PAIR,
                onClick = onNavigateToFastPair,
            )

            SpamCategoryCard(
                title = "Apple Continuity",
                subtitle = "iOS popups & modals",
                icon = Icons.Default.PhoneIphone,
                deviceCount = appleDeviceSets.size + appleActionSets.size,
                isActive = activeType == SpamType.APPLE_DEVICE_POPUP || activeType == SpamType.APPLE_ACTION_MODAL,
                onClick = onNavigateToApple,
            )

            SpamCategoryCard(
                title = "Samsung Easy Setup",
                subtitle = "Buds & Watch popups",
                icon = Icons.Default.Watch,
                deviceCount = samsungBudsSets.size + samsungWatchSets.size,
                isActive = activeType == SpamType.SAMSUNG_BUDS || activeType == SpamType.SAMSUNG_WATCH,
                onClick = onNavigateToSamsung,
            )

            SpamCategoryCard(
                title = "Windows Swift Pair",
                subtitle = "Windows 10/11 popups",
                icon = Icons.Default.DesktopWindows,
                deviceCount = swiftPairSets.size,
                isActive = activeType == SpamType.SWIFT_PAIR,
                onClick = onNavigateToSwiftPair,
            )

            SpamCategoryCard(
                title = "iBeacon Flood",
                subtitle = "Fake location beacons",
                icon = Icons.Default.LocationOn,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.IBEACON_FLOOD,
                onClick = onNavigateToIBeaconFlood,
            )

            SpamCategoryCard(
                title = "Chromecast Spam",
                subtitle = "Spoofed discovery",
                icon = Icons.Default.Cast,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.CHROMECAST_SPAM,
                onClick = onNavigateToChromecastSpam,
            )

            SpamCategoryCard(
                title = "AirTag Clone",
                subtitle = "Fake Find My signals",
                icon = Icons.Default.Sell,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.AIRTAG_CLONE,
                onClick = onNavigateToAirTagClone,
            )

            SpamCategoryCard(
                title = "Lovespouse",
                subtitle = "IoT toy control",
                icon = Icons.Default.Favorite,
                deviceCount = lovespouseSets.size,
                isActive = activeType == SpamType.LOVESPOUSE_PLAY,
                onClick = onNavigateToLovespouse,
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )

            SpamCategoryCard(
                title = "Packet Logger",
                subtitle = "Live traffic monitor",
                icon = Icons.Default.Terminal,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToPacketLogger,
            )

            SpamCategoryCard(
                title = "UUID Database",
                subtitle = "Searchable BLE UUIDs",
                icon = Icons.Default.Search,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToUuidDatabase,
            )

            SpamCategoryCard(
                title = "Advertisement Decoder",
                subtitle = "Raw ad parser",
                icon = Icons.Default.DataObject,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToAdDecoder,
            )

            SpamCategoryCard(
                title = "Bluetooth File Sender",
                subtitle = "Send files via BT OPP",
                icon = Icons.AutoMirrored.Filled.SendAndArchive,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToBtFileSender,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun DashboardStatusText(isRunningFlow: StateFlow<Boolean>) {
    val isRunning by isRunningFlow.collectAsState()
    Text(
        text = if (isRunning) "Spamming Active" else "Ready to spam",
        style = MaterialTheme.typography.bodySmall,
        color = if (isRunning) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun LiveStatsCard(
    packetsFlow: StateFlow<Long>,
    activeType: SpamType?,
    onStop: () -> Unit
) {
    val packetsSent by packetsFlow.collectAsState()
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = glowAlpha),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$packetsSent",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text("Packets Sent", style = MaterialTheme.typography.labelMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = activeType?.label ?: "—",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text("Active Mode", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        FilledTonalButton(
            onClick = onStop,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Icon(Icons.Default.Stop, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("STOP ALL SPAM", fontWeight = FontWeight.Bold)
        }
    }
}
