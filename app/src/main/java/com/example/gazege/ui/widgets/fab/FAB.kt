package com.example.gazege.ui.widgets.fab

import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import com.example.gazege.ui.theme.Shapes

@Composable
fun FAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = Shapes.small,
        content = icon
    )
}