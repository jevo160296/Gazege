package com.jmml.gazege.ui.views.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.AccountAndOwnerWithPockets
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.recursiveFirstOrNull
import com.jmml.gazege.ui.savers.PartialTransactionDetails
import com.jmml.gazege.ui.savers.PartialTransactionWithDetailsAndAccounts
import com.jmml.gazege.ui.views.account.AccountDropDownMenu
import com.jmml.gazege.ui.views.category.CategoryDropDown
import com.jmml.gazege.ui.widgets.ComboBox
import com.jmml.gazege.ui.widgets.DatePicker
import com.jmml.gazege.ui.widgets.NumberField
import com.jmml.gazege.ui.widgets.TextField
import com.jmml.gazege.ui.widgets.treeview.Node
import com.jmml.gazege.ui.widgets.treeview.NodeId
import java.time.LocalDate

@Composable
fun TransactionAndAccountsForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: PartialTransactionWithDetailsAndAccounts,
    accountList: List<AccountAndOwner>,
    onAccountAddRequested: () -> Unit,
    personList: List<Person>,
    onRealizarAnombreDeIdChanged: (detailIndexId: Int, personId: Int?) -> Unit,
    categoryList: List<Category>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    onDoneAction: () -> Unit,
    isComplete: Boolean,
    addAnotherTransaction: Boolean,
    onAddAnotherTransactionChanged: (Boolean) -> Unit,
    showSourceAccountField: Boolean = true,
    showDestinationAccountField: Boolean = true,
    onAmountChanged: (detailIndexId: Int, newAmount: Double) -> Unit,
    onDescriptionChanged: (detailIndexId: Int, newDescription: String) -> Unit,
    onSourceAccountIdChanged: (Int) -> Unit,
    onDestinationAccountIdChanged: (Int) -> Unit,
    onCategoryIdChanged: (detailIndexId: Int, categoryId: Int?) -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    showAddAnotherTransactionButton: Boolean
) {
    val selectedSourceId = transactionAndAccounts.sourceAccount?.id
    val selectedDestinationId = transactionAndAccounts.destinationAccount?.id
    val date: LocalDate? = transactionAndAccounts.transaction.date
    val transactionDetails = transactionAndAccounts.transaction.transactionDetails
    val nextAction: ImeAction = if (isComplete) {
        ImeAction.Done
    } else {
        ImeAction.Next
    }
    val keyboardActions = remember { KeyboardActions(onDone = { onDoneAction() }) }

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

    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        if (showAddAnotherTransactionButton) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = addAnotherTransaction,
                    onCheckedChange = onAddAnotherTransactionChanged
                )
                Text(text = stringResource(R.string.AddAnotherTransaction))
            }
        }
        if (showSourceAccountField) {
            AccountDropDownMenu(
                accountsList = accountList,
                selectedAccountNode = selectedSourceNode,
                label = { Text(stringResource(R.string.Cuenta_origen)) },
                onItemClick = {
                    if (it.content.account.id != null) {
                        onSourceAccountIdChanged(it.content.account.id)
                    }
                },
                deactivatedAccountList = deactivatedSourceAccountList,
                canClearSelection = false,
                onClearSelectionClicked = {},
                keyboardOptions = KeyboardOptions(imeAction = nextAction),
                keyboardActions = keyboardActions,
                onAccountAddRequested = onAccountAddRequested
            )
        }
        if (showDestinationAccountField) {
            AccountDropDownMenu(
                accountsList = accountList,
                selectedAccountNode = selectedDestinationNode,
                onItemClick = {
                    if (it.content.account.id != null) {
                        onDestinationAccountIdChanged(it.content.account.id)
                    }
                },
                label = { Text(stringResource(R.string.Cuenta_destino)) },
                deactivatedAccountList = deactivatedDestinationAccountList,
                canClearSelection = false,
                onClearSelectionClicked = {},
                keyboardOptions = KeyboardOptions(imeAction = nextAction),
                keyboardActions = keyboardActions,
                onAccountAddRequested = onAccountAddRequested
            )
        }

        DatePicker(
            value = date,
            onValueChange = {
                onDateChanged(it)
            }
        )

        transactionDetails.forEach {
            TransactionDetailsForm(
                transactionDetails = it,
                transactionDetailsIndex = 0,
                categoryList = categoryList,
                personList = personList,
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
                onAmountChanged = onAmountChanged,
                onDescriptionChanged = onDescriptionChanged,
                onCategoryIdChanged = onCategoryIdChanged,
                onRealizarAnombreDeIdChanged = onRealizarAnombreDeIdChanged,
                nextAction = nextAction,
                keyboardActions = keyboardActions
            )
        }
    }
}

