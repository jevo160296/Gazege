@file:Suppress("UNUSED_PARAMETER")

package com.example.gazege.ui.widgets

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToLong

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
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    OutlinedTextField(
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

@Composable
fun NumberField(
    value: Double,
    onValueChange: (Double) -> Unit,
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
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    val formatType by rememberSaveable {
        mutableStateOf(NumberTransformation.FormatType.Int)
    }
    val numberTransformation = remember { NumberTransformation() }
    var isEditing by remember { mutableStateOf(false) }
    var stringRepresentation by remember {
        mutableStateOf(numberTransformation.doubleToString(value, formatType))
    }
    val stringState = remember(value, stringRepresentation) {
        val valueRepresentation = numberTransformation.stringToDoubleOrNull(stringRepresentation)
        if (valueRepresentation != value) {
            numberTransformation.doubleToString(value, formatType)
        } else {
            stringRepresentation
        }
    }
    TextField(
        value = stringState,
        onValueChange = {
            val newValue = numberTransformation.stringToDoubleOrNull(it)
            if (newValue != null) {
                onValueChange(newValue)
                stringRepresentation = it
                    .replace(Regex("(\\.\\d{0,2})\\d*"), "$1")
            }
        },
        modifier = modifier.onFocusChanged {
            if (!it.hasFocus) {
                val newValue = numberTransformation.stringToDoubleOrNull(stringRepresentation)
                newValue?.let {
                    onValueChange(newValue)
                    stringRepresentation = numberTransformation.doubleToString(value, formatType)
                }
                isEditing = false
            } else {
                if (stringState in setOf("0", "0.0", "-0", "-0.0")) {
                    stringRepresentation = ""
                }
                isEditing = true
            }
        },
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
    val thousandsSeparator: Char = ',',
    private val decimalSeparator: Char = '.'
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val transformation = reformat(text.text)

        return TransformedText(
            AnnotatedString(transformation.formatted ?: ""),
            Offset(transformation),
        )
    }

    class Offset(
        private val transformation: Transformation
    ) : OffsetMapping {
        override fun originalToTransformed(offset: Int) =
            transformation.originalToTransformed[offset]

        override fun transformedToOriginal(offset: Int) =
            transformation.transformedToOriginal[offset]
    }

    enum class FormatType {
        Double, Int
    }

    fun reformat(original: String): Transformation {
        val parts = original.split(decimalSeparator)
        check(parts.size < 3) { "original text must have only one dot (use filteredDecimalText)" }

        val thousandsReplacementPattern = Regex("\\B(?=(?:\\d{3})+(?!\\d))")
        val formatted = "".plus(
            original.replace(Regex("\\."), decimalSeparator.toString())
                .replace(thousandsReplacementPattern, thousandsSeparator.toString())
        )

        val originalToTransformed = mutableListOf<Int>()
        val transformedToOriginal = mutableListOf<Int>()
        var specialCharsCount = 0

        formatted.forEachIndexed { index, char ->
            transformedToOriginal.add(index - specialCharsCount)
            if (thousandsSeparator == char) {
                specialCharsCount++
            } else {
                originalToTransformed.add(index + 1)
            }
        }
        originalToTransformed.add(0, 0)
        transformedToOriginal.add(transformedToOriginal.maxOrNull()?.plus(1) ?: 0)

        return Transformation(formatted, originalToTransformed, transformedToOriginal)
    }

    fun stringToDoubleOrNull(text: String, max: Long = 100000000000): Double? {
        return if (text in setOf("", "-0", "-", "0-", ".0", "-.", "-0.", ".")) {
            0.0
        } else if (!text.all { it.isDigit() || it in setOf('.', '-') }) {
            null
        } else {
            val transformedNumber = text.toDoubleOrNull()
            if (
                transformedNumber != null &&
                (transformedNumber > max.toDouble() || transformedNumber < -max.toDouble())
            ) {
                null
            } else {
                transformedNumber
            }
        }
    }

    fun doubleToString(
        number: Double?,
        formatType: FormatType = FormatType.Int
    ): String {
        return if (number == null) {
            ""
        } else {
            val coercedNumber: Double = number.times(100.0).roundToLong().div(100.0)
            val isNegative = coercedNumber < 0.0
            val negativeChar = if (isNegative) {
                "-"
            } else {
                ""
            }
            val wholePart = floor(coercedNumber.absoluteValue).toLong()
            val decimalPart =
                (coercedNumber.absoluteValue - wholePart)
                    .times(100.0)
                    .roundToLong()
                    .toDouble()
            if (decimalPart == 0.0 && formatType == FormatType.Int) {
                "$negativeChar$wholePart"
            } else {
                "$negativeChar$wholePart$decimalSeparator${"%02.0f".format(decimalPart)}"
            }
        }
    }
}


data class Transformation(
    val formatted: String?,
    val originalToTransformed: List<Int>,
    val transformedToOriginal: List<Int>,
)

@Preview(showBackground = true, heightDp = 620, widthDp = 420)
@Composable
private fun TextFieldPreview() {
    GazegeTheme {
        var number by remember {
            mutableStateOf(0.0)
        }
        var valor by remember {
            mutableStateOf("Valor1")
        }
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(value = valor, onValueChange = {
                valor = it
            })
            NumberField(value = number, onValueChange = {
                number = it
            })
        }
    }
}