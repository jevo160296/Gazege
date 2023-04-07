@file:Suppress("UNUSED_PARAMETER")

package com.example.gazege.ui.widgets

import android.icu.text.DecimalFormat
import android.os.Parcelable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.ui.theme.GazegeTheme
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.filledShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors()
) {
    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(
    value: SignedBigDecimal,
    onValueChange: (SignedBigDecimal) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.filledShape,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors()
) {
    var formatType by rememberSaveable {
        mutableStateOf(NumberTransformation.FormatType.Int)
    }
    val numberTransformation = NumberTransformation()
    val stringRepresentation = numberTransformation.signedBigDecimalToString(value, formatType)
    TextField(
        value = stringRepresentation,
        onValueChange = {
            formatType = if (it.contains(numberTransformation.decimalSeparator)) {
                NumberTransformation.FormatType.Double
            } else {
                NumberTransformation.FormatType.Int
            }
            var coercedRepresentation =
                if (stringRepresentation == "0" && it.contains('-')) {
                    "-0"
                } else if (stringRepresentation == "0" && it.endsWith("0")) {
                    it.take(1)
                } else if (stringRepresentation == "0.00" && it.endsWith("0.00")) {
                    "${it.take(1)}.00"
                } else {
                    it
                }
            coercedRepresentation = when (formatType) {
                NumberTransformation.FormatType.Int -> coercedRepresentation
                NumberTransformation.FormatType.Double -> coercedRepresentation.split(
                    numberTransformation.decimalSeparator
                ).mapIndexed { index, s ->
                    if (index == 1) {
                        s.take(2)
                    } else {
                        s
                    }
                }.joinToString(numberTransformation.decimalSeparator.toString())

            }
            val doubleRepresentation =
                numberTransformation.stringToBigDecimal(coercedRepresentation)
            if (doubleRepresentation != null) {
                onValueChange(doubleRepresentation)
            }
        },
        modifier = modifier,
        label = label,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions.copy(
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = keyboardActions,
        visualTransformation = numberTransformation,
        trailingIcon = trailingIcon
    )
}

class NumberTransformation(
    val thousandsSeparator: Char = DecimalFormat().decimalFormatSymbols.groupingSeparator,
    val decimalSeparator: Char = DecimalFormat().decimalFormatSymbols.decimalSeparator
) : VisualTransformation {
    enum class FormatType {
        Double, Int
    }

    class Offset(
        originalNumberLength: Int, val decimalPointPosition: Int? = null, startsWithMinus: Boolean
    ) : OffsetMapping {
        private val wholePartOriginalIntegerLength: Int =
            originalNumberLength - if (decimalPointPosition != null) {
                3
            } else {
                0
            } -
                    if (startsWithMinus) {
                        1
                    } else {
                        0
                    }

        private val transformedIntegerLength =
            wholePartOriginalIntegerLength + calculateTotalThousandSeparatorCount() +
                    if (startsWithMinus) {
                        1
                    } else {
                        0
                    }

        override fun originalToTransformed(offset: Int): Int =
            offset + calculateLeftTotalThousandSeparatorCount(offset)

        override fun transformedToOriginal(offset: Int): Int =
            offset - calculateLeftTotalThousandSeparatorCountTransformed(offset)

        fun calculateTotalThousandSeparatorCount(): Int = (wholePartOriginalIntegerLength - 1) / 3

        fun calculateRightTotalThousandSeparatorCount(offset: Int) = if (offset == 0) {
            calculateTotalThousandSeparatorCount()
        } else if (decimalPointPosition != null && offset >= decimalPointPosition) {
            0
        } else {
            (wholePartOriginalIntegerLength - offset) / 3
        }

        fun calculateLeftTotalThousandSeparatorCount(offset: Int) =
            calculateTotalThousandSeparatorCount() - calculateRightTotalThousandSeparatorCount(
                offset
            )

        fun calculateRightTotalThousandSeparatorCountTransformed(offset: Int): Int =
            (transformedIntegerLength - offset) / 4

        fun calculateLeftTotalThousandSeparatorCountTransformed(offset: Int): Int =
            calculateTotalThousandSeparatorCount() - calculateRightTotalThousandSeparatorCountTransformed(
                offset
            )
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val thousandsReplacementPattern = Regex("\\B(?=(?:\\d{3})+(?!\\d))")
        val textFormatted = "".plus(
            text.replace(Regex("\\."), decimalSeparator.toString())
                .replace(thousandsReplacementPattern, thousandsSeparator.toString())
        )
        val formatType = if (text.contains(decimalSeparator)) {
            FormatType.Double
        } else {
            FormatType.Int
        }
        val decimalPointPosition = when (formatType) {
            FormatType.Double -> text.indexOf(".") + 1
            FormatType.Int -> null
        }
        val offsetMapping = Offset(text.length, decimalPointPosition, text.startsWith('-'))
        return TransformedText(
            text = AnnotatedString(text = textFormatted), offsetMapping = offsetMapping
        )
    }

    fun stringToBigDecimal(text: String, max: Long = 100000000000): SignedBigDecimal? {
        return if (text == "") {
            SignedBigDecimal.ZERO
        } else if (text in listOf("-0", "-", "0-")) {
            SignedBigDecimal.NEGATIVE_ZERO
        } else {
            val transformedNumber = text.toBigDecimalOrNull()
            val transformedBigDecimal = transformedNumber?.toSignedBigDecimal()
            if (
                transformedBigDecimal != null &&
                (transformedBigDecimal > BigDecimal(max) || transformedBigDecimal < BigDecimal(-max))
            ) {
                null
            } else {
                transformedBigDecimal
            }
        }
    }

    private fun bigDecimalToString(
        number: BigDecimal?,
        formatType: FormatType = FormatType.Int
    ): String {
        return if (number == null) {
            ""
        } else {
            val coercedNumber: BigDecimal = number.setScale(2, RoundingMode.HALF_EVEN)
            val isNegative = coercedNumber < BigDecimal.ZERO
            val negativeChar = if (isNegative) {
                "-"
            } else {
                ""
            }
            val wholePart = coercedNumber
                .setScale(0, RoundingMode.DOWN)
                .abs()
            val decimalPart =
                (coercedNumber.abs() - wholePart)
                    .times(BigDecimal(100))
                    .setScale(0, RoundingMode.FLOOR)
                    .abs()
            if (decimalPart == BigDecimal(0) && formatType == FormatType.Int) {
                "$negativeChar$wholePart"
            } else {
                "$negativeChar$wholePart$decimalSeparator${"%02.0f".format(decimalPart)}"
            }
        }
    }

    fun signedBigDecimalToString(
        number: SignedBigDecimal,
        formatType: FormatType = FormatType.Int
    ): String = if (number is NegativeZeroBigDecimal) {
        "-0"
    } else {
        bigDecimalToString(number.value, formatType)
    }
}

fun Double.toSignedBigDecimal() = SignedBigDecimal.from(this)
fun BigDecimal.toSignedBigDecimal() = SignedBigDecimal.from(this)

@Parcelize
open class SignedBigDecimal protected constructor(val value: BigDecimal) : Parcelable {
    fun toDouble() = value.toDouble()
    operator fun compareTo(other: BigDecimal): Int {
        return this.value.compareTo(other)
    }

    operator fun compareTo(other: SignedBigDecimal): Int {
        return this.value.compareTo(other.value)
    }

    companion object {
        fun from(value: Double) = SignedBigDecimal(value.toBigDecimal())
        fun from(value: BigDecimal) = SignedBigDecimal(value)

        val NEGATIVE_ZERO: NegativeZeroBigDecimal get() = NegativeZeroBigDecimal()
        val ZERO: SignedBigDecimal get() = SignedBigDecimal(BigDecimal.ZERO)
    }
}

class NegativeZeroBigDecimal : SignedBigDecimal(BigDecimal.ZERO)

@Preview(showBackground = true, heightDp = 620, widthDp = 420)
@Composable
private fun TextFieldPreview() {
    GazegeTheme {
        var number by remember {
            mutableStateOf(BigDecimal.ZERO.toSignedBigDecimal())
        }
        var valor by remember {
            mutableStateOf("Valor1")
        }
        Column {
            TextField(value = valor, onValueChange = {
                valor = it
            })
            NumberField(value = number, onValueChange = {
                number = it
            })
        }
    }
}