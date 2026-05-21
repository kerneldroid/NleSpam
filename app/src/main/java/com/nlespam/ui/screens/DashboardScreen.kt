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

    // Multi-tools
    onNavigateToDiscoverySuite: () -> Unit,
    onNavigateToAnalysisSuite: () -> Unit,

    onNavigateToPacketLogger: () -> Unit,
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
            // Live stats card (Extracted to prevent full-screen recomposition)
            if (isRunning) {
                LiveStatsCard(
                    packetsFlow = viewModel.engine.packetsSent,
                    activeType = activeType,
                    onStop = { viewModel.stopSpam() }
                )
                Spacer(Modifier.height(4.dp))
            }

            // Category cards
            Text(
                text = "Attack Vectors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp),
            )

            SpamCategoryCard(
                title = "Mix All Spam",
                subtitle = "Everything combined — chaotic multi-protocol flood",
                icon = Icons.Default.Shuffle,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.MIXED_ALL,
                onClick = onNavigateToMixAll,
            )

            SpamCategoryCard(
                title = "iBeacon Flood",
                subtitle = "Spam fake iBeacon location beacons",
                icon = Icons.Default.LocationOn,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.IBEACON_FLOOD,
                onClick = onNavigateToIBeaconFlood,
            )

            SpamCategoryCard(
                title = "Chromecast Spam",
                subtitle = "Spoof Chromecast & Google Home discovery",
                icon = Icons.Default.Cast,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.CHROMECAST_SPAM,
                onClick = onNavigateToChromecastSpam,
            )

            SpamCategoryCard(
                title = "AirTag Clone",
                subtitle = "Broadcast fake Find My network signals",
                icon = Icons.Default.Sell,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.AIRTAG_CLONE,
                onClick = onNavigateToAirTagClone,
            )

            SpamCategoryCard(
                title = "Mix Spam",
                subtitle = "Fast Pair + Swift Pair + Samsung combined",
                icon = Icons.Default.Merge,
                deviceCount = 0,
                isActive = isRunning && activeType == SpamType.MIX_SPAM,
                onClick = onNavigateToMixSpam,
            )

            SpamCategoryCard(
                title = "Google Fast Pair",
                subtitle = "Android devices — headphones, speakers, phones",
                icon = Icons.Default.Bluetooth,
                deviceCount = fastPairSets.size,
                isActive = isRunning && activeType == SpamType.FAST_PAIR,
                onClick = onNavigateToFastPair,
            )

            SpamCategoryCard(
                title = "Apple Continuity",
                subtitle = "iOS popups — AirPods, Beats, Vision Pro",
                icon = Icons.Default.PhoneIphone,
                deviceCount = appleDeviceSets.size + appleActionSets.size,
                isActive = activeType == com.nlespam.models.SpamType.APPLE_DEVICE_POPUP || activeType == com.nlespam.models.SpamType.APPLE_ACTION_MODAL,
                onClick = onNavigateToApple,
            )

            SpamCategoryCard(
                title = "Samsung Easy Setup",
                subtitle = "Galaxy Buds & Watch popups",
                icon = Icons.Default.Watch,
                deviceCount = samsungBudsSets.size + samsungWatchSets.size,
                isActive = activeType == com.nlespam.models.SpamType.SAMSUNG_BUDS || activeType == com.nlespam.models.SpamType.SAMSUNG_WATCH,
                onClick = onNavigateToSamsung,
            )

            SpamCategoryCard(
                title = "Windows Swift Pair",
                subtitle = "Windows 10/11 Bluetooth popups",
                icon = Icons.Default.DesktopWindows,
                deviceCount = swiftPairSets.size,
                isActive = activeType == com.nlespam.models.SpamType.SWIFT_PAIR,
                onClick = onNavigateToSwiftPair,
            )

            SpamCategoryCard(
                title = "Lovespouse",
                subtitle = "IoT toy control — play & stop modes",
                icon = Icons.Default.Favorite,
                deviceCount = lovespouseSets.size,
                isActive = activeType == com.nlespam.models.SpamType.LOVESPOUSE_PLAY,
                onClick = onNavigateToLovespouse,
            )

            // Tools section
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )

            SpamCategoryCard(
                title = "Discovery Suite",
                subtitle = "BLE Scanner, Signal Monitor, RSSI Distance",
                icon = Icons.AutoMirrored.Filled.BluetoothSearching,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToDiscoverySuite,
            )

            SpamCategoryCard(
                title = "Analysis Suite",
                subtitle = "Payload Inspector, Ad Decoder, UUID DB",
                icon = Icons.Default.Code,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToAnalysisSuite,
            )

            SpamCategoryCard(
                title = "Packet Logger",
                subtitle = "Real-time log of outgoing spam packets",
                icon = Icons.Default.Terminal,
                deviceCount = 0,
                isActive = false,
                onClick = onNavigateToPacketLogger,
            )

            SpamCategoryCard(
                title = "Bluetooth File Sender",
                subtitle = "Send any file via BT OPP to nearby devices",
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
