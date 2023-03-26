package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.PartialTransactionAndAccounts
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.DatePicker
import com.example.gazege.ui.widgets.DropDownMenu
import com.example.gazege.ui.widgets.NumberField
import com.example.gazege.ui.widgets.TextField
import java.math.BigDecimal
import java.time.LocalDate

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
    onDateChanged: (LocalDate) -> Unit
) {
    val amount = BigDecimal(transactionAndAccounts.transaction.amount ?: 0.0)
    val description = transactionAndAccounts.transaction.description ?: ""
    val selectedSourceId = transactionAndAccounts.sourceAccount?.id
    val selectedDestinationId = transactionAndAccounts.destinationAccount?.id
    val selectedSource = accountList.firstOrNull { it.account.id == selectedSourceId }
    val selectedDestination = accountList.firstOrNull { it.account.id == selectedDestinationId }
    val sourceAccountsList = accountList.filter {
        it != selectedDestination
    }
    val destinationAccountsList = accountList.filter {
        it != selectedSource
    }
    val date: LocalDate = transactionAndAccounts.transaction.date ?: defaultDate
    if (transactionAndAccounts.transaction.date == null) {
        onTransactionAndAccountsChanged(
            transactionAndAccounts.copy().apply {
                transaction = transaction.copy(date = date)
            }
        )
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
                        transaction = transaction.copy(amount = it.toDouble())
                    }
                )
            },
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
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
                imeAction = ImeAction.Next
            )
        )
        if (sourceAccountsList.isNotEmpty()) {
            var dropDownExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            DropDownMenu(
                dropDownExpanded = dropDownExpanded,
                onExpandedChange = { dropDownExpanded = !dropDownExpanded },
                options = sourceAccountsList,
                selectedItem = selectedSource,
                itemToString = { it?.account?.name ?: "" },
                onItemClick = {
                    dropDownExpanded = false
                    if (it.account.id != null) {
                        onTransactionAndAccountsChanged(
                            transactionAndAccounts.copy().apply {
                                sourceAccount = it.account
                                transaction = transaction.copy(sourceId = it.account.id)
                            }
                        )
                    }
                },
                label = { Text("Source account") }
            ) {
                it.owner.name
            }
        } else {
            ButtonField(onClick = onAccountAddRequested) {
                Text("New account")
            }
        }
        if (destinationAccountsList.isNotEmpty()) {
            var dropDownExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            DropDownMenu(
                dropDownExpanded = dropDownExpanded,
                onExpandedChange = {
                    dropDownExpanded = !dropDownExpanded
                },
                options = destinationAccountsList,
                selectedItem = selectedDestination,
                itemToString = { it?.account?.name ?: "" },
                onItemClick = {
                    dropDownExpanded = false
                    if (it.account.id != null) {
                        onTransactionAndAccountsChanged(
                            transactionAndAccounts.copy().apply {
                                destinationAccount = it.account
                                transaction = transaction.copy(destinationId = it.account.id)
                            }
                        )
                    }
                },
                label = { Text("Destination account") }
            ) {
                it.owner.name
            }
        } else {
            ButtonField(onClick = onAccountAddRequested) {
                Text("New account")
            }
        }

        DatePicker(
            value = date,
            onValueChange = {
                onDateChanged(it)
            }
        )
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
                label = { Text("Persona") }
            )
        }
    }
}