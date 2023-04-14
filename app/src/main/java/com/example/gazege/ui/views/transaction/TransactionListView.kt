package com.example.gazege.ui.views.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.*
import java.time.LocalDate

@Composable
private fun TransactionViewHolder(
    transaction: TransactionAndAccountsAndCategory
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Row(modifier = Modifier.weight(1f)) {
                SmallEmphasis(text = "${stringResource(id = R.string.cuentas)}: ")
                SmallBody(text = transaction.sourceAccount.name)
                SmallEmphasis(text = " --> ")
                SmallBody(text = transaction.destinationAccount.name)
            }
            Row(modifier = Modifier.weight(1f)) {
                SmallEmphasis(text = "${stringResource(id = R.string.Categoria)}: ")
                SmallBody(
                    text = transaction.category?.name ?: stringResource(id = R.string.Sin_categoria)
                )
            }
            Row(modifier = Modifier.weight(2f)) {
                SmallEmphasis(text = "${stringResource(id = R.string.descripcion)}: ")
                SmallBody(text = transaction.transaction.description)
            }
        }
        Column(
            modifier = Modifier.align(Alignment.CenterVertically),
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = stringResource(id = R.string.Valor))
            LargeBody(text = doubleToMoneyString(transaction.transaction.amount))
        }
    }
}

@Composable
private fun TransactionRecyclerView(
    transactionList: List<TransactionAndAccountsAndCategory>,
    editTransaction: (TransactionAndAccountsAndCategory) -> Unit,
    delTransaction: (TransactionAndAccountsAndCategory) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    RecyclerView(
        elements = transactionList,
        onItemTapped = editTransaction,
        onItemLongPressed = delTransaction,
        modifier = modifier,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        groupSelector = {
            localDateToString(it.transaction.date, DateFormat.DAYMONTHYEAR)
        },
        viewHolder = {
            TransactionViewHolder(transaction = it)
        }
    ) {
        transactionLazyListItems(
            itemHolderPaddingValues,
            transactionList,
            editTransaction,
            delTransaction
        )
    }
}

@Composable
fun TransactionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    transactionList: List<TransactionAndAccountsAndCategory>,
    delTransaction: (Transaction) -> Unit,
    editTransaction: (Transaction) -> Unit,
    state: LazyListState,
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.transacciones))
    Column(modifier = modifier) {
        TransactionRecyclerView(
            transactionList = transactionList,
            editTransaction = { transactionAndAccounts ->
                editTransaction(transactionAndAccounts.transaction)
            },
            delTransaction = { transactionAndAccounts ->
                delTransaction(transactionAndAccounts.transaction)
            },
            itemHolderPaddingValues = itemHolderPaddingValues,
            state = state
        )
    }
}

fun LazyListScope.transactionLazyListItems(
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    transactionList: List<TransactionAndAccountsAndCategory>,
    editTransaction: (TransactionAndAccountsAndCategory) -> Unit,
    delTransaction: (TransactionAndAccountsAndCategory) -> Unit,
    colorSelector: @Composable (TransactionAndAccountsAndCategory) -> CardColors = { CardDefaults.cardColors() },
    groupSelector: (TransactionAndAccountsAndCategory) -> String = {
        localDateToString(it.transaction.date, DateFormat.DAYMONTHYEAR)
    }
) = itemsGrouped(
    itemHolderPaddingValues,
    transactionList,
    editTransaction,
    delTransaction,
    colorSelector,
    groupSelector,
) {
    TransactionViewHolder(transaction = it)
}

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionItem() {
    val person = Person(name = "Person", id = 0)
    val sourceAccount = Account(
        name = "Account1", ownerId = person.id ?: -1
    )
    val destinationAccount = Account(
        name = "Account2", ownerId = person.id ?: -1
    )
    val categoria = Category(
        id = 0, name = "Categoría", parentId = null
    )
    val transaction = Transaction(
        amount = 0.0, description = "Trans", date = LocalDate.now(),
        destinationId = destinationAccount.id ?: -1, sourceId = sourceAccount.id ?: -1,
        aNombreDe = null, categoryId = 0
    )
    val transactionAndAccounts = TransactionAndAccountsAndCategory(
        transaction = transaction, sourceAccount = sourceAccount,
        destinationAccount = destinationAccount, category = categoria
    )
    GazegeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            TransactionViewHolder(transaction = transactionAndAccounts)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionList() {
    DatabaseSample {
        TransactionRecyclerView(
            transactionList = transactionsAndAccountAndCategorySample,
            editTransaction = {},
            delTransaction = {},
            state = LazyListState()
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun PreviewTransactionPage() {
    DatabaseSample {
        GazegeTheme(darkTheme = true) {
            TransactionPage(
                transactionList = transactionsAndAccountAndCategorySample,
                state = LazyListState(),
                editTransaction = {},
                delTransaction = {},
                onTitleSetted = {}
            )
        }
    }
}