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
import com.example.gazege.ui.widgets.itemsGrouped
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.RecyclerTreeView
import com.example.gazege.ui.widgets.treeview.TreeScope
import com.example.gazege.ui.widgets.treeview.TreeState

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

@Composable
fun <T> GroupedLazyList(
    modifier: Modifier,
    state: LazyListState,
    contentPadding: PaddingValues,
    items: List<T>,
    groupSelector: (T) -> String,
    groupViewHolder: @Composable (String) -> Unit,
    itemViewHolder: @Composable (T) -> Unit
) = LazyColumn(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
) {
    itemsGrouped(
        elements = items,
        groupSelector = groupSelector,
        groupViewHolder = groupViewHolder,
        viewHolder = itemViewHolder
    )
}

@Composable
fun <N, C : Node<N, C>> SimpleTreeList(
    state: TreeState,
    contentPadding: PaddingValues,
    nodes: List<C>,
    nodeViewHolder: @Composable (node: C, scope: TreeScope<N, C>) -> Unit
) = RecyclerTreeView(
    nodes = nodes,
    treeState = state,
    itemHolderPaddingValues = contentPadding
) { node, scope ->
    nodeViewHolder(node, scope)
}