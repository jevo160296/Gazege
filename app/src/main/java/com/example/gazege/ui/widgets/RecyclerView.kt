package com.example.gazege.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun <T> RecyclerView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical,
    elements: List<T>,
    onItemTapped: (T) -> Unit = {},
    onItemLongPressed: (T) -> Unit = {},
    state: LazyListState,
    colorSelector: @Composable (T) -> CardColors = { CardDefaults.cardColors() },
    groupSelector: ((T) -> String)? = null,
    viewHolder: @Composable (T) -> Unit,
    content: LazyListScope.(elements: List<T>) -> Unit = {
        itemsGrouped(
            it,
            groupSelector
        ) { item ->
            ClickableCardViewHolderGenerator(
                item,
                onItemTapped,
                onItemLongPressed,
                colorSelector,
                viewHolder
            )
        }
    }
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        content(elements)
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyListScope.itemsGrouped(
    elements: List<T>,
    groupSelector: ((T) -> String)?,
    viewHolder: @Composable (T) -> Unit
) {
    val groupedItems = elements.groupBy { groupSelector?.invoke(it) }
    groupedItems.forEach { (group, indexItems) ->
        if (group != null) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
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
        itemsIndexed(indexItems) { _, item ->
            viewHolder(item)
        }
    }
}

@Composable
fun <T> ClickableCardViewHolderGenerator(
    item: T,
    onItemTapped: (item: T) -> Unit,
    onItemLongPressed: (T) -> Unit,
    colorSelector: @Composable (T) -> CardColors,
    viewHolder: @Composable (T) -> Unit
) = Card(
    modifier = Modifier.fillMaxWidth(),
    onClick = { onItemTapped(item) },
    onLongClick = { onItemLongPressed(item) },
    colors = colorSelector(item)
)
{
    Box(modifier = Modifier.padding(4.dp)) {
        viewHolder(item)
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
                contentPadding = PaddingValues(10.dp),
                state = LazyListState(),
                viewHolder = {
                    Text(text = it, modifier = Modifier.height(42.dp))
                },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
        }
    }
}