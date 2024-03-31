package com.jmml.gazege.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.truncate

fun doubleToMoneyString(double: Double): String {
    return "$ %,1.0f".format(double)
}

fun doubleToPercentageString(double: Double): String =
    when (double) {
        Double.NEGATIVE_INFINITY -> "-∞ %"
        Double.POSITIVE_INFINITY -> "∞ %"
        else -> "%.2f %%".format(double * 100.0)
    }

private fun round(number: Float, decimals: Int): Float =
    round(number * 10f.pow(decimals)) / 10f.pow(decimals)

private val NOTATIONS = mapOf(
    -24 to ("y" to 0),
    -23 to ("?" to -1),
    -22 to ("?" to -2),
    -21 to ("z" to 0),
    -20 to ("?" to -1),
    -19 to ("?" to -2),
    -18 to ("a" to 0),
    -17 to ("?" to -1),
    -16 to ("?" to -2),
    -15 to ("f" to 0),
    -14 to ("?" to -1),
    -13 to ("?" to -2),
    -12 to ("p" to 0),
    -11 to ("?" to -1),
    -10 to ("?" to -2),
    -9 to ("n" to 0),
    -8 to ("?" to -1),
    -7 to ("?" to -2),
    -6 to ("u" to 0),
    -5 to ("?" to -1),
    -4 to ("?" to -2),
    -3 to ("m" to 0),
    -2 to ("c" to 0),
    -1 to ("d" to 0),
    0 to ("" to 0),
    1 to ("D" to -1),
    2 to ("H" to -2),
    3 to ("K" to 0),
    4 to ("?" to -1),
    5 to ("?" to -2),
    6 to ("M" to 0),
    7 to ("?" to -1),
    8 to ("?" to -2),
    9 to ("G" to 0),
    10 to ("?" to -1),
    11 to ("?" to -2),
    12 to ("T" to 0),
    13 to ("?" to -1),
    14 to ("?" to -2),
    15 to ("P" to 0),
    16 to ("?" to -1),
    17 to ("?" to -2),
    18 to ("E" to 0),
    19 to ("?" to -1),
    20 to ("?" to -2),
    21 to ("Z" to 0),
    22 to ("?" to -1),
    23 to ("?" to -2),
    24 to ("Y" to 0)
)

fun floatToShortText(number: Float, decimals: Int): String {
    val absNumber = number.absoluteValue
    if (absNumber == 0.0f) {
        return "0"
    }
    val iniSize = truncate(log10(absNumber)).toInt()
    val iniCantWhole = round(absNumber / 10f.pow(iniSize), decimals)

    val (preSize, preCantWhole) = if (iniCantWhole < 1.0f) {
        iniSize - 1 to round(absNumber / 10f.pow(iniSize - 1), decimals)
    } else {
        iniSize to iniCantWhole
    }

    val notation = NOTATIONS[preSize]
    val (size, cantWhole) =
        if (notation == null) {
            preSize to preCantWhole
        } else {
            val difference = notation.second

            preSize + difference to round(absNumber / 10f.pow(preSize + difference), decimals)
        }

    val prefix = if (number < 0f) {
        "-"
    } else {
        ""
    }
    val suffix = if (size in NOTATIONS) {
        NOTATIONS[size]!!.first
    } else {
        "e$size"
    }
    return "$prefix${
        if (decimals == 0) {
            cantWhole.toInt()
        } else {
            cantWhole
        }
    }$suffix"
}

fun doubleToShortText(number: Double, decimals: Int): String =
    floatToShortText(number.toFloat(), decimals)

fun doubleToShortMoneyText(number: Double, decimals: Int): String =
    "$${doubleToShortText(number, decimals)}"

enum class DateFormat {
    YEARMONTHNAME,
    DAYMONTHYEAR
}

fun localDateToString(date: LocalDate, format: DateFormat): String {
    val pattern = when (format) {
        DateFormat.YEARMONTHNAME -> "yyyy-MMM"
        DateFormat.DAYMONTHYEAR -> "dd-MMM-yyyy"
    }
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return date.format(formatter)
}

fun stringToDouble(string: String): Double {
    return string.removePrefix("$ ").filter {
        it != "."[0]
    }.toDouble()
}