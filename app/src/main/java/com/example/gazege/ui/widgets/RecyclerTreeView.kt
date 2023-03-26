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
        mutableStateOf(RecyclerTreeScope(viewHolder) {
            if (expandedItems.contains(it)) {
                expandedItems.remove(it)
            } else {
                expandedItems.add(it)
            }
        })
    }
    LazyColumn {
        nodes(
            nodes,
            isExpanded = {
                expandedItems.contains(it)
            },
            recyclerTreeScope = recyclerTreeScope.value
        )
    }
}

fun <N, C : Node<N, C>> LazyListScope.nodes(
    nodes: List<C>,
    isExpanded: (C) -> Boolean,
    recyclerTreeScope: RecyclerTreeScope<N, C>
) {
    nodes.forEach { node ->
        node(
            node,
            isExpanded = isExpanded,
            recyclerTreeScope = recyclerTreeScope
        )
    }
}

fun <N, C : Node<N, C>> LazyListScope.node(
    node: C,
    isExpanded: (C) -> Boolean,
    recyclerTreeScope: RecyclerTreeScope<N, C>
) {
    item {
        recyclerTreeScope.viewHolder(node, recyclerTreeScope)
    }
    if (isExpanded(node)) {
        nodes(
            node.children,
            isExpanded = isExpanded,
            recyclerTreeScope = recyclerTreeScope
        )
    }
}

data class RecyclerTreeScope<N, C : Node<N, C>>(
    val viewHolder: @Composable (C, RecyclerTreeScope<N, C>) -> Unit,
    val toggleExpanded: (C) -> Unit
)