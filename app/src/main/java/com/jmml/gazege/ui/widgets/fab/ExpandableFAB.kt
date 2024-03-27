package com.jmml.gazege.ui.widgets.fab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jmml.gazege.ui.widgets.menu.DropdownMenu


@Composable
fun ExpandableFAB(
    modifier: Modifier = Modifier,
    columnModifier: Modifier = Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
    fab: @Composable (onClick: () -> Unit) -> Unit,
    options: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        fab { onClick() }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = columnModifier,
                horizontalAlignment = Alignment.End
            ) {
                options()
            }
        }
    }
}