package com.example.gazege.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.example.gazege.R
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
    dividedItems(items) {
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
    itemSpacing: Dp = dimensionResource(id = R.dimen.DefaultPadding),
    nodeViewHolder: @Composable (node: C, scope: TreeScope<N, C>) -> Unit
) = RecyclerTreeView(
    nodes = nodes,
    treeState = state,
    itemHolderPaddingValues = contentPadding,
    itemSpacing = itemSpacing
) { node, scope ->
    nodeViewHolder(node, scope)
}

fun <T> LazyListScope.dividedItems(
    items: List<T>,
    itemViewHolder: @Composable (T) -> Unit
) {
    val count = items.size * 2
    items(count = count) {
        if (it.mod(2) == 0) {
            itemViewHolder(items[it / 2])
        } else if (it < count - 1) {
            Divider()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <T> LazyListScope.itemsGrouped(
    elements: List<T>,
    groupSelector: (T) -> String,
    groupViewHolder: @Composable (String) -> Unit,
    viewHolder: @Composable (T) -> Unit
) {
    val groupedItems = elements.groupBy { groupSelector(it) }
    groupedItems.forEach { (group, indexItems) ->
        stickyHeader {
            Column(Modifier.fillMaxWidth()) {
                groupViewHolder(group)
                Divider(thickness = Dp.Hairline)
            }
        }
        dividedItems(items = indexItems) { item -> viewHolder(item) }
    }
}