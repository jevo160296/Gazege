package com.example.gazege.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyListScope.itemsGrouped(
    elements: List<T>,
    groupSelector: (T) -> String,
    groupViewHolder: @Composable (String) -> Unit,
    viewHolder: @Composable (T) -> Unit
) {
    val groupedItems = elements.groupBy { groupSelector(it) }
    groupedItems.forEach { (group, indexItems) ->
        stickyHeader { groupViewHolder(group) }
        items(items = indexItems) { item -> viewHolder(item) }
    }
}

@Composable
fun DefaultGroupViewHolder(group: String) = Box(
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

@Composable
fun <T> ClickableCardViewHolder(
    item: T,
    onItemTapped: (item: T) -> Unit,
    onItemLongPressed: (T) -> Unit,
    colors: CardColors,
    viewHolder: @Composable (T) -> Unit
) = Card(
    modifier = Modifier.fillMaxWidth(),
    onClick = { onItemTapped(item) },
    onLongClick = { onItemLongPressed(item) },
    colors = colors
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
            Pair(it, "Element $it")
        }
        LazyColumn {
            itemsGrouped(
                elements,
                groupSelector = { it.first.mod(10).toString() },
                groupViewHolder = { DefaultGroupViewHolder(it) }
            ) {
                Text(it.second)
            }
        }
    }
}