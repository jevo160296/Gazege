package com.jmml.gazege.ui.fragments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.savers.PartialPerson
import com.jmml.gazege.ui.savers.personSaver
import com.jmml.gazege.ui.views.person.PersonForm
import com.jmml.gazege.ui.widgets.Form

@Composable
fun PersonFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    person: Person? = null,
    onPersonAddRequested: (Person, SnackbarHostState) -> Unit
) {
    var personState by rememberSaveable(
        stateSaver = personSaver
    ) {
        mutableStateOf(
            if (person != null) {
                PartialPerson(
                    id = person.id,
                    name = person.name,
                    importance = person.importance
                )
            } else {
                PartialPerson.blankEntity()
            }
        )
    }
    val snackbarHostState = SnackbarHostState()
    val isComplete = personState.isComplete()
    val savePerson = {
        val fullPerson = personState.toFull()
        onPersonAddRequested(fullPerson, snackbarHostState)
    }
    Form(
        modifier = modifier,
        onSaveClicked = savePerson,
        isSavedButtonEnabled = isComplete,
        title = stringResource(R.string.persona),
        snackbarHostState = snackbarHostState,
    ) {
        PersonForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            person = personState,
            onPersonChanged = {
                personState = it
            },

            onDoneAction = savePerson,
            imeAction = if (isComplete) {
                ImeAction.Done
            } else {
                ImeAction.None
            }
        )
    }
}