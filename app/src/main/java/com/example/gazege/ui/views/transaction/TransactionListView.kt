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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionListItemDetails
import com.example.gazege.core.entities.TransactionType
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.templates.ClickableListItemViewHolder
import com.example.gazege.ui.templates.GroupedLazyList
import com.example.gazege.ui.templates.itemsGrouped
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.DefaultGroupViewHolder
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.LargeEmphasis
import com.example.gazege.ui.widgets.SmallEmphasis

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

@Composable
private fun TransactionViewHolder(
    transaction: TransactionListItemDetails
) {
    val iconText: @Composable (icon: Painter, text: String, color: Color) -> Unit =
        { icon, text, color ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.DefaultPadding))
            ) {
                Icon(painter = icon, contentDescription = "Icon", tint = color)
                SmallEmphasis(text = text, color = color)
            }
        }
    val tipoRow: @Composable () -> Unit = @Composable {
        when (transaction.transactionType) {
            TransactionType.INCOME -> iconText(
                painterResource(R.drawable.ingreso_icon),
                stringResource(R.string.Ingreso),
                GazegeTheme.gazegeColorScheme.income
            )

            TransactionType.OUTCOME -> iconText(
                painterResource(R.drawable.gasto_icon),
                stringResource(R.string.Gasto),
                GazegeTheme.gazegeColorScheme.outcome
            )

            TransactionType.TRANSFER -> iconText(
                painterResource(R.drawable.transfer_icon),
                stringResource(R.string.Transferencia),
                GazegeTheme.gazegeColorScheme.transfer
            )
        }
    }
    val accountRow: @Composable () -> Unit = @Composable {
        LargeEmphasis(text = "${stringResource(id = R.string.cuentas)}: ")
        if (transaction.sourceAccount.isIncome.not()) {
            LargeBody(text = transaction.sourceAccount.name, maxLines = 1)
        }
        if (transaction.sourceAccount.isIncome.not() && transaction.destinationAccount.isOutcome.not()) {
            LargeEmphasis(text = " --> ", maxLines = 1)
        }
        if (transaction.destinationAccount.isOutcome.not()) {
            LargeBody(text = transaction.destinationAccount.name, maxLines = 1)
        }
    }
    val categoryRow: @Composable () -> Unit = @Composable {
        LargeEmphasis(text = "${stringResource(id = R.string.Categoria)}: ")
        LargeBody(
            text = transaction.category?.name ?: stringResource(id = R.string.Sin_categoria)
        )
    }
    val descriptionRow: @Composable () -> Unit = @Composable {
        LargeEmphasis(text = "${stringResource(id = R.string.descripcion)}: ")
        LargeBody(text = transaction.transaction.description)
    }

    Column {
        tipoRow()
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.7f),
                verticalArrangement = Arrangement.Top
            ) {
                Row(modifier = Modifier) { accountRow() }
                Row(modifier = Modifier) { categoryRow() }
                Row(modifier = Modifier) { descriptionRow() }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.3f),
                horizontalAlignment = Alignment.End
            ) {
                LargeBody(text = doubleToMoneyString(transaction.transaction.amount))
            }
        }
    }
}

@Composable
private fun TransactionGroupItemViewHolder(
    transaction: TransactionListItemDetails,
    editTransaction: (TransactionListItemDetails) -> Unit,
    delTransaction: (TransactionListItemDetails) -> Unit
) = ClickableListItemViewHolder(
    onItemTapped = { editTransaction(transaction) },
    onItemLongPressed = { delTransaction(transaction) }
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

@Preview(showBackground = true)
@Composable
private fun PreviewTransactionItem() {
    DatabaseSample {
        GazegeTheme {
            Box(Modifier.background(MaterialTheme.colorScheme.background)) {
                TransactionViewHolder(transaction = transactionListItemDetailsSample.first())
            }
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