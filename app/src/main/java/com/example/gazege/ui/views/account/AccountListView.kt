package com.example.gazege.ui.views.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.templates.ClickableTreeListItemViewHolder
import com.example.gazege.ui.templates.SelectableTreeListItemViewHolder
import com.example.gazege.ui.templates.SimpleTreeList
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.SmallEmphasis
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.NodeId
import com.example.gazege.ui.widgets.treeview.TreeState
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import java.time.LocalDate

@Composable
private fun DefaultAccountViewHolder(
    account: AccountAndOwnerWithTransactions,
    accounts: List<AccountAndOwnerWithTransactions>,
    isExpanded: Boolean,
    startDate: LocalDate?,
    endDate: LocalDate?
) {
    val total = if (isExpanded) {
        AccountDao.getTotal(account, startDate, endDate)
    } else {
        AccountDao.getTotal(account, startDate, endDate) +
                AccountDao.getChildrenTotal(
                    AccountAndOwnerWithTransactionsAndPockets.from(account, accounts),
                    startDate,
                    endDate
                )
    }
    val totalString = if (isExpanded) {
        stringResource(id = R.string.total)
    } else {
        stringResource(id = R.string.TotalConBolsillos)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            LargeBody(text = account.account.name)
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            SmallEmphasis(text = totalString)
            LargeBody(text = doubleToMoneyString(total))
        }
    }
}

data class AccountAndOwnerWithTransactionsNode(
    override val content: AccountAndOwnerWithTransactions,
    val accountList: List<AccountAndOwnerWithTransactions>,
    override val level: Int,
    override val relativeIndex: Int,
    override val parentId: NodeId?
) : Node<AccountAndOwnerWithTransactions, AccountAndOwnerWithTransactionsNode> {
    override val children: List<AccountAndOwnerWithTransactionsNode>
        get() {
            val pockets = AccountAndOwnerWithTransactionsAndPockets.from(
                content, accountList
            ).pockets
            return pockets.mapIndexed { index, it ->
                AccountAndOwnerWithTransactionsNode(
                    it.accountAndOwnerWithTransactions,
                    accountList,
                    level + 1,
                    index,
                    this.id()
                )
            }
        }
}

