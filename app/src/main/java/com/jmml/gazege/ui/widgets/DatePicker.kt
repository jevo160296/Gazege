package com.jmml.gazege.ui.widgets

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jmml.gazege.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit = {},
    pattern: String = "yyyy-MM-dd",
) {
    var dropDownExpanded by remember {
        mutableStateOf(false)
    }
    var dateDialogShowing by remember {
        mutableStateOf(false)
    }
    val date: LocalDate = value ?: LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern(pattern)

    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
            if (dropDownExpanded) {
                dateDialogShowing = true
                dropDownExpanded = false
            }
        }
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = date.format(formatter),
            onValueChange = {},
            readOnly = true,
            enabled = false,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
            },
            label = { Text(stringResource(R.string.Fecha)) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
    }
    if (dateDialogShowing) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value
                ?.atStartOfDay()
                ?.toInstant(ZoneOffset.UTC)
                ?.toEpochMilli()
        )
        val confirmedEnabled =
            remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            onDismissRequest = { dateDialogShowing = false },
            confirmButton = {
                TextButton(
                    onClick =
                    {
                        dateDialogShowing = false
                        val selectedDate = datePickerState.selectedDateMillis
                        if (selectedDate != null) {
                            val transformedDate = Instant
                                .ofEpochMilli(selectedDate)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onValueChange(transformedDate)
                        }
                    },
                    enabled = confirmedEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dateDialogShowing = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}