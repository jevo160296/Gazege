package com.example.gazege.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

interface Node<T> {
    val content: String
    val children: List<T>
}

@Composable
fun <T : Node<T>> RecyclerTreeView(nodes: List<T>) {
    val expandedItems = remember { mutableStateListOf<T>() }
    LazyColumn {
        nodes(
            nodes,
            isExpanded = {
                expandedItems.contains(it)
            },
            toggleExpanded = {
                if (expandedItems.contains(it)) {
                    expandedItems.remove(it)
                } else {
                    expandedItems.add(it)
                }
            },
        )
    }
}

fun <T : Node<T>> LazyListScope.nodes(
    nodes: List<T>,
    isExpanded: (T) -> Boolean,
    toggleExpanded: (T) -> Unit,
) {
    nodes.forEach { node ->
        node(
            node,
            isExpanded = isExpanded,
            toggleExpanded = toggleExpanded,
        )
    }
}

fun <T : Node<T>> LazyListScope.node(
    node: T,
    isExpanded: (T) -> Boolean,
    toggleExpanded: (T) -> Unit,
) {
    item {
        Text(
            node.content,
            Modifier.clickable {
                toggleExpanded(node)
            }
        )
    }
    if (isExpanded(node)) {
        nodes(
            node.children,
            isExpanded = isExpanded,
            toggleExpanded = toggleExpanded,
        )
    }
}