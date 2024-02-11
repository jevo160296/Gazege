package com.example.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.fragments.TransactionFormFragment
import java.time.LocalDate

fun LocalDate.toInt() = let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }
fun NavGraphBuilder.screenEditTransaction(
    viewModel: MainViewModel,
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

        val selectedTransactionAndAccounts by viewModel.rememberTransactionAndAccounts(transactionId)
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()
        val allPerson by viewModel.rememberAllPerson()
        val categories by viewModel.rememberCategories()
        val budgetWithCalculatedDataAndCategory by viewModel.rememberBudgetAndCategoryWithCalculatedDataMap()

        TransactionFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            transactionAndAccounts = selectedTransactionAndAccounts,
            accountList = accountAndOwnerWithTransactions.map {
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
            viewModel.updateTransaction(editedTransaction)
            onNavigateUp()
        }
    }
}

fun NavController.navigateToEditTransaction(transactionId: Int?) {
    navigate("editTransaction/$transactionId")
}