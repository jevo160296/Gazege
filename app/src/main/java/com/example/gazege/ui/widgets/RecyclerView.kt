package com.example.gazege.ui.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> RecyclerView(
    modifier: Modifier = Modifier,
    elements: List<T>,
    viewHolder: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(elements) { item ->
            viewHolder(item)
        }
    }
}