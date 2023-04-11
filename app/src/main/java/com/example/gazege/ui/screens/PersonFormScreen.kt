package com.example.gazege.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.PartialPerson
import com.example.gazege.ui.savers.personSaver
import com.example.gazege.ui.views.person.PersonForm
import com.example.gazege.ui.widgets.Form
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddPerson(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable("addPerson") {
        val allPerson by viewModel.allPerson.observeAsState(emptyList())

        val coroutineScope = rememberCoroutineScope()
        PersonFormScreen(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            onPersonAddRequested = { person, snackBarHostSate ->
                val namesList =
                    allPerson.map { persona -> persona.name }
                val sePuedeAgregar = person.name !in namesList
                if (sePuedeAgregar) {
                    viewModel.insertPerson(person, onErrorAction = {
                        coroutineScope.launch {
                            snackBarHostSate.showSnackbar("Error agregando a la persona: $it")
                        }
                    }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackBarHostSate.showSnackbar("Error, nombre repetido.")
                    }
                }
            }
        )
    }
}

fun NavController.navigateToAddPerson() {
    navigate("addPerson")
}

fun NavGraphBuilder.screenEditPerson(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    composable(
        "editPerson/{personId}",
        arguments = listOf(navArgument("personId") { type = NavType.IntType })
    ) { navBack ->
        val allPerson by viewModel.allPerson.observeAsState(emptyList())

        val coroutineScope = rememberCoroutineScope()

        val personId = navBack.arguments?.getInt("personId")
        val selectedPerson = allPerson
            .firstOrNull { it.id == personId }
        PersonFormScreen(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            onPersonAddRequested = { person, snackBarHostSate ->
                val namesList =
                    allPerson.map { persona -> persona.name }
                val sePuedeEditar = person.name !in namesList
                if (sePuedeEditar) {
                    viewModel.updatePerson(person, onErrorAction = {
                        coroutineScope.launch {
                            snackBarHostSate.showSnackbar("Error editando persona $it")
                        }
                    }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackBarHostSate.showSnackbar("Error, nombre repetido.")
                    }
                }
            },
            person = selectedPerson
        )
    }
}

fun NavController.navigateToEditPerson(personId: Int?) {
    navigate("editPerson/$personId")
}

@Composable
fun PersonFormScreen(
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
        title = "Person",
        snackbarHostState = snackbarHostState
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