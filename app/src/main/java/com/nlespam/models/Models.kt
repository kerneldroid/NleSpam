package com.nlespam.models

import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertisingSetCallback
import android.os.ParcelUuid

/**
 * Target platform for the advertisement
 */
enum class AdvertisementTarget(val label: String) {
    ANDROID("Android"),
    IOS("iOS"),
    WINDOWS("Windows"),
    SAMSUNG("Samsung"),
    LOVESPOUSE("Lovespouse"),
    BEACON("Beacon"),
    GOOGLE("Google"),
    XIAOMI("Xiaomi")
}

/**
 * Type of spam attack
 */
enum class SpamType(val label: String) {
    FAST_PAIR("Fast Pair"),
    SAMSUNG_BUDS("Samsung Buds"),
    SAMSUNG_WATCH("Samsung Watch"),
    APPLE_DEVICE_POPUP("Apple Device Popup"),
    APPLE_ACTION_MODAL("Apple Action Modal"),
    SWIFT_PAIR("Swift Pair"),
    LOVESPOUSE_PLAY("Lovespouse Play"),
    LOVESPOUSE_STOP("Lovespouse Stop"),
    IBEACON_FLOOD("iBeacon Flood"),
    CHROMECAST_SPAM("Chromecast Spam"),
    AIRTAG_CLONE("AirTag Clone"),
    MIX_SPAM("Mix Spam"),
    MIXED_ALL("Mix All")
}

/**
 * Detected spam entry from BLE scan radar
 */
data class SpamRadarEntry(
    val deviceAddress: String,
    val rssi: Int,
    val detectedType: String,
    val manufacturerId: Int?,
    val rawPayloadHex: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * TX Power level
 */
enum class TxPowerLevel(val value: Int) {
    ULTRA_LOW(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3)
}

/**
 * Manufacturer-specific data payload
 */
data class ManufacturerData(
    val manufacturerId: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ManufacturerData) return false
        return manufacturerId == other.manufacturerId && data.contentEquals(other.data)
    }
    override fun hashCode(): Int = 31 * manufacturerId + data.contentHashCode()
}

/**
 * Service data payload
 */
data class ServiceData(
    val serviceUuid: ParcelUuid,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServiceData) return false
        return serviceUuid == other.serviceUuid && data.contentEquals(other.data)
    }
    override fun hashCode(): Int = 31 * serviceUuid.hashCode() + data.contentHashCode()
}

/**
 * A complete advertisement configuration ready to be broadcast
 */
data class AdvertisementSet(
    val title: String,
    val target: AdvertisementTarget,
    val type: SpamType,
    val manufacturerData: ManufacturerData? = null,
    val serviceData: ServiceData? = null,
    val includeDeviceName: Boolean = false,
    val includeTxPower: Boolean = false,
    val connectable: Boolean = false,
    val legacyMode: Boolean = true,
    val scanResponseManufacturerData: ManufacturerData? = null,
    var isSelected: Boolean = true
)

/**
 * Manufacturer IDs
 */
object ManufacturerIds {
    const val APPLE = 76        // 0x004C
    const val MICROSOFT = 6     // 0x0006
    const val SAMSUNG = 117     // 0x0075
    const val TYPO_PRODUCTS = 255 // 0x00FF
    const val XIAOMI = 911      // 0x038F
    const val GOOGLE = 224      // 0x00E0
}

data class PacketLogEntry(
    val timestamp: Long,
    val type: String,
    val payloadSize: Int,
    val packetNumber: Long,
)
