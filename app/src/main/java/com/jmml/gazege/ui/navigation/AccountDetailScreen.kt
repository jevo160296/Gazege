package com.jmml.gazege.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.ui.views.AccountAction
import com.jmml.gazege.ui.views.AddTransactionAction
import com.jmml.gazege.ui.views.TransactionAction
import com.jmml.gazege.ui.views.account.AccountDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

fun NavGraphBuilder.screenAccountDetail(
    viewModelAccountDetail: MainViewModel.ViewModelAccountDetail,
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

        val accountFilterValue =
            viewModelAccountDetail.rememberAccountFilterValue(accountId = accountId).value
        val categoriesFilter by viewModelAccountDetail.rememberCategoriesFilter(
            accountId = accountId
        )
        val descriptionFilter by viewModelAccountDetail.rememberDescriptionFilter(
            accountId = accountId
        )
        val valueFilter by viewModelAccountDetail.rememberAccountValueFilter(accountId = accountId)
        val data by viewModelAccountDetail.rememberAccountDetailData(
            accountId
        )
        var fabExpanded by remember { mutableStateOf(false) }

        val accountAndOwner by viewModelAccountDetail.rememberAccountAndOwner()

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
                showGraphs = showGraphs,
                onShowGraphsChanged = {
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            showGraphs = it
                        }
                    }
                },
                onAction = { actionAccount, action ->
                    when (action) {
                        AccountAction.EDIT -> onNavigateToEditAccount(accountId)
                        AccountAction.DELETE -> {
                            onNavigateUp()
                            viewModelAccountDetail.deleteAccount(actionAccount)
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
                onTransactionAction = { transaction, action ->
                    val transactionId = transaction.id
                    when (action) {
                        TransactionAction.EDIT -> onNavigateToEditTransaction(transactionId)
                        TransactionAction.DELETE -> viewModelAccountDetail.deleteTransaction(
                            transaction
                        )
                    }
                },
                filters = accountFilterValue,
                onFiltersChanged = viewModelAccountDetail::updateAccountFilter,
                categoriesFilter = categoriesFilter,
                onCategoriesFilterChanged = viewModelAccountDetail::updateCategoryFilter,
                descriptionFilterState = descriptionFilter,
                onDescriptionFilterStateChanged = viewModelAccountDetail::updateDescriptionFilter,
                valueFilter = valueFilter,
                onValueFilterChanged = viewModelAccountDetail::updateValueFilter,
                dateRange = viewModelAccountDetail.rememberDateRange().value,
                onDateRangeChange = viewModelAccountDetail::onDateRangeChange
            )
        } else {
            Text("Cuenta vacía")
        }
    }
}

fun NavController.navigateToAccountDetail(accountId: Int?) {
    navigate("accountDetail/$accountId")
}