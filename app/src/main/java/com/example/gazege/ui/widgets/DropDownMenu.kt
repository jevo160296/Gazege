package com.example.gazege.ui.widgets

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropDownMenu(
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<T>,
    selectedItem: T?,
    itemToString: (T?) -> String,
    onItemClick: (T) -> Unit,
    label: @Composable () -> Unit
) {
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
            options.map {
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
}