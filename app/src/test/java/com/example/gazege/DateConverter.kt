package com.example.gazege

import com.example.gazege.core.converters.Converters
import com.example.gazege.core.stableMinusMonths
import com.example.gazege.core.stablePlusMonths
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class DateConverterUnitTest {
    private fun testOneDate(localDate: LocalDate, converter: Converters): LocalDate? {
        val transformedDate = converter.fromDate(localDate)
        return converter.toDate(transformedDate)
    }

    private fun testOneDateLong(localDate: LocalDate, converter: Converters): Pair<Long?, Long> {
        val longFromConverter = converter.fromDate(localDate)
        val dateFromLocalDate =
            Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.ofHours(-5)))
        val longFromDate = dateFromLocalDate.time
        return Pair(longFromConverter, longFromDate)
    }

    @Test
    fun testConversion(){
        val startDate = LocalDate.of(2022, 1, 1)
        val dates: Array<LocalDate> = (0..365).map {
            startDate.plusDays(it.toLong())
        }.toTypedArray()
        val converter = Converters()
        dates.forEach {
            assertEquals(it, testOneDate(it, converter))
        }
    }

    @Test
    fun testConversionCompatibleWithDate(){
        val startDate = LocalDate.of(2022, 1, 1)
        val dates: Array<LocalDate> = (0..365).map {
            startDate.plusDays(it.toLong())
        }.toTypedArray()
        val converter = Converters()
        dates.forEach {
            val pairs = testOneDateLong(it, converter)
            assertEquals(pairs.first, pairs.second)
        }
    }
}

class DateFunctionsUnitTest {
    data class Example(
        val date: LocalDate,
        val months: Long,
        val expectedDate: LocalDate
    )

    @Test
    fun testStablePlusMonths() {
        val examples = listOf(
            Example(
                LocalDate.of(2023, 2, 28),
                1,
                LocalDate.of(2023, 3, 31)
            ),
            Example(
                LocalDate.of(2023, 3, 31),
                3,
                LocalDate.of(2023, 6, 30)
            ),
            Example(
                LocalDate.of(2023, 6, 30),
                8,
                LocalDate.of(2024, 2, 29)
            )
        )
        examples.forEach {
            assertEquals(
                "Fechas no iguales",
                it.expectedDate,
                stablePlusMonths(it.date, it.months)
            )
        }
    }

    @Test
    fun testStableMinusMonths() {
        val examples = listOf(
            Example(
                LocalDate.of(2023, 2, 28),
                1,
                LocalDate.of(2023, 1, 31)
            ),
            Example(
                LocalDate.of(2023, 1, 31),
                3,
                LocalDate.of(2022, 10, 31)
            ),
            Example(
                LocalDate.of(2022, 10, 31),
                8,
                LocalDate.of(2022, 2, 28)
            )
        )
        examples.forEach {
            assertEquals(
                "Fechas no iguales",
                it.expectedDate,
                stableMinusMonths(it.date, it.months)
            )
        }
    }
}