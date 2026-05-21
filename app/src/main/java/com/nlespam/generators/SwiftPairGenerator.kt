package com.nlespam.generators

import com.nlespam.helpers.HexUtils
import com.nlespam.models.*

class SwiftPairGenerator : SpamGenerator {
    override val name = "Windows Swift Pair"

    private val prependedBytes = HexUtils.decodeHex("030080")

    val deviceNames = listOf(
        "Free AirPods Pro!",
        "Galaxy Buds Pro",
        "Bose QC Ultra",
        "Xbox Controller",
        "Surface Headphones",
        "WH-1000XM5",
        "AirPods Max",
        "Beats Studio Pro",
        "JBL Flip 6",
        "Magic Keyboard",
    )

    override fun generate(): List<AdvertisementSet> = deviceNames.map { deviceName ->
        AdvertisementSet(
            title = deviceName,
            target = AdvertisementTarget.WINDOWS,
            type = SpamType.SWIFT_PAIR,
            manufacturerData = ManufacturerData(
                ManufacturerIds.MICROSOFT,
                prependedBytes + deviceName.toByteArray()
            ),
        )
    }
}
