package com.example.gazege.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun doubleToString(double: Double): String {
    return "$ %,1.0f".format(double)
}

enum class DateFormat {
    YEARMONTHNAME,
    DAYYEARMONTH
}

fun localDateToString(date: LocalDate, format: DateFormat): String {
    val pattern = when (format) {
        DateFormat.YEARMONTHNAME -> "yyyy-MMM"
        DateFormat.DAYYEARMONTH -> "dd-yyyy-M"
    }
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return date.format(formatter)
}

fun stringToDouble(string: String): Double {
    return string.removePrefix("$ ").filter {
        it != "."[0]
    }.toDouble()
}