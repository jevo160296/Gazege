package com.example.gazege.ui

fun doubleToString(double: Double): String{
    return "$ %.0f".format(double)
}

fun stringToDouble(string: String): Double{
    return string.removePrefix("$ ").filter {
        it != "."[0]
    }.toDouble()
}