package com.example.gazege.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.ui.theme.Typography

@Composable
fun SmallEmphasis(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
        textAlign = textAlign,
        color = color
    )
}

@Composable
fun SmallBody(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        textAlign = textAlign,
        color = color
    )
}

@Composable
fun LargeEmphasis(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun LargeBody(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = textAlign,
        color = color
    )
}

@Composable
fun MediumHeadline(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified
) {
    Text(
        text,
        modifier = modifier,
        style = Typography.headlineMedium,
        textAlign = textAlign,
        color = color
    )
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