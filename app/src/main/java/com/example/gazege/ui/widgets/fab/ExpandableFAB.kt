package com.example.gazege.ui.widgets.fab

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import com.example.gazege.ui.widgets.FAB

@Composable
fun ExpandableFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    options: @Composable ColumnScope.() -> Unit
) {
    FAB(onClick = onClick, icon = icon)
}