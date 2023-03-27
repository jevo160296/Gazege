package com.example.gazege.ui.widgets.treeview

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection

interface Node<N, C : Node<N, C>> {
    val content: N
    val relativeIndex: Int
    val level: Int
    val children: List<C>

    fun expanded(expandedItems: List<NodeId>): Boolean {
        return id() in expandedItems
    }

    fun id(): NodeId {
        return NodeId.from(this)
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