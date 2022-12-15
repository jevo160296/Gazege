package com.example.gazege.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.doubleToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.*
import java.util.*

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
                        amount=(index * trans_index).toDouble(),
                        description = "",
                        sourceId = 2,
                        destinationId = index,
                        date = Date()
                    )
                },
                outTransactions = (1..40).map{ trans_index ->
                    Transaction(
                        amount=(index * trans_index/(index + trans_index)).toDouble(),
                        description = "",
                        sourceId = index,
                        destinationId = 3,
                        date = Date()
                    )
                }
            )
        }
    }.flatten().sortedBy { it.account.id }
}

@Composable
private fun AccountViewHolder(account: AccountAndOwnerWithTransactions) {
    Row(verticalAlignment = Alignment.CenterVertically){
        Column(modifier = Modifier.weight(1F)) {
            Row {
                SmallEmphasis(text = "Account:", modifier = Modifier.padding(end = 4.dp))
                SmallBody(text = account.account.name)
            }
            Row {
                SmallEmphasis(text = "Owner:", modifier = Modifier.padding(end = 4.dp))
                SmallBody(text = account.owner.name)
            }

        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = "Total ")
            LargeBody(text = doubleToString(account.getTotal()))
        }
    }
}

@Composable
fun AccountRecyclerView(
    accountList: List<AccountAndOwnerWithTransactions>,
    onItemTapped: (AccountAndOwnerWithTransactions) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    state: LazyListState
) {
    RecyclerView(
        elements = accountList,
        onItemTapped = onItemTapped,
        modifier = modifier,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state
    ) {
        AccountViewHolder(account = it)
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
                amount=(1 * trans_index).toDouble(),
                description = "",
                sourceId = 2,
                destinationId = 1,
                date = Date()
            )
        },
        outTransactions = (1..40).map{ trans_index ->
            Transaction(
                amount=(1 * trans_index/(1 + trans_index)).toDouble(),
                description = "",
                sourceId = 1,
                destinationId = 3,
                date = Date()
            )
        }
    )
    AccountViewHolder(account = accountAndOwnerWithTransactions)
}

@Preview(showBackground = true, widthDp = 240, heightDp = 320)
@Composable
private fun PreviewAccountList() {
    GazegeTheme {
        AccountRecyclerView(
            accountList = getAccountSample(), onItemTapped = {},
            state = LazyListState()
        )
    }
}