package com.example.gazege.ui.widgets

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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