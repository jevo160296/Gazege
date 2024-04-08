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

        val accountFilterValue by viewModelAccountDetail.rememberAccountFilterValue(
            accountId = accountId
        )
        val categories by viewModelAccountDetail.rememberCategoriesWithSubcategories()
        var categoriesFilter by viewModelAccountDetail.rememberCategoriesFilter(
            accountId = accountId,
            categories = categories
        )
        val descriptionFilter by viewModelAccountDetail.rememberDescriptionFilter(
            accountId = accountId
        )
        val data by viewModelAccountDetail.rememberAccountDetailData(
            accountId,
            accountFilterValue,
            categoriesFilter,
            descriptionFilter
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
                onAction = { actionAccount, action ->
                    when (action) {
                        AccountAction.EDIT -> onNavigateToEditAccount(accountId)
                        AccountAction.DELETE -> {
                            onNavigateUp()
                            viewModelAccountDetail.deleteAccount(actionAccount)
                        }
                    }
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
                filters = accountFilterValue,
                onFiltersChanged = viewModelAccountDetail::updateAccountFilter,
                categoriesFilter = categoriesFilter,
                onCategoriesFilterChanged = {
                    categoriesFilter = it
                },
                descriptionFilterState = descriptionFilter,
                onDescriptionFilterStateChanged = viewModelAccountDetail::updateDescriptionFilter
            )
        } else {
            Text("Cuenta vacía")
        }
    }
}

fun NavController.navigateToAccountDetail(accountId: Int?) {
    navigate("accountDetail/$accountId")
}