@Composable
private fun AccountClickableTreeView(
    accountList: List<AccountAndOwnerWithTransactions>,
    delAccount: ((AccountAndOwnerWithTransactions) -> Unit)?,
    editAccount: ((AccountAndOwnerWithTransactions) -> Unit)?,
    detailAccount: (AccountAndOwnerWithTransactions) -> Unit,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    treeState: TreeState,
    viewHolder: @Composable (AccountAndOwnerWithTransactionsNode) -> Unit
) {
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    val nodes = accountList
        .filter {
            it.account.parentId == null
        }
        .mapIndexed { index, it ->
            AccountAndOwnerWithTransactionsNode(
                it,
                accountList,
                0,
                index,
                null
            )
        }
    SimpleTreeList(
        state = treeState,
        contentPadding = itemHolderPaddingValues,
        nodes = nodes
    ) { node, scope ->
        ClickableTreeListItemViewHolder(
            showExpandIcon = node.children.isNotEmpty(),
            isExpanded = scope.isExpanded(node),
            onIsExpandedChanged = { scope.toggleExpanded(node) },
            level = node.level,
            onItemTapped = { detailAccount(node.content) },
            onItemLongPressed = { menuIdExpanded = node.content.account.id }
        ) {
            Box(
                Modifier.padding(8.dp)
            ) {
                viewHolder(node)
                if (editAccount != null || delAccount != null) {
                    DropdownMenu(
                        expanded = menuIdExpanded == node.content.account.id,
                        onDismissRequest = { menuIdExpanded = null }
                    ) {
                        if (editAccount != null) {
                            DropdownMenuItem(
                                text = { Text(text = "Edit") },
                                onClick = {
                                    menuIdExpanded = null
                                    editAccount(node.content)
                                })
                        }
                        if (delAccount != null) {
                            DropdownMenuItem(
                                text = { Text(text = "Delete") },
                                onClick = {
                                    menuIdExpanded = null
                                    delAccount(node.content)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSelectableTreeView(
    accountList: List<AccountAndOwnerWithTransactions>,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    treeState: TreeState,
    onAccountStateChanged: (account: Account, nuevoEstado: Boolean) -> Unit,
    viewHolder: @Composable (AccountAndOwnerWithTransactionsNode) -> Unit
) {
    val nodes = accountList
        .filter {
            it.account.parentId == null
        }
        .mapIndexed { index, it ->
            AccountAndOwnerWithTransactionsNode(
                it,
                accountList,
                0,
                index,
                null
            )
        }
    SimpleTreeList(
        state = treeState,
        contentPadding = itemHolderPaddingValues,
        nodes = nodes
    ) { node, scope ->
        SelectableTreeListItemViewHolder(
            level = node.level,
            showExpandIcon = node.children.isNotEmpty(),
            isExpanded = scope.isExpanded(node),
            onIsExpandedChanged = { scope.toggleExpanded(node) },
            state = if (node.content.account.includedInTotal) {
                ToggleableState.On
            } else {
                ToggleableState.Off
            },
            onSelectionClick = {
                onAccountStateChanged(
                    node.content.account,
                    node.content.account.includedInTotal.not()
                )
            }
        ) {
            viewHolder(node)
        }
    }
}

@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    accountList: List<AccountAndOwnerWithTransactions>,
    treeState: TreeState,
    delAccount: ((Account) -> Unit)?,
    editAccount: ((Account) -> Unit)?,
    detailAccount: (Account) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    viewHolder: @Composable (AccountAndOwnerWithTransactionsNode) -> Unit = {
        DefaultAccountViewHolder(
            account = it.content,
            accounts = accountList,
            startDate = startDate,
            endDate = endDate,
            isExpanded = it.expanded(treeState.expandedItems)
        )
    },
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.cuentas))
    Column(modifier = modifier) {
        AccountClickableTreeView(
            accountList = accountList,
            delAccount = if (delAccount != null) {
                { delAccount(it.account) }
            } else {
                null
            },
            editAccount = if (editAccount != null) {
                { editAccount(it.account) }
            } else {
                null
            },
            detailAccount = { detailAccount(it.account) },
            itemHolderPaddingValues = itemHolderPaddingValues,
            treeState = treeState,
            viewHolder = viewHolder
        )
    }
}

@Composable
fun AccountSelectionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    accountList: List<AccountAndOwnerWithTransactions>,
    treeState: TreeState,
    onAccountStateChanged: (account: Account, nuevoEstado: Boolean) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    viewHolder: @Composable (AccountAndOwnerWithTransactionsNode) -> Unit = {
        DefaultAccountViewHolder(
            account = it.content,
            accounts = accountList,
            startDate = startDate,
            endDate = endDate,
            isExpanded = it.expanded(treeState.expandedItems)
        )
    }
) {
    Column(modifier = modifier) {
        AccountSelectableTreeView(
            accountList = accountList,
            treeState = treeState,
            onAccountStateChanged = onAccountStateChanged,
            itemHolderPaddingValues = itemHolderPaddingValues,
            viewHolder = viewHolder
        )
    }
}

@Preview(showBackground = true, widthDp = 240)
@Composable
private fun PreviewAccountItem() {
    val owner = Person(id = 0, name = "Persona")
    val account = Account(name = "Cuenta 1", ownerId = 0)
    val accountAndOwnerWithTransactions = AccountAndOwnerWithTransactions(
        owner = owner,
        account = account,
        inTransactions = (1..100).map { trans_index ->
            Transaction(
                amount = (1 * trans_index).toDouble(),
                description = "",
                sourceId = 2,
                destinationId = 1,
                date = LocalDate.now(),
                aNombreDe = null,
                categoryId = null
            )
        },
        outTransactions = (1..40).map { trans_index ->
            Transaction(
                amount = (1 * trans_index / (1 + trans_index)).toDouble(),
                description = "",
                sourceId = 1,
                destinationId = 3,
                date = LocalDate.now(),
                aNombreDe = null,
                categoryId = null
            )
        }
    )
    DefaultAccountViewHolder(
        account = accountAndOwnerWithTransactions,
        startDate = null,
        endDate = null,
        accounts = listOf(),
        isExpanded = false
    )
}

@Preview(showBackground = true, widthDp = 240, heightDp = 320)
@Composable
private fun PreviewAccountTreeView() {
    val treeState = rememberTreeState()
    DatabaseSample {
        GazegeTheme {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AccountClickableTreeView(
                    accountList = accountAndOwnerWithTransactionsSample,
                    delAccount = {},
                    editAccount = {},
                    detailAccount = {},
                    treeState = treeState
                ) { acc ->
                    DefaultAccountViewHolder(
                        account = acc.content,
                        startDate = null,
                        endDate = null,
                        accounts = accountAndOwnerWithTransactionsSample,
                        isExpanded = acc.expanded(treeState.expandedItems)
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    heightDp = 640
)
@Composable
private fun PreviewPage() {
    DatabaseSample {
        GazegeTheme(darkTheme = false) {
            AccountPage(
                accountList = accountAndOwnerWithTransactionsSample,
                treeState = rememberTreeState(),
                editAccount = {},
                delAccount = {},
                startDate = null,
                endDate = null,
                detailAccount = {}
            ) {}
        }
    }
}