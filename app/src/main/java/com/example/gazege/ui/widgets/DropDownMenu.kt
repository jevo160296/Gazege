package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.widgets.treeview.ColumnTreeView
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <N, C : Node<N, C>> DropDownTreeMenu(
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<C>,
    selectedItem: C?,
    itemToString: (C?) -> String,
    onItemClick: (C) -> Unit,
    label: @Composable () -> Unit,
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
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
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
                onItemClick = onItemClick,
                itemToString = itemToString,
                onExpandedChange = onExpandedChange
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <N, C : Node<N, C>> OptionsGroupTreeView(
    groupedOptions: Map<String?, List<C>>,
    onItemClick: (C) -> Unit,
    itemToString: (C?) -> String,
    onExpandedChange: (Boolean) -> Unit
) {
    val nodes: List<C> = groupedOptions.flatMap {
        it.value
    }
    val inverseMap: Map<NodeId, String?> = groupedOptions.flatMap { (key, value) ->
        value.map {
            it.id() to key
        }
    }.toMap()
    ColumnTreeView(nodes = nodes, groupSelector = {
        inverseMap[it.id()]
    }) { node, treeScope ->
        Row {
            Spacer(modifier = Modifier.width(node.level.dp * 8))
            if (node.children.isNotEmpty()) {
                IconToggleButton(
                    modifier = Modifier.width(32.dp),
                    checked = treeScope.isExpanded(node),
                    onCheckedChange = { treeScope.toggleExpanded(node) }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowRight),
                        contentDescription = "Desc"
                    )
                }
            } else {
                Spacer(Modifier.width(32.dp))
            }
            DropdownMenuItem(
                text = { Text(itemToString(node)) },
                onClick = {
                    onExpandedChange(false)
                    onItemClick(node)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
}
