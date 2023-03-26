package com.example.gazege.ui.widgets

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

interface Node<N, C> {
    val content: N
    val children: List<C>
}

@Composable
fun <N, C : Node<N, C>> RecyclerTreeView(
    nodes: List<C>,
    viewHolder: @Composable (C, RecyclerTreeScope<N, C>) -> Unit
) {
    val expandedItems = remember { mutableStateListOf<C>() }
    val recyclerTreeScope = remember {
        mutableStateOf(
            RecyclerTreeScope(
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
            recyclerTreeScope = recyclerTreeScope.value
        )
    }
}

fun <N, C : Node<N, C>> LazyListScope.nodes(
    nodes: List<C>,
    recyclerTreeScope: RecyclerTreeScope<N, C>
) {
    nodes.forEach { node ->
        node(
            node,
            treeScope = recyclerTreeScope
        )
    }
}

fun <N, C : Node<N, C>> LazyListScope.node(
    node: C,
    treeScope: RecyclerTreeScope<N, C>
) {
    item {
        treeScope.viewHolder(node, treeScope)
    }
    if (treeScope.isExpanded(node)) {
        nodes(
            node.children,
            recyclerTreeScope = treeScope
        )
    }
}

data class RecyclerTreeScope<N, C : Node<N, C>>(
    val viewHolder: @Composable (C, RecyclerTreeScope<N, C>) -> Unit,
    val isExpanded: (C) -> Boolean,
    val toggleExpanded: (C) -> Unit
)