package com.jmml.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.ui.fragments.LoadingTransactionFormFragment
import com.jmml.gazege.ui.fragments.TransactionFormFragment
import com.jmml.zoo.clases.Result
import java.time.LocalDate

fun LocalDate.toInt() = let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }
fun NavGraphBuilder.screenEditTransaction(
    viewModelEditTransaction: MainViewModel.ViewModelEditTransaction,
    onNavigateUp: () -> Unit,
    onNavigateToAddAccount: () -> Unit
) {
    composable(
        "editTransaction/{transactionId}",
        arguments = listOf(navArgument("transactionId") {
            type = NavType.IntType
        })
    ) { navBackStackEntry ->
        val transactionId = navBackStackEntry.arguments?.getInt("transactionId")

        val selectedTransactionAndAccounts by viewModelEditTransaction.rememberTransactionAndAccounts(
            transactionId
        )
        val accountAndOwnerWithTransactions =
            viewModelEditTransaction.rememberAccountAndOwnerWithTransactions().value
        val allPerson by viewModelEditTransaction.rememberAllPerson()
        val categories by viewModelEditTransaction.rememberCategories()
        val budgetWithCalculatedDataAndCategory by viewModelEditTransaction.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData()

        when (accountAndOwnerWithTransactions) {
            is Result.Error -> Text(text = "Error ${accountAndOwnerWithTransactions.exception}")
            Result.Loading -> LoadingTransactionFormFragment()
            is Result.Success -> TransactionFormFragment(
                contentPadding = PaddingValues(8.dp),
                itemSpacing = 8.dp,
                transactionAndAccounts = selectedTransactionAndAccounts,
                accountList = accountAndOwnerWithTransactions.data.map {
                    AccountAndOwner(
                        it.account,
                        it.owner
                    )
                },
                personList = allPerson,
                categoryList = categories,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
                onAccountAddRequested = onNavigateToAddAccount
            ) { editedTransaction, _ ->
                viewModelEditTransaction.updateTransaction(editedTransaction)
                onNavigateUp()
            }
        }
    }
}

fun NavController.navigateToEditTransaction(transactionId: Int?) {
    navigate("editTransaction/$transactionId")
}