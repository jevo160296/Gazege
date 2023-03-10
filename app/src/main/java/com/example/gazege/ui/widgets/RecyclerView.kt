package com.example.gazege.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> RecyclerView(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    elements: List<T>,
    onItemTapped: (T) -> Unit = {},
    onItemLongPressed: (T) -> Unit = {},
    state: LazyListState,
    colorSelector: @Composable (T) -> CardColors = { CardDefaults.cardColors() },
    groupSelector: ((T) -> String)? = null,
    viewHolder: @Composable (T) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    var lastGroup: String? = null
    var firstGroup: String? = null
    val groupedItems = elements.groupBy {
        val group = groupSelector?.invoke(it)
        lastGroup = group
        firstGroup = firstGroup ?: group
        group
    }
    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        val calculatedStart = itemHolderPaddingValues.calculateStartPadding(layoutDirection)
        val calculatedEnd = itemHolderPaddingValues.calculateEndPadding(layoutDirection)
        groupedItems.forEach { (group, indexItems) ->
            if (group != null) {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(start = calculatedStart, end = calculatedEnd)
                    ) {
                        LargeEmphasis(
                            group,
                            color = MaterialTheme
                                .colorScheme
                                .onBackground
                        )
                    }
                }
            }
            itemsIndexed(indexItems) { index, item ->
                val isFirstElement = index == 0 && group == firstGroup
                val isLastElement = index == indexItems.lastIndex && group == lastGroup
                val paddingValues: PaddingValues =
                    if (isFirstElement) {
                        PaddingValues(
                            top = calculatedTop,
                            start = calculatedStart,
                            end = calculatedEnd
                        )
                    } else if (isLastElement) {
                        PaddingValues(
                            top = 4.dp,
                            bottom = calculatedBottom,
                            start = calculatedStart,
                            end = calculatedEnd
                        )
                    } else {
                        PaddingValues(
                            top = 4.dp,
                            start = calculatedStart,
                            end = calculatedEnd
                        )
                    }
                Card(
                    modifier =
                    Modifier
                        .padding(paddingValues)
                        .fillMaxWidth(),
                    onClick = { onItemTapped(item) },
                    onLongClick = { onItemLongPressed(item) },
                    colors = colorSelector(item)
                )
                {
                    Box(modifier = Modifier.padding(4.dp)) {
                        viewHolder(item)
                    }
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
                    println("Item tapped $it")
                },
                itemHolderPaddingValues = PaddingValues(10.dp),
                state = LazyListState()
            ) {
                Text(text = it, modifier = Modifier.height(42.dp))
            }
        }
    }
}