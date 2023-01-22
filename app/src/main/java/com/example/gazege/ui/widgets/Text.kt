package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.ui.theme.Typography

@Composable
fun SmallEmphasis(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, modifier = modifier)
}

@Composable
fun SmallBody(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, modifier = modifier)
}

@Composable
fun LargeEmphasis(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.labelLarge)
}

@Composable
fun LargeBody(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier, style = MaterialTheme.typography.bodyLarge)
}

@Composable
fun MediumHeadline(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = Typography.headlineMedium)
}

@Preview(showBackground = true)
@Composable
private fun Preview(){
    Column {
        MediumHeadline(text = "Medium Headline")
        LargeEmphasis(text = "Large emphasis")
        LargeBody(text = "Large body")
        SmallEmphasis(text = "Small emphasis")
        SmallBody(text = "Small body")
    }
}