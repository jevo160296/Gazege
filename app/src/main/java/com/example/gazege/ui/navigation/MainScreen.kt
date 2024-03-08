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
    viewModelMain: MainViewModel.ViewModelMain,
    sampleModule: MainViewModel.SampleModule,
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
        val allPerson by viewModelMain.rememberAllPerson()
        val accountAndOwnerWithTransactions by viewModelMain.rememberAccountAndOwnerWithTransactions()
        val filteredTransactionListItemDetails by viewModelMain.rememberFilteredTransactionListItemDetails()
        val principalPersonSummaryState by viewModelMain.rememberPersonSummaryState()
        val range by viewModelMain.rememberRange()
        val transactionFilters by viewModelMain.rememberTransactionFiltersValue()
        val categoriesFiltersValue by viewModelMain.rememberCategoriesFiltersValue()
        val personFilterValue by viewModelMain.rememberPersonFilterValue()
        val valueFilterState by viewModelMain.rememberValueFilterValue()
        val descriptionFilterState by viewModelMain.rememberDescriptionFilterValue()
        val today by viewModelMain.rememberToday()

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
                delPerson = viewModelMain::deletePerson,
                delAccount = viewModelMain::deleteAccount,
                delTransaction = viewModelMain::deleteTransaction,
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
                    viewModelMain.updateRange(
                        startDate,
                        endDate
                    )
                },
                onSettingsClicked = onNavigateToSettings,
                onSaldoActualClick = onNavigateToSaldoActualSettings,
                onPersonFilterValueChanged = viewModelMain::updatePersonFilterValue,
                showVertical = showVertical,
                principalPersonSummaryState = principalPersonSummaryState,
                drawerState = drawerState,
                onOpenCategoriesRequested = onNavigateToCategories,
                onOpenBudgetRequested = onNavigateToBudget,
                transactionFilters = transactionFilters,
                onTransactionFiltersChanged = viewModelMain::updateTransactionFilters,
                categoriesFilter = categoriesFiltersValue,
                onCategoriesFilterChanged = viewModelMain::updateCategoriasFiltersValue,
                onInitDatabaseSample = if (GazegeTheme.appMode == AppMode.DEBUG) {
                    {
                        sample(it, sampleModule)
                    }
                } else {
                    {}
                },
                valueFilterState = valueFilterState,
                onValueFilterStateChanged = viewModelMain::updateValueFilterValue,
                descriptionFilterState = descriptionFilterState,
                onDescriptionFilterStateChanged = viewModelMain::updateDescriptionFilterValue,
                onTodayChangeRequested = viewModelMain::updateToday,
                today = today
            )
        }
    }
}