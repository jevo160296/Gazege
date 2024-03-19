package com.example.gazege.ui

import com.jmml.gazege.ui.floatToShortText
import org.junit.Test

class FormattersTest {
    @Test
    fun testFloatToShortText() {
        val numberRange = 0.000000000000000000001f..100000000000000000000000000f
        val numberList = generateSequence(seed = numberRange.start) {
            if (it < numberRange.endInclusive) {
                it * 10
            } else {
                null
            }
        }.toList()

        val formatted = numberList.map { it to floatToShortText(it, 2) }

        println("number: $numberRange")
        println("formatted: $formatted")
    }
}