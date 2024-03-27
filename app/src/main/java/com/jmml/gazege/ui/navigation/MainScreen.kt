package com.jmml.gazege.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.NavPosition
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.data.sample
import com.jmml.gazege.ui.fragments.MainFragment
import com.jmml.gazege.ui.theme.AppMode
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.views.AddTransactionAction
import java.time.LocalDate

fun NavGraphBuilder.screenMain(
    viewModelMain: MainViewModel.ViewModelMain,
    viewModelCategoryList: MainViewModel.ViewModelCategoryList,
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
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit,
    onExportCategoryRequested: (Category) -> Unit,
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
                principalPersonSummaryState = principalPersonSummaryState,
                navPosition = navPosition,
                range = range,
                personFilterValue = personFilterValue,
                transactionFilters = transactionFilters,
                categoriesFilter = categoriesFiltersValue,
                valueFilterState = valueFilterState,
                snackbarHostState = snackbarHostState,
                today = today,
                delPerson = viewModelMain::deletePerson,
                delAccount = viewModelMain::deleteAccount,
                delTransaction = viewModelMain::deleteTransaction,
                delCategory = viewModelCategoryList::deleteCategory,
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
                onNavigateToAddCategory = onNavigateToAddCategory,
                onNavigateToEditCategory = onNavigateToEditCategory,
                onNavigateToAddBudget = onNavigateToAddBudget,
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
                onTransactionFiltersChanged = viewModelMain::updateTransactionFilters,
                onCategoriesFilterChanged = viewModelMain::updateCategoriasFiltersValue,
                descriptionFilterState = descriptionFilterState,
                onDescriptionFilterStateChanged = viewModelMain::updateDescriptionFilterValue,
                onValueFilterStateChanged = viewModelMain::updateValueFilterValue,
                onShowTypeChanged = viewModelCategoryList::updateShowType,
                onInitDatabaseSample = if (GazegeTheme.appMode == AppMode.DEBUG) {
                    {
                        sample(it, sampleModule)
                    }
                } else {
                    {}
                },
                onTodayChangeRequested = viewModelMain::updateToday,
                onExportCategoryRequested = onExportCategoryRequested,
                showVertical = showVertical,
                showType = viewModelCategoryList.rememberShowType().value,
                categoriasState = viewModelCategoryList.rememberEditarCategoriasState().value
            )
        }
    }
}

fun NavController.navigateoToMain() {
    navigate(
        "main",
        navOptions = navOptions {
            popUpTo(this@navigateoToMain.graph.id) { inclusive = true }
        }
    )
}