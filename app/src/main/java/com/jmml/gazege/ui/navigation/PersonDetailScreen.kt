package com.jmml.gazege.ui.navigation

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
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.R
import com.jmml.gazege.ui.fragments.EmptySummaryStateUI
import com.jmml.gazege.ui.fragments.ReloadingPersonSummaryStateUI
import com.jmml.gazege.ui.views.PersonAction
import com.jmml.gazege.ui.views.PromissoryNoteAction
import com.jmml.gazege.ui.views.TransactionAction
import com.jmml.gazege.ui.views.person.PersonDetail
import com.jmml.zoo.ui.state.ZIndefiniteCircularProgressIndicator

fun NavGraphBuilder.screenPersonDetail(
    viewModelPersonDetail: MainViewModel.ViewModelPersonDetail,
    onNavigateUp: () -> Unit,
    onNavigateToEditPerson: (Int?) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit,
    onNavigateToEditPromissoryNote: (Int?) -> Unit
) {
    composable(
        "personDetail/{personId}",
        arguments = listOf(
            navArgument("personId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val principalPerson = viewModelPersonDetail.rememberPrincipalPerson().value
        val allPerson by viewModelPersonDetail.rememberAllPerson()
        val personSummaryState = viewModelPersonDetail.rememberPersonSummaryState().value

        val personId = navStack.arguments?.getInt("personId")
        val person = allPerson.firstOrNull { it.id == personId }
        when (personSummaryState) {
            is FullPersonSummaryState -> {
                val deuda = personSummaryState.deudasFlujo[person] ?: 0.0
                if (person != null && principalPerson != null) {
                    PersonDetail(
                        principalPerson = principalPerson,
                        person = person,
                        onPersonAction = { _, action ->
                            when (action) {
                                PersonAction.EDIT -> onNavigateToEditPerson(personId)
                                PersonAction.DELETE -> {
                                    onNavigateUp()
                                    viewModelPersonDetail.deletePerson(person)
                                }
                            }
                        },
                        viewModelPersonDetail = viewModelPersonDetail,
                        onTransactionAction = { transaction, action ->
                            val transactionId = transaction.id
                            when (action) {
                                TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                                TransactionAction.DELETE -> viewModelPersonDetail.deleteTransaction(
                                    transaction
                                )
                            }
                        },
                        onTransactionDetailsAction = { transactionDetails, action ->
                            val transactionId = transactionDetails.transactionId
                            when (action) {
                                TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                                TransactionAction.DELETE -> viewModelPersonDetail.deleteTransactionDetails(
                                    transactionDetails
                                )
                            }
                        },
                        onPromissoryNoteAction = { promissoryNote, action ->
                            val promissoryNoteId = promissoryNote.id
                            when (action) {
                                PromissoryNoteAction.EDIT -> onNavigateToEditPromissoryNote(
                                    promissoryNoteId
                                )

                                PromissoryNoteAction.DELETE -> viewModelPersonDetail.deletePromissoryNote(
                                    promissoryNote
                                )
                            }
                        },
                        deuda = deuda
                    )
                } else {
                    // TODO Develop UI for empty person
                    Text(text = "Empty person or principal person")
                }
            }

            is LoadingPersonSummaryState -> {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.DefaultPadding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ZIndefiniteCircularProgressIndicator()
                    Text(stringResource(id = R.string.LoadingPersonSummaryView))
                }
            }

            EmptyPersonSummaryState -> EmptySummaryStateUI()
            is ReloadingPersonSummaryState -> ReloadingPersonSummaryStateUI()
        }
    }
}

fun NavController.navigateToPersonDetail(personId: Int?) {
    navigate("personDetail/$personId")
}