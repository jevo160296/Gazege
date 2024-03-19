package com.example.gazege

import com.jmml.gazege.core.export.parseDate
import com.jmml.gazege.core.export.realizeFormatter
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private data class Example(
    val dateText: String,
    val expectedDate: LocalDate
)

private infix fun String.expects(that: LocalDate): Example =
    Example(this, that)

class ExportTests {
    @Test
    fun testParseDate() {
        val inputDatesText = listOf(
            "01-01-2023" expects LocalDate.of(2023, 1, 1),
            "1-1-2023" expects LocalDate.of(2023, 1, 1),
            "1/1/2023" expects LocalDate.of(2023, 1, 1),
            "2023-01-01" expects LocalDate.of(2023, 1, 1),
            "2023-1-1" expects LocalDate.of(2023, 1, 1),
            "2023/1/01" expects LocalDate.of(2023, 1, 1),
            "2023/07/05" expects LocalDate.of(2023, 7, 5)
        )
        inputDatesText.forEach {
            val formatter = realizeFormatter(arrayOf(it.dateText))
            val parsedDate = parseDate(it.dateText, formatter)
            assertTrue(it.expectedDate == parsedDate)
        }
    }
}