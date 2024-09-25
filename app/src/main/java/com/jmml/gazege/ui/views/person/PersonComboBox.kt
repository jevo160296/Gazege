package com.jmml.gazege.ui.views.person

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.widgets.ComboBox

@Composable
fun PersonComboBox(
    modifier: Modifier = Modifier,
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
        label = { Text(stringResource(R.string.persona)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDoneAction() })
    )
}