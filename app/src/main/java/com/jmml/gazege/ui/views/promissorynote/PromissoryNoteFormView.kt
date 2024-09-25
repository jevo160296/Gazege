package com.jmml.gazege.ui.views.promissorynote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.savers.PartialPromissoryNote
import com.jmml.gazege.ui.savers.promissoryNoteSaver
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.views.person.PersonComboBox
import com.jmml.gazege.ui.widgets.DatePicker
import com.jmml.gazege.ui.widgets.Form
import com.jmml.gazege.ui.widgets.NumberField
import com.jmml.gazege.ui.widgets.TextField
import com.jmml.zoo.debug.recomposeHighlighter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PromissoryNoteFormPage(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    promissoryNote: PromissoryNote? = null,
    personList: List<Person>,
    onPromissoryNoteChanged: (PromissoryNote, SnackbarHostState) -> Unit
) {
    val (promissoryNoteState, onPromissoryNoteStateChanged) = rememberSaveable(stateSaver = promissoryNoteSaver)
    {
        mutableStateOf(
            if (promissoryNote != null) {
                PartialPromissoryNote.from(promissoryNote)
            } else {
                PartialPromissoryNote(
                    null,
                    0.0,
                    LocalDate.now(),
                    null,
                    null,
                    ""
                )
            }
        )
    }
    val snackbarHostState = SnackbarHostState()
    val savePromissoryNote = {
        if (promissoryNoteState.isComplete()) {
            onPromissoryNoteChanged(promissoryNoteState.toFull(), snackbarHostState)
        }
    }
    Form(
        modifier = modifier,
        onSaveClicked = savePromissoryNote,
        isSavedButtonEnabled = promissoryNoteState.isComplete(),
        title = "Promissory note",
        snackbarHostState = snackbarHostState,
        itemSpacing = dimensionResource(R.dimen.DefaultPadding),
        itemsColumnsModifier = Modifier.padding(contentPadding)
    ) {
        PromissoryNoteForm(
            promissoryNote = promissoryNoteState,
            onPromissoryNoteChanged = onPromissoryNoteStateChanged,
            personList = personList,
            onDoneAction = savePromissoryNote
        )
    }
}

@Composable
private fun PromissoryNoteForm(
    promissoryNote: PartialPromissoryNote,
    onPromissoryNoteChanged: (PartialPromissoryNote) -> Unit,
    personList: List<Person>,
    onDoneAction: () -> Unit
) {
    val selectedSource = personList.firstOrNull { it.id == promissoryNote.sourceId }
    val selectedDestination = personList.firstOrNull { it.id == promissoryNote.destinationId }
    val focusRequester = remember { FocusRequester() }
    NumberField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .recomposeHighlighter(),
        value = promissoryNote.amount ?: 0.0,
        onValueChange = { onPromissoryNoteChanged(promissoryNote.copy(amount = it)) },
        label = { Text(stringResource(id = R.string.Valor)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onDoneAction() })
    )
    TextField(
        modifier = Modifier.recomposeHighlighter(),
        value = promissoryNote.description ?: "",
        onValueChange = { onPromissoryNoteChanged(promissoryNote.copy(description = it)) },
        label = { Text(text = stringResource(id = R.string.descripcion)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            capitalization = KeyboardCapitalization.Sentences
        ),
        keyboardActions = KeyboardActions(onDone = { onDoneAction() })
    )
    DatePicker(
        value = promissoryNote.date,
        onValueChange = { onPromissoryNoteChanged(promissoryNote.copy(date = it)) }
    )
    PersonComboBox(
        modifier = Modifier.recomposeHighlighter(),
        personList = personList,
        selectedPerson = selectedSource,
        onDoneAction = onDoneAction,
        onClick = { onPromissoryNoteChanged(promissoryNote.copy(sourceId = it.id)) }
    )
    PersonComboBox(
        modifier = Modifier.recomposeHighlighter(),
        personList = personList,
        selectedPerson = selectedDestination,
        onDoneAction = onDoneAction,
        onClick = { onPromissoryNoteChanged(promissoryNote.copy(destinationId = it.id)) }
    )
    LaunchedEffect(key1 = Unit) {
        // Waits 100 milliseconds until request focus to avoid calling the ime when expandable fab expands
        delay(100)
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
private fun PromissoryNotePreview() {
    val (promissoryNote, onPromissotyNoteChanged) = remember {
        mutableStateOf(
            PartialPromissoryNote.from(
                PromissoryNote(
                    0, 0.1, LocalDate.now(), 0, 1, "Desc"
                )
            )
        )
    }
    GazegeTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        )
        {
            DatabaseSample {
                PromissoryNoteForm(
                    promissoryNote = promissoryNote,
                    onPromissoryNoteChanged = onPromissotyNoteChanged,
                    personList = personSample
                ) { }
            }
        }
    }
}

@Preview
@Composable
private fun PromissoryNoteFormPagePreview() {
    GazegeTheme {
        val coroutine = rememberCoroutineScope()
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
        )
        {
            DatabaseSample {
                PromissoryNoteFormPage(
                    personList = personSample,
                ) { promissoryNote, snackbarHostState ->
                    coroutine.launch {
                        snackbarHostState.showSnackbar(
                            message = "Promissory note saved, desc: ${promissoryNote.description}"
                        )
                    }
                }
            }
        }
    }
}