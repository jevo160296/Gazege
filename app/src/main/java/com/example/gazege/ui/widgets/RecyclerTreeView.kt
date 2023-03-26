package com.example.gazege.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

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
    viewHolder: @Composable (C, TreeScope<N, C>) -> Unit
) {
    val expandedItems = remember { mutableStateListOf<C>() }
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
                groupViewHolder = groupViewHolder
            )
        )
    }
    LazyColumn(
        state = state
    ) {
        nodes(
            nodes,
            treeScope = treeScope.value
        )
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
    if (currentGroup != null && previousGroup != currentGroup) {
        stickyHeader {
            treeScope.groupViewHolder(currentGroup)
        }
    }
    item {
        treeScope.viewHolder(node, treeScope)
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
    val isExpanded: (C) -> Boolean,
    val toggleExpanded: (C) -> Unit
)