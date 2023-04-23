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
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.fragments.AccountFormFragment
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenEditAccount(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(
        "editAccount/{accountId}",
        arguments = listOf(navArgument("accountId") { type = NavType.IntType })
    ) { navStack ->
        val accountAndOwnerWithTransactionsAndPockets by viewModel.rememberAccountAndOwnerWithTransactionsAndPockets()
        val allPerson by viewModel.rememberAllPerson()
        val allAccount by viewModel.rememberAllAccount()
        val incomeAccount by viewModel.rememberIncomeAccount()
        val outcomeAccount by viewModel.rememberOutcomeAccount()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()

        val coroutineScope = rememberCoroutineScope()

        val accountId = navStack.arguments?.getInt("accountId")
        val selectedAccountAndOwnerWithTransactions =
            accountAndOwnerWithTransactionsAndPockets
                .firstOrNull { it.accountAndOwnerWithTransactions.account.id == accountId }
        val selectedAccountAndOwnerBalance =
            selectedAccountAndOwnerWithTransactions?.let {
                AccountDao.getTotal(
                    it.accountAndOwnerWithTransactions,
                    null,
                    null
                ) +
                        AccountDao.getChildrenTotal(it, null, null)
            }
        val selectedAccountAndOwner = selectedAccountAndOwnerWithTransactions
            ?.let {
                AccountAndOwner(
                    account = it.accountAndOwnerWithTransactions.account,
                    owner = it.accountAndOwnerWithTransactions.owner
                )
            }
        AccountFormFragment(
            personList = allPerson,
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(8.dp),
            onPersonAddRequested = onNavigateToAddPerson,
            onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                val accountOwnerIdList = allAccount
                    .filter { it.id != account.id }
                    .map { Pair(it.name, it.ownerId) }
                val accountOwnerId = Pair(account.name, account.ownerId)
                val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                if (sePuedeAgregar) {
                    viewModel.updateAccount(
                        account,
                        onErrorAction = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Error añadiendo la cuenta: $it")
                            }
                        },
                        onCompleitionAction = { addedId ->
                            if (incomeAccountId != null && outcomeAccountId != null) {
                                val valorAjuste =
                                    newBalance - (selectedAccountAndOwnerBalance
                                        ?: 0.0)
                                viewModel.realizarAjuste(
                                    accountId = addedId.toInt(),
                                    amount = valorAjuste,
                                    incomeAccountId = incomeAccountId,
                                    outcomeAccountId = outcomeAccountId
                                )
                            }
                        }
                    ).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("Las personas no pueden tener cuentas con nombres repetidos")
                    }
                }
            },
            accountAndOwner = selectedAccountAndOwner,
            currentBalance = selectedAccountAndOwnerBalance ?: 0.0,
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onSetIncomeOutcomeAccount = onNavigateToSettings,
            accountAndOwnerList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(it.account, it.owner)
            }
        )
    }
}

fun NavController.navigateToEditAccount(accountId: Int?) {
    navigate("editAccount/$accountId")
}