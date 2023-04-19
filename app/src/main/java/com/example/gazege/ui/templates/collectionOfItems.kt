package com.example.gazege.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.gazege.R

@Composable
fun <T> SimpleLazyList(
    modifier: Modifier,
    state: LazyListState,
    contentPadding: PaddingValues,
    items: List<T>,
    itemViewHolder: @Composable (T) -> Unit
) = LazyColumn(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
) {
    items(items = items) {
        itemViewHolder(it)
    }
}