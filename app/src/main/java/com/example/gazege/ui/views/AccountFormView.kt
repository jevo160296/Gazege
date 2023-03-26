package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.PartialAccountAndOwner
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.NumberField
import com.example.gazege.ui.widgets.TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAndOwnerForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: PartialAccountAndOwner,
    personList: List<Person>,
    currentBalance: Double,
    onCurrentBalanceChanged: (Double) -> Unit,
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
        if (incomeAccountId != null && outcomeAccountId != null && incomeAccountId != accountAndOwner.account.id && outcomeAccountId != accountAndOwner.account.id) {
            NumberField(
                value = currentBalance.toBigDecimal(),
                onValueChange = { onCurrentBalanceChanged(it.toDouble()) }
            )
        } else {
            ButtonField(onClick = onSetIncomeOutcomeAccount) {
                Text("Configurar income y/o outcome account")
            }
        }
    }
}