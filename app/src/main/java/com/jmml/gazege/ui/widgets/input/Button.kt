package com.jmml.gazege.ui.widgets.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.ui.theme.GazegeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    expanded: Boolean = false,
    dropDownExpanded: Boolean = false,
    onDropDownExpandedChange: (Boolean) -> Unit = {},
    options: List<String> = emptyList(),
    onOptionSelected: (String) -> Unit = {},
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconToggleButtonColors = IconButtonDefaults.filledTonalIconToggleButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val expandedState = remember {
        MutableTransitionState(false)
    }
    LaunchedEffect(expanded) { expandedState.targetState = expanded }
    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = onDropDownExpandedChange
    ) {
        Surface(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier
                .animateContentSize()
                .menuAnchor()
                .semantics { role = Role.Checkbox },
            enabled = enabled,
            shape = shape,
            color = colors.containerColor(enabled, checked).value,
            contentColor = colors.contentColor(enabled, checked).value,
            interactionSource = interactionSource
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(FilledTonalIconButtonTokens.ContainerSize)
            ) {
                Box(
                    modifier = Modifier.size(FilledTonalIconButtonTokens.ContainerSize),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                AnimatedVisibility(
                    visibleState = expandedState
                ) {
                    Row(
                        modifier = Modifier.padding(end = FilledTonalIconButtonTokens.EndPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        text()
                        DropDownArrow(dropDownExpanded)
                    }
                }
            }
        }
        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { onDropDownExpandedChange(false) }
        ) {
            options.map {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = { onOptionSelected(it) }
                )
            }
        }
    }
}

@Composable
private fun DropDownArrow(isExpanded: Boolean) {
    val rotation = if (isExpanded) 180f else 0f
    val animatedRotation by animateFloatAsState(targetValue = rotation, label = "Rotation")
    Icon(
        imageVector = Icons.Default.ArrowDropDown,
        modifier = Modifier.rotate(animatedRotation),
        contentDescription = null
    )
}

internal object FilledTonalIconButtonTokens {
    val ContainerSize = 40.0.dp
    val EndPadding = 8.dp
}

@Composable
internal fun IconToggleButtonColors.containerColor(
    enabled: Boolean,
    checked: Boolean
): State<Color> {
    val target = when {
        !enabled -> disabledContainerColor
        !checked -> containerColor
        else -> checkedContainerColor
    }
    return rememberUpdatedState(target)
}

@Composable
internal fun IconToggleButtonColors.contentColor(enabled: Boolean, checked: Boolean): State<Color> {
    val target = when {
        !enabled -> disabledContentColor
        !checked -> contentColor
        else -> checkedContentColor
    }
    return rememberUpdatedState(target)
}

@Preview
@Composable
private fun ExpandableIconToggleButtonPreview() {
    var selectedIcon by remember { mutableIntStateOf(0) }
    var dropDownExpanded by remember { mutableStateOf(false) }
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf(options.firstOrNull() ?: "") }
    GazegeTheme {
        Column(
            Modifier.systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconToggleButton(
                checked = selectedIcon == 0,
                onCheckedChange = {
                    if (it) selectedIcon = 0
                }
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
            }
            ExpandableIconToggleButton(
                checked = selectedIcon == 1,
                expanded = selectedIcon == 1,
                dropDownExpanded = dropDownExpanded,
                onCheckedChange = {
                    if (it) selectedIcon = 1
                    else dropDownExpanded = true
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(text = selectedOption)
                },
                options = options,
                onDropDownExpandedChange = { if (!it) dropDownExpanded = false },
                onOptionSelected = {
                    selectedOption = it
                    dropDownExpanded = false
                }
            )
            FilledTonalIconToggleButton(
                checked = selectedIcon == 2,
                onCheckedChange = {
                    if (it) selectedIcon = 2
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null
                )
            }
        }
    }
}