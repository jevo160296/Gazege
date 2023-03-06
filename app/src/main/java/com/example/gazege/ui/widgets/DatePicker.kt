package com.example.gazege.ui.widgets

import android.app.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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
    val date: LocalDate = value ?: LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern(pattern)
    val dialog = DatePickerDialog(
        LocalContext.current,
        { _, year, month, dayOfMonth ->
            onValueChange(LocalDate.of(year, month + 1, dayOfMonth))
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth,
    )

    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
            if(dropDownExpanded){
                dialog.show()
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
            label = { Text("Date") },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
    }
}