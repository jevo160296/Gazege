package com.example.gazege.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun doubleToMoneyString(double: Double): String {
    return "$ %,1.0f".format(double)
}

fun doubleToPercentageString(double: Double): String {
    return "%.2f %%".format(double * 100.0)
}

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