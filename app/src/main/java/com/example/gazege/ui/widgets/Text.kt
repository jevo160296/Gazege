package com.example.gazege.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.ui.theme.Typography

@Composable
fun SmallEmphasis(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) = textFunctionWrapper(MaterialTheme.typography.labelSmall)(
    text,
    modifier,
    textAlign,
    color,
    maxLines
)

@Composable
fun SmallBody(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) = textFunctionWrapper(MaterialTheme.typography.bodySmall)(
    text,
    modifier,
    textAlign,
    color,
    maxLines
)

@Composable
fun LargeEmphasis(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) = textFunctionWrapper(MaterialTheme.typography.labelLarge)(
    text,
    modifier,
    textAlign,
    color,
    maxLines
)

@Composable
fun LargeBody(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) = textFunctionWrapper(MaterialTheme.typography.bodyLarge)(
    text,
    modifier,
    textAlign,
    color,
    maxLines
)

@Composable
fun MediumHeadline(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) = textFunctionWrapper(Typography.headlineMedium)(text, modifier, textAlign, color, maxLines)

private fun textFunctionWrapper(
    style: TextStyle
): @Composable (String, Modifier, TextAlign?, Color, Int) -> Unit {
    val function: @Composable (
        text: String,
        modifier: Modifier,
        textAlign: TextAlign?,
        color: Color,
        maxLines: Int
    ) -> Unit = @Composable { text, modifier, textAlign, color, maxLines ->
        Text(
            text = text,
            modifier = modifier,
            style = style,
            textAlign = textAlign,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
    return function
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Column(Modifier.background(Color.White)) {
        MediumHeadline(text = "Medium Headline")
        LargeEmphasis(text = "Large emphasis")
        LargeBody(text = "Large body")
        SmallEmphasis(text = "Small emphasis")
        SmallBody(text = "Small body")
    }
}