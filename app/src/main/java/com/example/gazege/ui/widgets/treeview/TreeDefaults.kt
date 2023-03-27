package com.example.gazege.ui.widgets.treeview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun DefaultTreeLeadingIcon(isExpanded: Boolean) = if (isExpanded) {
    Icon(
        painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowDown),
        contentDescription = "Collapse"
    )
} else {
    Icon(
        painter = rememberVectorPainter(image = Icons.Default.KeyboardArrowRight),
        contentDescription = "Expand"
    )
}