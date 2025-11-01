package com.example.touchpad

import android.util.Log

const val TAG = "Parser"

fun parseAndExecuteScript(script: String) {
    var currentX = 0
    var currentY = 0
    var isButtonDown = false

    script.split(";")
        .map { it.removeSpaces() }
        .filter { it.isNotEmpty() }
        .forEach { command ->
            when {
                command.isCoordinates() -> {
                    parseCoordinates(command)?.let { (x, y) ->
                        if (isButtonDown) {
                            // touchDown(x, y)
                        }
                        currentX = x
                        currentY = y
                    }
                }
                command.equals("down", ignoreCase = true) -> {
                    isButtonDown = true
                    // touchDown(currentX, currentY)
                }
                command.equals("up", ignoreCase = true) -> {
                    isButtonDown = false
                    // touchUp(currentX, currentY)
                }
                else -> Log.e(TAG, "Unknown command: $command")
            }
        }
}

fun String.isCoordinates() = matches(Regex("\\d+,\\d+"))

fun parseCoordinates(command: String): Pair<Int, Int>? {
    return try {
        val (x, y) = command.split(",").map { it.toInt() }
        if (x in 0..<Hid.LOGICAL_SCREEN_MAX && y in 0..<Hid.LOGICAL_SCREEN_MAX) {
            Pair(x, y)
        } else {
            Log.e(TAG, "Coordinates out of bounds: $x, $y")
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Invalid coordinate format: $command", e)
        null
    }
}

fun String.removeSpaces() = replace(" ", "")
