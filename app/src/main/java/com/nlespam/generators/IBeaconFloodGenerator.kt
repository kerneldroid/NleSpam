package com.nlespam.generators

import com.nlespam.models.*
import kotlin.random.Random

class IBeaconFloodGenerator : SpamGenerator {
    override val name = "iBeacon Flood"

    // iBeacon format: type=0x02, length=0x15, UUID(16 bytes), Major(2), Minor(2), TX Power(1)
    // Apple manufacturer ID 0x004C

    val beacons = listOf(
        "Starbucks Rewards" to "B9407F30-F5F8-466E-AFF9-25556B57FE6D",
        "McDonald's Offers" to "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6",
        "Target Store" to "A0B13730-3A9A-11E3-AA6E-0800200C9A66",
        "Walmart Savings" to "74278BDA-B644-4520-8F0C-720EAF059935",
        "Apple Store" to "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0",
        "Nike Member" to "F7826DA6-4FA2-4E98-8024-BC5B71E0893E",
        "Museum Guide" to "D9B9EC1F-3925-43D0-80A9-1E39D4CEA95C",
        "Airport Gate A1" to "FDA50693-A4E2-4FB1-AFCF-C6EB07647825",
        "Hotel Lobby" to "B0702880-A295-A8AB-F734-031A98A512DE",
        "Stadium Section 101" to "8DEEFBB9-F738-4297-8040-96668BB44281",
        "Free Wi-Fi Here!" to "A495BB10-C5B1-4B44-B512-1370F02D74DE",
        "Parking Garage P2" to "A495BB20-C5B1-4B44-B512-1370F02D74DE",
        "Concert Merch 50% Off" to "A495BB30-C5B1-4B44-B512-1370F02D74DE",
        "Gallery Tour Start" to "A495BB40-C5B1-4B44-B512-1370F02D74DE",
        "Emergency Exit →" to "A495BB50-C5B1-4B44-B512-1370F02D74DE",
    )

    private fun uuidToBytes(uuid: String): ByteArray {
        val hex = uuid.replace("-", "")
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    override fun generate(): List<AdvertisementSet> = beacons.map { (label, uuid) ->
        val major = Random.nextInt(0, 65535)
        val minor = Random.nextInt(0, 65535)
        val txPower: Byte = (-59).toByte() // typical iBeacon TX power at 1m

        val payload = byteArrayOf(0x02, 0x15) +            // iBeacon type + length
                uuidToBytes(uuid) +                          // UUID 16 bytes
                byteArrayOf(
                    (major shr 8).toByte(), (major and 0xFF).toByte(),  // Major
                    (minor shr 8).toByte(), (minor and 0xFF).toByte(),  // Minor
                    txPower                                              // TX Power
                )

        AdvertisementSet(
            title = "📍 $label",
            target = AdvertisementTarget.BEACON,
            type = SpamType.IBEACON_FLOOD,
            manufacturerData = ManufacturerData(ManufacturerIds.APPLE, payload),
        )
    }
}
