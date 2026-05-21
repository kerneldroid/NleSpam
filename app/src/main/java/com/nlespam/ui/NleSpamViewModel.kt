package com.nlespam.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit
import com.nlespam.engine.BleAdvertiserEngine
import com.nlespam.generators.*
import com.nlespam.models.*
import com.nlespam.service.SpamForegroundService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class ThemeColor {
    DYNAMIC, DEFAULT, BLUE, RED, GREEN, PURPLE
}

class NleSpamViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("nlespam_settings", Context.MODE_PRIVATE)
    val engine = BleAdvertiserEngine(application)

    init {
        // Handle stop signal from service (notification)
        viewModelScope.launch {
            GlobalSignals.stopSignal.collect {
                stopSpam()
            }
        }
    }

    // Generators (Lazy initialization to save memory)
    private val fastPairGen by lazy { FastPairGenerator() }
    private val samsungBudsGen by lazy { SamsungBudsGenerator() }
    private val samsungWatchGen by lazy { SamsungWatchGenerator() }
    private val appleDeviceGen by lazy { AppleDevicePopupGenerator() }
    private val appleActionGen by lazy { AppleActionModalGenerator() }
    private val swiftPairGen by lazy { SwiftPairGenerator() }
    private val lovespouseGen by lazy { LovespouseGenerator() }
    private val ibeaconFloodGen by lazy { IBeaconFloodGenerator() }
    private val chromecastSpamGen by lazy { ChromecastSpamGenerator() }
    private val airTagCloneGen by lazy { AirTagCloneGenerator() }

    // All advertisement sets by type (Immutable lists for better Flow performance)
    private val _fastPairSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val fastPairSets = _fastPairSets.asStateFlow()

    private val _samsungBudsSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val samsungBudsSets = _samsungBudsSets.asStateFlow()

    private val _samsungWatchSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val samsungWatchSets = _samsungWatchSets.asStateFlow()

    private val _appleDeviceSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val appleDeviceSets = _appleDeviceSets.asStateFlow()

    private val _appleActionSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val appleActionSets = _appleActionSets.asStateFlow()

    private val _swiftPairSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val swiftPairSets = _swiftPairSets.asStateFlow()

    private val _lovespouseSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val lovespouseSets = _lovespouseSets.asStateFlow()

    private val _ibeaconFloodSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val ibeaconFloodSets = _ibeaconFloodSets.asStateFlow()

    private val _chromecastSpamSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val chromecastSpamSets = _chromecastSpamSets.asStateFlow()

    private val _airTagCloneSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val airTagCloneSets = _airTagCloneSets.asStateFlow()

    // Mix Spam — FastPair + SwiftPair + Samsung (no Lovespouse)
    private val _mixSpamSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val mixSpamSets = _mixSpamSets.asStateFlow()

    // Mixed All — combines all generators, shuffled
    private val _mixAllSets = MutableStateFlow<List<AdvertisementSet>>(emptyList())
    val mixAllSets = _mixAllSets.asStateFlow()

    init {
        generateAll()
        engine.intervalMs = prefs.getLong("intervalMs", 100L)
        engine.txPower = prefs.getInt("txPower", 3) // HIGH
    }

    fun generateAll() {
        _fastPairSets.value = fastPairGen.generate()
        _samsungBudsSets.value = samsungBudsGen.generate()
        _samsungWatchSets.value = samsungWatchGen.generate()
        _appleDeviceSets.value = appleDeviceGen.generate()
        _appleActionSets.value = appleActionGen.generate()
        _swiftPairSets.value = swiftPairGen.generate()
        _lovespouseSets.value = lovespouseGen.generate()
        _ibeaconFloodSets.value = ibeaconFloodGen.generate()
        _chromecastSpamSets.value = chromecastSpamGen.generate()
        _airTagCloneSets.value = airTagCloneGen.generate()
        _mixSpamSets.value = buildMixSpamSets()
        _mixAllSets.value = buildMixAllSets()
    }

    private fun buildMixSpamSets(): List<AdvertisementSet> {
        val all = mutableListOf<AdvertisementSet>()
        all.addAll(fastPairGen.generate())
        all.addAll(swiftPairGen.generate())
        all.addAll(samsungBudsGen.generate())
        all.addAll(samsungWatchGen.generate())
        all.shuffle()
        return all
    }

    private fun buildMixAllSets(): List<AdvertisementSet> {
        val all = mutableListOf<AdvertisementSet>()
        all.addAll(fastPairGen.generate())
        all.addAll(samsungBudsGen.generate())
        all.addAll(samsungWatchGen.generate())
        all.addAll(appleDeviceGen.generate())
        all.addAll(appleActionGen.generate())
        all.addAll(swiftPairGen.generate())
        all.addAll(lovespouseGen.generate())
        all.addAll(ibeaconFloodGen.generate())
        all.addAll(chromecastSpamGen.generate())
        all.addAll(airTagCloneGen.generate())
        all.shuffle()
        return all
    }

    fun reshuffleMixAll() {
        _mixAllSets.value = buildMixAllSets()
    }

    // Active spam type
    private val _activeSpamType = MutableStateFlow<SpamType?>(null)
    val activeSpamType = _activeSpamType.asStateFlow()

    // Settings
    private val _intervalMs = MutableStateFlow(prefs.getLong("intervalMs", 100L))
    val intervalMs = _intervalMs.asStateFlow()

    private val _txPower = MutableStateFlow(prefs.getInt("txPower", 3)) // HIGH
    val txPower = _txPower.asStateFlow()

    private val _useForegroundService = MutableStateFlow(prefs.getBoolean("useForegroundService", true))
    val useForegroundService = _useForegroundService.asStateFlow()

    private val _themeColor = MutableStateFlow(
        runCatching { ThemeColor.valueOf(prefs.getString("themeColor", "DYNAMIC") ?: "DYNAMIC") }.getOrDefault(ThemeColor.DYNAMIC)
    )
    val themeColor = _themeColor.asStateFlow()

    private val _useOledTheme = MutableStateFlow(prefs.getBoolean("useOledTheme", false))
    val useOledTheme = _useOledTheme.asStateFlow()

    // --- Advanced Settings ---

    private val _vibrateOnStart = MutableStateFlow(prefs.getBoolean("vibrateOnStart", true))
    val vibrateOnStart = _vibrateOnStart.asStateFlow()

    private val _autoStopSeconds = MutableStateFlow(prefs.getInt("autoStopSeconds", 0))
    val autoStopSeconds = _autoStopSeconds.asStateFlow()

    private val _packetBatchSize = MutableStateFlow(prefs.getInt("packetBatchSize", 1))
    val packetBatchSize = _packetBatchSize.asStateFlow()

    private val _stealthMode = MutableStateFlow(prefs.getBoolean("stealthMode", false))
    val stealthMode = _stealthMode.asStateFlow()

    // --- Customization Settings ---
    private val _scanDurationSeconds = MutableStateFlow(prefs.getInt("scanDurationSeconds", 30))
    val scanDurationSeconds = _scanDurationSeconds.asStateFlow()

    private val _shuffleIntervalSeconds = MutableStateFlow(prefs.getInt("shuffleIntervalSeconds", 0))
    val shuffleIntervalSeconds = _shuffleIntervalSeconds.asStateFlow()

    private val _loopMode = MutableStateFlow(prefs.getString("loopMode", "SEQUENTIAL") ?: "SEQUENTIAL")
    val loopMode = _loopMode.asStateFlow()

    private val _notificationSound = MutableStateFlow(prefs.getBoolean("notificationSound", false))
    val notificationSound = _notificationSound.asStateFlow()

    private val _isControlBarExpanded = MutableStateFlow(true)
    val isControlBarExpanded = _isControlBarExpanded.asStateFlow()

    private val _showCustomInterval = MutableStateFlow(prefs.getBoolean("showCustomInterval", false))
    val showCustomInterval = _showCustomInterval.asStateFlow()

    private val _pendingNavRoute = MutableStateFlow<String?>(null)
    val pendingNavRoute = _pendingNavRoute.asStateFlow()

    fun setInterval(ms: Long) {
        _intervalMs.value = ms
        engine.intervalMs = ms
        prefs.edit { putLong("intervalMs", ms) }
    }
    
    fun setTxPower(power: Int) {
        _txPower.value = power
        engine.txPower = power
        prefs.edit { putInt("txPower", power) }
    }
    
    fun setUseForegroundService(use: Boolean) {
        _useForegroundService.value = use
        prefs.edit { putBoolean("useForegroundService", use) }
    }
    
    fun setThemeColor(color: ThemeColor) {
        _themeColor.value = color
        prefs.edit { putString("themeColor", color.name) }
    }

    fun setUseOledTheme(use: Boolean) {
        _useOledTheme.value = use
        prefs.edit { putBoolean("useOledTheme", use) }
    }

    fun setVibrateOnStart(use: Boolean) {
        _vibrateOnStart.value = use
        prefs.edit { putBoolean("vibrateOnStart", use) }
    }

    fun setAutoStopSeconds(seconds: Int) {
        _autoStopSeconds.value = seconds
        prefs.edit { putInt("autoStopSeconds", seconds) }
    }

    fun setPacketBatchSize(size: Int) {
        _packetBatchSize.value = size
        prefs.edit { putInt("packetBatchSize", size) }
    }

    fun setStealthMode(use: Boolean) {
        _stealthMode.value = use
        prefs.edit { putBoolean("stealthMode", use) }
        if (use) {
            // Stealth: force low TX + slow interval
            setTxPower(1) // LOW
            setInterval(500L)
        }
    }

    fun setScanDurationSeconds(seconds: Int) {
        _scanDurationSeconds.value = seconds
        prefs.edit { putInt("scanDurationSeconds", seconds) }
    }

    fun setShuffleIntervalSeconds(seconds: Int) {
        _shuffleIntervalSeconds.value = seconds
        prefs.edit { putInt("shuffleIntervalSeconds", seconds) }
    }

    fun setLoopMode(mode: String) {
        _loopMode.value = mode
        prefs.edit { putString("loopMode", mode) }
    }

    fun setNotificationSound(use: Boolean) {
        _notificationSound.value = use
        prefs.edit { putBoolean("notificationSound", use) }
    }

    fun setControlBarExpanded(expanded: Boolean) {
        _isControlBarExpanded.value = expanded
    }

    fun setShowCustomInterval(show: Boolean) {
        _showCustomInterval.value = show
        prefs.edit { putBoolean("showCustomInterval", show) }
    }

    fun navigateTo(route: String) {
        _pendingNavRoute.value = route
    }

    fun clearPendingNav() {
        _pendingNavRoute.value = null
    }

    fun toggleDeviceSelection(type: SpamType, index: Int) {
        val flow = getFlowForType(type)
        val current = flow.value
        if (index in current.indices) {
            flow.value = current.mapIndexed { i, set ->
                if (i == index) set.copy(isSelected = !set.isSelected) else set
            }
        }
    }

    fun selectAll(type: SpamType, selected: Boolean) {
        val flow = getFlowForType(type)
        flow.value = flow.value.map { it.copy(isSelected = selected) }
    }

    private var notificationJob: Job? = null

    fun startSpam(type: SpamType) {
        val sets = getFlowForType(type).value
        engine.start(sets)
        _activeSpamType.value = type

        val route = when (type) {
            SpamType.FAST_PAIR -> com.nlespam.ui.navigation.Routes.FAST_PAIR
            SpamType.APPLE_DEVICE_POPUP, SpamType.APPLE_ACTION_MODAL -> com.nlespam.ui.navigation.Routes.APPLE
            SpamType.SAMSUNG_BUDS, SpamType.SAMSUNG_WATCH -> com.nlespam.ui.navigation.Routes.SAMSUNG
            SpamType.SWIFT_PAIR -> com.nlespam.ui.navigation.Routes.SWIFT_PAIR
            SpamType.LOVESPOUSE_PLAY, SpamType.LOVESPOUSE_STOP -> com.nlespam.ui.navigation.Routes.LOVESPOUSE
            SpamType.IBEACON_FLOOD -> com.nlespam.ui.navigation.Routes.IBEACON_FLOOD
            SpamType.CHROMECAST_SPAM -> com.nlespam.ui.navigation.Routes.CHROMECAST_SPAM
            SpamType.AIRTAG_CLONE -> com.nlespam.ui.navigation.Routes.AIRTAG_CLONE
            SpamType.MIX_SPAM -> com.nlespam.ui.navigation.Routes.MIX_SPAM
            SpamType.MIXED_ALL -> com.nlespam.ui.navigation.Routes.MIX_ALL
        }

        val ctx = getApplication<Application>()
        if (_useForegroundService.value) {
            ctx.startForegroundService(
                Intent(ctx, SpamForegroundService::class.java).apply {
                    putExtra(SpamForegroundService.EXTRA_ROUTE, route)
                }
            )
        }

        // Update Live Notification with packet count every 2 seconds
        notificationJob?.cancel()
        notificationJob = viewModelScope.launch {
            engine.isRunning.collectLatest { running ->
                while (running) {
                    delay(2000)
                    if (_useForegroundService.value) {
                        val intent = Intent(ctx, SpamForegroundService::class.java).apply {
                            action = SpamForegroundService.ACTION_UPDATE_PROGRESS
                            putExtra(SpamForegroundService.EXTRA_PACKETS, engine.packetsSent.value)
                            putExtra(SpamForegroundService.EXTRA_ROUTE, route)
                        }
                        ctx.startService(intent)
                    }
                }
            }
        }
    }

    fun stopSpam() {
        engine.stop()
        notificationJob?.cancel()
        notificationJob = null
        _activeSpamType.value = null
        _isControlBarExpanded.value = true  // Restore bar when stopped

        val ctx = getApplication<Application>()
        ctx.stopService(Intent(ctx, SpamForegroundService::class.java))
    }

    private fun getFlowForType(type: SpamType): MutableStateFlow<List<AdvertisementSet>> {
        return when (type) {
            SpamType.FAST_PAIR -> _fastPairSets
            SpamType.SAMSUNG_BUDS -> _samsungBudsSets
            SpamType.SAMSUNG_WATCH -> _samsungWatchSets
            SpamType.APPLE_DEVICE_POPUP -> _appleDeviceSets
            SpamType.APPLE_ACTION_MODAL -> _appleActionSets
            SpamType.SWIFT_PAIR -> _swiftPairSets
            SpamType.LOVESPOUSE_PLAY, SpamType.LOVESPOUSE_STOP -> _lovespouseSets
            SpamType.IBEACON_FLOOD -> _ibeaconFloodSets
            SpamType.CHROMECAST_SPAM -> _chromecastSpamSets
            SpamType.AIRTAG_CLONE -> _airTagCloneSets
            SpamType.MIX_SPAM -> _mixSpamSets
            SpamType.MIXED_ALL -> _mixAllSets
        }
    }

    override fun onCleared() {
        engine.destroy()
    }
}
