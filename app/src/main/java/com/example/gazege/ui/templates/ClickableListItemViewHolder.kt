package com.example.gazege.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

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