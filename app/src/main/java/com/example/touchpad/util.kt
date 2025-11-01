package com.example.touchpad

fun shortToLittleEndianByteArray(value: Short): ByteArray {
    return byteArrayOf(
        (value.toInt() and 0xFF).toByte(),         // [0] low byte
        ((value.toInt() shr 8) and 0xFF).toByte()   // [1] high byte
    )
}

fun removeSpaces(input: String): String {
    return input.replace("\\s".toRegex(), "")
}