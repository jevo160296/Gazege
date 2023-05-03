package com.example.gazege

import androidx.compose.ui.text.AnnotatedString
import com.example.gazege.ui.widgets.NumberTransformation
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

data class ExampleFromDouble(
    val numberTransformation: NumberTransformation,
    val numberOriginal: Double,
    val transformedOffsetExpected: List<Int>,
    val originalOffsetExpected: List<Int>
) {
    val textOriginal: String = numberOriginal.let {
        val numberTransformation = NumberTransformation(
            thousandsSeparator = ',',
            decimalSeparator = '.'
        )
        numberTransformation.doubleToString(it)
    }
    val textTransformed: String = textOriginal.let {
        numberTransformation.filter(AnnotatedString(text = it)).text.text
    }
    val offsetTransformed: NumberTransformation.Offset = numberOriginal.let {
        val numberTransformation = NumberTransformation(
            thousandsSeparator = ',',
            decimalSeparator = '.'
        )
        val transformation = numberTransformation.reformat(textOriginal)
        NumberTransformation.Offset(transformation)
    }
}

data class PartialExampleFromText(
    val originalText: String,
    val numberTransformation: NumberTransformation,
    val expectedFilteredText: String,
    val transformedToOriginalOffsetExpected: List<Int>,
    val originalToTransformedOffsetExpected: List<Int>
) {
    fun toFull(): ExampleFromText = ExampleFromText(
        originalText,
        numberTransformation,
        expectedFilteredText,
        transformedToOriginalOffsetExpected,
        originalToTransformedOffsetExpected
    )
}

data class ExampleFromText(
    val originalText: String,
    val numberTransformation: NumberTransformation,
    val expectedFilteredText: String,
    val transformedToOriginalOffsetExpected: List<Int>,
    val originalToTransformedOffsetExpected: List<Int>
) {
    private val transformedText = numberTransformation.filter(AnnotatedString(originalText))
    private val calculatedFilteredText = transformedText.text.text
    private val calculatedOffsetMapping = transformedText.offsetMapping
    fun assertFilterText() {
        assertEquals(expectedFilteredText, calculatedFilteredText)
    }

    fun assertTransformedToOriginalOffset() {
        val calculatedOriginalOffset =
            "_$expectedFilteredText" // Se debe añadir un carácter adicional al inicio pues representa la posición al inicio del texto
                .mapIndexed { index, _ ->
                    calculatedOffsetMapping.transformedToOriginal(index)
                }

        assertArrayEquals(
            transformedToOriginalOffsetExpected.toTypedArray(),
            calculatedOriginalOffset.toTypedArray()
        )
    }

    fun assertOriginalToTransformedOffset() {
        val calculatedTransformedOffset =
            "_$originalText" // Se debe añadir un carácter adicional al inicio pues representa la posición al inicio del texto
                .mapIndexed { index, _ ->
                    calculatedOffsetMapping.originalToTransformed(index)
                }
        assertArrayEquals(
            originalToTransformedOffsetExpected.toTypedArray(),
            calculatedTransformedOffset.toTypedArray()
        )
    }
}

