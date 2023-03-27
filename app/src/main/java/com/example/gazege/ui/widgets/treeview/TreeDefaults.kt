package com.example.gazege.ui.widgets.treeview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp

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

@Composable
fun DefaultHeader(startPadding: Dp, endPadding: Dp, viewHolder: @Composable () -> Unit) = Row {
    Spacer(Modifier.width(startPadding))
    Box(Modifier.weight(1f)) {
        viewHolder()
    }
    Spacer(Modifier.width(endPadding))
}

@Composable
fun DefaultItemHolder(startPadding: Dp, endPadding: Dp, viewHolder: @Composable () -> Unit) = Row {
    Spacer(Modifier.width(startPadding))
    Box(Modifier.weight(1f)) {
        viewHolder()
    }
    Spacer(Modifier.width(endPadding))
}