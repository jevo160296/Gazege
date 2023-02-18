package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.savers.*
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.NumberField
import com.example.gazege.ui.widgets.TextField
import java.math.BigDecimal
import java.util.*

@Composable
fun PersonFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    person: Person? = null,
    onPersonAddRequested: (Person) -> Unit
) {
    var personState by rememberSaveable(
        stateSaver = personSaver
    ) {
        mutableStateOf(
            if (person != null) {
                PartialPerson(
                    id = person.id,
                    name = person.name
                )
            } else {
                PartialPerson()
            }
        )
    }
    Form(
        modifier = modifier,
        onSaveClicked = {
            val fullPerson = personState.toFull()
            onPersonAddRequested(fullPerson)
        },
        isSavedButtonEnabled = true,
        title = "Person"
    ) {
        PersonForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            person = personState,
            onPersonChanged = {
                personState = it
            }
        )
    }
}

@Composable
fun AccountFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: AccountAndOwner? = null,
    personList: List<Person>,
    onPersonAddRequested: () -> Unit,
    onAccountAndOwnerAdd: (Account) -> Unit
) {
    var accountAndOwnerState by rememberSaveable(
        stateSaver = accountAndOwnerSaver
    ) {
        mutableStateOf(
            if (accountAndOwner != null) {
                PartialAccountAndOwner(
                    account = accountAndOwner.account.let {
                        PartialAccount(
                            id = it.id,
                            name = it.name,
                            ownerId = it.ownerId,
                            initial_balance = it.initial_balance
                        )
                    },
                    owner = accountAndOwner.owner
                )
            } else {
                PartialAccountAndOwner()
            }
        )
    }
    val completeState = accountAndOwnerState.isComplete()
    Form(
        modifier = modifier,
        onSaveClicked = {
            val fullAccountAndOwner = accountAndOwnerState.toFull()
            onAccountAndOwnerAdd(fullAccountAndOwner.account)
        },
        isSavedButtonEnabled = completeState,
        title = "Account"
    ) {
        AccountAndOwnerForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            accountAndOwner = accountAndOwnerState,
            personList = personList,
            onPersonAddRequested = onPersonAddRequested,
            onAccountAndOwnerChanged = {
                accountAndOwnerState = it
            }
        )
    }
}

@Composable
fun TransactionFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: TransactionAndAccounts? = null,
    accountList: List<Account>,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsAdd: (Transaction) -> Unit
) {
    var transactionAndAccountsState by rememberSaveable(
        stateSaver = transactionSaver
    ) {
        mutableStateOf(
            if (transactionAndAccounts != null) {
                PartialTransactionAndAccounts(
                    transaction = transactionAndAccounts.transaction.let {
                        PartialTransaction(
                            id = it.id,
                            amount = it.amount,
                            description = it.description,
                            sourceId = it.sourceId,
                            destinationId = it.destinationId,
                            date = it.date
                        )
                    },
                    sourceAccount = transactionAndAccounts.sourceAccount,
                    destinationAccount = transactionAndAccounts.destinationAccount
                )
            } else {
                PartialTransactionAndAccounts()
            }
        )
    }
    val completeState = transactionAndAccountsState.isComplete()
    Form(
        modifier = modifier,
        isSavedButtonEnabled = completeState,
        title = "Transaction",
        onSaveClicked = {
            val fullTransactionAndAccounts = transactionAndAccountsState.toFull()
            onTransactionAndAccountsAdd(fullTransactionAndAccounts.transaction)
        }
    ) {
        TransactionAndAccountsForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            transactionAndAccounts = transactionAndAccountsState,
            accountList = accountList,
            onAccountAddRequested = onAccountAddRequested,
            onTransactionAndAccountsChanged = {
                transactionAndAccountsState = it
            }
        )
    }
}

@Composable
private fun Form(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit,
    isSavedButtonEnabled: Boolean,
    title: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        floatingActionButton = {
            if (isSavedButtonEnabled) {
                FloatingActionButton(
                    onClick = onSaveClicked,
                    shape = Shapes.small
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_check_24),
                        contentDescription = ""
                    )
                }
            }
        }) {
        Column(modifier = Modifier.padding(it)) {
            MediumHeadline(title)
            content()
        }
    }
}

