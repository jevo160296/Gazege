package com.example.gazege

import com.example.gazege.core.DateConverter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

class DateConverterUnitTest {
    private fun testOneDate(localDate: LocalDate, converter: DateConverter): LocalDate? {
        val transformedDate = converter.fromDate(localDate)
        return converter.toDate(transformedDate)
    }

    private fun testOneDateLong(localDate: LocalDate, converter: DateConverter): Pair<Long?, Long>{
        val longFromConverter = converter.fromDate(localDate)
        val dateFromLocalDate = Date.from(localDate.atStartOfDay().toInstant(ZoneOffset.ofHours(-5)))
        val longFromDate = dateFromLocalDate.time
        return Pair(longFromConverter, longFromDate)
    }

    @Test
    fun testConversion(){
        val startDate = LocalDate.of(2022, 1, 1)
        val dates: Array<LocalDate> = (0..365).map {
            startDate.plusDays(it.toLong())
        }.toTypedArray()
        val converter = DateConverter()
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
        val converter = DateConverter()
        dates.forEach {
            val pairs = testOneDateLong(it, converter)
            assertEquals(pairs.first, pairs.second)
        }
    }
}