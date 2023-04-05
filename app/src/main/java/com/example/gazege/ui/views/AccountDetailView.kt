package com.example.gazege.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.dateBetween
import com.example.gazege.core.entities.*
import com.example.gazege.core.firstDayOfMonth
import com.example.gazege.ui.accountDeleitionConfirmationBuilder
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.transactionDeleitionConfirmationBuilder
import com.example.gazege.ui.widgets.DataView
import com.example.gazege.ui.widgets.LargeEmphasis
import com.example.gazege.ui.widgets.MediumHeadline
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

data class AccountDetailData constructor(
    val account: AccountAndOwnerWithTransactionsAndPockets,
    val allAccounts: List<Account>,
    val allCategories: List<Category>,
    val startDate: LocalDate?,
    val endDate: LocalDate?
) {
    val total = AccountDao.getTotal(account.accountAndOwnerWithTransactions, startDate, endDate)
    val chilrenTotal = AccountDao.getChildrenTotal(account, startDate, endDate)
    private val allTransactions = account
        .allTransactionsWithPocketTransactions
        .sortedByDescending { it.date }
        .filter { dateBetween(it.date, startDate, endDate) }
    private val outTransactions = account
        .allOutTransactionsWithOutPocketTransactions
        .sortedByDescending { it.date }
        .filter { dateBetween(it.date, startDate, endDate) }
    val allTransactionsAndAccountsAndCategory: List<TransactionAndAccountsAndCategory> =
        TransactionAndAccountsAndCategory.from(
            allTransactions,
            allAccounts,
            allCategories
        )

    val plotData: PlotData = PlotData(outTransactions)

    companion object {
        fun build(
            account: AccountAndOwnerWithTransactionsAndPockets,
            allAccounts: List<Account>,
            allCategories: List<Category>,
            startDate: LocalDate?,
            endDate: LocalDate?,
        ): AccountDetailData {
            return AccountDetailData(
                account = account,
                allAccounts = allAccounts,
                allCategories = allCategories,
                startDate = startDate,
                endDate = endDate
            )
        }
    }
}

data class PlotData(
    val transactions: List<Transaction>
) {
    val maxDate = transactions.maxOfOrNull { it.date }
    val minDate = transactions.minOfOrNull { it.date }
    val monthSpan = if (maxDate != null && minDate != null) {
        Period.between(minDate, maxDate).toTotalMonths()
    } else {
        null
    }
    val groupedGastos = listOf(
        *transactions.toTypedArray(),
        *if (minDate != null && maxDate != null) {
            generateSequence(
                seedFunction = {
                    Transaction(
                        null,
                        0.0,
                        "",
                        -1,
                        -1,
                        null,
                        minDate,
                        null
                    )
                },
                nextFunction = { trx ->
                    val newDate = trx.date.plusDays(1)
                    if (newDate <= maxDate) {
                        Transaction(
                            null,
                            0.0,
                            "",
                            -1,
                            -1,
                            null,
                            newDate,
                            null
                        )
                    } else {
                        null
                    }
                }
            ).toList().toTypedArray()
        } else {
            arrayOf()
        })
        .groupBy {
            if (monthSpan != null && monthSpan > 1) {
                firstDayOfMonth(it.date)
            } else {
                it.date
            }
        }
        .map { it.key to it.value.sumOf { trx -> trx.amount } }
        .let { listOf(*it.toTypedArray()) }
        .sortedBy { it.first }
        .mapIndexed { index, (date, y) ->
            Entry(date, index.toFloat(), y.toFloat())
        }
    val chartEntryModel = ChartEntryModelProducer(groupedGastos).getModel()
}

class Entry(
    val date: LocalDate,
    override val x: Float,
    override val y: Float
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = Entry(date, x, y)
}

@Composable
fun Plot(plotData: PlotData?) {
    if (plotData == null) {
        MediumHeadline("Null")
    } else {
        NotNullPlot(plotData)
    }
}

