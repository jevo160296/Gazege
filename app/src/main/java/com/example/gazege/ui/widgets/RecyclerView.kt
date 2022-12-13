package com.example.gazege.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun <T> RecyclerView(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    elements: List<T>,
    onItemTapped: (T) -> Unit = {},
    state: LazyListState,
    viewHolder: @Composable (T) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        val calculatedStart = itemHolderPaddingValues.calculateStartPadding(layoutDirection)
        val calculatedEnd = itemHolderPaddingValues.calculateEndPadding(layoutDirection)
        itemsIndexed(elements) { index, item ->
            val paddingValues: PaddingValues =
                if (index == 0) {
                    PaddingValues(
                        top = calculatedTop,
                        start = calculatedStart,
                        end = calculatedEnd
                    )
                } else if (index < elements.lastIndex) {
                    PaddingValues(
                        top = 4.dp,
                        start = calculatedStart,
                        end = calculatedEnd
                    )
                } else {
                    PaddingValues(
                        top = 4.dp,
                        bottom = calculatedBottom,
                        start = calculatedStart,
                        end = calculatedEnd
                    )
                }
            Card(
                modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .clickable {
                        onItemTapped(item)
                    }
            )
            {
                Box(modifier = Modifier.padding(4.dp)) {
                    viewHolder(item)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 240)
@Composable
private fun RecyclerViewPreview() {
    GazegeTheme {
        val elements = (0..30).map {
            "Element $it"
        }
        Column {
            RecyclerView(
                elements = elements,
                onItemTapped = {
                    println(it)
                },
                itemHolderPaddingValues = PaddingValues(10.dp),
                state = LazyListState()
            ) {
                Text(text = it, modifier = Modifier.height(42.dp))
            }
        }
    }
}