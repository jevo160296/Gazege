package com.jmml.gazege.ui.views.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
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
import com.jmml.gazege.ui.savers.PartialNewTransactionDetails
import com.jmml.gazege.ui.savers.PartialTransactionWithDetailsAndAccounts
import com.jmml.gazege.ui.views.account.AccountDropDownMenu
import com.jmml.gazege.ui.views.category.CategoryDropDown
import com.jmml.gazege.ui.widgets.ComboBox
import com.jmml.gazege.ui.widgets.DatePicker
import com.jmml.gazege.ui.widgets.NumberField
import com.jmml.gazege.ui.widgets.TextField
import com.jmml.gazege.ui.widgets.treeview.Node
import com.jmml.gazege.ui.widgets.treeview.NodeId
import kotlinx.coroutines.delay
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
    onAddTransactionDetailRequested: () -> Unit,
    onRemoveTransactionDetailRequested: (transactionIndex: Int) -> Unit,
    onAmountChanged: (detailIndexId: Int, newAmount: Double) -> Unit,
    onDescriptionChanged: (detailIndexId: Int, newDescription: String) -> Unit,
    onSourceAccountIdChanged: (Int) -> Unit,
    onDestinationAccountIdChanged: (Int) -> Unit,
    onCategoryIdChanged: (detailIndexId: Int, categoryId: Int?) -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    showAddAnotherTransactionButton: Boolean,
    focusRequester: FocusRequester
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

        TransactionDetailListForm(
            transactionDetails = transactionDetails,
            categoryList = categoryList,
            personList = personList,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            onAmountChanged = onAmountChanged,
            onDescriptionChanged = onDescriptionChanged,
            onCategoryIdChanged = onCategoryIdChanged,
            onRealizarAnombreDeIdChanged = onRealizarAnombreDeIdChanged,
            nextAction = nextAction,
            keyboardActions = keyboardActions,
            onAddTransactionDetailRequested = onAddTransactionDetailRequested,
            onRemoveTransactionDetailRequested = onRemoveTransactionDetailRequested,
            focusRequester = focusRequester
        )
        LaunchedEffect(transactionAndAccounts.transaction.transactionId == null) {
            delay(100)
            if (transactionAndAccounts.transaction.transactionId == null) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
private fun ColumnScope.TransactionDetailListForm(
    contentPadding: PaddingValues = PaddingValues(),
    transactionDetails: List<PartialNewTransactionDetails>,
    categoryList: List<Category>,
    personList: List<Person>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    onAddTransactionDetailRequested: () -> Unit,
    onRemoveTransactionDetailRequested: (transactionIndex: Int) -> Unit,
    onAmountChanged: (detailIndexId: Int, newAmount: Double) -> Unit,
    onDescriptionChanged: (detailIndexId: Int, newDescription: String) -> Unit,
    onCategoryIdChanged: (transactionDetailIndex: Int, categoryId: Int?) -> Unit,
    onRealizarAnombreDeIdChanged: (transactionDetailIndex: Int, personId: Int?) -> Unit,
    nextAction: ImeAction,
    keyboardActions: KeyboardActions,
    focusRequester: FocusRequester
) {
    var isSplitted by remember(transactionDetails.count()) {
        mutableStateOf(transactionDetails.count() > 1)
    }
    val innerContentPadding =
        if (isSplitted) PaddingValues(start = dimensionResource(R.dimen.DefaultPadding) * 2)
        else PaddingValues()

    if (isSplitted) {
        NumberField(
            value = transactionDetails.sumOf { it.amount ?: 0.0 },
            enabled = false,
            readOnly = true,
            onValueChange = {},
            label = { Text(stringResource(id = R.string.Valor)) },
            singleLine = true,
        )
    }

    val cantElements = transactionDetails.count()
    transactionDetails.forEachIndexed { index, it ->
        val isBetweenElements = index + 1 < cantElements
        TransactionDetailsForm(
            contentPadding = innerContentPadding,
            transactionDetails = it,
            transactionDetailsIndex = index,
            categoryList = categoryList,
            personList = personList,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            isSplitted = isSplitted,
            onSplittedChanged = { isSplitted = it },
            onAmountChanged = onAmountChanged,
            onDescriptionChanged = onDescriptionChanged,
            onCategoryIdChanged = onCategoryIdChanged,
            onRealizarAnombreDeIdChanged = onRealizarAnombreDeIdChanged,
            onRemoveTransactionDetailRequested = onRemoveTransactionDetailRequested,
            nextAction = nextAction,
            keyboardActions = keyboardActions,
            focusRequester = focusRequester
        )
        if (isBetweenElements) {
            Spacer(
                Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
    if (isSplitted) {
        IconButton(
            modifier = Modifier.align(Alignment.End),
            onClick = onAddTransactionDetailRequested
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_add_24),
                contentDescription = "Add"
            )
        }
    }
}

@Composable
private fun TransactionDetailsForm(
    contentPadding: PaddingValues = PaddingValues(),
    transactionDetails: PartialNewTransactionDetails,
    transactionDetailsIndex: Int,
    categoryList: List<Category>,
    personList: List<Person>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    isSplitted: Boolean,
    onSplittedChanged: (newSplitted: Boolean) -> Unit,
    onAmountChanged: (detailIndexId: Int, newAmount: Double) -> Unit,
    onDescriptionChanged: (detailIndexId: Int, newDescription: String) -> Unit,
    onCategoryIdChanged: (transactionDetailIndex: Int, categoryId: Int?) -> Unit,
    onRealizarAnombreDeIdChanged: (transactionDetailIndex: Int, personId: Int?) -> Unit,
    onRemoveTransactionDetailRequested: (transactionIndex: Int) -> Unit,
    nextAction: ImeAction,
    keyboardActions: KeyboardActions,
    focusRequester: FocusRequester? = null
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

    val focusModifier = focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier

    NumberField(
        modifier = Modifier
            .padding(contentPadding)
            .then(focusModifier),
        value = amount,
        onValueChange = { onAmountChanged(transactionDetailsIndex, it) },
        label = { Text(stringResource(id = R.string.Valor)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = nextAction
        ),
        keyboardActions = keyboardActions,
        trailingIcon = if (isSplitted) {
            null
        } else {
            {
                IconButton(onClick = {
                    onSplittedChanged(true)
                }) {
                    Icon(painterResource(R.drawable.split_24), "Split")
                }
            }
        }
    )
    TextField(
        modifier = Modifier.padding(contentPadding),
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
            modifier = Modifier.padding(contentPadding),
            categoryList = categoryList,
            budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
            selectedCategory = selectedCategory,
            label = { Text(stringResource(id = R.string.Categoria)) },
            keyboardOptions = KeyboardOptions(imeAction = nextAction),
            keyboardActions = keyboardActions
        ) { onCategoryIdChanged(transactionDetailsIndex, it?.id) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = realizarANombreDe,
                onCheckedChange = {
                    if (!it) {
                        onRealizarAnombreDeIdChanged(transactionDetailsIndex, null)
                    }
                    realizarANombreDe = it
                })
            Text(text = stringResource(R.string.Realizar_a_nombre_de_otra_persona))
        }
        AnimatedVisibility(visible = isSplitted) {
            IconButton(
                onClick = {
                    onRemoveTransactionDetailRequested(transactionDetailsIndex)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete),
                    contentDescription = "Add"
                )
            }
        }
    }
    if (realizarANombreDe) {
        var dropDownExpanded by rememberSaveable {
            mutableStateOf(false)
        }
        val selectedItem =
            personList.firstOrNull { it.id == aNombreDe }
        ComboBox(
            modifier = Modifier.padding(contentPadding),
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