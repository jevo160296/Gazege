package com.example.gazege.ui.widgets.treeview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

@Composable
fun <N, C : Node<N, C>> RecyclerTreeView(
    modifier: Modifier = Modifier,
    nodes: List<C>,
    groupSelector: (C) -> String? = { null },
    groupViewHolder: @Composable (String) -> Unit = { Text(it) },
    treeState: TreeState = rememberTreeState(),
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    itemSpacing: Dp,
    viewHolder: @Composable (node: C, scope: TreeScope<N, C>) -> Unit
) {
    val expandedItems = treeState.expandedItems
    val layoutDirection = LocalLayoutDirection.current
    val startPadding = itemHolderPaddingValues.calculateStartPadding(layoutDirection)
    val endPadding = itemHolderPaddingValues.calculateEndPadding(layoutDirection)
    val treeScope = TreeScope(
        viewHolder,
        toggleExpanded = {
            if (it.expanded(expandedItems)) {
                expandedItems.remove(NodeId.from(it))
            } else {
                expandedItems.add(NodeId.from(it))
            }
        },
        isExpanded = {
            it.expanded(expandedItems)
        },
        groupSelector = groupSelector,
        groupViewHolder = groupViewHolder
    )
    LazyColumn(
        modifier = modifier,
        state = treeState.listState,
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        item {
            Spacer(modifier = Modifier.height(calculatedTop))
        }
        nodes(
            nodes,
            startPadding = startPadding,
            endPadding = endPadding,
            treeScope = treeScope
        )
        item {
            Spacer(Modifier.height(calculatedBottom))
        }
    }
}

private fun <N, C : Node<N, C>> LazyListScope.nodes(
    nodes: List<C>,
    parentGroup: String? = null,
    startPadding: Dp,
    endPadding: Dp,
    treeScope: TreeScope<N, C>
) {
    var previousGroup: String? = parentGroup
    var currentGroup: String?
    nodes.forEach { node ->
        currentGroup = treeScope.groupSelector(node)
        node(
            node,
            previousGroup = previousGroup,
            currentGroup = currentGroup,
            startPadding = startPadding,
            endPadding = endPadding,
            treeScope = treeScope
        )
        previousGroup = currentGroup
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun <N, C : Node<N, C>> LazyListScope.node(
    node: C,
    previousGroup: String?,
    currentGroup: String?,
    startPadding: Dp,
    endPadding: Dp,
    treeScope: TreeScope<N, C>
) {
    if (currentGroup != null && previousGroup != currentGroup) {
        stickyHeader {
            DefaultHeader(startPadding = startPadding, endPadding = endPadding) {
                treeScope.groupViewHolder(currentGroup)
            }
        }
    }
    item {
        DefaultItemHolder(startPadding = startPadding, endPadding = endPadding) {
            treeScope.viewHolder(node, treeScope)
        }
    }
    if (treeScope.isExpanded(node)) {
        nodes(
            node.children,
            parentGroup = currentGroup,
            startPadding = startPadding,
            endPadding = endPadding,
            treeScope = treeScope
        )
    }
}