class TextFieldUnitTestDecimal {
    private val numberTransformation = NumberTransformation(
        thousandsSeparator = '.', decimalSeparator = ','
    )
    private val exampleFromDoubles: List<ExampleFromDouble> = listOf(
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 1234567890.12,
            transformedOffsetExpected = listOf(
                0, 1, 3, 4, 5, 7, 8, 9, 11, 12, 13, 14, 15, 16
            ),//   1  2  3  4  5  6  7  8   9   0   ,   1   2
            //     1  .  2  3  4  .  5  6   7   .   8   9   0
            //  0  1  2  3  4  5  6  7  8   9  10  11  12  13
            originalOffsetExpected = listOf
                (
                0, 1, 1, 2, 3, 4, 4, 5, 6, 7, 7, 8, 9, 10, 11, 12, 13
            ),//   1  .  2  3  4  .  5  6  7  .  8  9  0   ,   1   2
            //  0  1  2  3  4  5  6  7  8  9  10 11 12 13
            //     1  2  3  4  5  6  7  8  9  0  ,  1  2
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 123.45,
            transformedOffsetExpected = listOf(0, 1, 2, 3, 4, 5, 6),
            originalOffsetExpected = listOf(0, 1, 2, 3, 4, 5, 6), //123
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 12345.67,
            transformedOffsetExpected = listOf(0, 1, 2, 4, 5, 6, 7, 8, 9),
            originalOffsetExpected = listOf(0, 1, 2, 2, 3, 4, 5, 6, 7, 8), //12.345
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 1234567.89,
            transformedOffsetExpected = listOf(0, 1, 3, 4, 5, 7, 8, 9, 10, 11, 12),
            originalOffsetExpected = listOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7, 8, 9, 10), // 1.234.567
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 12345678.90,
            transformedOffsetExpected = listOf(0, 1, 2, 4, 5, 6, 8, 9, 10, 11, 12, 13),
            originalOffsetExpected = listOf(0, 1, 2, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10, 11)
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 1234.5,
            transformedOffsetExpected = listOf(
                0, 1, 3, 4, 5, 6, 7, 8
            ),
            // _  1  2  3  4  .  5  6
            // _  1  ,  2  3  4  .  5  0
            // 0  1  2  3  4  5  6  7  0

            originalOffsetExpected = listOf(
                0, 1, 1, 2, 3, 4, 5, 6, 7
            ),
            // _  1  ,  2  3  4  .  5  0
            // _  1  2  3  4  .  5  0
            // 0  1  2  3  4  5  6  7  0
        )
    )

    @Test
    fun originalToTransformed() {
        exampleFromDoubles.forEach {
            checkOneOriginalToTransformed(
                it.textOriginal,
                it.textTransformed,
                it.transformedOffsetExpected.toTypedArray(),
                it.offsetTransformed
            )
        }
    }

    private fun checkOneOriginalToTransformed(
        textOriginal: String,
        textTransformed: String,
        transformedOffsetExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val offsetList = (0..textOriginal.length).map { it }
        val calculatedTransformedOffset =
            offsetList.map { offsetTransformed.originalToTransformed(it) }.toTypedArray()
        val reconstructedText = reconstructText(textTransformed, calculatedTransformedOffset)
        assertArrayEquals(transformedOffsetExpected, calculatedTransformedOffset)
        assertEquals(textOriginal, reconstructedText)
    }

    @Test
    fun transformedToOriginal() {
        exampleFromDoubles.forEach {
            checkOneTransformedToOriginal(
                it.textOriginal,
                it.textTransformed,
                it.originalOffsetExpected.toTypedArray(),
                it.offsetTransformed
            )
        }
    }

    private fun checkOneTransformedToOriginal(
        textOriginal: String,
        textTransformed: String,
        originalOffsetExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val offsetList = (0..textTransformed.length).map { it }
        val calculatedOriginalOffset =
            offsetList.map { offsetTransformed.transformedToOriginal(it) }.toTypedArray()
        val offsetToReconstructOriginal =
            textTransformed.toList().zip(calculatedOriginalOffset.drop(1)).filter { (text, _) ->
                text != numberTransformation.thousandsSeparator
            }.map { (_, offset) ->
                offset
            }.toTypedArray()
        val reconstructedText = reconstructText(
            textOriginal, arrayOf(0, *offsetToReconstructOriginal)
        )
        assertArrayEquals(originalOffsetExpected, calculatedOriginalOffset)
        assertEquals(textOriginal, reconstructedText)
    }

    private fun reconstructText(text: String, indices: Array<Int>): String =
        indices.drop(1).map { text[it - 1] }.joinToString("").replace(',', '.')
}

