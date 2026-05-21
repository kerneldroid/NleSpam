package com.nlespam.generators

import com.nlespam.helpers.HexUtils
import com.nlespam.models.*

class LovespouseGenerator : SpamGenerator {
    override val name = "Lovespouse"

    private val lovespousePrefix = "FFFF006DB643CE97FE427C"
    private val lovespousePlayAppendix = "03038FAE"
    private val lovespouseStopAppendix = "03038FAF"

    val plays = mapOf(
        "E49C6C" to "Classic 1",
        "E7075E" to "Classic 2",
        "E68E4F" to "Classic 3",
        "E1313B" to "Classic 4",
        "E0B82A" to "Classic 5",
        "E32318" to "Classic 6",
        "E2AA09" to "Classic 7",
        "ED5DF1" to "Classic 8",
        "ECD4E0" to "Classic 9",
        "D41F5D" to "Independent 1-1",
        "D7846F" to "Independent 1-2",
        "D60D7E" to "Independent 1-3",
        "D1B20A" to "Independent 1-4",
        "D0B31B" to "Independent 1-5",
        "D3A029" to "Independent 1-6",
        "D22938" to "Independent 1-7",
        "DDDEC0" to "Independent 1-8",
        "DC57D1" to "Independent 1-9",
        "A4982E" to "Independent 2-1",
        "A7031C" to "Independent 2-2",
        "A68A0D" to "Independent 2-3",
        "A13579" to "Independent 2-4",
        "A0BC68" to "Independent 2-5",
        "A3275A" to "Independent 2-6",
        "A2AE4B" to "Independent 2-7",
        "AD59B3" to "Independent 2-8",
        "ACD0A2" to "Independent 2-9",
    )

    override fun generate(): List<AdvertisementSet> {
        val playSets = plays.map { (hexId, modeName) ->
            val payload = lovespousePrefix + hexId + lovespousePlayAppendix
            AdvertisementSet(
                title = "▶ $modeName",
                target = AdvertisementTarget.LOVESPOUSE,
                type = SpamType.LOVESPOUSE_PLAY,
                manufacturerData = ManufacturerData(ManufacturerIds.TYPO_PRODUCTS, HexUtils.decodeHex(payload)),
            )
        }

        val stopSets = plays.map { (hexId, modeName) ->
            val payload = lovespousePrefix + hexId + lovespouseStopAppendix
            AdvertisementSet(
                title = "⏹ Stop $modeName",
                target = AdvertisementTarget.LOVESPOUSE,
                type = SpamType.LOVESPOUSE_STOP,
                manufacturerData = ManufacturerData(ManufacturerIds.TYPO_PRODUCTS, HexUtils.decodeHex(payload)),
                isSelected = false,
            )
        }

        return playSets + stopSets
    }
}
