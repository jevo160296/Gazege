package com.example.gazege.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.R
import com.example.gazege.ui.views.PersonAction
import com.example.gazege.ui.views.TransactionAction
import com.example.gazege.ui.views.person.PersonDetail
import com.example.gazege.ui.widgets.GIndefiniteCircularProgressIndicator

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
        val personSummaryState = viewModel.rememberPersonSummaryState().value

        val personId = navStack.arguments?.getInt("personId")
        val person = allPerson.firstOrNull { it.id == personId }
        when (personSummaryState) {
            is FullPersonSummaryState -> {
                val deuda = personSummaryState.deudasFlujo[person] ?: 0.0
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
                    // TODO Develop UI for empty person
                    Text(text = "Empty person")
                }
            }

            is LoadingPersonSummaryState -> {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.DefaultPadding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GIndefiniteCircularProgressIndicator()
                    Text(stringResource(id = R.string.LoadingPersonSummaryView))
                }
            }
        }
    }
}

fun NavController.navigateToPersonDetail(personId: Int?) {
    navigate("personDetail/$personId")
}