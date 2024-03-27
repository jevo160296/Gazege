package com.jmml.gazege.ui.views.account

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.MainViewModel.Companion.applyCategoriesFilter
import com.jmml.gazege.MainViewModel.Companion.applyDescriptionFilter
import com.jmml.gazege.MainViewModel.Companion.applyIncomeFilter
import com.jmml.gazege.MainViewModel.Companion.applyOutcomeFilter
import com.jmml.gazege.MainViewModel.Companion.applyTransferFilter
import com.jmml.gazege.NavPosition
import com.jmml.gazege.R
import com.jmml.gazege.core.dao.AccountDao
import com.jmml.gazege.core.dateBetween
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.plot.Plot
import com.jmml.gazege.plot.PlotDataFromTransactions
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.accountDeleitionConfirmationBuilder
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.templates.DynamicAddEntityFAB
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.transactionDeleitionConfirmationBuilder
import com.jmml.gazege.ui.views.AccountAction
import com.jmml.gazege.ui.views.AddTransactionAction
import com.jmml.gazege.ui.views.BottomSheetController
import com.jmml.gazege.ui.views.EntityDetail
import com.jmml.gazege.ui.views.TransactionAction
import com.jmml.gazege.ui.views.transaction.transactionLazyListItems
import com.jmml.gazege.ui.widgets.BooleanFilters
import com.jmml.gazege.ui.widgets.DataView
import com.jmml.gazege.ui.widgets.DoubleFilter
import com.jmml.gazege.ui.widgets.Filter
import com.jmml.gazege.ui.widgets.INCOME_FILTER
import com.jmml.gazege.ui.widgets.LargeEmphasis
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.OUTCOME_FILTER
import com.jmml.gazege.ui.widgets.TRANSFER_FILTER
import com.jmml.gazege.ui.widgets.TextFilter
import com.jmml.gazege.ui.widgets.booleanFilterOf
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AccountDetailData(
    val account: AccountAndOwner,
    val total: Double,
    val childrenTotal: Double,
    val allTransactions: List<TransactionListItemDetails>,
    val inTransactions: List<TransactionListItemDetails>,
    val outTransactions: List<TransactionListItemDetails>,
    val allAccounts: List<Account>,
    val allCategories: List<Category>,
    val budget: List<Budget>,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val principalPerson: Person?
) {
    val allTransactionsListItemDetails: List<TransactionListItemDetails> = allTransactions

    val expensesPlotDataFromTransactions: PlotDataFromTransactions =
        PlotDataFromTransactions(outTransactions)
    val incomePlotDataFromTransactions: PlotDataFromTransactions =
        PlotDataFromTransactions(inTransactions)
    val flowPlotDataFromTransactions: PlotDataFromTransactions = PlotDataFromTransactions(
        listOf(
            *inTransactions.toTypedArray(),
            *outTransactions
                .map {
                    it.copy(transaction = it.transaction.copy(amount = -it.transaction.amount))
                }
                .toTypedArray()
        ))

    companion object {
        suspend fun build(
            account: AccountAndOwnerWithTransactionsAndPockets,
            allAccounts: List<Account>,
            allCategories: List<Category>,
            budget: List<Budget>,
            startDate: LocalDate?,
            endDate: LocalDate?,
            principalPerson: Person?,
            transactionFilters: BooleanFilters<String, Nothing>,
            categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
            descriptionFilter: TextFilter
        ): AccountDetailData {
            return AccountDetailData(
                account = AccountAndOwner(
                    account.accountAndOwnerWithTransactions.account,
                    account.accountAndOwnerWithTransactions.owner
                ),
                total = AccountDao.getTotal(
                    account.accountAndOwnerWithTransactions,
                    startDate,
                    endDate
                ),
                childrenTotal = AccountDao.getChildrenTotal(account, startDate, endDate),
                allAccounts = allAccounts,
                allCategories = allCategories,
                budget = budget,
                startDate = startDate,
                endDate = endDate,
                principalPerson = principalPerson,
                allTransactions = account
                    .allTransactionsWithPocketTransactions
                    .sortedByDescending { it.date }
                    .filter { dateBetween(it.date, startDate, endDate) }
                    .let {
                        TransactionListItemDetails.from(
                            it,
                            allCategories,
                            allAccounts,
                            principalPerson?.id
                        )
                    }
                    .applyIncomeFilter(transactionFilters[INCOME_FILTER])
                    .applyOutcomeFilter(transactionFilters[OUTCOME_FILTER])
                    .applyTransferFilter(transactionFilters[TRANSFER_FILTER])
                    .applyCategoriesFilter(categoriesFilter)
                    .applyDescriptionFilter(descriptionFilter),
                inTransactions = account
                    .allInTransactionsWithInPocketTransactions
                    .sortedByDescending { it.date }
                    .filter { dateBetween(it.date, startDate, endDate) }
                    .let {
                        TransactionListItemDetails.from(
                            it,
                            allCategories,
                            allAccounts,
                            principalPerson?.id
                        )
                    },
                outTransactions = account
                    .allOutTransactionsWithOutPocketTransactions
                    .sortedByDescending { it.date }
                    .filter { dateBetween(it.date, startDate, endDate) }
                    .let {
                        TransactionListItemDetails.from(
                            it,
                            allCategories,
                            allAccounts,
                            principalPerson?.id
                        )
                    }
            )
        }
    }
}

