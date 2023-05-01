package com.example.gazege.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Account
import com.example.gazege.ui.views.AccountAction
import com.example.gazege.ui.views.AddTransactionAction
import com.example.gazege.ui.views.TransactionAction
import com.example.gazege.ui.views.account.AccountDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

fun NavGraphBuilder.screenAccountDetail(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEditAccount: (Int?) -> Unit,
    onNavigateToAddTransaction: (LocalDate, AddTransactionAction, Account) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit
) {
    composable(
        "accountDetail/{accountId}",
        arguments = listOf(
            navArgument("accountId") {
                type = NavType.IntType
            }
        )
    ) { navStack ->
        val accountId = navStack.arguments?.getInt("accountId")
        var incomeFilter by rememberSaveable { mutableStateOf(true) }
        var outcomeFilter by rememberSaveable { mutableStateOf(true) }
        var transferFilter by rememberSaveable { mutableStateOf(true) }
        val data by viewModel.rememberAccountDetailData(
            accountId,
            incomeFilter,
            outcomeFilter,
            transferFilter
        )
        var fabExpanded by remember { mutableStateOf(false) }

        val accountAndOwner by viewModel.rememberAccountAndOwner()

        val coroutineScope = rememberCoroutineScope()

        val account = accountAndOwner
            .firstOrNull { it.account.id == accountId }
        if (account != null) {
            var showGraphs by remember {
                mutableStateOf(false)
            }
            AccountDetail(
                accountAndOwner = account,
                data = data,
                onAction = { actionAccount, action ->
                    when (action) {
                        AccountAction.EDIT -> onNavigateToEditAccount(accountId)
                        AccountAction.DELETE -> {
                            onNavigateUp()
                            viewModel.deleteAccount(actionAccount)
                        }
                    }
                },
                onTransactionAction = { transaction, action ->
                    val transactionId = transaction.id
                    when (action) {
                        TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                        TransactionAction.DELETE -> viewModel.deleteTransaction(
                            transaction
                        )
                    }
                },
                showGraphs = showGraphs,
                onShowGraphsChanged = {
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            showGraphs = it
                        }
                    }
                },
                fabExpanded = fabExpanded,
                onFabExpandedChanged = { fabExpanded = it },
                onAddTransactionRequested = {
                    onNavigateToAddTransaction(
                        LocalDate.now(),
                        it,
                        account.account
                    )
                },
                incomeFilterValue = incomeFilter,
                outcomeFilterValue = outcomeFilter,
                transferFilterValue = transferFilter,
                onIncomeFilterValueChanged = { incomeFilter = it },
                onTransferFilterValueChanged = { transferFilter = it },
                onOutcomeFilterValueChanged = { outcomeFilter = it }
            )
        } else {
            Text("Cuenta vacía")
        }
    }
}

fun NavController.navigateToAccountDetail(accountId: Int?) {
    navigate("accountDetail/$accountId")
}