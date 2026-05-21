package com.nlespam.helpers

object HexUtils {
    fun decodeHex(hex: String): ByteArray {
        check(hex.length % 2 == 0) { "Hex string must have even length" }
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    fun intToHex(value: Int): String = "%02x".format(value)

    fun byteToHex(value: Byte): String = "%02x".format(value)

    fun randomHexBytes(count: Int): ByteArray = kotlin.random.Random.nextBytes(count)
}