@Composable
fun AccountDetail(
    accountAndOwner: AccountAndOwner,
    data: AccountDetailData?,
    showGraphs: Boolean,
    onShowGraphsChanged: (Boolean) -> Unit,
    onAction: (account: Account, action: AccountAction) -> Unit,
    fabExpanded: Boolean,
    onFabExpandedChanged: (Boolean) -> Unit,
    onAddTransactionRequested: (AddTransactionAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit,
    filters: BooleanFilters<String, Nothing>,
    onFiltersChanged: (newFilters: BooleanFilters<String, Nothing>) -> Unit,
    categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
    onCategoriesFilterChanged: (newFilters: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    descriptionFilterState: TextFilter,
    onDescriptionFilterStateChanged: (TextFilter) -> Unit
) {
    var innerShowGraphs by remember {
        mutableStateOf(showGraphs)
    }
    val showLoadingScreen = data == null || data.account.account.id != accountAndOwner.account.id
    Crossfade(targetState = showLoadingScreen, label = "") {
        if (it) {
            NullAccountDetail(accountAndOwner)
        } else {
            NotNullAccountDetail(
                data = data!!,
                onAction = onAction,
                onTransactionAction = onTransactionAction,
                showGraphs = showGraphs,
                switchEnabled = innerShowGraphs,
                onShowGraphsChanged = {
                    innerShowGraphs = it
                    onShowGraphsChanged(it)
                },
                dynamicFabEnabled = true,
                fabExpanded = fabExpanded,
                onFabExpandedChanged = onFabExpandedChanged,
                onAddTransactionRequested = onAddTransactionRequested,
                filters = filters,
                onFiltersChanged = onFiltersChanged,
                categoriesFilter = categoriesFilter,
                onCategoriesFilterChanged = onCategoriesFilterChanged,
                descriptionFilterState = descriptionFilterState,
                onDescriptionFilterStateChanged = onDescriptionFilterStateChanged
            )
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun NotNullAccountDetail(
    data: AccountDetailData,
    showGraphs: Boolean,
    switchEnabled: Boolean,
    dynamicFabEnabled: Boolean,
    onShowGraphsChanged: (Boolean) -> Unit,
    onAction: (account: Account, action: AccountAction) -> Unit,
    fabExpanded: Boolean,
    onFabExpandedChanged: (Boolean) -> Unit,
    onAddTransactionRequested: (AddTransactionAction) -> Unit,
    filters: BooleanFilters<String, Nothing>,
    onFiltersChanged: (newFilters: BooleanFilters<String, Nothing>) -> Unit,
    categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
    onCategoriesFilterChanged: (newFilters: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    descriptionFilterState: TextFilter,
    onDescriptionFilterStateChanged: (TextFilter) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    val total = data.total
    val childrenTotal = data.childrenTotal
    val allTransactionsAndAccountsAndCategory = data.allTransactionsListItemDetails
    val accountWithPockets = data.account
    val account = accountWithPockets

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var modalController: BottomSheetController? by remember {
        mutableStateOf(null)
    }
    EntityDetail(
        modalController = modalController,
        title = stringResource(id = R.string.cuenta) +
                " ${account.account.name}",
        onEditClick = {
            onAction(
                account.account,
                AccountAction.EDIT
            )
        },
        onDeleteClick = {
            modalController = BottomSheetController(
                getMsg = {
                    val accountName = account.account.name
                    accountDeleitionConfirmationBuilder()(accountName)
                },
                action = {
                    onAction(
                        account.account,
                        AccountAction.DELETE
                    )
                }
            )
            scope.launch {
                sheetState.show()
            }
        },
        sheetState = sheetState,
        floatingActionButton = {
            if (dynamicFabEnabled) {
                DynamicAddEntityFAB(
                    fabExpanded = fabExpanded,
                    onFabExpandedChanged = onFabExpandedChanged,
                    navPosition = NavPosition.TRANSACCIONES,
                    onAddPersonRequested = { },
                    onAddAccountRequested = { },
                    onAddCategoryRequested = { },
                    onAddTransactionRequested = onAddTransactionRequested
                )
            }
        }
    ) {
        LargeEmphasis(
            text =
            stringResource(id = R.string.Propietario) +
                    " ${account.owner.name}",
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
        )
        Filter(
            modifier = Modifier.fillMaxWidth(),
            dateFilterVisible = false,
            startDate = null,
            endDate = null,
            onRangeChanged = { _, _ -> },
            transactionsFilterVisible = true,
            transactionFilters = filters,
            onTransactionFiltersChanged = onFiltersChanged,
            categoriesFilter = categoriesFilter,
            onCategoriesFilterChanged = onCategoriesFilterChanged,
            valueFilterState = DoubleFilter(0.0f..0.0f, 0.0f..0.0f),
            onValueFilterStateChanged = {},
            descriptionFilterState = descriptionFilterState,
            onDescriptionFilterStateChanged = onDescriptionFilterStateChanged
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            DataView(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.total),
                value = doubleToMoneyString(total),
                enabled = false
            )
            DataView(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.TotalConBolsillos),
                value = doubleToMoneyString(total + childrenTotal),
                enabled = false
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding)),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            Switch(
                checked = switchEnabled,
                onCheckedChange = onShowGraphsChanged,
                thumbContent = if (switchEnabled) {
                    @Composable {
                        Icon(
                            modifier = Modifier
                                .size(SwitchDefaults.IconSize),
                            painter = painterResource(id = R.drawable.ic_round_check_24),
                            contentDescription = "Check",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                } else {
                    null
                }
            )
            LargeEmphasis(text = stringResource(id = R.string.MostrarGraficos))
        }
        LazyColumn(
            contentPadding =
            dimensionResource(id = R.dimen.DefaultPadding).let {
                PaddingValues(
                    top = it,
                    start = it,
                    end = it,
                    bottom = dimensionResource(id = R.dimen.FABDefaultSpace)
                )
            }
        ) {
            if (showGraphs) {
                item(contentType = "plotTitle") {
                    LargeEmphasis(text = stringResource(id = R.string.Gastos))
                }
                item(contentType = "plot") {
                    Plot(data.expensesPlotDataFromTransactions)
                }
                item(contentType = "plotTitle") {
                    LargeEmphasis(text = stringResource(id = R.string.Ingresos))
                }
                item(contentType = "plot") {
                    Plot(data.incomePlotDataFromTransactions)
                }
                item(contentType = "plotTitle") {
                    LargeEmphasis(
                        text =
                        "${stringResource(id = R.string.Flujo)} (" +
                                "${stringResource(id = R.string.Ingresos)} -" +
                                "${stringResource(id = R.string.Gastos)})"
                    )
                }
                item(contentType = "plot") {
                    Plot(data.flowPlotDataFromTransactions)
                }
            }
            stickyHeader(contentType = "transactionsTitle") {
                MediumHeadline(text = stringResource(id = R.string.transacciones))
            }
            transactionLazyListItems(
                transactionList = allTransactionsAndAccountsAndCategory,
                editTransaction = { onTransactionAction(it.transaction, TransactionAction.EDIT) },
                delTransaction = {
                    modalController = BottomSheetController(
                        getMsg = {
                            transactionDeleitionConfirmationBuilder()()
                        },
                        action = {
                            onTransactionAction(it.transaction, TransactionAction.DELETE)
                        }
                    )
                    scope.launch { sheetState.show() }
                }
            )
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun NullAccountDetail(
    account: AccountAndOwner
) {
    NotNullAccountDetail(
        AccountDetailData(
            AccountAndOwner(
                account.account,
                account.owner
            ),
            0.0,
            0.0,
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            null,
            null,
            null
        ),
        onAction = { _, _ -> },
        onTransactionAction = { _, _ -> },
        showGraphs = false,
        onShowGraphsChanged = {},
        switchEnabled = false,
        fabExpanded = false,
        onAddTransactionRequested = {},
        onFabExpandedChanged = {},
        dynamicFabEnabled = false,
        filters = booleanFilterOf(emptyList()),
        onFiltersChanged = {},
        categoriesFilter = booleanFilterOf(emptyList()),
        onCategoriesFilterChanged = {},
        descriptionFilterState = TextFilter(null),
        onDescriptionFilterStateChanged = {}
    )
}


@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
private fun AccountDetailPreview() {
    val snackBackState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    var accountDetailData by remember { mutableStateOf<AccountDetailData?>(null) }
    DatabaseSample {
        GazegeTheme {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackBackState)
                }
            ) {
                Box(Modifier.padding(it)) {
                    LaunchedEffect(key1 = Unit) {
                        scope.launch {
                            accountDetailData = AccountDetailData.build(
                                accountAndOwnerWithTransactionsAndPocketsSample.first(),
                                accountSample,
                                categorieSample,
                                budgetSample,
                                startDateSample,
                                endDateSample,
                                personSample.first(),
                                booleanFilterOf(
                                    listOf(
                                        INCOME_FILTER, TRANSFER_FILTER, OUTCOME_FILTER
                                    )
                                ),
                                booleanFilterOf(emptyList()),
                                descriptionFilter = TextFilter(null)
                            )
                        }
                    }
                    AccountDetail(
                        accountAndOwner = accountAndOwnerSample.first(),
                        data = accountDetailData,
                        onAction = { account, action ->
                            scope.launch {
                                snackBackState.showSnackbar(
                                    when (action) {
                                        AccountAction.EDIT -> "Edit ${account.name}"
                                        AccountAction.DELETE -> "Delete ${account.name}"
                                    }
                                )
                            }
                        },
                        onTransactionAction = { transaction, action ->
                            scope.launch {
                                snackBackState.showSnackbar(
                                    when (action) {
                                        TransactionAction.EDIT -> "Edit ${transaction.date}"
                                        TransactionAction.DELETE -> "Delete ${transaction.date}"
                                    }
                                )
                            }
                        },
                        showGraphs = false,
                        onShowGraphsChanged = {},
                        onAddTransactionRequested = {},
                        onFabExpandedChanged = {},
                        fabExpanded = false,
                        filters = booleanFilterOf(emptyList()),
                        onFiltersChanged = {},
                        categoriesFilter = booleanFilterOf(emptyList()),
                        onCategoriesFilterChanged = {},
                        descriptionFilterState = TextFilter(null),
                        onDescriptionFilterStateChanged = {}
                    )
                }
            }
        }
    }
}