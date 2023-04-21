package com.example.gazege.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    colors: CardColors = CardDefaults.cardColors(),
    enabled: Boolean = true,
    content: @Composable (ColumnScope.() -> Unit)
) {
    var cardModifier = modifier.clip(CardDefaults.shape)
    if (enabled) {
        cardModifier = cardModifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    }
    androidx.compose.material3.Card(
        modifier = cardModifier,
        colors = colors,
        content = content
    )
}

@Preview(widthDp = 100, heightDp = 50)
@Composable
fun CardPreview() {
    val hostState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    Column {
        Card(
            Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(42.dp),
            onClick = {
                scope.launch {
                    hostState.showSnackbar("On click")
                }
            },
            onLongClick = {
                scope.launch {
                    hostState.showSnackbar("On long click")
                }
            }
        ) {
            Text("Card")
        }
        SnackbarHost(hostState = hostState)
    }
}