class TextFieldUnitTestDecimalNegative {
    private val numberTransformation = NumberTransformation(
        thousandsSeparator = '.', decimalSeparator = ','
    )
    private val exampleFromDoubles: List<ExampleFromDouble> = listOf(
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = -1234567890.12,
            transformedOffsetExpected = listOf(
                0, 1, 3, 4, 5, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17
            ),
            //     -  1  2  3  4  5  6  7   8   9   0   ,   1   2
            //  0  1  2  3  4  5  6  7  8   9   10  11  12  13  14  15  16  17
            //     -  1  .  2  3  4  .  5   6   7   .   8   9   0   ,   1   2
            originalOffsetExpected = listOf(
                0, 1, 2, 2, 3, 4, 5, 5, 6, 7, 8, 8, 9, 10, 11, 12, 13, 14
            ),
            //     -  1  .  2  3  4  .  5  6  7  .  8  9  0   ,   1   2
            //  0  1  2  3  4  5  6  7  8  9  10 11 12 13 14
            //     -  1  2  3  4  5  6  7  8  9  0  ,  1  2
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 123.45,
            transformedOffsetExpected = listOf(0, 1, 2, 3, 4, 5, 6),
            originalOffsetExpected = listOf(0, 1, 2, 3, 4, 5, 6), //123
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 12345.67,
            transformedOffsetExpected = listOf(0, 1, 2, 4, 5, 6, 7, 8, 9),
            originalOffsetExpected = listOf(0, 1, 2, 2, 3, 4, 5, 6, 7, 8), //12.345
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 1234567.89,
            transformedOffsetExpected = listOf(0, 1, 3, 4, 5, 7, 8, 9, 10, 11, 12),
            originalOffsetExpected = listOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7, 8, 9, 10), // 1.234.567
        ),
        ExampleFromDouble(
            numberTransformation = numberTransformation,
            numberOriginal = 12345678.90,
            transformedOffsetExpected = listOf(0, 1, 2, 4, 5, 6, 8, 9, 10, 11, 12, 13),
            originalOffsetExpected = listOf(0, 1, 2, 2, 3, 4, 5, 5, 6, 7, 8, 9, 10, 11),
        )
    )

    @Test
    fun originalToTransformed() {
        exampleFromDoubles.forEach {
            checkOneOriginalToTransformed(
                it.textOriginal,
                it.textTransformed,
                it.offsetTransformed
            )
        }
    }

    private fun checkOneOriginalToTransformed(
        textOriginal: String,
        textTransformed: String,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val offsetList = (0..textOriginal.length).map { it }
        val calculatedTransformedOffset =
            offsetList.map { offsetTransformed.originalToTransformed(it) }.toTypedArray()
        val reconstructedText = reconstructText(textTransformed, calculatedTransformedOffset)
        assertEquals(textOriginal, reconstructedText)
    }

    @Test
    fun transformedToOriginal() {
        exampleFromDoubles.forEach {
            checkOneTransformedToOriginal(
                it.textOriginal,
                it.textTransformed,
                it.originalOffsetExpected.toTypedArray(),
                it.offsetTransformed
            )
        }
    }

    private fun checkOneTransformedToOriginal(
        textOriginal: String,
        textTransformed: String,
        originalOffsetExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val offsetList = (0..textTransformed.length).map { it }
        val calculatedOriginalOffset =
            offsetList.map { offsetTransformed.transformedToOriginal(it) }.toTypedArray()
        val offsetToReconstructOriginal =
            textTransformed
                .toList()
                .zip(calculatedOriginalOffset.drop(1))
                .filter { (text, _) ->
                    text != numberTransformation.thousandsSeparator
                }.map { (_, offset) ->
                    offset
                }.toTypedArray()
        val reconstructedText = reconstructText(
            textOriginal.replace('.', ','),
            arrayOf(0, *offsetToReconstructOriginal)
        ).replace(',', '.')
        assertArrayEquals(originalOffsetExpected, calculatedOriginalOffset)
        assertEquals(textOriginal, reconstructedText)
    }

    private fun reconstructText(text: String, indices: Array<Int>): String =
        indices
            .drop(1)
            .map { if (text[it - 1] != '.') text[it - 1] else text[it - 2] }
            .joinToString("")
            .replace(',', '.')
}

