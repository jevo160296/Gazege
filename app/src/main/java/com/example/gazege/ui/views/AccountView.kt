package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallEmphasis
import java.time.LocalDate

fun getAccountSample(): List<AccountAndOwnerWithTransactions> {
    return getPersonSample().map { person ->
        listOf(1, 2, 3, 4, 5).map { index ->
            val account = Account(
                id = index,
                name = "Cuenta$index",
                ownerId = person.id ?: -1,
                initial_balance = (person.id ?: -1).toDouble() * 10 + index
            )
            AccountAndOwnerWithTransactions(
                account = account,
                owner = person,
                inTransactions = (1..100).map { trans_index ->
                    Transaction(
                        amount = (index * trans_index).toDouble(),
                        description = "",
                        sourceId = 2,
                        destinationId = index,
                        date = LocalDate.now()
                    )
                },
                outTransactions = (1..40).map { trans_index ->
                    Transaction(
                        amount = (index * trans_index / (index + trans_index)).toDouble(),
                        description = "",
                        sourceId = index,
                        destinationId = 3,
                        date = LocalDate.now()
                    )
                }
            )
        }
    }.flatten().sortedBy { it.account.id }
}

@Composable
private fun DefaultAccountViewHolder(
    account: AccountAndOwnerWithTransactions,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            SmallEmphasis(
                text = "${stringResource(id = R.string.cuenta)}:",
                modifier = Modifier.padding(end = 4.dp)
            )
            LargeBody(text = account.account.name)
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = "${stringResource(id = R.string.total)} ")
            LargeBody(text = doubleToString(account.getTotal(startDate, endDate)))
        }
    }
}

@Composable
private fun AccountRecyclerView(
    accountList: List<AccountAndOwnerWithTransactions>,
    delAccount: (AccountAndOwnerWithTransactions) -> Unit,
    editAccount: (AccountAndOwnerWithTransactions) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState,
    colorSelector: @Composable (AccountAndOwnerWithTransactions) -> CardColors = { CardDefaults.cardColors() },
    viewHolder: @Composable (AccountAndOwnerWithTransactions) -> Unit
) {
    RecyclerView(
        elements = accountList,
        onItemTapped = editAccount,
        onItemLongPressed = delAccount,
        modifier = modifier,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        colorSelector = colorSelector,
        viewHolder = viewHolder
    )
}

@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    accountList: List<AccountAndOwnerWithTransactions>,
    state: LazyListState,
    delAccount: (Account) -> Unit,
    editAccount: (Account) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    viewHolder: @Composable (AccountAndOwnerWithTransactions) -> Unit = {
        DefaultAccountViewHolder(
            account = it,
            startDate = startDate,
            endDate = endDate
        )
    },
    colorSelector: @Composable (AccountAndOwnerWithTransactions) -> CardColors = { CardDefaults.cardColors() },
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.cuentas))
    Column(modifier = modifier) {
        AccountRecyclerView(
            accountList = accountList,
            delAccount = { delAccount(it.account) },
            editAccount = { editAccount(it.account) },
            itemHolderPaddingValues = itemHolderPaddingValues,
            state = state,
            colorSelector = colorSelector,
            viewHolder = viewHolder
        )
    }
}

@Preview(showBackground = true, widthDp = 240)
@Composable
private fun PreviewAccountItem() {
    val owner = Person(id = 0, name = "Persona")
    val account = Account(name = "Cuenta 1", ownerId = 0, initial_balance = 0.0)
    val accountAndOwnerWithTransactions = AccountAndOwnerWithTransactions(
        owner = owner,
        account = account,
        inTransactions = (1..100).map { trans_index ->
            Transaction(
                amount = (1 * trans_index).toDouble(),
                description = "",
                sourceId = 2,
                destinationId = 1,
                date = LocalDate.now()
            )
        },
        outTransactions = (1..40).map { trans_index ->
            Transaction(
                amount = (1 * trans_index / (1 + trans_index)).toDouble(),
                description = "",
                sourceId = 1,
                destinationId = 3,
                date = LocalDate.now()
            )
        }
    )
    DefaultAccountViewHolder(
        account = accountAndOwnerWithTransactions,
        startDate = null,
        endDate = null
    )
}

@Preview(showBackground = true, widthDp = 240, heightDp = 320)
@Composable
private fun PreviewAccountList() {
    GazegeTheme {
        AccountRecyclerView(
            accountList = getAccountSample(),
            delAccount = {},
            editAccount = {},
            state = LazyListState()
        ) { acc -> DefaultAccountViewHolder(account = acc, startDate = null, endDate = null) }
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    heightDp = 640
)
@Composable
private fun PreviewPage() {
    GazegeTheme(darkTheme = false) {
        AccountPage(
            accountList = getAccountSample(),
            state = LazyListState(),
            editAccount = {},
            delAccount = {},
            startDate = null,
            endDate = null
        ) {}
    }
}