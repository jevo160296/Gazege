package com.example.gazege.ui.widgets.treeview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun <N, C : Node<N, C>> ColumnTreeView(
    nodes: List<C>,
    groupSelector: (C) -> String? = { null },
    groupViewHolder: @Composable (String) -> Unit = { Text(it) },
    treeState: TreeState = rememberTreeState(),
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    viewHolder: @Composable (node: C, treeSope: TreeScope<N, C>) -> Unit
) {
    val expandedItems = treeState.expandedItems
    val layoutDirection = LocalLayoutDirection.current
    val treeScope = remember {
        mutableStateOf(
            TreeScope(
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
        )
    }
    Column {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        Spacer(modifier = Modifier.height(calculatedTop))
        Nodes(
            nodes,
            treeScope = treeScope.value
        )
        Spacer(Modifier.height(calculatedBottom))
    }
}

@Composable
fun <N, C : Node<N, C>> ColumnScope.Nodes(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <N, C : Node<N, C>> ColumnScope.Node(
    node: C,
    previousGroup: String?,
    currentGroup: String?,
    treeScope: TreeScope<N, C>
) {
    val itemHolderPaddingValues = treeScope.itemHolderPaddingValues
    val layoutDirection = treeScope.layoutDirection
    if (currentGroup != null && previousGroup != currentGroup) {
        treeScope.groupViewHolder(currentGroup)
    }
    Row {
        Spacer(Modifier.width(itemHolderPaddingValues.calculateStartPadding(layoutDirection)))
        Box(Modifier.weight(1f)) {
            treeScope.viewHolder(node, treeScope)
        }
        Spacer(Modifier.width(itemHolderPaddingValues.calculateEndPadding(layoutDirection)))
    }
    if (treeScope.isExpanded(node)) {
        Nodes(
            node.children,
            parentGroup = currentGroup,
            treeScope = treeScope
        )
    }
}