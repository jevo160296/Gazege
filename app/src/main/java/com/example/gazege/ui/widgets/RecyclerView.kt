package com.example.gazege.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun <T> RecyclerView(
    modifier: Modifier = Modifier,
    elements: List<T>,
    onItemTapped: (T) -> Unit = {},
    viewHolder: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(elements) { index, item ->
            Surface(modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onItemTapped(item)
                })
            {
                viewHolder(item)
            }
            if (index < elements.lastIndex) {
                Divider()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 240)
@Composable
private fun RecyclerViewPreview() {
    val elements = (0..3).map {
        "Element $it"
    }
    Column {
        RecyclerView(
            elements = elements,
            onItemTapped = {
                println(it)
            }
        ) {
            Text(text = it, modifier = Modifier.height(42.dp))
        }
    }
}