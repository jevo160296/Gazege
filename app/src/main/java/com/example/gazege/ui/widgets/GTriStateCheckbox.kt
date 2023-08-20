package com.example.gazege.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun GTriStateCheckbox(
    state: ToggleableState,
    onClick: () -> Unit,
    colors: CheckboxColors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.secondary,
        checkmarkColor = MaterialTheme.colorScheme.onSecondary,
        uncheckedColor = MaterialTheme.colorScheme.onSecondaryContainer
    ),
    enabled: Boolean = true
) {
    TriStateCheckbox(
        state = state,
        onClick = onClick,
        colors = colors,
        enabled = enabled
    )
}

@Preview
@Composable
fun GTristateCheckboxPreview() {
    var state by remember {
        mutableStateOf(ToggleableState.Off)
    }
    GazegeTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GTriStateCheckbox(
                    state = state,
                    onClick = {
                        state = when (state) {
                            ToggleableState.On -> ToggleableState.Indeterminate
                            ToggleableState.Off -> ToggleableState.On
                            ToggleableState.Indeterminate -> ToggleableState.Off
                        }
                    })
                GTriStateCheckbox(
                    state = state,
                    onClick = {
                        state = when (state) {
                            ToggleableState.On -> ToggleableState.Indeterminate
                            ToggleableState.Off -> ToggleableState.On
                            ToggleableState.Indeterminate -> ToggleableState.Off
                        }
                    },
                    enabled = false
                )
            }
        }
    }
}