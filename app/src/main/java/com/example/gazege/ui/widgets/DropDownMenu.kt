package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
