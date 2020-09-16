package com.toantran.trackme.extension

import java.lang.NumberFormatException
import java.util.*
import kotlin.math.abs


fun Double.toFixed(number: Int) : Double {
    val pattern = "%.${number}f"
    try {
        return String.format(pattern, this).toDouble()
    } catch (e: NumberFormatException) {
        return 0.0
    }
}

fun Float.toFixed(number: Int) : Float {
    val pattern = "%.${number}f"
    try {
        return String.format(pattern, this).toFloat()
    } catch (e: NumberFormatException) {
        return 0f
    }
}

fun Date.diffTimeInHours(date: Date): Float {
    val diff = this.time - date.time
    return abs(diff * 1f / (1000 * 60 * 60));
}