package com.example.gazege.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.*
import com.example.gazege.ui.accountDeleitionConfirmationBuilder
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.transactionDeleitionConfirmationBuilder
import com.example.gazege.ui.widgets.DataView
import com.example.gazege.ui.widgets.LargeEmphasis
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.ModalSheetContent
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BottomSheetController(
    val getMsg: @Composable () -> String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EntityDetail(
    modalController: BottomSheetController?,
    title: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    sheetState: ModalBottomSheetState,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalBottomSheetLayout(
        sheetContent = {
            ModalSheetContent(
                onSiClicked = {
                    modalController?.action?.let { it() }
                    scope.launch { sheetState.hide() }
                },
                onNoClicked = { scope.launch { sheetState.hide() } },
                titleText = stringResource(id = R.string.confirmar_eliminacion),
                bodyText = modalController?.getMsg?.invoke() ?: ""
            )
        },
        sheetState = sheetState
    ) {
        Column(
            Modifier.padding(dimensionResource(id = R.dimen.DefaultPadding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            TopAppBar(
                title = {
                    MediumHeadline(
                        text = title
                    )
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete"
                        )
                    }
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))) {
                content()
            }
        }
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
        Box(
            Modifier
                .border(BorderStroke(1.dp, Color.Blue))
                .height(120.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Plot")
        }
        MediumHeadline(text = stringResource(id = R.string.transacciones))
        val transactions = listOf(
            *account.accountAndOwnerWithTransactions.inTransactions.toTypedArray(),
            *account.accountAndOwnerWithTransactions.outTransactions.toTypedArray()
        )
            .sortedByDescending { it.date }
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