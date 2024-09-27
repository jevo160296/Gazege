package com.jmml.gazege.ui.views.person

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.widgets.ComboBox

@Composable
fun PersonComboBox(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    personList: List<Person>,
    selectedPerson: Person?,
    onDoneAction: () -> Unit,
    onClick: (Person) -> Unit
) {
    val (expanded, onExpandedChange) = rememberSaveable { mutableStateOf(false) }

    ComboBox(
        modifier = modifier,
        dropDownExpanded = expanded,
        onExpandedChange = onExpandedChange,
        options = personList,
        selectedItem = selectedPerson,
        itemToString = { it?.name ?: "" },
        onItemClick = onClick,
        label = label,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDoneAction() })
    )
}