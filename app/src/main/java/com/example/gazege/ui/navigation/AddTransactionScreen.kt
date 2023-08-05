package com.example.gazege.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Account
import com.example.gazege.ui.fragments.TransactionFormFragment
import com.example.gazege.ui.views.AddTransactionAction
import java.time.LocalDate

fun NavGraphBuilder.screenAddTransaction(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(
        "addTransaction?yearmonthday={yearmonthday}?transactionaction={transactionaction}?requestingAccountId={requestingAccountId}",
        deepLinks = listOf(navDeepLink {
            uriPattern = "$URI?transactionaction={transactionaction}"
            action = Intent.ACTION_VIEW
        }),
        arguments = listOf(
            navArgument("yearmonthday") {
                type = NavType.IntType
                defaultValue = LocalDate.now().toInt()
            },
            navArgument("transactionaction") {
                type = NavType.StringType
            },
            navArgument("requestingAccountId") {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { navBackStackEntry ->
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()
        val allPerson by viewModel.rememberAllPerson()
        val allAccount by viewModel.rememberAllAccount()
        val categories by viewModel.rememberCategories()

        LaunchedEffect(key1 = allPerson.isNotEmpty()) {
            if (allPerson.isNotEmpty()) {
                onDataLoaded()
            }
        }

        val yearMonthDay = navBackStackEntry.arguments?.getInt("yearmonthday")
            ?: LocalDate.now().let {
                it.year * 100 + it.monthValue
            }
        val transactionActionName =
            navBackStackEntry.arguments?.getString("transactionaction")
        val transactionAction =
            transactionActionName?.let { AddTransactionAction.valueOf(it) }
                ?: AddTransactionAction.ADD_TRANSFER
        val requestingAccountId = navBackStackEntry.arguments?.getInt("requestingAccountId")
        val requestingAccount: Account? = allAccount.firstOrNull { it.id == requestingAccountId }
            ?.takeIf { acc -> acc.id != null && acc.id >= 0 }
        val initialSourceDestinationAccount: Pair<Account?, Account?> = when (transactionAction) {
            AddTransactionAction.ADD_EXPENSE -> Pair(requestingAccount, outcomeAccount)
            AddTransactionAction.ADD_INCOME -> Pair(incomeAccount, requestingAccount)
            AddTransactionAction.ADD_TRANSFER -> Pair(null, null)
        }
        val initialSourceAccount: Account? = initialSourceDestinationAccount.first
        val initialDestinationAccount: Account? = initialSourceDestinationAccount.second
        val orderedAccounts =
            if (transactionAction == AddTransactionAction.ADD_TRANSFER) {
                viewModel.rememberAccountAndOwner().value
            } else {
                viewModel.rememberAccountAndOwnerUserFirst().value
            }
        TransactionFormFragment(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            accountList = orderedAccounts,
            personList = allPerson,
            categoryList = categories,
            defaultDate = LocalDate.of(
                yearMonthDay.div(10000),
                yearMonthDay.mod(10000).div(100),
                yearMonthDay.mod(100)
            ),
            fixedSourceAccount = initialSourceAccount,
            fixedDestinationAccount = initialDestinationAccount,
            onAccountAddRequested = onNavigateToAddAccount
        ) { newTransaction, addAnotherTransaction ->
            viewModel.insertTransaction(newTransaction)
            if (!addAnotherTransaction) {
                onNavigateUp()
            }
        }
    }
}

fun NavController.navigateToAddTransaction(
    date: LocalDate,
    transactionAction: AddTransactionAction
) {
    val yearmonthday = date.toInt()
    val transactionaction = transactionAction.name
    navigate("addTransaction?yearmonthday=$yearmonthday?transactionaction=$transactionaction?requestingAccountId=${-1}")
}

fun NavController.navigateToAddTransaction(
    date: LocalDate,
    transactionAction: AddTransactionAction,
    requestingAccount: Account
) {
    val yearmonthday = date.toInt()
    val transactionaction = transactionAction.name
    val requestingAccountId = requestingAccount.id ?: -1
    navigate("addTransaction?yearmonthday=$yearmonthday?transactionaction=$transactionaction?requestingAccountId=$requestingAccountId")
}