package com.jmml.gazege.ui.widgets

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver

enum class GFilterChipLevel { Primary, Secondary }

@Composable
fun GFilterChip(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    selected: Boolean,
    level: GFilterChipLevel = GFilterChipLevel.Primary
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = label,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme
                .colorScheme
                .background
                .copy(
                    alpha = if (level == GFilterChipLevel.Primary) 0F else 0.4F
                )
                .compositeOver(MaterialTheme.colorScheme.tertiaryContainer)
        )
    )
}