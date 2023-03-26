package com.example.gazege.ui.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

interface Node<N, C> {
    val content: N
    val level: Int
    val children: List<C>
}

@Composable
fun <N, C : Node<N, C>> RecyclerTreeView(
    nodes: List<C>,
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
                }
            ))
    }
    LazyColumn {
        nodes(
            nodes,
            treeScope = treeScope.value
        )
    }
}

fun <N, C : Node<N, C>> LazyListScope.nodes(
    nodes: List<C>,
    treeScope: TreeScope<N, C>
) {
    nodes.forEach { node ->
        node(
            node,
            treeScope = treeScope
        )
    }
}

fun <N, C : Node<N, C>> LazyListScope.node(
    node: C,
    treeScope: TreeScope<N, C>
) {
    item {
        treeScope.viewHolder(node, treeScope)
    }
    if (treeScope.isExpanded(node)) {
        nodes(
            node.children,
            treeScope = treeScope
        )
    }
}

data class TreeScope<N, C : Node<N, C>>(
    val viewHolder: @Composable (C, TreeScope<N, C>) -> Unit,
    val isExpanded: (C) -> Boolean,
    val toggleExpanded: (C) -> Unit
)