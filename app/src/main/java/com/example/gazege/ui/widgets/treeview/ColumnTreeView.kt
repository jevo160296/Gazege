package com.example.gazege.ui.widgets.treeview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun <N, C : Node<N, C>> ColumnTreeView(
    nodes: List<C>,
    groupSelector: (C) -> String? = { null },
    groupViewHolder: @Composable (String) -> Unit = { Text(it) },
    treeState: TreeState = rememberTreeState(),
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    viewHolder: @Composable (node: C, scope: TreeScope<N, C>) -> Unit
) {
    val expandedItems = treeState.expandedItems
    val layoutDirection = LocalLayoutDirection.current
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
        groupViewHolder = groupViewHolder,
        itemHolderPaddingValues = itemHolderPaddingValues,
        layoutDirection = layoutDirection
    )
    Column {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        Spacer(modifier = Modifier.height(calculatedTop))
        Nodes(
            nodes,
            treeScope = treeScope
        )
        Spacer(Modifier.height(calculatedBottom))
    }
}

@Composable
private fun <N, C : Node<N, C>> ColumnScope.Nodes(
    nodes: List<C>,
    parentGroup: String? = null,
    treeScope: TreeScope<N, C>
) {
    var previousGroup: String? = parentGroup
    var currentGroup: String?
    nodes.forEach { node ->
        currentGroup = treeScope.groupSelector(node)
        Node(
            node,
            previousGroup = previousGroup,
            currentGroup = currentGroup,
            treeScope = treeScope
        )
        previousGroup = currentGroup
    }
}

@Composable
private fun <N, C : Node<N, C>> ColumnScope.Node(
    node: C,
    previousGroup: String?,
    currentGroup: String?,
    treeScope: TreeScope<N, C>
) {
    val itemHolderPaddingValues = treeScope.itemHolderPaddingValues
    val layoutDirection = treeScope.layoutDirection
    val startPadding = itemHolderPaddingValues.calculateStartPadding(layoutDirection)
    val endPadding = itemHolderPaddingValues.calculateEndPadding(layoutDirection)
    if (currentGroup != null && previousGroup != currentGroup) {
        DefaultHeader(startPadding = startPadding, endPadding = endPadding) {
            treeScope.groupViewHolder(currentGroup)
        }
    }
    DefaultItemHolder(startPadding = startPadding, endPadding = endPadding) {
        treeScope.viewHolder(node, treeScope)
    }
    if (treeScope.isExpanded(node)) {
        Nodes(
            node.children,
            parentGroup = currentGroup,
            treeScope = treeScope
        )
    }
}