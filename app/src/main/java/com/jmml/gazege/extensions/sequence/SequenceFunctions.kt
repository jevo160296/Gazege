package com.jmml.gazege.extensions.sequence

fun <T> Sequence<T>.repeat() = sequence {
    while (true) {
        yieldAll(this@repeat)
    }
}