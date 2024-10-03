package com.jmml.gazege.ui.views.person

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.widgets.ComboBox
import com.jmml.zoo.clases.Result
import com.jmml.zoo.ui.state.ZIndefiniteCircularProgressIndicator

@Composable
private fun PersonComboBox(
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

@Composable
fun PersonComboBox(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    personList: Result<List<Person>>,
    selectedPerson: Person?,
    onDoneAction: () -> Unit,
    onClick: (Person) -> Unit
) {
    Crossfade(targetState = personList, label = "") {
        when (it) {
            is Result.Error -> Text("Error: ${it.exception.message}")
            Result.Loading -> ZIndefiniteCircularProgressIndicator(modifier = modifier)
            is Result.Success -> PersonComboBox(
                modifier = modifier,
                label = label,
                personList = it.data,
                selectedPerson = selectedPerson,
                onDoneAction = onDoneAction,
                onClick = onClick
            )
        }
    }
}