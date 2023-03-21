package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import com.example.gazege.ui.widgets.DatePicker
import com.example.gazege.ui.widgets.DropDownMenu
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.NumberField
import com.example.gazege.ui.widgets.TextField
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Composable
fun PersonFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    person: Person? = null,
    onPersonAddRequested: (Person, SnackbarHostState) -> Unit
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
    val snackbarHostState = SnackbarHostState()
    Form(
        modifier = modifier,
        onSaveClicked = {
            val fullPerson = personState.toFull()
            onPersonAddRequested(fullPerson, snackbarHostState)
        },
        isSavedButtonEnabled = true,
        title = "Person",
        snackbarHostState = snackbarHostState
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
    currentBalance: Double,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onSetIncomeOutcomeAccount: () -> Unit,
    onPersonAddRequested: () -> Unit,
    onAccountAndOwnerAdd: (Account, Double, SnackbarHostState, Int?, Int?) -> Unit
) {
    var currentBalanceState by rememberSaveable {
        mutableStateOf(currentBalance)
    }
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
                            ownerId = it.ownerId
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
    val snackbarHostState = SnackbarHostState()
    Form(
        modifier = modifier,
        onSaveClicked = {

            val fullAccountAndOwner = accountAndOwnerState.toFull()
            onAccountAndOwnerAdd(
                fullAccountAndOwner.account,
                currentBalanceState,
                snackbarHostState,
                incomeAccount?.id,
                outcomeAccount?.id
            )
        },
        isSavedButtonEnabled = completeState,
        snackbarHostState = snackbarHostState,
        title = "Account"
    ) {
        AccountAndOwnerForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            accountAndOwner = accountAndOwnerState,
            personList = personList,
            currentBalance = currentBalanceState,
            onCurrentBalanceChanged = { currentBalanceState = it },
            onPersonAddRequested = onPersonAddRequested,
            onAccountAndOwnerChanged = {
                accountAndOwnerState = it
            },
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onSetIncomeOutcomeAccount = onSetIncomeOutcomeAccount
        )
    }
}

@Composable
fun TransactionFormFragment(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    transactionAndAccounts: TransactionAndAccounts? = null,
    accountList: List<AccountAndOwner>,
    onAccountAddRequested: () -> Unit,
    onTransactionAndAccountsAdd: (Transaction) -> Unit,
    personList: List<Person>,
    defaultDate: LocalDate = LocalDate.now()
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
                            date = it.date,
                            aNombreDe = it.aNombreDe
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
    var realizarANombreDe by rememberSaveable {
        mutableStateOf(transactionAndAccountsState.transaction.aNombreDe != null)
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
            },
            defaultDate = defaultDate,
            onDateChanged = {
                transactionAndAccountsState = transactionAndAccountsState.copy().apply {
                    transaction = this.transaction.copy(date = it)
                }
            },
            realizarANombreDe = realizarANombreDe,
            onRealizarANombreDeChanged = { realizarANombreDe = it },
            personList = personList,
            onRealizarAnombreDeIdChanged = {
                transactionAndAccountsState = transactionAndAccountsState.copy().apply {
                    transaction = this.transaction.copy(aNombreDe = it)
                }
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
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        snackbarHost = {
            if (snackbarHostState == null) {
                androidx.compose.material.SnackbarHost(hostState = it)
            } else {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background,
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

@Composable
private fun TransactionAndAccountsForm(
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

@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun PreviewLight() {
    val formTypes = object {
        val Account = 0
        val Transaction = 1
        val Person = 2
    }
    GazegeTheme {
        val personList by rememberSaveable {
            mutableStateOf(
                (5..10).map {
                    Person(it, "Person $it")
                }
            )
        }
        var formType by rememberSaveable {
            mutableStateOf(formTypes.Account)
        }
        when (formType) {
            formTypes.Person -> {
                PersonFormFragment(
                    onPersonAddRequested = { _, _ ->
                        formType = formTypes.Account
                    }
                )
            }
            formTypes.Account -> {
                AccountFormFragment(
                    personList = personList,
                    onPersonAddRequested = {

                    },
                    onAccountAndOwnerAdd = { _, _, _, _, _ ->
                        formType = formTypes.Transaction
                    },
                    currentBalance = 0.0,
                    incomeAccount = null,
                    outcomeAccount = null,
                    onSetIncomeOutcomeAccount = {}
                )
            }
            formTypes.Transaction -> {
                TransactionFormFragment(
                    accountList = listOf(),
                    onAccountAddRequested = { },
                    onTransactionAndAccountsAdd = {
                        formType = formTypes.Person
                    },
                    personList = listOf()
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
                    Account(name = "Cuenta1", ownerId = 1)
                )
            )
        }
        var formType by rememberSaveable {
            mutableStateOf(formTypes.Person)
        }
        when (formType) {
            formTypes.Person -> {
                PersonFormFragment(
                    onPersonAddRequested = { it, _ ->
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
                    onAccountAndOwnerAdd = { _, _, _, _, _ ->
                        accountList = arrayOf(
                            *accountList,
                            Account(name = "Nueva cuenta", ownerId = 1)
                        )
                    },
                    currentBalance = 0.0,
                    incomeAccount = null,
                    outcomeAccount = null,
                    onSetIncomeOutcomeAccount = {}
                )
            }
            else -> {}
        }
    }
}