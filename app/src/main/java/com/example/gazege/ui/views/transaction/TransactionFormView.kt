package com.example.gazege.ui.views.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.AccountAndOwnerWithPockets
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.PartialTransactionAndAccounts
import com.example.gazege.ui.views.account.AccountDropDownMenu
import com.example.gazege.ui.views.category.CategoryDropDown
import com.example.gazege.ui.widgets.*
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.NodeId
import java.time.LocalDate

data class AccountAndOwnerNode(
    override val content: AccountAndOwner,
    val accountList: List<AccountAndOwner>,
    override val level: Int,
    override val relativeIndex: Int,
    val deactivatedAccountList: List<AccountAndOwner>,
    override val parentId: NodeId?,
) : Node<AccountAndOwner, AccountAndOwnerNode> {
    val isActive: Boolean get() = this.content !in deactivatedAccountList
    override val children: List<AccountAndOwnerNode>
        get() {
            val pockets = AccountAndOwnerWithPockets.from(
                content, accountList
            ).pockets
            return pockets.mapIndexed { index, it ->
                AccountAndOwnerNode(
                    it.accountAndOwner,
                    accountList = accountList,
                    level + 1,
                    index,
                    deactivatedAccountList = deactivatedAccountList,
                    this.id()
                )
            }
        }

    companion object {
        fun from(
            accountList: List<AccountAndOwner>,
            deactivatedAccountList: List<AccountAndOwner>
        ): List<AccountAndOwnerNode> {
            return accountList.filter {
                it.account.parentId == null
            }.mapIndexed { index, it ->
                AccountAndOwnerNode(
                    it,
                    accountList,
                    0,
                    index,
                    deactivatedAccountList = deactivatedAccountList,
                    null
                )
            }
        }
    }
}

