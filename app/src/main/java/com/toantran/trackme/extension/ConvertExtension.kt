package com.toantran.trackme.extension


fun Double.toFixed(number: Int) : Double {
    val pattern = "%.${number}f"
    return String.format(pattern, this).toDouble()
}