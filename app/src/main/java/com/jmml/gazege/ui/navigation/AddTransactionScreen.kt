package com.jmml.gazege.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.ui.fragments.LoadingTransactionFormFragment
import com.jmml.gazege.ui.fragments.TransactionFormFragment
import com.jmml.gazege.ui.views.AddAction
import com.jmml.gazege.ui.views.AddPromissoryNoteAction
import com.jmml.gazege.ui.views.AddTransactionAction
import com.jmml.zoo.clases.Result
import java.time.LocalDate

fun NavGraphBuilder.screenAddTransaction(
    viewModelAddTransaction: MainViewModel.ViewModelAddTransaction,
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
        val incomeAccount by viewModelAddTransaction.rememberIncomeAccount()
        val outcomeAccount by viewModelAddTransaction.rememberOutcomeAccount()
        val allPerson by viewModelAddTransaction.rememberAllPerson()
        val allAccount by viewModelAddTransaction.rememberAllAccount()
        val categories by viewModelAddTransaction.rememberCategories()
        val budgetWithCalculatedDataAndCategory =
            viewModelAddTransaction.rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData().value

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
                viewModelAddTransaction.rememberAccountAndOwner().value
            } else {
                viewModelAddTransaction.rememberAccountAndOwnerUserFirst().value
            }
        when (budgetWithCalculatedDataAndCategory) {
            is Result.Error -> Text(text = "Error: ${budgetWithCalculatedDataAndCategory.exception}")
            Result.Loading -> LoadingTransactionFormFragment()
            is Result.Success -> TransactionFormFragment(
                contentPadding = PaddingValues(8.dp),
                itemSpacing = 8.dp,
                accountList = orderedAccounts,
                personList = allPerson,
                categoryList = categories,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory.data,
                defaultDate = LocalDate.of(
                    yearMonthDay.div(10000),
                    yearMonthDay.mod(10000).div(100),
                    yearMonthDay.mod(100)
                ),
                fixedSourceAccount = initialSourceAccount,
                fixedDestinationAccount = initialDestinationAccount,
                onAccountAddRequested = onNavigateToAddAccount
            ) { newTransaction, addAnotherTransaction ->
                viewModelAddTransaction.insertTransaction(newTransaction)
                if (!addAnotherTransaction) {
                    onNavigateUp()
                }
            }
        }
    }
}

fun NavGraphBuilder.screenAddPromissoryNote(
    viewModelAddPromissoryNote: MainViewModel.ViewModelAddPromissoryNote,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(
        "addPromissoryNote?yearmonthday={yearmonthday}",
        deepLinks = listOf(navDeepLink {
            uriPattern = "$URI?promissoryNoteAction={promissoryNoteAction}"
            action = Intent.ACTION_VIEW
        }),
        arguments = listOf(
            navArgument("yearmonthday") {
                type = NavType.IntType
                defaultValue = LocalDate.now().toInt()
            }
        )
    ) {
        Text("Add promissory note action")
    }
}

fun NavController.navigateToAddTransaction(
    date: LocalDate,
    transactionAction: AddAction
) {
    when (transactionAction) {
        is AddTransactionAction -> {
            val yearmonthday = date.toInt()
            val transactionaction = transactionAction.name
            navigate("addTransaction?yearmonthday=$yearmonthday?transactionaction=$transactionaction?requestingAccountId=${-1}")
        }

        is AddPromissoryNoteAction -> navigate("addPromissoryNote?")
    }
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