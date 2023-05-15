package com.example.gazege.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.ui.views.PersonAction
import com.example.gazege.ui.views.TransactionAction
import com.example.gazege.ui.views.person.PersonDetail

fun NavGraphBuilder.screenPersonDetail(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEditPerson: (Int?) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit
) {
    composable(
        "personDetail/{personId}",
        arguments = listOf(
            navArgument("personId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val allPerson by viewModel.rememberAllPerson()
        val personSummaryState by viewModel.rememberPersonSummaryState()

        val personId = navStack.arguments?.getInt("personId")
        val person = allPerson.firstOrNull { it.id == personId }
        val deuda = personSummaryState?.deudasFlujo?.get(person) ?: 0.0
        if (person != null) {
            PersonDetail(
                person = person,
                onPersonAction = { _, action ->
                    when (action) {
                        PersonAction.EDIT -> onNavigateToEditPerson(personId)
                        PersonAction.DELETE -> {
                            onNavigateUp()
                            viewModel.deletePerson(person)
                        }
                    }
                },
                viewModel = viewModel,
                onTransactionAction = { transaction, action ->
                    val transactionId = transaction.id
                    when (action) {
                        TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                        TransactionAction.DELETE -> viewModel.deleteTransaction(transaction)
                    }
                },
                deuda = deuda
            )
        } else {
            Text(text = "Empty person")
        }
    }
}

fun NavController.navigateToPersonDetail(personId: Int?) {
    navigate("personDetail/$personId")
}