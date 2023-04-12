package com.example.gazege.core

import com.example.gazege.core.entities.AbsoluteMonthDays
import com.example.gazege.core.entities.WeekDays
import com.example.gazege.core.entities.toByteString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.random.Random
import kotlin.random.nextInt

interface IExample {
    val expectedStringRepresentation: String
    val expectedInt: Int
    val calculatedStringRepresentation: String
    val calculatedInt: Int
}

class BudgetTests {
    private fun IExample.assert() {
        assertEquals(this.expectedStringRepresentation, this.calculatedStringRepresentation)
        assertEquals(this.expectedInt, this.calculatedInt)
    }

    @Test
    fun testWeekDaysStringRepresentation() {
        data class Example(
            val weekDays: WeekDays,
            override val expectedStringRepresentation: String,
            override val expectedInt: Int
        ) : IExample {
            override val calculatedStringRepresentation get() = weekDays.toInt().toByteString(7)
            override val calculatedInt get() = weekDays.toInt()
        }

        val manualExamples = listOf(
            Example(
                WeekDays(
                    sunday = false,
                    monday = false,
                    tuesday = false,
                    wednesday = false,
                    thursday = false,
                    friday = false,
                    saturday = false
                ),
                expectedStringRepresentation = "0000000",
                expectedInt = 0b0000000
            ),
            Example(
                WeekDays(
                    sunday = true,
                    monday = false,
                    tuesday = false,
                    wednesday = true,
                    thursday = false,
                    friday = false,
                    saturday = true
                ),
                expectedStringRepresentation = "1001001",
                expectedInt = 0b1001001
            ),
            Example(
                WeekDays(
                    sunday = false,
                    monday = true,
                    tuesday = false,
                    wednesday = false,
                    thursday = false,
                    friday = true,
                    saturday = false
                ),
                expectedStringRepresentation = "0100010",
                expectedInt = 0b0100010
            )
        )

        manualExamples.forEach { it.assert() }

        (0..0b1111111).forEach {
            val byteString = it.toString(2).padStart(7, '0')
            val example = Example(
                WeekDays.from(it),
                expectedStringRepresentation = byteString,
                expectedInt = it
            )
            example.assert()
        }
    }

    @Test
    fun testAbsoluteMonthDays() {
        data class Example(
            val absoluteMonthDays: AbsoluteMonthDays,
            override val expectedStringRepresentation: String,
            override val expectedInt: Int
        ) : IExample {
            override val calculatedStringRepresentation
                get() = absoluteMonthDays.toInt().toByteString(31)
            override val calculatedInt get() = absoluteMonthDays.toInt()
        }

        val manualExamples = listOf(
            Example(
                AbsoluteMonthDays(setOf(1, 5, 28, 30)),
                expectedStringRepresentation = "1000100000000000000000000001010",
                expectedInt = 0b1000100000000000000000000001010
            ),
            Example(
                AbsoluteMonthDays(setOf()),
                expectedStringRepresentation = "0000000000000000000000000000000",
                expectedInt = 0b0000000000000000000000000000000
            ),
            Example(
                AbsoluteMonthDays((1..31).toSet()),
                expectedStringRepresentation = "1111111111111111111111111111111",
                expectedInt = 0b1111111111111111111111111111111
            )
        )

        manualExamples.forEach { it.assert() }

        val shouldThrowError = arrayOf(
            { AbsoluteMonthDays(setOf(0)) },
            { AbsoluteMonthDays(setOf(32)) },
            { AbsoluteMonthDays(setOf(0, 32)) }
        )

        shouldThrowError.forEach { assertThrows(AssertionError::class.java) { it() } }

        val random = Random(3)

        (0..10000)
            .map { random.nextInt(0..0b1111111111111111111111111111111) }
            .forEach {
                val byteString = it.toString(2).padStart(31, '0')
                val example = Example(
                    AbsoluteMonthDays.from(it),
                    expectedStringRepresentation = byteString,
                    expectedInt = it
                )
                example.assert()
            }
    }
}