class NumberFieldUnitTestText {
    private val numberTransformation = NumberTransformation(
        thousandsSeparator = '.', decimalSeparator = ','
    )
    private val examplesFromText: List<ExampleFromText> = listOf(
        PartialExampleFromText(
            numberTransformation = numberTransformation,
            originalText = "-1234.56",
            expectedFilteredText = "-1.234,56",
            transformedToOriginalOffsetExpected = listOf(
                0, 1, 2, 2, 3, 4, 5, 6, 7, 8
            ),//_  -  1  ,  2  3  4  .  5  6
            //  _  -  1  2  3  4  .  5  6
            //  0  1  2  3  4  5  6  7  8  9
            originalToTransformedOffsetExpected = listOf(
                0, 1, 2, 4, 5, 6, 7, 8, 9
            ),//_  -  1  2  3  4  .  5  6
            //  _  -  1  ,  2  3  4  .  5  6
            //  0  1  2  3  4  5  6  7  8  9
        ),
        PartialExampleFromText(
            numberTransformation = numberTransformation,
            originalText = "-12345678.90",
            expectedFilteredText = "-12.345.678,90",
            transformedToOriginalOffsetExpected = listOf(
                0, 1, 2, 3, 3, 4, 5, 6, 6, 7, 8, 9, 10, 11, 12
            ),//_  -  1  2  ,  3  4  5  ,  6  7  8  .  9  0
            //  _  -  1  2  3  4  5  6  7  8  .  9  0
            //  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4
            originalToTransformedOffsetExpected = listOf(
                0, 1, 2, 3, 5, 6, 7, 9, 10, 11, 12, 13, 14
            ),//_  -  1  2  3  4  5  6  7  8  .  9  0
            //  _  -  1  2  ,  3  4  5  ,  6  7  8  .  9  0
            //  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4
        ),
        PartialExampleFromText(
            numberTransformation = numberTransformation,
            originalText = "1234.56",
            expectedFilteredText = "1.234,56",
            transformedToOriginalOffsetExpected = listOf(
                0, 1, 1, 2, 3, 4, 5, 6, 7
            ),//_  1  ,  2  3  4  .  5  6
            //  _  1  2  3  4  .  5  6
            //  0  1  2  3  4  5  6  7  8
            originalToTransformedOffsetExpected = listOf(
                0, 1, 3, 4, 5, 6, 7, 8
            ),//_  1  2  3  4  .  5  6
            //  _  1  ,  2  3  4  .  5  6
            //  0  1  2  3  4  5  6  7  8
        ),
        PartialExampleFromText(
            numberTransformation = numberTransformation,
            originalText = "1234",
            expectedFilteredText = "1.234",
            transformedToOriginalOffsetExpected = listOf(
                0, 1, 1, 2, 3, 4
            ),//_  1  ,  2  3  4
            //  _  1  2  3  4
            //  0  1  2  3  4  5
            originalToTransformedOffsetExpected = listOf(
                0, 1, 3, 4, 5
            ),//_  1  2  3  4
            //  _  1  ,  2  3  4
            //  0  1  2  3  4  5  6  7  8
        ),
        PartialExampleFromText(
            numberTransformation = numberTransformation,
            originalText = "1234.5",
            expectedFilteredText = "1.234,5",
            transformedToOriginalOffsetExpected = listOf(
                0, 1, 1, 2, 3, 4, 5, 6
            ),//_  1  ,  2  3  4  .  5
            //  _  1  2  3  4  .  5
            //  0  1  2  3  4  5  6  7
            originalToTransformedOffsetExpected = listOf(
                0, 1, 3, 4, 5, 6, 7
            ),//_  1  2  3  4  .  5
            //  _  1  ,  2  3  4  .  5
            //  0  1  2  3  4  5  6  7
        )
    ).map(PartialExampleFromText::toFull)

    @Test
    fun testFilterText() {
        examplesFromText.forEach(ExampleFromText::assertFilterText)
    }

    @Test
    fun testTransformedToOriginalOffset() {
        examplesFromText.forEach(ExampleFromText::assertTransformedToOriginalOffset)
    }

    @Test
    fun testOriginalToTransformedOffset() {
        examplesFromText.forEach(ExampleFromText::assertOriginalToTransformedOffset)
    }
}