package com.nlespam.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.*
import androidx.compose.ui.unit.dp
import com.nlespam.ui.NleSpamViewModel
import com.nlespam.ui.ThemeColor

@Composable
fun SettingsGroupItem(
    title: String,
    subtitle: String? = null,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
) {
    val cornerRadius = 24.dp
    val connectionRadius = 4.dp

    val shape = when {
        isFirst && isLast -> androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
        isFirst -> androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = cornerRadius, topEnd = cornerRadius,
            bottomStart = connectionRadius, bottomEnd = connectionRadius
        )
        isLast -> androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = connectionRadius, topEnd = connectionRadius,
            bottomStart = cornerRadius, bottomEnd = cornerRadius
        )
        else -> androidx.compose.foundation.shape.RoundedCornerShape(connectionRadius)
    }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (subtitle != null) {
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (trailing != null) trailing()
            }
            if (bottomContent != null) {
                Spacer(Modifier.height(12.dp))
                bottomContent()
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NleSpamViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    val intervalMs by viewModel.intervalMs.collectAsState()
    val txPower by viewModel.txPower.collectAsState()
    val useForeground by viewModel.useForegroundService.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val useOled by viewModel.useOledTheme.collectAsState()

    // Advanced settings
    val vibrateOnStart by viewModel.vibrateOnStart.collectAsState()
    val autoStopSeconds by viewModel.autoStopSeconds.collectAsState()
    val packetBatchSize by viewModel.packetBatchSize.collectAsState()
    val stealthMode by viewModel.stealthMode.collectAsState()

    // Customization settings
    val scanDuration by viewModel.scanDurationSeconds.collectAsState()
    val shuffleInterval by viewModel.shuffleIntervalSeconds.collectAsState()
    val loopMode by viewModel.loopMode.collectAsState()
    val notificationSound by viewModel.notificationSound.collectAsState()

    val showCustomInterval by viewModel.showCustomInterval.collectAsState()
    var customIntervalText by remember { mutableStateOf(intervalMs.toString()) }

    // Easter egg: About click counter
    var aboutClickCount by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            
            SettingsGroupHeader("Bluetooth Payload")

            SettingsGroupItem(
                title = "Advertising Interval",
                subtitle = "Time between advertisement packets: ${intervalMs}ms",
                isFirst = true,
                isLast = false,
                trailing = {
                    IconButton(onClick = {
                        if (!showCustomInterval) {
                            customIntervalText = intervalMs.toString()
                        }
                        viewModel.setShowCustomInterval(!showCustomInterval)
                    }) {
                        Icon(if (showCustomInterval) Icons.Default.Done else Icons.Default.Edit, "Edit")
                    }
                },
                bottomContent = {
                    AnimatedContent(targetState = showCustomInterval, label = "interval_input") { isCustom ->
                        if (isCustom) {
                            OutlinedTextField(
                                value = customIntervalText,
                                onValueChange = {
                                    customIntervalText = it
                                    val v = it.toLongOrNull()
                                    if (v != null && v in 10..10000) viewModel.setInterval(v)
                                },
                                label = { Text("Custom Interval (ms)") },
                                placeholder = { Text("e.g. 150") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = {
                                        val v = customIntervalText.toLongOrNull()
                                        if (v != null && v in 10..10000) viewModel.setInterval(v)
                                        viewModel.setShowCustomInterval(false)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium,
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Slider(
                                    value = intervalMs.toFloat(),
                                    onValueChange = { viewModel.setInterval(it.toLong()) },
                                    valueRange = 20f..1000f,
                                    steps = 19,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("20ms (Fast)", style = MaterialTheme.typography.labelSmall)
                                    Text("1000ms (Slow)", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "TX Power Level",
                subtitle = "Higher = further range, more visible",
                isFirst = false,
                isLast = true,
                bottomContent = {
                    val powerLabels = listOf("ULow", "Low", "Medium", "High")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        powerLabels.forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = txPower == index,
                                onClick = { viewModel.setTxPower(index) },
                                shape = SegmentedButtonDefaults.itemShape(index, powerLabels.size),
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            )

            SettingsGroupHeader("Personalization")

            SettingsGroupItem(
                title = "Foreground Service",
                subtitle = "Keep spam running when app is in background",
                isFirst = true,
                isLast = false,
                onClick = { viewModel.setUseForegroundService(!useForeground) },
                trailing = {
                    Switch(
                        checked = useForeground,
                        onCheckedChange = { viewModel.setUseForegroundService(it) },
                    )
                }
            )

            SettingsGroupItem(
                title = "OLED Pure Black Theme",
                subtitle = "Uses absolute black instead of dark grey for the background. Only active when device is in Dark Mode.",
                isFirst = false,
                isLast = false,
                onClick = { viewModel.setUseOledTheme(!useOled) },
                trailing = {
                    Switch(
                        checked = useOled,
                        onCheckedChange = { viewModel.setUseOledTheme(it) },
                    )
                }
            )

            SettingsGroupItem(
                title = "Theme Color",
                subtitle = "Customize the app aesthetic",
                isFirst = false,
                isLast = true,
                bottomContent = {
                     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                         SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                             val options = listOf(ThemeColor.DYNAMIC to "You", ThemeColor.DEFAULT to "Cyan", ThemeColor.BLUE to "Blue")
                             options.forEachIndexed { i, (color, label) ->
                                 SegmentedButton(
                                     selected = themeColor == color,
                                     onClick = { viewModel.setThemeColor(color) },
                                     shape = SegmentedButtonDefaults.itemShape(i, 3),
                                 ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                             }
                         }
                         SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                             val options = listOf(ThemeColor.RED to "Red", ThemeColor.GREEN to "Green", ThemeColor.PURPLE to "Purple")
                             options.forEachIndexed { i, (color, label) ->
                                 SegmentedButton(
                                     selected = themeColor == color,
                                     onClick = { viewModel.setThemeColor(color) },
                                     shape = SegmentedButtonDefaults.itemShape(i, 3),
                                 ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                             }
                         }
                     }
                }
            )

            // =================== ADVANCED SETTINGS ===================
            SettingsGroupHeader("Advanced")

            SettingsGroupItem(
                title = "Vibrate on Spam Start",
                subtitle = "Haptic feedback when spamming begins",
                isFirst = true,
                isLast = false,
                onClick = { viewModel.setVibrateOnStart(!vibrateOnStart) },
                trailing = {
                    Switch(
                        checked = vibrateOnStart,
                        onCheckedChange = { viewModel.setVibrateOnStart(it) },
                    )
                }
            )

            SettingsGroupItem(
                title = "Auto-stop Timer",
                subtitle = if (autoStopSeconds == 0) "Disabled — Run until stopped manually"
                           else "Auto-stop after ${autoStopSeconds}s",
                isFirst = false,
                isLast = false,
                bottomContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = autoStopSeconds.toFloat(),
                            onValueChange = { viewModel.setAutoStopSeconds(it.toInt()) },
                            valueRange = 0f..300f,
                            steps = 29,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Off", style = MaterialTheme.typography.labelSmall)
                            Text("5 min", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "Packet Batch Size",
                subtitle = "Number of packets sent per burst cycle",
                isFirst = false,
                isLast = false,
                bottomContent = {
                    val batchOptions = listOf(1, 3, 5, 10)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        batchOptions.forEachIndexed { index, size ->
                            SegmentedButton(
                                selected = packetBatchSize == size,
                                onClick = { viewModel.setPacketBatchSize(size) },
                                shape = SegmentedButtonDefaults.itemShape(index, batchOptions.size),
                            ) {
                                Text("$size", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "Stealth Mode",
                subtitle = "Forces low TX power + slow interval for covert operation. Overrides Bluetooth Payload settings when enabled.",
                isFirst = false,
                isLast = true,
                onClick = { viewModel.setStealthMode(!stealthMode) },
                trailing = {
                    Switch(
                        checked = stealthMode,
                        onCheckedChange = { viewModel.setStealthMode(it) },
                    )
                }
            )

            // =================== CUSTOMIZATION ===================
            SettingsGroupHeader("Customization")

            SettingsGroupItem(
                title = "Scan Duration",
                subtitle = "Auto-stop BLE scanner after ${scanDuration}s",
                isFirst = true,
                isLast = false,
                bottomContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = scanDuration.toFloat(),
                            onValueChange = { viewModel.setScanDurationSeconds(it.toInt()) },
                            valueRange = 5f..120f,
                            steps = 22,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("5s", style = MaterialTheme.typography.labelSmall)
                            Text("120s", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "Shuffle Interval",
                subtitle = if (shuffleInterval == 0) "Disabled — Mix All uses initial shuffle only"
                           else "Reshuffle Mix All payloads every ${shuffleInterval}s",
                isFirst = false,
                isLast = false,
                bottomContent = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Slider(
                            value = shuffleInterval.toFloat(),
                            onValueChange = { viewModel.setShuffleIntervalSeconds(it.toInt()) },
                            valueRange = 0f..60f,
                            steps = 11,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Off", style = MaterialTheme.typography.labelSmall)
                            Text("60s", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "Loop Mode",
                subtitle = "How devices are iterated during spam",
                isFirst = false,
                isLast = false,
                bottomContent = {
                    val modes = listOf("SEQUENTIAL" to "Sequential", "RANDOM" to "Random", "REVERSE" to "Reverse")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        modes.forEachIndexed { index, (value, label) ->
                            SegmentedButton(
                                selected = loopMode == value,
                                onClick = { viewModel.setLoopMode(value) },
                                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "Notification Sound",
                subtitle = "Play a short sound on each successful packet burst",
                isFirst = false,
                isLast = true,
                onClick = { viewModel.setNotificationSound(!notificationSound) },
                trailing = {
                    Switch(
                        checked = notificationSound,
                        onCheckedChange = { viewModel.setNotificationSound(it) },
                    )
                }
            )

            // =================== ABOUT (with Easter Egg) ===================
            SettingsGroupHeader("About")

            SettingsGroupItem(
                title = "About NleSpam v2.0.5",
                subtitle = "BLE & Bluetooth attack toolkit with Material 3 Expressive design. Supports 10+ attack vectors across Google, Apple, Samsung, Microsoft, and more.",
                isFirst = true,
                isLast = false,
                onClick = {
                    aboutClickCount++
                    val remaining = 7 - aboutClickCount
                    when {
                        remaining > 4 -> {
                            // Silent — no toast yet
                        }
                        remaining == 4 -> {
                            Toast.makeText(context, "🤔 Interesting...", Toast.LENGTH_SHORT).show()
                        }
                        remaining == 3 -> {
                            Toast.makeText(context, "Keep going... $remaining more", Toast.LENGTH_SHORT).show()
                        }
                        remaining == 2 -> {
                            Toast.makeText(context, "Almost there... $remaining more", Toast.LENGTH_SHORT).show()
                        }
                        remaining == 1 -> {
                            Toast.makeText(context, "👀 One more tap...", Toast.LENGTH_SHORT).show()
                        }
                        remaining <= 0 -> {
                            aboutClickCount = 0
                            Toast.makeText(context, "🎉 You found the secret!", Toast.LENGTH_SHORT).show()
                            // Rickroll!
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                            context.startActivity(intent)
                        }
                    }
                }
            )

            SettingsGroupItem(
                title = "What's New in v2.0.5",
                subtitle = buildString {
                    appendLine("🔴 Attacks:")
                    appendLine("• iBeacon Flood — fake location beacons")
                    appendLine("• Chromecast Spam — spoofed Google Cast")
                    appendLine("• AirTag Clone — fake Find My signals")
                    appendLine("• Mix Spam — combined Fast Pair + Swift Pair + Samsung")
                    appendLine()
                    appendLine("🛠 Tools:")
                    appendLine("• BLE Device Scanner")
                    appendLine("• MAC Randomizer")
                    appendLine("• Payload Inspector")
                    appendLine("• RSSI Distance Calculator")
                    appendLine("• Packet Logger")
                    appendLine("• Bluetooth File Blast")
                    appendLine("• UUID Database — searchable BLE UUIDs")
                    appendLine("• Signal Monitor — live RSSI graphs")
                    appendLine("• Advertisement Decoder — raw ad parser")
                    appendLine()
                    appendLine("⚙ Settings:")
                    appendLine("• Scan Duration, Shuffle Interval, Loop Mode")
                    appendLine("• Notification Sound, Stealth Mode, Batch Size")
                    appendLine("• OLED theme, MAC randomization, Auto-stop")
                    appendLine()
                    append("🥚 Hidden easter egg 👀")
                },
                isFirst = false,
                isLast = true,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