@Composable
fun TransactionAndAccountsForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: PartialTransactionAndAccounts,
    accountList: List<AccountAndOwner>,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsChanged: (PartialTransactionAndAccounts) -> Unit,
    defaultDate: LocalDate = LocalDate.now(),
    realizarANombreDe: Boolean,
    onRealizarANombreDeChanged: (Boolean) -> Unit,
    personList: List<Person>,
    onRealizarAnombreDeIdChanged: (Int?) -> Unit,
    categoryList: List<Category>,
    onDoneAction: () -> Unit,
    isComplete: Boolean,
    showSourceAccountField: Boolean = true,
    showDestinationAccountField: Boolean = true,
    onDateChanged: (LocalDate) -> Unit
) {
    val amount = transactionAndAccounts.transaction.amount ?: SignedBigDecimal.ZERO
    val description = transactionAndAccounts.transaction.description ?: ""
    val selectedSourceId = transactionAndAccounts.sourceAccount?.id
    val selectedDestinationId = transactionAndAccounts.destinationAccount?.id
    val date: LocalDate = transactionAndAccounts.transaction.date ?: defaultDate
    val selectedCategoryId = transactionAndAccounts.transaction.categoryId
    if (transactionAndAccounts.transaction.date == null) {
        onTransactionAndAccountsChanged(
            transactionAndAccounts.copy().apply {
                transaction = transaction.copy(date = date)
            }
        )
    }
    val nextAction: ImeAction = if (isComplete) {
        ImeAction.Done
    } else {
        ImeAction.Next
    }
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        NumberField(
            value = amount,
            onValueChange = {
                onTransactionAndAccountsChanged(
                    transactionAndAccounts.copy().apply {
                        transaction = transaction.copy(amount = it)
                    }
                )
            },
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = nextAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDoneAction() }
            )
        )
        TextField(
            value = description,
            onValueChange = {
                onTransactionAndAccountsChanged(
                    transactionAndAccounts.copy().apply {
                        transaction = transaction.copy(description = it)
                    }
                )
            },
            label = { Text(text = "Description") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = nextAction,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDoneAction() }
            )
        )
        val selectedSource = accountList.firstOrNull { it.account.id == selectedSourceId }
        val selectedDestination = accountList.firstOrNull { it.account.id == selectedDestinationId }
        val deactivatedSourceAccountList = accountList.filter {
            it.account.id == selectedDestination?.account?.id
        }
        val deactivatedDestinationAccountList = accountList.filter {
            it.account.id == selectedSource?.account?.id
        }
        val selectedSourceNode = selectedSource?.let {
            AccountAndOwnerNode(
                selectedSource,
                accountList,
                0,
                0,
                deactivatedAccountList = deactivatedSourceAccountList,
                null
            )
        }
        val selectedDestinationNode = selectedDestination?.let {
            AccountAndOwnerNode(
                selectedDestination,
                accountList,
                0,
                0,
                deactivatedAccountList = deactivatedDestinationAccountList,
                null
            )
        }
        if (showSourceAccountField) {
            if (accountList.isNotEmpty()) {
                AccountDropDownMenu(
                    accountsList = accountList,
                    selectedAccountNode = selectedSourceNode,
                    label = { Text("Source account") },
                    onItemClick = {
                        if (it.content.account.id != null) {
                            onTransactionAndAccountsChanged(
                                transactionAndAccounts.copy().apply {
                                    sourceAccount = it.content.account
                                    transaction = transaction.copy(sourceId = it.content.account.id)
                                }
                            )
                        }
                    },
                    deactivatedAccountList = deactivatedSourceAccountList,
                    canClearSelection = false,
                    onClearSelectionClicked = {},
                    keyboardOptions = KeyboardOptions(imeAction = nextAction),
                    keyboardActions = KeyboardActions(
                        onDone = { onDoneAction() }
                    )
                )
            } else {
                ButtonField(onClick = onAccountAddRequested) {
                    Text("New account")
                }
            }
        }
        if (showDestinationAccountField) {
            if (accountList.isNotEmpty()) {
                AccountDropDownMenu(
                    accountsList = accountList,
                    selectedAccountNode = selectedDestinationNode,
                    onItemClick = {
                        if (it.content.account.id != null) {
                            onTransactionAndAccountsChanged(
                                transactionAndAccounts.copy().apply {
                                    destinationAccount = it.content.account
                                    transaction =
                                        transaction.copy(destinationId = it.content.account.id)
                                }
                            )
                        }
                    },
                    label = { Text("Destination account") },
                    deactivatedAccountList = deactivatedDestinationAccountList,
                    canClearSelection = false,
                    onClearSelectionClicked = {},
                    keyboardOptions = KeyboardOptions(imeAction = nextAction),
                    keyboardActions = KeyboardActions(
                        onDone = { onDoneAction() }
                    )
                )
            } else {
                ButtonField(onClick = onAccountAddRequested) {
                    Text("New account")
                }
            }
        }

        DatePicker(
            value = date,
            onValueChange = {
                onDateChanged(it)
            }
        )
        val selectedCategory = categoryList.firstOrNull { it.id == selectedCategoryId }
        if (categoryList.isNotEmpty()) {
            CategoryDropDown(
                categoryList = categoryList,
                selectedCategory = selectedCategory,
                label = { Text(stringResource(id = R.string.Categoria)) },
                keyboardOptions = KeyboardOptions(imeAction = nextAction),
                keyboardActions = KeyboardActions(
                    onDone = { onDoneAction() }
                )
            ) {
                onTransactionAndAccountsChanged(
                    transactionAndAccounts.copy().apply {
                        transaction = transaction.copy(categoryId = it?.id)
                    }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = realizarANombreDe, onCheckedChange = {
                if (!it) {
                    onRealizarAnombreDeIdChanged(null)
                }
                onRealizarANombreDeChanged(it)
            })
            Text(text = "Realizar a nombre de otra persona")
        }
        if (realizarANombreDe) {
            var dropDownExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            val selectedItem =
                personList.firstOrNull { it.id == transactionAndAccounts.transaction.aNombreDe }
            DropDownMenu(
                dropDownExpanded = dropDownExpanded,
                onExpandedChange = { dropDownExpanded = it },
                options = personList,
                selectedItem = selectedItem,
                itemToString = { it?.name ?: "" },
                onItemClick = { onRealizarAnombreDeIdChanged(it.id) },
                label = { Text("Persona") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onDoneAction() }
                )
            )
        }
    }
}