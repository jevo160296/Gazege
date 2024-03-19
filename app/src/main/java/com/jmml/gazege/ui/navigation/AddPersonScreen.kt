package com.jmml.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.fragments.PersonFormFragment
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddPerson(
    viewModelAddPerson: MainViewModel.ViewModelAddPerson,
    onNavigateUp: () -> Unit
) {
    composable("addPerson") {
        val allPerson by viewModelAddPerson.rememberAllPerson()

        val coroutineScope = rememberCoroutineScope()
        PersonFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            onPersonAddRequested = { person, snackBarHostSate ->
                val namesList =
                    allPerson.map { persona -> persona.name }
                val sePuedeAgregar = person.name !in namesList
                if (sePuedeAgregar) {
                    viewModelAddPerson.insertPerson(person, onErrorAction = {
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