@Composable
fun NotNullPlot(
    data: PlotData
) {
    val chartEntryModel = data.chartEntryModel
    val horizontalAxisValueFormatter =
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, chartValues ->
            (chartValues.chartEntryModel.entries.first().getOrNull(value.toInt()) as? Entry)
                ?.date
                ?.run { "$dayOfMonth/$monthValue" }
                .orEmpty()
        }
    val verticalAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        doubleToString(value.toDouble())
    }
    Box(
        Modifier
            .height(130.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Chart(
            chart = lineChart(),
            model = chartEntryModel,
            bottomAxis = bottomAxis(valueFormatter = horizontalAxisValueFormatter),
            startAxis = startAxis(valueFormatter = verticalAxisValueFormatter)
        )
    }
}

@Composable
fun AccountDetail(
    accountAndOwnerWithTransactionsAndPockets: AccountAndOwnerWithTransactionsAndPockets,
    data: AccountDetailData?,
    onDataUpdateRequested: (account: AccountAndOwnerWithTransactionsAndPockets?) -> Unit,
    onAction: (account: Account, action: AccountAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    val account = accountAndOwnerWithTransactionsAndPockets
        .accountAndOwnerWithTransactions
        .let { AccountAndOwner(it.account, it.owner) }
    if (data == null) {
        NullAccountDetail(account)
    } else {
        NotNullAccountDetail(
            data = data,
            onAction = onAction,
            onTransactionAction = onTransactionAction
        )
    }
    LaunchedEffect(key1 = accountAndOwnerWithTransactionsAndPockets) {
        onDataUpdateRequested(accountAndOwnerWithTransactionsAndPockets)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotNullAccountDetail(
    data: AccountDetailData,
    onAction: (account: Account, action: AccountAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    val total = data.total
    val childrenTotal = data.chilrenTotal
    val allTransactionsAndAccountsAndCategory = data.allTransactionsAndAccountsAndCategory
    val accountWithPockets = data.account
    val account = accountWithPockets.accountAndOwnerWithTransactions
    val plotData = data.plotData

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
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
        sheetState = sheetState
    ) {
        LargeEmphasis(
            text =
            stringResource(id = R.string.Propietario) +
                    " ${account.owner.name}"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DataView(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.total),
                value = doubleToString(total),
                enabled = false
            )
            DataView(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.TotalConBolsillos),
                value = doubleToString(total + childrenTotal),
                enabled = false
            )
        }
        LargeEmphasis(text = stringResource(id = R.string.Gastos))
        Plot(plotData)
        MediumHeadline(text = stringResource(id = R.string.transacciones))
        TransactionPage(
            modifier = Modifier.navigationBarsPadding(),
            transactionList = allTransactionsAndAccountsAndCategory,
            delTransaction = {
                modalController = BottomSheetController(
                    getMsg = {
                        transactionDeleitionConfirmationBuilder()()
                    },
                    action = {
                        onTransactionAction(it, TransactionAction.DELETE)
                    }
                )
                scope.launch { sheetState.show() }
            },
            editTransaction = { onTransactionAction(it, TransactionAction.EDIT) },
            state = rememberLazyListState(),
            onTitleSetted = {}
        )
    }
}

@Composable
private fun NullAccountDetail(
    account: AccountAndOwner
) {
    NotNullAccountDetail(
        AccountDetailData(
            AccountAndOwnerWithTransactionsAndPockets.from(
                AccountAndOwnerWithTransactions(
                    account.account,
                    account.owner,
                    listOf(),
                    listOf()
                ),
                listOf()
            ),
            listOf(),
            listOf(),
            null,
            null
        ),
        onAction = { _, _ -> },
        onTransactionAction = { _, _ -> }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 300, heightDp = 600)
@Composable
private fun AccountDetailPreview() {
    val accounts = getAccountSample()
    val categories = getCategoriesSample()
    val account = accounts.let {
        AccountAndOwnerWithTransactionsAndPockets.from(it.first(), it)
    }
    val snackBackState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    GazegeTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBackState)
            }
        ) {
            Box(Modifier.padding(it)) {
                AccountDetail(
                    accountAndOwnerWithTransactionsAndPockets = account,
                    data = AccountDetailData(
                        account = account,
                        allAccounts = accounts.map { it.account },
                        allCategories = categories,
                        startDate = null,
                        endDate = null
                    ),
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
                    onDataUpdateRequested = {}
                )
            }
        }
    }
}