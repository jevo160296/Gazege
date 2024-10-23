package com.jmml.gazege.ui.views.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.ITransactionListDetailGrouped
import com.jmml.gazege.core.entities.ITransactionListDetailIndividual
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.core.entities.TransactionListItemDetailsWithAccount
import com.jmml.gazege.core.entities.TransactionListItemDetailsWithSign
import com.jmml.gazege.core.entities.TransactionType
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.DateFormat
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.localDateToString
import com.jmml.gazege.ui.templates.ClickableListItemViewHolder
import com.jmml.gazege.ui.templates.GroupedLazyList
import com.jmml.gazege.ui.templates.itemsGrouped
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.DefaultGroupViewHolder
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.LargeEmphasis
import com.jmml.gazege.ui.widgets.PulsatingCard
import com.jmml.gazege.ui.widgets.SmallEmphasis

@Composable
fun <T : ITransactionListDetailGrouped> LoadedTransactionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    transactionList: List<T>,
    delTransaction: (TransactionWithDetails) -> Unit,
    editTransaction: (TransactionWithDetails) -> Unit,
    state: LazyListState,
    nestedScrollConnection: NestedScrollConnection? = null,
    onTitleSetted: (String) -> Unit,
    onZeroElementsChanged: (Boolean) -> Unit,
    onFirstElementVisibleChanged: (isVisible: Boolean) -> Unit,
    dateViewHolder: @Composable (String) -> Unit = { TransactionHeaderViewHolder(it) },
    transactionViewHolder: @Composable (T) -> Unit = { transaction ->
        TransactionGroupItemViewHolder(
            transaction,
            { editTransaction(it.transaction) },
            { delTransaction(it.transaction) })
    }
) {
    LaunchedEffect(transactionList.isEmpty()) { onZeroElementsChanged(transactionList.isEmpty()) }
    val firstElementIsVisible by remember { derivedStateOf { state.firstVisibleItemIndex == 0 } }
    LaunchedEffect(firstElementIsVisible) { onFirstElementVisibleChanged(firstElementIsVisible) }
    onTitleSetted(stringResource(id = R.string.transacciones))
    Column(modifier = modifier) {
        LoadedTransactionRecyclerView(
            transactionList = transactionList,
            modifier = nestedScrollConnection?.let { Modifier.nestedScroll(nestedScrollConnection) }
                ?: Modifier,
            contentPadding = itemHolderPaddingValues,
            state = state,
            transactionViewHolder = transactionViewHolder,
            dateViewHolder = dateViewHolder
        )
    }
}

@Composable
fun LoadingTransactionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.transacciones))
    Column(modifier = modifier) {
        LoadingTransactionRecyclerView(
            contentPadding = itemHolderPaddingValues
        )
    }
}

fun transactionGroupSelector(transaction: TransactionWithDetails): String =
    localDateToString(transaction.date, DateFormat.DAYMONTHYEAR)

@Composable
fun TransactionHeaderViewHolder(group: String) = DefaultGroupViewHolder(group)

fun <T : ITransactionListDetailGrouped> LazyListScope.transactionLazyListItems(
    transactionList: List<T>,
    editTransaction: (T) -> Unit,
    delTransaction: (T) -> Unit,
    groupSelector: (T) -> String = { transactionGroupSelector(it.transaction) }
) = itemsGrouped(
    transactionList,
    groupSelector,
    groupViewHolder = { TransactionHeaderViewHolder(it) }
) {
    TransactionGroupItemViewHolder(it, editTransaction, delTransaction)
}

@Composable
fun TransactionViewHolder(
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
            text = transaction
                .categoriesString
                ?: stringResource(id = R.string.Sin_categoria)
        )
    }
    val descriptionRow: @Composable () -> Unit = @Composable {
        LargeEmphasis(text = "${stringResource(id = R.string.descripcion)}: ")
        LargeBody(text = transaction.transaction.descriptionString)
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
                LargeBody(text = doubleToMoneyString(transaction.transaction.totalAmount))
            }
        }
    }
}

