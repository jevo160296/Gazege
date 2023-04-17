package com.example.gazege.ui.widgets.treeview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R

@Composable
fun DefaultTreeLeadingIcon(isExpanded: Boolean) = if (isExpanded) {
    Icon(
        painter = painterResource(id = R.drawable.ic_round_arrow_drop_down_24),
        contentDescription = "Collapse"
    )
} else {
    Icon(
        painter = painterResource(id = R.drawable.round_arrow_right_24),
        contentDescription = "Expand",
        tint = MaterialTheme.colorScheme.onBackground
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

@Composable
fun <N, C : Node<N, C>> TreeScope<N, C>.DefaultItemHolderWithExpandIcon(
    startPadding: Dp,
    endPadding: Dp,
    node: C,
    viewHolder: @Composable () -> Unit
) =
    DefaultItemHolder(startPadding = startPadding, endPadding = endPadding) {
        val isExpanded = isExpanded(node)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(node.level.dp * 8))
            if (node.children.isNotEmpty()) {
                IconToggleButton(
                    modifier = Modifier
                        .width(42.dp)
                        .height(42.dp),
                    checked = isExpanded,
                    onCheckedChange = { toggleExpanded(node) }
                ) {
                    DefaultTreeLeadingIcon(isExpanded = isExpanded)
                }
            } else {
                Spacer(
                    Modifier
                        .width(42.dp)
                        .height(42.dp)
                )
            }
            viewHolder()
        }
    }