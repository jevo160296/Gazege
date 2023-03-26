package com.example.gazege.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

interface Node<N, C : Node<N, C>> {
    val content: N
    val level: Int
    val children: List<C>
}

@Composable
fun <N, C : Node<N, C>> RecyclerTreeView(
    nodes: List<C>,
    groupSelector: (C) -> String? = { null },
    groupViewHolder: @Composable (String) -> Unit = { Text(it) },
    state: LazyListState = rememberLazyListState(),
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    viewHolder: @Composable (C, TreeScope<N, C>) -> Unit
) {
    val expandedItems = remember { mutableStateListOf<C>() }
    val layoutDirection = LocalLayoutDirection.current
    val treeScope = remember {
        mutableStateOf(
            TreeScope(
                viewHolder,
                toggleExpanded = {
                    if (expandedItems.contains(it)) {
                        expandedItems.remove(it)
                    } else {
                        expandedItems.add(it)
                    }
                },
                isExpanded = {
                    expandedItems.contains(it)
                },
                groupSelector = groupSelector,
                groupViewHolder = groupViewHolder,
                itemHolderPaddingValues = itemHolderPaddingValues,
                layoutDirection = layoutDirection
            )
        )
    }
    LazyColumn(
        state = state
    ) {
        val calculatedTop = itemHolderPaddingValues.calculateTopPadding()
        val calculatedBottom = itemHolderPaddingValues.calculateBottomPadding()
        item {
            Spacer(modifier = Modifier.height(calculatedTop))
        }
        nodes(
            nodes,
            treeScope = treeScope.value
        )
        item {
            Spacer(Modifier.height(calculatedBottom))
        }
    }
}

fun <N, C : Node<N, C>> LazyListScope.nodes(
    nodes: List<C>,
    parentGroup: String? = null,
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
            treeScope = treeScope
        )
        previousGroup = currentGroup
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <N, C : Node<N, C>> LazyListScope.node(
    node: C,
    previousGroup: String?,
    currentGroup: String?,
    treeScope: TreeScope<N, C>
) {
    val itemHolderPaddingValues = treeScope.itemHolderPaddingValues
    val layoutDirection = treeScope.layoutDirection
    if (currentGroup != null && previousGroup != currentGroup) {
        stickyHeader {
            treeScope.groupViewHolder(currentGroup)
        }
    }
    item {
        Row {
            Spacer(Modifier.width(itemHolderPaddingValues.calculateStartPadding(layoutDirection)))
            Box(Modifier.weight(1f)) {
                treeScope.viewHolder(node, treeScope)
            }
            Spacer(Modifier.width(itemHolderPaddingValues.calculateEndPadding(layoutDirection)))
        }
    }
    if (treeScope.isExpanded(node)) {
        nodes(
            node.children,
            parentGroup = currentGroup,
            treeScope = treeScope
        )
    }
}

data class TreeScope<N, C : Node<N, C>>(
    val viewHolder: @Composable (C, TreeScope<N, C>) -> Unit,
    val groupViewHolder: @Composable (String) -> Unit,
    val groupSelector: (C) -> String?,
    val itemHolderPaddingValues: PaddingValues,
    val layoutDirection: LayoutDirection,
    val isExpanded: (C) -> Boolean,
    val toggleExpanded: (C) -> Unit
)