@Composable
fun TransactionWithAccountViewHolder(
    transaction: TransactionListItemDetailsWithAccount
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
            text = transaction.categoriesString ?: stringResource(id = R.string.Sin_categoria)
        )
    }
    val descriptionRow: @Composable () -> Unit = @Composable {
        LargeEmphasis(text = "${stringResource(id = R.string.descripcion)}: ")
        LargeBody(text = transaction.transaction.descriptionString)
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
                LargeBody(
                    text = doubleToMoneyString(transaction.transaction.totalAmount),
                    color = when (transaction.transactionAccountType) {
                        TransactionType.INCOME -> GazegeTheme.gazegeColorScheme.income
                        TransactionType.OUTCOME -> GazegeTheme.gazegeColorScheme.outcome
                        TransactionType.TRANSFER -> MaterialTheme.colorScheme.onBackground
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionWithSignViewHolder(transaction: TransactionListItemDetailsWithSign) {
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
                LargeBody(
                    text = doubleToMoneyString(transaction.transaction.amount),
                    color = (transaction.transaction.amount * transaction.sign).let {
                        when {
                            it < 0 -> GazegeTheme.gazegeColorScheme.income
                            it > 0 -> GazegeTheme.gazegeColorScheme.outcome
                            else -> MaterialTheme.colorScheme.onBackground
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun <T : ITransactionListDetailGrouped> TransactionGroupItemViewHolder(
    transaction: T,
    editTransaction: (T) -> Unit,
    delTransaction: (T) -> Unit
) {
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    ClickableListItemViewHolder(
        onItemTapped = { editTransaction(transaction) },
        onItemLongPressed = { menuIdExpanded = transaction.transaction.transaction.id }
    ) {
        Box {
            when (transaction) {
                is TransactionListItemDetails -> TransactionViewHolder(transaction = transaction)
                is TransactionListItemDetailsWithAccount -> TransactionWithAccountViewHolder(
                    transaction = transaction
                )
                else -> throw Error("Transaction list type UI not implemented.")
            }
        }
        DropdownMenu(
            expanded = menuIdExpanded == transaction.transaction.transaction.id,
            onDismissRequest = { menuIdExpanded = null }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Editar)) },
                onClick = {
                    menuIdExpanded = null
                    editTransaction(transaction)
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Eliminar)) },
                onClick = {
                    menuIdExpanded = null
                    delTransaction(transaction)
                }
            )
        }
    }
}

@Composable
fun <T : ITransactionListDetailIndividual> TransactionGroupItemViewHolder(
    transaction: T,
    editTransaction: (T) -> Unit,
    delTransaction: (T) -> Unit
) {
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    ClickableListItemViewHolder(
        onItemTapped = { editTransaction(transaction) },
        onItemLongPressed = { menuIdExpanded = transaction.transaction.transaction.id }
    ) {
        Box {
            when (transaction) {
                is TransactionListItemDetailsWithSign -> TransactionWithSignViewHolder(transaction = transaction)
                else -> throw Error("Transaction list type UI not implemented.")
            }
        }
        DropdownMenu(
            expanded = menuIdExpanded == transaction.transaction.transaction.id,
            onDismissRequest = { menuIdExpanded = null }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Editar)) },
                onClick = {
                    menuIdExpanded = null
                    editTransaction(transaction)
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.Eliminar)) },
                onClick = {
                    menuIdExpanded = null
                    delTransaction(transaction)
                }
            )
        }
    }
}

@Composable
private fun <T : ITransactionListDetailGrouped> LoadedTransactionRecyclerView(
    transactionList: List<T>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: LazyListState,
    dateViewHolder: @Composable (String) -> Unit,
    transactionViewHolder: @Composable (T) -> Unit
) = GroupedLazyList(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
    items = transactionList,
    groupSelector = { transactionGroupSelector(it.transaction) },
    groupViewHolder = dateViewHolder,
    itemViewHolder = transactionViewHolder
)

@Composable
private fun LoadingTransactionRecyclerView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) = GroupedLazyList(
    modifier = modifier,
    state = LazyListState(),
    contentPadding = contentPadding,
    items = (0..10).toList(),
    groupSelector = { "" },
    groupViewHolder = {
        PulsatingCard(
            modifier = Modifier
                .width(90.dp)
                .height(18.dp)
                .clip(shape = MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.scrim,
            minAlpha = 0.0F,
            maxAlpha = 0.2F
        )
    }
) {
    PulsatingCard(
        modifier
            .height(94.dp)
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium),
        color = MaterialTheme.colorScheme.scrim,
        minAlpha = 0.0F,
        maxAlpha = 0.2F
    )
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
        LoadedTransactionRecyclerView(
            transactionList = transactionListItemDetailsSample,
            state = LazyListState(),
            transactionViewHolder = { TransactionGroupItemViewHolder(it, {}, {}) },
            dateViewHolder = { TransactionHeaderViewHolder(it) }
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun PreviewTransactionPage() {
    DatabaseSample {
        GazegeTheme(darkTheme = true) {
            LoadedTransactionPage(
                transactionList = transactionListItemDetailsSample,
                state = LazyListState(),
                editTransaction = {},
                delTransaction = {},
                onTitleSetted = {},
                onZeroElementsChanged = {},
                onFirstElementVisibleChanged = {}
            )
        }
    }
}