@Composable
private fun TransactionDetailsForm(
    contentPadding: PaddingValues = PaddingValues(),
    transactionDetails: PartialTransactionDetails,
    transactionDetailsIndex: Int,
    categoryList: List<Category>,
    personList: List<Person>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    onAmountChanged: (detailIndexId: Int, newAmount: Double) -> Unit,
    onDescriptionChanged: (detailIndexId: Int, newDescription: String) -> Unit,
    onCategoryIdChanged: (transactionDetailIndex: Int, categoryId: Int?) -> Unit,
    onRealizarAnombreDeIdChanged: (transactionDetailIndex: Int, personId: Int?) -> Unit,
    nextAction: ImeAction,
    keyboardActions: KeyboardActions,
) {
    val amount = transactionDetails.amount ?: 0.0
    val description = transactionDetails.description ?: ""
    val aNombreDe = transactionDetails.aNombreDe
    val selectedCategoryId = transactionDetails.categoryId

    val selectedCategory =
        budgetWithCalculatedDataAndCategory.recursiveFirstOrNull { it.category.category.id == selectedCategoryId }

    var realizarANombreDe by rememberSaveable(aNombreDe) {
        mutableStateOf(aNombreDe != null)
    }

    NumberField(
        value = amount,
        onValueChange = { onAmountChanged(transactionDetailsIndex, it) },
        label = { Text(stringResource(id = R.string.Valor)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = nextAction
        ),
        keyboardActions = keyboardActions
    )
    TextField(
        value = description,
        onValueChange = { onDescriptionChanged(transactionDetailsIndex, it) },
        label = { Text(text = stringResource(id = R.string.descripcion)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = nextAction,
            capitalization = KeyboardCapitalization.Sentences
        ),
        keyboardActions = keyboardActions
    )
    if (categoryList.isNotEmpty()) {
        CategoryDropDown(
            categoryList = categoryList,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            selectedCategory = selectedCategory,
            label = { Text(stringResource(id = R.string.Categoria)) },
            keyboardOptions = KeyboardOptions(imeAction = nextAction),
            keyboardActions = keyboardActions
        ) { onCategoryIdChanged(transactionDetailsIndex, it?.id) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = realizarANombreDe, onCheckedChange = {
            if (!it) {
                onRealizarAnombreDeIdChanged(transactionDetailsIndex, null)
            }
            realizarANombreDe = it
        })
        Text(text = stringResource(R.string.Realizar_a_nombre_de_otra_persona))
    }
    if (realizarANombreDe) {
        var dropDownExpanded by rememberSaveable {
            mutableStateOf(false)
        }
        val selectedItem =
            personList.firstOrNull { it.id == aNombreDe }
        ComboBox(
            dropDownExpanded = dropDownExpanded,
            onExpandedChange = { dropDownExpanded = it },
            options = personList,
            selectedItem = selectedItem,
            itemToString = { it?.name ?: "" },
            onItemClick = { onRealizarAnombreDeIdChanged(transactionDetailsIndex, it.id) },
            label = { Text(stringResource(R.string.persona)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = keyboardActions
        )
    }
}

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