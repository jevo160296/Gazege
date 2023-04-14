package com.example.gazege.ui.views.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.dao.AccountDao
import com.example.gazege.core.entities.*
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.person.getPersonSample
import com.example.gazege.ui.widgets.Card
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallEmphasis
import com.example.gazege.ui.widgets.treeview.*
import java.time.LocalDate
import kotlin.random.Random

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
private fun AccountTreeView(
    accountList: List<AccountAndOwnerWithTransactions>,
    delAccount: ((AccountAndOwnerWithTransactions) -> Unit)?,
    editAccount: ((AccountAndOwnerWithTransactions) -> Unit)?,
    detailAccount: (AccountAndOwnerWithTransactions) -> Unit,
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    treeState: TreeState,
    colorSelector: @Composable (AccountAndOwnerWithTransactions) -> CardColors = { CardDefaults.cardColors() },
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
    RecyclerTreeView(
        nodes = nodes,
        treeState = treeState,
        itemHolderPaddingValues = itemHolderPaddingValues
    ) { node, scope ->
        val isExpanded = scope.isExpanded(node)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(node.level.dp * 8))
            if (node.children.isNotEmpty()) {
                IconToggleButton(
                    modifier = Modifier.width(42.dp),
                    checked = isExpanded,
                    onCheckedChange = { scope.toggleExpanded(node) }
                ) {
                    DefaultTreeLeadingIcon(isExpanded = isExpanded)
                }
            } else {
                Spacer(
                    Modifier
                        .width(42.dp)
                        .height(42.dp)
                )
            }
            Card(
                modifier = modifier.padding(vertical = 4.dp),
                onClick = { detailAccount(node.content) },
                onLongClick = { menuIdExpanded = node.content.account.id },
                colors = colorSelector(node.content)
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
    colorSelector: @Composable (AccountAndOwnerWithTransactions) -> CardColors = { CardDefaults.cardColors() },
    onTitleSetted: (String) -> Unit
) {
    onTitleSetted(stringResource(id = R.string.cuentas))
    Column(modifier = modifier) {
        AccountTreeView(
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
            colorSelector = colorSelector,
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

fun getAccountSample(personSample: List<Person>): List<Account> {
    var index = 0
    val random = Random(3)
    return personSample
        .flatMap {
            when (it.name) {
                "Pablo" -> Pair(
                    0, (0..100).map {
                        val pIndex = index++
                        val hasParent = random.nextBoolean()
                        val parentId = if (hasParent && pIndex > 0) {
                            random.nextInt(pIndex)
                        } else {
                            null
                        }
                        Triple(pIndex, "Cuenta $pIndex", parentId)
                    }
                )
                "Banco" -> Pair(
                    1, listOf(
                        Triple(index++, "Banco", null)
                    )
                )
                "Petunia" -> Pair(
                    2, listOf(
                        Triple(index++, "Petunia", null)
                    )
                )
                "Hortensia" -> Pair(
                    3, listOf(
                        Triple(index++, "Hortensia", null)
                    )
                )
                "__ESPECIAL__" -> Pair(
                    4, listOf(
                        Triple(index++, "__INGRESOS__", null),
                        Triple(index++, "__GASTOS__", null)
                    )
                )
                else -> null
            }
                .let { pair ->
                    if (pair != null) {
                        val ownerId = pair.first
                        val accounts = pair.second
                        accounts.map { triple ->
                            val isIncome = triple.second == "__INGRESOS__"
                            val isOutcome = triple.second == "__GASTOS__"
                            Account(
                                triple.first,
                                triple.second,
                                ownerId,
                                triple.third,
                                isIncome = isIncome,
                                isOutcome = isOutcome
                            )
                        }
                    } else {
                        listOf()
                    }
                }
        }
}

fun getAccountAndOwnerWithTransactionsSample(
    accountSample: List<Account>,
    personSample: List<Person>
): List<AccountAndOwnerWithTransactions> {
    var index = 0
    val random = Random(3)
    val accounts: List<Account> = accountSample
    val transactions: List<Transaction> = (0..500).map {
        val from = personSample
            .let {
                val selected = random.nextInt(it.size)
                it[selected].id
            }
        val to = personSample
            .filter { it.id != from }
            .let {
                val selected = random.nextInt(it.size)
                it[selected].id
            }
        val sourceAccount = accounts
            .filter { it.ownerId == from }
            .let {
                val selected = random.nextInt(it.size)
                it[selected].id
            }
        val destinationAccount = accounts
            .filter { it.ownerId == to }
            .let {
                val selected = random.nextInt(it.size)
                it[selected].id
            }
        Transaction(
            index++,
            random.nextDouble(100.0, 500000.0),
            description = "",
            sourceId = sourceAccount ?: 0,
            destinationId = destinationAccount ?: 1,
            date = LocalDate.now(),
            aNombreDe = null,
            categoryId = null
        )
    }
    return AccountAndOwnerWithTransactions.from(accounts, personSample, transactions)
}

fun getAccountAndOwnerWithTransactionsAndPocketsSample(
    accountAndOwnerWithTransactionsSample: List<AccountAndOwnerWithTransactions>
): List<AccountAndOwnerWithTransactionsAndPockets> {
    return AccountAndOwnerWithTransactionsAndPockets.from(
        accountAndOwnerWithTransactionsSample
    )
}

@Preview(showBackground = true, widthDp = 240, heightDp = 320)
@Composable
private fun PreviewAccountList() {
    GazegeTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            val personSample = getPersonSample()
            val accountSample = getAccountSample(personSample)
            AccountRecyclerView(
                accountList = getAccountAndOwnerWithTransactionsSample(accountSample, personSample),
                delAccount = {},
                editAccount = {},
                state = LazyListState()
            ) { acc ->
                DefaultAccountViewHolder(
                    account = acc, startDate = null, endDate = null,
                    accounts = listOf(), isExpanded = false
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 240, heightDp = 320)
@Composable
private fun PreviewAccountTreeView() {
    val personSample = getPersonSample()
    val accountSample = getAccountSample(personSample)
    val accountAndOwnerWithTransactions =
        getAccountAndOwnerWithTransactionsSample(accountSample, personSample)
    val treeState = rememberTreeState()
    GazegeTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        ) {
            AccountTreeView(
                accountList = accountAndOwnerWithTransactions,
                delAccount = {},
                editAccount = {},
                detailAccount = {},
                treeState = treeState
            ) { acc ->
                DefaultAccountViewHolder(
                    account = acc.content,
                    startDate = null,
                    endDate = null,
                    accounts = accountAndOwnerWithTransactions,
                    isExpanded = acc.expanded(treeState.expandedItems)
                )
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
    GazegeTheme(darkTheme = false) {
        val personSample = getPersonSample()
        val accountSample = getAccountSample(personSample)
        val accountAndOwnerWithTransactions =
            getAccountAndOwnerWithTransactionsSample(accountSample, personSample)

        AccountPage(
            accountList = accountAndOwnerWithTransactions,
            treeState = rememberTreeState(),
            editAccount = {},
            delAccount = {},
            startDate = null,
            endDate = null,
            detailAccount = {}
        ) {}
    }
}