package com.jmml.gazege.ui.widgets.treeview

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
import androidx.compose.ui.unit.Dp

@Composable
fun <N, C : Node<N, C>> ColumnTreeView(
    nodes: List<C>,
    groupSelector: (C) -> String? = { null },
    groupViewHolder: @Composable (String) -> Unit = { Text(it) },
    treeState: TreeState = rememberTreeState(),
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    nodeVisible: (C) -> Boolean = { true },
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
                treeState.collapseItem(NodeId.from(it))
            } else {
                treeState.expandItem(NodeId.from(it))
            }
        },
        isExpanded = {
            it.expanded(expandedItems)
        },
        groupSelector = groupSelector,
        groupViewHolder = groupViewHolder
    )
    Column {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        Spacer(modifier = Modifier.height(calculatedTop))
        Nodes(
            nodes.filter(nodeVisible),
            startPadding = startPadding,
            endPadding = endPadding,
            treeScope = treeScope,
            nodeVisible = nodeVisible
        )
        Spacer(Modifier.height(calculatedBottom))
    }
}

@Composable
private fun <N, C : Node<N, C>> ColumnScope.Nodes(
    nodes: List<C>,
    nodeVisible: (C) -> Boolean,
    parentGroup: String? = null,
    startPadding: Dp,
    endPadding: Dp,
    treeScope: TreeScope<N, C>
) {
    var previousGroup: String? = parentGroup
    var currentGroup: String?
    nodes.forEach { node ->
        currentGroup = treeScope.groupSelector(node)
        Node(
            node,
            nodeVisible = nodeVisible,
            previousGroup = previousGroup,
            currentGroup = currentGroup,
            startPadding = startPadding,
            endPadding = endPadding,
            treeScope = treeScope
        )
        previousGroup = currentGroup
    }
}

@Composable
private fun <N, C : Node<N, C>> ColumnScope.Node(
    node: C,
    nodeVisible: (C) -> Boolean,
    previousGroup: String?,
    currentGroup: String?,
    startPadding: Dp,
    endPadding: Dp,
    treeScope: TreeScope<N, C>
) {
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
            node.children.filter(nodeVisible),
            parentGroup = currentGroup,
            startPadding = startPadding,
            endPadding = endPadding,
            treeScope = treeScope,
            nodeVisible = nodeVisible
        )
    }
}