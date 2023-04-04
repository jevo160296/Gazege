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

class Entry(
    val date: LocalDate,
    override val x: Float,
    override val y: Float
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = Entry(date, x, y)
}

@Composable
fun Plot(transactions: List<Transaction>) {
    val maxDate = transactions.maxOfOrNull { it.date }
    val minDate = transactions.minOfOrNull { it.date }
    val dateRange = if (maxDate != null && minDate != null) {
        Period.between(minDate, maxDate).days
    } else {
        null
    }
    val groupedTransactions = transactions
        .sortedBy { it.date }
        .groupBy {
            if (dateRange != null && dateRange > 60) {
                firstDayOfMonth(it.date)
            } else {
                it.date
            }
        }
        .map {
            it.key to it.value.sumOf { trx -> trx.amount }
        }
        .mapIndexed { index, (date, y) ->
            Entry(date, index.toFloat(), y.toFloat())
        }
    val chartEntryModel = ChartEntryModelProducer(groupedTransactions).getModel()
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountDetail(
    account: AccountAndOwnerWithTransactionsAndPockets,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    onAction: (account: Account, action: AccountAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    val total = AccountDao.getTotal(account.accountAndOwnerWithTransactions, startDate, endDate)
    val childrenTotal = AccountDao.getChildrenTotal(account, startDate, endDate)
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var modalController: BottomSheetController? by remember {
        mutableStateOf(null)
    }
    EntityDetail(
        modalController = modalController,
        title = stringResource(id = R.string.cuenta) +
                " ${account.accountAndOwnerWithTransactions.account.name}",
        onEditClick = {
            onAction(
                account.accountAndOwnerWithTransactions.account,
                AccountAction.EDIT
            )
        },
        onDeleteClick = {
            modalController = BottomSheetController(
                getMsg = {
                    val accountName = account.accountAndOwnerWithTransactions.account.name
                    accountDeleitionConfirmationBuilder()(accountName)
                },
                action = {
                    onAction(
                        account.accountAndOwnerWithTransactions.account,
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
                    " ${account.accountAndOwnerWithTransactions.owner.name}"
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
        val transactions = listOf(
            *account.accountAndOwnerWithTransactions.inTransactions.toTypedArray(),
            *account.accountAndOwnerWithTransactions.outTransactions.toTypedArray()
        )
            .sortedByDescending { it.date }
            .filter { it.date >= startDate && it.date <= endDate }
        Plot(transactions)
        MediumHeadline(text = stringResource(id = R.string.transacciones))
        val transactionsAndAccountsAndCategory = TransactionAndAccountsAndCategory.from(
            transactions,
            allAccounts,
            allCategories
        )
        TransactionPage(
            transactionList = transactionsAndAccountsAndCategory,
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
                    account = account,
                    startDate = null,
                    endDate = null,
                    allAccounts = accounts.map { it.account },
                    allCategories = categories,
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
                    }
                )
            }
        }
    }
}