@Composable
private fun PersonForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    person: PartialPerson,
    onPersonChanged: (PartialPerson) -> Unit
) {
    val name = person.name ?: ""
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        TextField(
            value = name,
            onValueChange = {
                onPersonChanged(
                    person.copy(name = it)
                )
            },
            label = { Text("Nombre") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountAndOwnerForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: PartialAccountAndOwner,
    personList: List<Person>,
    onPersonAddRequested: () -> Unit,
    onAccountAndOwnerChanged: (PartialAccountAndOwner) -> Unit
) {
    val name: String = accountAndOwner.account.name ?: ""
    val selectedOwner: Person? = accountAndOwner.owner
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionAndAccountsForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: PartialTransactionAndAccounts,
    accountList: List<Account>,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsChanged: (PartialTransactionAndAccounts) -> Unit
) {
    val amount = BigDecimal(transactionAndAccounts.transaction.amount ?: 0.0)
    val description = transactionAndAccounts.transaction.description ?: ""
    val selectedSource = transactionAndAccounts.sourceAccount
    val selectedDestination = transactionAndAccounts.destinationAccount
    val sourceAccountsList = accountList.filter {
        it != selectedDestination
    }
    val destinationAccountsList = accountList.filter {
        it != selectedSource
    }
    val date = transactionAndAccounts.transaction.date
    if (date == null) {
        onTransactionAndAccountsChanged(
            transactionAndAccounts.copy().apply {
                transaction = transaction.copy(date = Date())
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
            ExposedDropdownMenuBox(
                expanded = dropDownExpanded,
                onExpandedChange = {
                    dropDownExpanded = !dropDownExpanded
                }
            ) {
                TextField(
                    modifier = Modifier.menuAnchor(),
                    value = selectedSource?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
                    },
                    label = { Text("Source account") },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false }
                ) {
                    sourceAccountsList.map {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                dropDownExpanded = false
                                if (it.id != null) {
                                    onTransactionAndAccountsChanged(
                                        transactionAndAccounts.copy().apply {
                                            sourceAccount = it
                                            transaction = transaction.copy(sourceId = it.id)
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
            ButtonField(onClick = onAccountAddRequested) {
                Text("New account")
            }
        }
        if (destinationAccountsList.isNotEmpty()) {
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
                    value = selectedDestination?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
                    },
                    label = { Text("Destination account") },
                    colors = ExposedDropdownMenuDefaults.textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false }
                ) {
                    destinationAccountsList.map {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                dropDownExpanded = false
                                if (it.id != null) {
                                    onTransactionAndAccountsChanged(
                                        transactionAndAccounts.copy().apply {
                                            destinationAccount = it
                                            transaction = transaction.copy(destinationId = it.id)
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
            ButtonField(onClick = onAccountAddRequested) {
                Text("New account")
            }
        }
    }
}

@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun PreviewLight() {
    val formTypes = object {
        val Account = 0
        val Transaction = 1
        val Person = 2
    }
    GazegeTheme {
        var personList by rememberSaveable {
            mutableStateOf(
                (5..10).map {
                    Person(it, "Person $it")
                }
            )
        }
        var accountList by rememberSaveable {
            mutableStateOf(
                arrayOf(
                    Account(name = "Cuenta1", ownerId = 1, initial_balance = 0.0)
                )
            )
        }
        var formType by rememberSaveable {
            mutableStateOf(formTypes.Person)
        }
        when (formType) {
            formTypes.Person -> {
                PersonFormFragment(
                    onPersonAddRequested = {
                        personList = listOf(
                            *personList.toTypedArray(),
                            it
                        )
                        formType = formTypes.Transaction
                        formType = formTypes.Account
                    }
                )
            }
            formTypes.Account -> {
                AccountFormFragment(
                    personList = personList,
                    onPersonAddRequested = {
                        personList = listOf(
                            *personList.toTypedArray(),
                            Person(1, "Nueva persona")
                        )
                        formType = formTypes.Person
                    },
                    onAccountAndOwnerAdd = {
                        accountList = arrayOf(
                            *accountList,
                            Account(name = "Nueva cuenta", ownerId = 1, initial_balance = 0.0)
                        )
                    }
                )
            }
            else -> {}
        }
    }
}

@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun PreviewDark() {
    val formTypes = object {
        val Account = 0
        val Transaction = 1
        val Person = 2
    }
    GazegeTheme(darkTheme = true) {
        var personList by rememberSaveable {
            mutableStateOf(
                (5..10).map {
                    Person(it, "Person $it")
                }
            )
        }
        var accountList by rememberSaveable {
            mutableStateOf(
                arrayOf(
                    Account(name = "Cuenta1", ownerId = 1, initial_balance = 0.0)
                )
            )
        }
        var formType by rememberSaveable {
            mutableStateOf(formTypes.Person)
        }
        when (formType) {
            formTypes.Person -> {
                PersonFormFragment(
                    onPersonAddRequested = {
                        personList = listOf(
                            *personList.toTypedArray(),
                            it
                        )
                        formType = formTypes.Transaction
                        formType = formTypes.Account
                    }
                )
            }
            formTypes.Account -> {
                AccountFormFragment(
                    personList = personList,
                    onPersonAddRequested = {
                        personList = listOf(
                            *personList.toTypedArray(),
                            Person(1, "Nueva persona")
                        )
                        formType = formTypes.Person
                    },
                    onAccountAndOwnerAdd = {
                        accountList = arrayOf(
                            *accountList,
                            Account(name = "Nueva cuenta", ownerId = 1, initial_balance = 0.0)
                        )
                    }
                )
            }
            else -> {}
        }
    }
}