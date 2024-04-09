package com.jmml.gazege.ui.widgets.input

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)
) = androidx.compose.material3.TextButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    content = content
)