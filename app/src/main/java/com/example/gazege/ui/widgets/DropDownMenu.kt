package com.example.gazege.ui.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.ui.widgets.treeview.ColumnTreeView
import com.example.gazege.ui.widgets.treeview.DefaultTreeLeadingIcon
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.NodeId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionsGroupView(
    groupedOptions: Map<String?, List<T>>,
    onItemClick: (T) -> Unit,
    itemToString: (T?) -> String,
    onExpandedChange: (Boolean) -> Unit
) {
    groupedOptions.map {
        val group = it.key
        val values = it.value
        if (group != null) {
            Text(group, modifier = Modifier.padding(4.dp))
        }
        values.map {
            DropdownMenuItem(
                text = { Text(itemToString(it)) },
                onClick = {
                    onExpandedChange(false)
                    onItemClick(it)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownMenu(
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<T>,
    selectedItem: T?,
    itemToString: (T?) -> String,
    onItemClick: (T) -> Unit,
    label: @Composable () -> Unit,
    groupByKeySelector: ((T) -> String)? = null
) {
    val groupedOptions = options.groupBy { groupByKeySelector?.invoke(it) }
    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = itemToString(selectedItem),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
            },
            label = label,
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            OptionsGroupView(
                groupedOptions = groupedOptions,
                onItemClick = onItemClick,
                itemToString = itemToString,
                onExpandedChange = onExpandedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun <N, C : Node<N, C>> DropDownTreeMenu(
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<C>,
    selectedItem: C?,
    itemToString: (C?) -> String,
    label: @Composable () -> Unit,
    viewHolder: @Composable (C) -> Unit,
    canClearSelection: Boolean = false,
    onClearSelectionClicked: () -> Unit = {},
    groupByKeySelector: ((C) -> String)? = null
) {
    val groupedOptions = options.groupBy { groupByKeySelector?.invoke(it) }
    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = onExpandedChange
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = itemToString(selectedItem),
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                val showClearButton = canClearSelection && selectedItem != null
                AnimatedContent(
                    targetState = showClearButton,
                    transitionSpec = {
                        scaleIn() with scaleOut()
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (it) {
                        IconButton(onClick = onClearSelectionClicked) {
                            Icon(
                                painter = painterResource(id = R.drawable.clear_selection),
                                contentDescription = "Clear"
                            )
                        }
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
                    }
                }
            },
            label = label,
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            OptionsGroupTreeView(
                groupedOptions = groupedOptions,
                viewHolder = viewHolder
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <N, C : Node<N, C>> OptionsGroupTreeView(
    groupedOptions: Map<String?, List<C>>,
    viewHolder: @Composable (C) -> Unit
) {
    val nodes: List<C> = groupedOptions.flatMap {
        it.value
    }
    val inverseMap: Map<NodeId, String?> = groupedOptions.flatMap { (key, value) ->
        value.map {
            it.id() to key
        }
    }.toMap()
    val contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
    ColumnTreeView(
        nodes = nodes,
        groupSelector = { inverseMap[it.id()] },
        itemHolderPaddingValues = contentPadding
    ) { node, treeScope ->
        Row {
            Spacer(modifier = Modifier.width(node.level.dp * 8))
            val isExpanded = treeScope.isExpanded(node)
            if (node.children.isNotEmpty()) {
                IconToggleButton(
                    modifier = Modifier.width(32.dp),
                    checked = isExpanded,
                    onCheckedChange = { treeScope.toggleExpanded(node) }
                ) {
                    DefaultTreeLeadingIcon(isExpanded = isExpanded)
                }
            } else {
                Spacer(Modifier.width(32.dp))
            }
            viewHolder(node)
        }
    }
}

@Composable
fun <N, C : Node<N, C>> DefaultDropDownViewHolder(
    itemToString: (C?) -> String,
    node: C,
    onExpandedChange: (Boolean) -> Unit,
    onItemClick: (C) -> Unit,
    contentPadding: PaddingValues,
    enabled: Boolean
) {
    val layoutDirection = LocalLayoutDirection.current
    DropdownMenuItem(
        text = { Text(itemToString(node)) },
        onClick = {
            onExpandedChange(false)
            onItemClick(node)
        },
        contentPadding = contentPadding.let {
            PaddingValues(
                start = 8.dp,
                top = it.calculateTopPadding(),
                bottom = it.calculateBottomPadding(),
                end = it.calculateEndPadding(layoutDirection)
            )
        },
        enabled = enabled
    )
}
