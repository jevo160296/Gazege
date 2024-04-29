package com.jmml.gazege.ui.views.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.savers.PartialAccountAndOwner
import com.jmml.gazege.ui.views.transaction.AccountAndOwnerNode
import com.jmml.gazege.ui.widgets.ComboBox
import com.jmml.gazege.ui.widgets.NumberField
import com.jmml.gazege.ui.widgets.TextField
import com.jmml.zoo.ui.input.ButtonField

@Composable
fun AccountAndOwnerForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: PartialAccountAndOwner,
    accountAndOwnerList: List<AccountAndOwner>,
    personList: List<Person>,
    currentBalance: Double,
    onCurrentBalanceChanged: (Double) -> Unit,
    onPersonAddRequested: () -> Unit,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onSetIncomeOutcomeAccount: () -> Unit,
    onDoneAction: () -> Unit,
    isComplete: Boolean,
    onAccountAndOwnerChanged: (PartialAccountAndOwner) -> Unit
) {
    val name: String = accountAndOwner.account.name ?: ""
    val selectedOwner: Person? = accountAndOwner.owner
    val incomeAccountId = incomeAccount?.id
    val outcomeAccountId = outcomeAccount?.id
    val selectable = { it: AccountAndOwner ->
        it.account.ownerId == selectedOwner?.id &&
                (accountAndOwner.account.id == null ||
                        it.account.parentId != accountAndOwner.account.id) &&
                it.account.id != accountAndOwner.account.id
    }
    val filteredAccountAndOwnerList = accountAndOwnerList.filter {
        it.owner.id == selectedOwner?.id &&
                it.account.id != accountAndOwner.account.id
    }
    val notSelectableParentAccounts = filteredAccountAndOwnerList.filter { !selectable(it) }
    val selectedParentAccountAndOwner = filteredAccountAndOwnerList.firstOrNull {
        it.account.id == accountAndOwner.account.parentId &&
                selectable(it)
    }
    val selectedParentAccountAndOwnerId = selectedParentAccountAndOwner?.account?.id
    val nextAction: ImeAction = if (isComplete) {
        ImeAction.Done
    } else {
        ImeAction.Next
    }
    if (selectedParentAccountAndOwnerId != accountAndOwner.account.parentId) {
        onAccountAndOwnerChanged(accountAndOwner.copy().apply {
            account = account.copy(parentId = selectedParentAccountAndOwnerId)
        })
    }

    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = name,
            onValueChange = {
                onAccountAndOwnerChanged(
                    accountAndOwner.copy().apply {
                        account = account.copy(name = it)
                    }
                )
            },
            label = { Text(stringResource(id = R.string.nombre)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = nextAction,
                capitalization = KeyboardCapitalization.Sentences
            ),
            keyboardActions = KeyboardActions(onDone = { onDoneAction() }),
        )
        if (personList.isNotEmpty()) {
            var dropDownExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            ComboBox(
                dropDownExpanded = dropDownExpanded,
                onExpandedChange = { dropDownExpanded = !dropDownExpanded },
                options = personList,
                selectedItem = selectedOwner,
                itemToString = { it?.name ?: "" },
                onItemClick = {
                    if (it.id != null) {
                        onAccountAndOwnerChanged(
                            accountAndOwner.copy().apply {
                                owner = it
                                account = account.copy(ownerId = it.id)
                            }
                        )
                    }
                },
                label = { Text(stringResource(R.string.Propietario)) }
            )
        } else {
            ButtonField(onClick = onPersonAddRequested) {
                Text(stringResource(R.string.Nueva_persona))
            }
        }
        AccountDropDownMenu(
            accountsList = filteredAccountAndOwnerList,
            selectedAccountNode = selectedParentAccountAndOwner?.let {
                AccountAndOwnerNode(
                    selectedParentAccountAndOwner,
                    filteredAccountAndOwnerList,
                    0,
                    0,
                    listOf(),
                    null
                )
            },
            label = { Text(stringResource(id = R.string.cuentaPadre)) },
            onItemClick = {
                onAccountAndOwnerChanged(
                    accountAndOwner.copy().apply {
                        account = account.copy(parentId = it.content.account.id)
                    }
                )
            },
            deactivatedAccountList = notSelectableParentAccounts,
            canClearSelection = true,
            onClearSelectionClicked = {
                onAccountAndOwnerChanged(
                    accountAndOwner.copy().apply {
                        account = account.copy(parentId = null)
                    }
                )
            },
            keyboardActions = KeyboardActions(onDone = { onDoneAction() }),
            keyboardOptions = KeyboardOptions(imeAction = nextAction),
            onAccountAddRequested = null
        )
        if (incomeAccountId != null && outcomeAccountId != null && incomeAccountId != accountAndOwner.account.id && outcomeAccountId != accountAndOwner.account.id) {
            NumberField(
                value = currentBalance,
                onValueChange = { onCurrentBalanceChanged(it) },
                label = { Text(stringResource(id = R.string.balance_actual)) },
                keyboardActions = KeyboardActions(onDone = { onDoneAction() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        } else {
            ButtonField(onClick = onSetIncomeOutcomeAccount) {
                Text(stringResource(R.string.Configurar_income_outcome))
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }
}