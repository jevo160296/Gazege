package com.example.gazege.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.widgets.treeview.DefaultTreeLeadingIcon

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableListItemViewHolder(
    onItemTapped: () -> Unit,
    onItemLongPressed: () -> Unit,
    content: @Composable () -> Unit
) = Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(CardDefaults.shape)
        .background(MaterialTheme.colorScheme.background)
        .combinedClickable(
            onClick = onItemTapped,
            onLongClick = onItemLongPressed
        )
        .padding(4.dp)
) {
    content()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableTreeListItemViewHolder(
    level: Int,
    showExpandIcon: Boolean,
    isExpanded: Boolean,
    onIsExpandedChanged: () -> Unit,
    onItemTapped: () -> Unit,
    onItemLongPressed: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.background,
    itemViewHolder: @Composable () -> Unit
) = Row(verticalAlignment = Alignment.CenterVertically) {
    Spacer(Modifier.width(level.dp * 8))
    if (showExpandIcon) {
        IconToggleButton(
            modifier = Modifier.width(42.dp),
            checked = isExpanded,
            onCheckedChange = { onIsExpandedChanged() }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .background(containerColor)
            .combinedClickable(
                onClick = onItemTapped,
                onLongClick = onItemLongPressed
            )
    ) {
        itemViewHolder()
    }
}

@Composable
fun SelectableTreeListItemViewHolder(
    level: Int,
    showExpandIcon: Boolean,
    isExpanded: Boolean,
    onIsExpandedChanged: () -> Unit,
    state: ToggleableState,
    onSelectionClick: (ToggleableState) -> Unit,
    itemViewHolder: @Composable () -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically
) {
    if (showExpandIcon) {
        IconToggleButton(
            modifier = Modifier.width(42.dp),
            checked = isExpanded,
            onCheckedChange = { onIsExpandedChanged() }
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
    Spacer(Modifier.width(8.dp))
    TriStateCheckbox(
        state = state,
        onClick = {
            onSelectionClick(
                when (state) {
                    ToggleableState.On -> ToggleableState.Off
                    ToggleableState.Off -> ToggleableState.On
                    ToggleableState.Indeterminate -> ToggleableState.Off
                }
            )
        }
    )
    Spacer(modifier = Modifier.width(level.dp * 8))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .background(MaterialTheme.colorScheme.background)
    ) {
        itemViewHolder()
    }
}