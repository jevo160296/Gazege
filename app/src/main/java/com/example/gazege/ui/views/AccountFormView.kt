package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.PartialAccountAndOwner
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.NumberField
import com.example.gazege.ui.widgets.SignedBigDecimal
import com.example.gazege.ui.widgets.TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAndOwnerForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: PartialAccountAndOwner,
    accountAndOwnerList: List<AccountAndOwner>,
    personList: List<Person>,
    currentBalance: SignedBigDecimal,
    onCurrentBalanceChanged: (SignedBigDecimal) -> Unit,
    onPersonAddRequested: () -> Unit,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onSetIncomeOutcomeAccount: () -> Unit,
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
    if (selectedParentAccountAndOwnerId != accountAndOwner.account.parentId) {
        onAccountAndOwnerChanged(accountAndOwner.copy().apply {
            account = account.copy(parentId = selectedParentAccountAndOwnerId)
        })
    }
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        TextField(
            value = name,
            onValueChange = {
                onAccountAndOwnerChanged(
                    accountAndOwner.copy().apply {
                        account = account.copy(name = it)
                    }
                )
            },
            label = { Text("Nombre") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )
        if (personList.isNotEmpty()) {
            var dropDownExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            ExposedDropdownMenuBox(
                expanded = dropDownExpanded,
                onExpandedChange = {
                    dropDownExpanded = !dropDownExpanded
                }
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    value = selectedOwner?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
                    },
                    label = { Text("Owner") },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false }
                ) {
                    personList.map {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                dropDownExpanded = false
                                if (it.id != null) {
                                    onAccountAndOwnerChanged(
                                        accountAndOwner.copy().apply {
                                            owner = it
                                            account = account.copy(ownerId = it.id)
                                        }
                                    )
                                }
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        } else {
            ButtonField(onClick = onPersonAddRequested) {
                Text("New person")
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
            }
        )
        if (incomeAccountId != null && outcomeAccountId != null && incomeAccountId != accountAndOwner.account.id && outcomeAccountId != accountAndOwner.account.id) {
            NumberField(
                value = currentBalance,
                onValueChange = { onCurrentBalanceChanged(it) },
                label = { Text(stringResource(id = R.string.balance_actual)) }
            )
        } else {
            ButtonField(onClick = onSetIncomeOutcomeAccount) {
                Text("Configurar income y/o outcome account")
            }
        }
    }
}