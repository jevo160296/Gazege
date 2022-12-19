package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
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
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt
import com.example.gazege.ui.widgets.TextField

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
            if(person != null){
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
        onSaveClicked = {
            val fullPerson = personState.toFull()
            onPersonAddRequested(fullPerson)
        },
        isSavedButtonEnabled = true
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
){
    var accountAndOwnerState by rememberSaveable(
        stateSaver = accountAndOwnerSaver
    ) {
        mutableStateOf(
            if(accountAndOwner != null){
                PartialAccountAndOwner(
                    account = accountAndOwner.account.let{
                        PartialAccount(
                            id = it.id,
                            name = it.name,
                            ownerId = it.ownerId,
                            initial_balance = it.initial_balance
                        )
                    },
                    owner = accountAndOwner.owner
                )
            }
            else{
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
        isSavedButtonEnabled = completeState
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
){
    var transactionAndAccountsState by rememberSaveable(
        stateSaver = transactionSaver
    ) {
        mutableStateOf(
            if(transactionAndAccounts != null){
                PartialTransactionAndAccounts(
                    transaction = transactionAndAccounts.transaction.let{
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
            }
            else{
                PartialTransactionAndAccounts()
            }
        )
    }
    val completeState = transactionAndAccountsState.isComplete()
    Form(
        modifier = modifier,
        isSavedButtonEnabled = completeState,
        onSaveClicked = {
            val fullTransactionAndAccounts = transactionAndAccountsState.toFull()
            onTransactionAndAccountsAdd(fullTransactionAndAccounts.transaction)
        }
    ){
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
    content: @Composable () -> Unit){
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        floatingActionButton = {
            if(isSavedButtonEnabled){
                FloatingActionButton(
                    onClick = onSaveClicked,
                    shape = Shapes.small
                    ){
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_check_24),
                        contentDescription = ""
                    )
                }
            }
            }) {
        Column(modifier = Modifier.padding(it)) {
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
    Column(modifier = modifier.padding(contentPadding),
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
    Column(modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        TextField(
            value = name,
            onValueChange = {
                onAccountAndOwnerChanged(
                    accountAndOwner.copy().apply {
                        account = account.copy(name=it)
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
                            text = {Text(it.name)},
                            onClick = {
                                dropDownExpanded = false
                                if(it.id != null){
                                    onAccountAndOwnerChanged(
                                        accountAndOwner.copy().apply{
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
        } else{
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
    val amount = (transactionAndAccounts.transaction.amount ?: 0.0).roundToInt().toString()
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
    if(date == null){
        onTransactionAndAccountsChanged(
            transactionAndAccounts.copy().apply{
                transaction = transaction.copy(date = Date())
            }
        )
    }
    Column(modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)) {
        TextField(
            value = amount,
            onValueChange = {
                onTransactionAndAccountsChanged(
                    transactionAndAccounts.copy().apply {
                        transaction = transaction.copy(amount= it.toDouble())
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
        if (sourceAccountsList.isNotEmpty()){
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
                            text = {Text(it.name)},
                            onClick = {
                                dropDownExpanded = false
                                if(it.id != null){
                                    onTransactionAndAccountsChanged(
                                        transactionAndAccounts.copy().apply{
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
        }
        else{
            ButtonField(onClick = onAccountAddRequested) {
                Text("New account")
            }
        }
        if (destinationAccountsList.isNotEmpty()){
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
                            text = {Text(it.name)},
                            onClick = {
                                dropDownExpanded = false
                                if(it.id != null){
                                    onTransactionAndAccountsChanged(
                                        transactionAndAccounts.copy().apply{
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
        }
        else{
            ButtonField(onClick = onAccountAddRequested) {
                Text("New account")
            }
        }
    }
}

@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun Preview() {
    val formTypes = object {
        val Account = 0
        val Transaction = 1
        val Person = 2
    }
    GazegeTheme {
        val personList = (5..10).map {
            Person(it, "Person $it")
        }
        var formType by rememberSaveable {
            mutableStateOf(formTypes.Person)
        }
        var person by rememberSaveable(
            stateSaver = personSaver
        ) {
            mutableStateOf(
                PartialPerson()
            )
        }
        var accountAndOwner by rememberSaveable(
            stateSaver = accountAndOwnerSaver
        ) {
            mutableStateOf(
                PartialAccountAndOwner()
            )
        }
        val scaffoldState: ScaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
            scaffoldState = scaffoldState,
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    scope.launch {
                        when(formType){
                            formTypes.Person -> {
                                val personAdded = person
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "Added $personAdded"
                                )
                            }
                            formTypes.Account -> {
                                val accountAndOwnerAdded = accountAndOwner
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "Added $accountAndOwnerAdded"
                                )
                            }
                            formTypes.Transaction -> TODO()
                        }
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_check_24),
                        contentDescription = ""
                    )
                }
            }
        ) {
            when(formType){
                formTypes.Person -> {
                    PersonForm(
                        modifier = Modifier.padding(it),
                        contentPadding = PaddingValues(8.dp),
                        itemSpacing = 8.dp,
                        person = person
                    ){ changedPerson ->
                        person = changedPerson
                    }
                }
                formTypes.Account -> {
                    AccountAndOwnerForm(
                        modifier = Modifier.padding(it),
                        contentPadding = PaddingValues(8.dp),
                        itemSpacing = 8.dp,
                        accountAndOwner = accountAndOwner,
                        personList = personList,
                        onPersonAddRequested = {
                            formType = formTypes.Person
                        }
                    ) { changedAccount ->
                        accountAndOwner = changedAccount
                    }
                }
                else -> {}
            }
        }
    }
}