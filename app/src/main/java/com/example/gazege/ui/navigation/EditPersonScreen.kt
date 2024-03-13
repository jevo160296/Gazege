package com.example.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.ui.fragments.PersonFormFragment
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenEditPerson(
    viewModelEditPerson: MainViewModel.ViewModelEditPerson,
    onNavigateUp: () -> Unit
) {
    composable(
        "editPerson/{personId}",
        arguments = listOf(navArgument("personId") { type = NavType.IntType })
    ) { navBack ->
        val allPerson by viewModelEditPerson.rememberAllPerson()

        val coroutineScope = rememberCoroutineScope()

        val personId = navBack.arguments?.getInt("personId")
        val selectedPerson = allPerson
            .firstOrNull { it.id == personId }
        PersonFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            onPersonAddRequested = { person, snackBarHostSate ->
                val namesList =
                    allPerson.map { persona -> persona.name }
                val sePuedeEditar = person.name !in namesList
                if (sePuedeEditar) {
                    viewModelEditPerson.updatePerson(person, onErrorAction = {
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