package com.example.gazege

import androidx.compose.ui.text.AnnotatedString
import com.example.gazege.ui.widgets.NumberTransformation
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class TextFieldUnitTestWhole {
    private val numberTransformation = NumberTransformation(
        thousandsSeparator = '.', decimalSeparator = ','
    )
    private val numberOriginals = arrayOf(1234567890, 123, 12345, 1234567, 12345678)
    private val textOriginals = numberOriginals.map { numberOriginal ->
        numberOriginal.toString()
    }.toTypedArray()
    private val textTransforms = textOriginals.map { stringOriginal ->
        numberTransformation.filter(AnnotatedString(text = stringOriginal)).text.text
    }.toTypedArray()
    private val offsetTransformers = numberOriginals.map { numberOriginal ->
        NumberTransformation.Offset(numberOriginal.toString().length)
    }

    @Test
    fun calculateTotalThousandSeparatorCount() {
        val calculatedThousandSeparatorCounts = offsetTransformers.map { offsetTransformer ->
            offsetTransformer.calculateTotalThousandSeparatorCount()
        }.toTypedArray()
        val expectedCalculatedThousandSeparatorCount = arrayOf(3, 0, 1, 2, 2)
        assertArrayEquals(
            expectedCalculatedThousandSeparatorCount, calculatedThousandSeparatorCounts
        )
    }

    @Test
    fun calculateRightTotalThousandSeparatorCount() {
        textOriginals.indices.map {
            object {
                val textOriginal = textOriginals[it]
                val rightTotalCountExpected = arrayOf(
                    arrayOf(3, 3, 2, 2, 2, 1, 1, 1, 0, 0, 0),
                    arrayOf(0, 0, 0, 0),
                    arrayOf(1, 1, 1, 0, 0, 0),
                    arrayOf(2, 2, 1, 1, 1, 0, 0, 0),
                    arrayOf(2,2,2,1,1,1,0,0,0)
                )[it]
                val offsetTransformed = offsetTransformers[it]
            }
        }.forEach {
            checkOneRightTotalThousandSeparatorCount(
                it.textOriginal, it.rightTotalCountExpected, it.offsetTransformed
            )
        }
    }

    private fun checkOneRightTotalThousandSeparatorCount(
        textOriginal: String,
        rightTotalCountExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val rightTotalCalculated = (0..textOriginal.length).map {
            offsetTransformed.calculateRightTotalThousandSeparatorCount(it)
        }.toTypedArray()
        assertArrayEquals(rightTotalCountExpected, rightTotalCalculated)
    }

    @Test
    fun calculateLeftTotalThousandSeparatorCount() {
        textOriginals.indices.map {
            object {
                val textOriginal = textOriginals[it]
                val rightTotalCountExpected = arrayOf(
                    arrayOf(0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3),
                    arrayOf(0, 0, 0, 0),
                    arrayOf(0, 0, 0, 1, 1, 1),
                    arrayOf(0, 0, 1, 1, 1, 2, 2, 2),
                    arrayOf(0,0,0,1,1,1,2,2,2)
                )[it]
                val offsetTransformed = offsetTransformers[it]
            }
        }.forEach {
            checkOneLeftTotalThousandSeparatorCount(
                it.textOriginal, it.rightTotalCountExpected, it.offsetTransformed
            )
        }
    }

    private fun checkOneLeftTotalThousandSeparatorCount(
        textOriginal: String,
        leftTotalCountExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val leftTotalCalculated = (0..textOriginal.length).map {
            offsetTransformed.calculateLeftTotalThousandSeparatorCount(it)
        }.toTypedArray()
        assertArrayEquals(leftTotalCountExpected, leftTotalCalculated)
    }

    @Test
    fun originalToTransformed() {
        (textOriginals.indices).map {
            object {
                val textOriginal: String = textOriginals[it]
                val textTransformed: String = textTransforms[it]
                val transformedOffsetExpected = arrayOf(
                    arrayOf(0, 1, 3, 4, 5, 7, 8, 9, 11, 12, 13),
                    arrayOf(0, 1, 2, 3),
                    arrayOf(0, 1, 2, 4, 5, 6),
                    arrayOf(0, 1, 3, 4, 5, 7, 8, 9),
                    arrayOf(0, 1, 2, 4, 5, 6, 8, 9, 10)
                )[it]
                val offsetTransformed: NumberTransformation.Offset = offsetTransformers[it]
            }
        }.forEach {
            checkOneOriginalToTransformed(
                it.textOriginal,
                it.textTransformed,
                it.transformedOffsetExpected,
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
    fun calculateRightTotalThousandSeparatorCountTransformed() {
        (textOriginals.indices).map {
            object {
                val textTransformed: String = textTransforms[it]
                val rightTotalCountExpected = arrayOf(
                    arrayOf(3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0, 0), //1.234.567.890
                    arrayOf(0, 0, 0, 0), //123
                    arrayOf(1, 1, 1, 0, 0, 0, 0), //12.345
                    arrayOf(2, 2, 1, 1, 1, 1, 0, 0, 0, 0), // 1.234.567
                    arrayOf(2,2,2,1,1,1,1,0,0,0,0)
                )[it]
                val offsetTransformed: NumberTransformation.Offset = offsetTransformers[it]
            }
        }.forEach {
            checkOneCalculateRightTotalThousandSeparatorCountTransformed(
                it.textTransformed, it.rightTotalCountExpected, it.offsetTransformed
            )
        }
    }

    private fun checkOneCalculateRightTotalThousandSeparatorCountTransformed(
        textTransformed: String,
        rightTotalCountExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val rightTotalCalculated = (0..textTransformed.length).map {
            offsetTransformed.calculateRightTotalThousandSeparatorCountTransformed(it)
        }.toTypedArray()
        assertArrayEquals(rightTotalCountExpected, rightTotalCalculated)
    }

    @Test
    fun calculateLeftTotalThousandSeparatorCountTransformed() {
        textOriginals.indices.map {
            object {
                val textTransformed = textTransforms[it]
                val leftTotalCountExpected = arrayOf(
                    arrayOf(0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3),
                    arrayOf(0, 0, 0, 0),
                    arrayOf(0, 0, 0, 1, 1, 1, 1),
                    arrayOf(0, 0, 1, 1, 1, 1, 2, 2, 2, 2),
                    arrayOf(0,0,0,1,1,1,1,2,2,2,2)
                )[it]
                val offsetTransformed = offsetTransformers[it]
            }
        }.forEach {
            checkOneLeftTotalThousandSeparatorCountTransformed(
                it.textTransformed, it.leftTotalCountExpected, it.offsetTransformed
            )
        }
    }

    private fun checkOneLeftTotalThousandSeparatorCountTransformed(
        textTransformed: String,
        leftTotalCountExpected: Array<Int>,
        offsetTransformed: NumberTransformation.Offset
    ) {
        val leftTotalCalculated = (0..textTransformed.length).map {
            offsetTransformed.calculateLeftTotalThousandSeparatorCountTransformed(it)
        }.toTypedArray()
        assertArrayEquals(leftTotalCountExpected, leftTotalCalculated)
    }

    @Test
    fun transformedToOriginal() {
        (textOriginals.indices).map {
            object {
                val textOriginal: String = textOriginals[it]
                val textTransformed: String = textTransforms[it]
                val originalOffsetExpected = arrayOf(
                    arrayOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7, 7, 8, 9, 10), //1.234.567.890
                    arrayOf(0, 1, 2, 3), //123
                    arrayOf(0, 1, 2, 2, 3, 4, 5), //12.345
                    arrayOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7), // 1.234.567
                    arrayOf(0,1,2,2,3,4,5,5,6,7,8)
                )[it]
                val offsetTransformed: NumberTransformation.Offset = offsetTransformers[it]
            }
        }.forEach {
            checkOneTransformedToOriginal(
                it.textOriginal, it.textTransformed, it.originalOffsetExpected, it.offsetTransformed
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
                    text != '.'
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
        indices.drop(1).map { text[it - 1] }.joinToString("")
}