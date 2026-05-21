package com.nlespam.generators

import com.nlespam.models.AdvertisementSet

interface SpamGenerator {
    val name: String
    fun generate(): List<AdvertisementSet>
}
