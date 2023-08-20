package com.example.gazege.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.NavPosition
import com.example.gazege.sample.data.sample
import com.example.gazege.ui.fragments.MainFragment
import com.example.gazege.ui.theme.AppMode
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.AddTransactionAction
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.screenMain(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToEditPerson: (Int?) -> Unit,
    onNavigateToPersonDetail: (Int?) -> Unit,
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditAccount: (Int?) -> Unit,
    onNavigateToAccountDetail: (Int?) -> Unit,
    onNavigateToAddTransaction: (date: LocalDate, action: AddTransactionAction) -> Unit,
    onNavigateToEditTransaction: (Int?) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSaldoActualSettings: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable("main") {
        val allPerson by viewModel.rememberAllPerson()
        val accountAndOwnerWithTransactions by viewModel.rememberAccountAndOwnerWithTransactions()
        val filteredTransactionListItemDetails by viewModel.rememberFilteredTransactionListItemDetails()
        val principalPersonSummaryState by viewModel.rememberPersonSummaryState()
        val range by viewModel.rememberRange()
        val transactionFilters by viewModel.rememberTransactionFiltersValue()
        val categoriesFiltersValue by viewModel.rememberCategoriesFiltersValue()
        val personFilterValue by viewModel.rememberPersonFilterValue()
        val valueFilterState by viewModel.rememberValueFilterValue()
        val descriptionFilterState by viewModel.rememberDescriptionFilterValue()

        var navPosition: NavPosition by rememberSaveable {
            mutableStateOf(NavPosition.TRANSACCIONES)
        }
        val sheetState = rememberModalBottomSheetState()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val snackbarHostState = SnackbarHostState()

        val dataLoaded = filteredTransactionListItemDetails is LoadedTransactionDetailsState

        LaunchedEffect(key1 = dataLoaded) {
            if (dataLoaded) {
                onDataLoaded()
            }
        }

        BoxWithConstraints {
            val showVertical = maxWidth <= 700.dp
            MainFragment(
                allPerson = allPerson,
                accountList = accountAndOwnerWithTransactions,
                filteredTransactionList = filteredTransactionListItemDetails,
                navPosition = navPosition,
                range = range,
                personFilterValue = personFilterValue,
                sheetState = sheetState,
                snackbarHostState = snackbarHostState,
                delPerson = viewModel::deletePerson,
                delAccount = viewModel::deleteAccount,
                delTransaction = viewModel::deleteTransaction,
                onAddPersonRequested = onNavigateToAddPerson,
                onEditPersonRequested = { onNavigateToEditPerson(it.id) },
                onPersonDetailRequested = { onNavigateToPersonDetail(it.id) },
                onAddAccountRequested = onNavigateToAddAccount,
                onEditAccountRequested = { onNavigateToEditAccount(it.id) },
                onAccountDetailRequested = { onNavigateToAccountDetail(it.id) },
                onAddTransactionRequested = {
                    val startDate = range.first
                    val esMesActual =
                        range.first?.withDayOfMonth(1) == LocalDate.now()
                            .withDayOfMonth(1)
                    val esMesPosterior =
                        startDate != null &&
                                startDate.withDayOfMonth(1) > LocalDate.now()
                            .withDayOfMonth(1)
                    val date = if (esMesActual || startDate == null) LocalDate.now()
                    else if (esMesPosterior) startDate.withDayOfMonth(1) else
                        startDate.withDayOfMonth(1).plusMonths(1L)
                            .minusDays(1L)
                    onNavigateToAddTransaction(date, it)
                },
                onEditTransactionRequested = { onNavigateToEditTransaction(it.id) },
                onNavStatusChanged = { navPosition = it },
                onRangeChanged = { startDate, endDate ->
                    viewModel.updateRange(
                        startDate,
                        endDate
                    )
                },
                onSettingsClicked = onNavigateToSettings,
                onSaldoActualClick = onNavigateToSaldoActualSettings,
                onPersonFilterValueChanged = viewModel::updatePersonFilterValue,
                showVertical = showVertical,
                principalPersonSummaryState = principalPersonSummaryState,
                drawerState = drawerState,
                onOpenCategoriesRequested = onNavigateToCategories,
                onOpenBudgetRequested = onNavigateToBudget,
                transactionFilters = transactionFilters,
                onTransactionFiltersChanged = viewModel::updateTransactionFilters,
                categoriesFilter = categoriesFiltersValue,
                onCategoriesFilterChanged = viewModel::updateCategoriasFiltersValue,
                onInitDatabaseSample = if (GazegeTheme.appMode == AppMode.DEBUG) {
                    {
                        sample(it, viewModel)
                    }
                } else {
                    {}
                },
                valueFilterState = valueFilterState,
                onValueFilterStateChanged = viewModel::updateValueFilterValue,
                descriptionFilterState = descriptionFilterState,
                onDescriptionFilterStateChanged = viewModel::updateDescriptionFilterValue
            )
        }
    }
}