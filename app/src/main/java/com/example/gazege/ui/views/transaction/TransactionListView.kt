package com.example.gazege.ui.views.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionListItemDetails
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.templates.GroupedLazyList
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.ClickableCardViewHolder
import com.example.gazege.ui.widgets.DefaultGroupViewHolder
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.LargeEmphasis
import com.example.gazege.ui.widgets.SmallEmphasis
import com.example.gazege.ui.widgets.itemsGrouped
import java.time.LocalDate

@Composable
private fun TransactionViewHolder(
    transaction: TransactionListItemDetails
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f),
            verticalArrangement = Arrangement.Top
        ) {
            Row(modifier = Modifier) {
                LargeEmphasis(text = "${stringResource(id = R.string.cuentas)}: ")
                LargeBody(text = transaction.sourceAccount.name)
                LargeEmphasis(text = " --> ")
                LargeBody(text = transaction.destinationAccount.name)
            }
            Row(modifier = Modifier) {
                LargeEmphasis(text = "${stringResource(id = R.string.Categoria)}: ")
                LargeBody(
                    text = transaction.category?.name ?: stringResource(id = R.string.Sin_categoria)
                )
            }
            Row(modifier = Modifier) {
                LargeEmphasis(text = "${stringResource(id = R.string.descripcion)}: ")
                LargeBody(text = transaction.transaction.description)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(0.3f),
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = stringResource(id = R.string.Valor))
            LargeBody(text = doubleToMoneyString(transaction.transaction.amount))
        }
    }
}

@Composable
private fun TransactionGroupItemViewHolder(
    transaction: TransactionListItemDetails,
    editTransaction: (TransactionListItemDetails) -> Unit,
    delTransaction: (TransactionListItemDetails) -> Unit
) = ClickableCardViewHolder(
    onItemTapped = { editTransaction(transaction) },
    onItemLongPressed = { delTransaction(transaction) },
    colors = CardDefaults.cardColors()
) {
    TransactionViewHolder(transaction = transaction)
}

@Composable
private fun TransactionRecyclerView(
    transactionList: List<TransactionListItemDetails>,
    editTransaction: (TransactionListItemDetails) -> Unit,
    delTransaction: (TransactionListItemDetails) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: LazyListState
) = GroupedLazyList(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
    items = transactionList,
    groupSelector = { transactionGroupSelector(it.transaction) },
    groupViewHolder = { TransactionHeaderViewHolder(it) }
) {
    TransactionGroupItemViewHolder(it, editTransaction, delTransaction)
}

@Composable
fun TransactionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    transactionList: List<TransactionListItemDetails>,
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
            contentPadding = itemHolderPaddingValues,
            state = state
        )
    }
}

fun transactionGroupSelector(transaction: Transaction): String =
    localDateToString(transaction.date, DateFormat.DAYMONTHYEAR)

@Composable
fun TransactionHeaderViewHolder(group: String) = DefaultGroupViewHolder(group)

fun LazyListScope.transactionLazyListItems(
    transactionList: List<TransactionListItemDetails>,
    editTransaction: (TransactionListItemDetails) -> Unit,
    delTransaction: (TransactionListItemDetails) -> Unit,
    groupSelector: (TransactionListItemDetails) -> String = { transactionGroupSelector(it.transaction) }
) = itemsGrouped(
    transactionList,
    groupSelector,
    groupViewHolder = { TransactionHeaderViewHolder(it) }
) {
    TransactionGroupItemViewHolder(it, editTransaction, delTransaction)
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
    val transactionListItemDetails = TransactionListItemDetails(
        transaction = transaction,
        category = categoria,
        sourceAccount,
        destinationAccount
    )
    GazegeTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            TransactionViewHolder(transaction = transactionListItemDetails)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionList() {
    DatabaseSample {
        TransactionRecyclerView(
            transactionList = transactionListItemDetailsSample,
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
                transactionList = transactionListItemDetailsSample,
                state = LazyListState(),
                editTransaction = {},
                delTransaction = {},
                onTitleSetted = {}
            )
        }
    }
}