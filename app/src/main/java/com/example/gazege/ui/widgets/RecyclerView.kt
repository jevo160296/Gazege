package com.example.gazege.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun <T> RecyclerView(
    modifier: Modifier = Modifier,
    elements: List<T>,
    onItemTapped: (T) -> Unit = {},
    viewHolder: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(elements) { index, item ->
            val paddingValues: PaddingValues =
                if (index == 0) {
                    PaddingValues(bottom = 4.dp)
                } else if (index < elements.lastIndex) {
                    PaddingValues(vertical = 4.dp)
                } else {
                    PaddingValues(top = 4.dp)
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
}