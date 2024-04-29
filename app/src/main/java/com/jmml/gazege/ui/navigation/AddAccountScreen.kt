package com.jmml.gazege.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.ui.fragments.AccountFormFragment
import com.jmml.gazege.ui.fragments.LoadingAccountFormFragment
import com.jmml.zoo.clases.Result
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddAccount(
    viewModelAddAccount: MainViewModel.ViewModelAddAccount,
    onNavigateToAddPerson: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable("addAccount") {
        val allPerson by viewModelAddAccount.rememberAllPerson()
        val allAccount by viewModelAddAccount.rememberAllAccount()
        val incomeAccount by viewModelAddAccount.rememberIncomeAccount()
        val outcomeAccount by viewModelAddAccount.rememberOutcomeAccount()
        val accountAndOwnerWithTransactions =
            viewModelAddAccount.rememberAccountAndOwnerWithTransactions().value
        val today by viewModelAddAccount.rememberToday()

        val coroutineScope = rememberCoroutineScope()
        when (accountAndOwnerWithTransactions) {
            is Result.Error -> Text("Error ${accountAndOwnerWithTransactions.exception}")
            Result.Loading -> LoadingAccountFormFragment()
            is Result.Success -> AccountFormFragment(
                contentPadding = PaddingValues(8.dp),
                itemSpacing = 8.dp,
                personList = allPerson,
                onPersonAddRequested = onNavigateToAddPerson,
                onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                    val accountOwnerIdList = allAccount.map {
                        Pair(it.name, it.ownerId)
                    }
                    val accountOwnerId = Pair(account.name, account.ownerId)
                    val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                    if (sePuedeAgregar) {
                        viewModelAddAccount.insertAccount(
                            account,
                            onErrorAction = {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar("Error añadiento cuenta $it")
                                }
                            },
                            onCompleitionAction = { addedId ->
                                if (incomeAccountId != null && outcomeAccountId != null) {
                                    val valorAjuste = newBalance
                                    viewModelAddAccount.realizarAjuste(
                                        accountId = addedId.toInt(),
                                        amount = valorAjuste,
                                        incomeAccountId = incomeAccountId,
                                        outcomeAccountId = outcomeAccountId,
                                        today = today
                                    )
                                }
                            }).invokeOnCompletion {
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
                currentBalance = 0.0,
                incomeAccount = incomeAccount,
                outcomeAccount = outcomeAccount,
                onSetIncomeOutcomeAccount = onNavigateToSettings,
                accountAndOwnerList = accountAndOwnerWithTransactions.data.map {
                    AccountAndOwner(it.account, it.owner)
                }
            )
        }
    }
}

fun NavController.navigateToAddAccount() {
    navigate("addAccount")
}