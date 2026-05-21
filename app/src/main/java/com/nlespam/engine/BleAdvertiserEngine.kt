package com.nlespam.engine

import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.nlespam.models.AdvertisementSet
import com.nlespam.models.BleDatabase
import com.nlespam.models.SpamRadarEntry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Classifies BLE advertisements by manufacturer ID and payload patterns
 */
object SpamClassifier {
    private val FAST_PAIR_UUID = ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")

    fun classify(result: ScanResult): String {
        val record = result.scanRecord ?: return "Unknown"
        val address = result.device?.address

        // Check service UUIDs for Fast Pair
        val serviceUuids = record.serviceUuids
        if (serviceUuids != null) {
            for (uuid in serviceUuids) {
                if (uuid == FAST_PAIR_UUID) return "Fast Pair"
            }
        }

        // Check manufacturer data
        val mfData = record.manufacturerSpecificData
        if (mfData != null && mfData.size() > 0) {
            for (i in 0 until mfData.size()) {
                val mfId = mfData.keyAt(i)
                val data = mfData.valueAt(i)

                when (mfId) {
                    76 -> { // Apple 0x004C
                        if (data != null && data.isNotEmpty()) {
                            return when (data[0].toInt() and 0xFF) {
                                0x07 -> "Apple Action Modal"
                                0x05, 0x09, 0x10 -> "Apple Device Popup"
                                else -> "Apple Continuity"
                            }
                        }
                        return "Apple Continuity"
                    }
                    6 -> return "Swift Pair"       // Microsoft 0x0006
                    117 -> {                        // Samsung 0x0075
                        if (data != null && data.size >= 2) {
                            return when (data[0].toInt() and 0xFF) {
                                0x01 -> "Samsung Buds"
                                0x02 -> "Samsung Watch"
                                else -> "Samsung"
                            }
                        }
                        return "Samsung"
                    }
                    255 -> return "Lovespouse"       // 0x00FF
                }

                // Fallback to database lookup
                val dbCompany = BleDatabase.getCompanyName(mfId, address)
                if (dbCompany != null) return dbCompany
            }
        }

        // OUI lookup as last resort
        val ouiCompany = BleDatabase.getCompanyName(null, address)
        if (ouiCompany != null) return ouiCompany

        return "Unknown BLE"
    }

    fun getManufacturerId(result: ScanResult): Int? {
        val mfData = result.scanRecord?.manufacturerSpecificData ?: return null
        if (mfData.size() > 0) return mfData.keyAt(0)
        return null
    }

    fun getRawPayloadHex(result: ScanResult): String {
        val bytes = result.scanRecord?.bytes ?: return ""
        return bytes.joinToString("") { "%02X".format(it) }
    }
}

class BleAdvertiserEngine(private val context: Context) {
    private val tag = "BleAdvertiserEngine"

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter get() = bluetoothManager.adapter
    private val advertiser: BluetoothLeAdvertiser? get() = bluetoothAdapter?.bluetoothLeAdvertiser
    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _packetsSent = MutableStateFlow(0L)
    val packetsSent = _packetsSent.asStateFlow()

    private val _activeAdvertisers = MutableStateFlow(0)
    val activeAdvertisers = _activeAdvertisers.asStateFlow()

    private var spamJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var intervalMs: Long = 100L
    var txPower: Int = AdvertiseSettings.ADVERTISE_TX_POWER_HIGH

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    fun isMultipleAdvertisementSupported(): Boolean {
        return bluetoothAdapter?.isMultipleAdvertisementSupported == true
    }

    fun start(advertisementSets: List<AdvertisementSet>) {
        if (_isRunning.value) return
        val adv = advertiser ?: run {
            Log.e(tag, "BLE Advertiser not available")
            return
        }

        _isRunning.value = true
        _packetsSent.value = 0

        val selectedSets = advertisementSets.filter { it.isSelected }
        if (selectedSets.isEmpty()) {
            _isRunning.value = false
            return
        }

        spamJob = scope.launch {
            val semaphore = Semaphore(0)
            val callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                    _activeAdvertisers.value = 1
                    semaphore.release()
                }

                override fun onStartFailure(errorCode: Int) {
                    Log.w(tag, "Adv failed: $errorCode")
                    semaphore.release()
                }
            }

            while (isActive && _isRunning.value) {
                for (adSet in selectedSets) {
                    if (!isActive || !_isRunning.value) break
                    try {
                        startAdvertising(adv, adSet, callback)
                        
                        // Wait for start callback (max 500ms)
                        semaphore.tryAcquire(500, TimeUnit.MILLISECONDS)
                        
                        _packetsSent.value++
                        delay(intervalMs)
                        
                        adv.stopAdvertising(callback)
                        _activeAdvertisers.value = 0
                    } catch (e: SecurityException) {
                        Log.e(tag, "Permission denied: ${e.message}")
                        _isRunning.value = false
                        break
                    } catch (e: Exception) {
                        Log.e(tag, "Error: ${e.message}")
                    }
                }
            }
            try { adv.stopAdvertising(callback) } catch (_: Exception) {}
        }
    }

    private fun startAdvertising(adv: BluetoothLeAdvertiser, adSet: AdvertisementSet, callback: AdvertiseCallback) {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(txPower)
            .setConnectable(adSet.connectable)
            .setTimeout(0)
            .build()

        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.setIncludeDeviceName(adSet.includeDeviceName)
        dataBuilder.setIncludeTxPowerLevel(adSet.includeTxPower)
        adSet.manufacturerData?.let { dataBuilder.addManufacturerData(it.manufacturerId, it.data) }
        adSet.serviceData?.let { dataBuilder.addServiceUuid(it.serviceUuid) }
        adSet.serviceData?.let { dataBuilder.addServiceData(it.serviceUuid, it.data) }
        val advertiseData = dataBuilder.build()

        var scanResponse: AdvertiseData? = null
        adSet.scanResponseManufacturerData?.let {
            scanResponse = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addManufacturerData(it.manufacturerId, it.data)
                .build()
        }

        if (scanResponse != null) {
            adv.startAdvertising(settings, advertiseData, scanResponse, callback)
        } else {
            adv.startAdvertising(settings, advertiseData, callback)
        }
    }

    fun stop() {
        _isRunning.value = false
        spamJob?.cancel()
        spamJob = null
        _activeAdvertisers.value = 0
    }

    fun destroy() {
        stop()
        scope.cancel()
    }
}
