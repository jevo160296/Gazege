package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.NavPosition
import com.example.gazege.R
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.*
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.views.*
import com.example.gazege.ui.widgets.Filter
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.ModalSheetContent
import com.example.gazege.ui.widgets.PersonMonthSummaryView
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainFragment(
    personList: List<PersonWithAccounts>,
    onAddPersonRequested: () -> Unit,
    onEditPersonRequested: (Person) -> Unit,
    delPerson: (Person) -> Unit,
    accountList: List<AccountAndOwnerWithTransactions>,
    onAddAccountRequested: () -> Unit,
    onEditAccountRequested: (Account) -> Unit,
    delAccount: (Account) -> Unit,
    allTransactionList: List<TransactionAndAccounts>,
    filteredTransactionList: List<TransactionAndAccounts>,
    onAddTransactionRequested: () -> Unit,
    onEditTransactionRequested: (Transaction) -> Unit,
    delTransaction: (Transaction) -> Unit,
    navPosition: NavPosition,
    onNavStatusChanged: (NavPosition) -> Unit,
    range: Pair<LocalDate?, LocalDate?>,
    onRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    sheetState: ModalBottomSheetState,
    snackbarHostState: SnackbarHostState,
    onSettingsClicked: () -> Unit,
    onSaldoActualClick: () -> Unit,
    principalPerson: Person?
) {
    val transactionState = rememberLazyListState()
    val accountState = rememberTreeState()
    val personState = rememberLazyListState()

    val scope = rememberCoroutineScope()
    var action by remember {
        mutableStateOf({})
    }
    var nombreItem by remember {
        mutableStateOf("")
    }
    var title: String by rememberSaveable {
        mutableStateOf("Gazedge")
    }

    val startDate = range.first
    val endDate = range.second

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = Shapes.medium,
        sheetContent = {
            ModalSheetContent(
                titleText = stringResource(id = R.string.confirmar_eliminacion),
                bodyText = stringResource(id = R.string.confirma_la_eliminacion_de).format(
                    nombreItem
                ),
                onSiClicked = {
                    action()
                    action = {}
                    nombreItem = ""
                    scope.launch { sheetState.hide() }
                },
                onNoClicked = {
                    action = {}
                    nombreItem = ""
                    scope.launch { sheetState.hide() }
                }
            )
        }) {
        Scaffold(floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (navPosition) {
                        NavPosition.PERSONS -> onAddPersonRequested()
                        NavPosition.CUENTAS -> onAddAccountRequested()
                        NavPosition.TRANSACCIONES -> onAddTransactionRequested()
                    }
                }, shape = Shapes.small
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "Add"
                )
            }
        },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false,
            backgroundColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = navPosition == NavPosition.CUENTAS,
                        onClick = { onNavStatusChanged(NavPosition.CUENTAS) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.ic_baseline_account_balance_wallet_24
                                ), contentDescription = "Accounts"
                            )
                        })
                    NavigationBarItem(selected = navPosition == NavPosition.TRANSACCIONES,
                        onClick = { onNavStatusChanged(NavPosition.TRANSACCIONES) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_home_24),
                                contentDescription = "Transactions"
                            )
                        })
                    NavigationBarItem(selected = navPosition == NavPosition.PERSONS,
                        onClick = { onNavStatusChanged(NavPosition.PERSONS) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_person_24),
                                contentDescription = "Persons"
                            )
                        })
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                val principalPersonWithAccounts = personList
                    .firstOrNull { person -> person.person.id == principalPerson?.id }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MediumHeadline(text = title)
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.baseline_settings_24
                                ), contentDescription = "Settings"
                            )
                        }
                    }
                    Filter(
                        startDate,
                        endDate,
                        onRangeChanged = onRangeChanged
                    )
                    PersonMonthSummaryView(
                        saldoActual = principalPersonWithAccounts?.let {
                            PersonDao.getTotal(it, null, null)
                        } ?: 0.0,
                        ingresos = principalPersonWithAccounts?.let {
                            PersonDao.getIngresos(
                                it,
                                range.first,
                                range.second
                            )
                        } ?: 0.0,
                        egresos = principalPersonWithAccounts?.let {
                            PersonDao.getEgresos(it, range.first, range.second)
                        }
                            ?: 0.0,
                        onSaldoActualClick = onSaldoActualClick
                    )
                }
            }
        ) {
            val paddingValues = it.let {
                PaddingValues(
                    top = it.calculateTopPadding() + 8.dp,
                    bottom = it.calculateBottomPadding() + 90.dp,
                    start = it.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
                    end = it.calculateEndPadding(LocalLayoutDirection.current) + 8.dp
                )
            }
            when (navPosition) {
                NavPosition.TRANSACCIONES -> {
                    val laTransaccion = stringResource(id = R.string.la_transaccion)
                    Column {
                        TransactionPage(
                            transactionList = filteredTransactionList,
                            itemHolderPaddingValues = paddingValues,
                            state = transactionState,
                            delTransaction = { transaction ->
                                action = { delTransaction(transaction) }
                                nombreItem = laTransaccion
                                scope.launch { sheetState.show() }
                            },
                            editTransaction = onEditTransactionRequested,
                            onTitleSetted = { newTitle -> title = newTitle }
                        )
                    }
                }
                NavPosition.CUENTAS -> {
                    val laCuenta = stringResource(id = R.string.la_cuenta)
                    AccountPage(
                        accountList = accountList.filter { person ->
                            person.owner.id == principalPerson?.id
                        },
                        itemHolderPaddingValues = paddingValues,
                        treeState = accountState,
                        delAccount = { account ->
                            action = { delAccount(account) }
                            nombreItem = laCuenta.format(account.name)
                            scope.launch { sheetState.show() }
                        },
                        editAccount = onEditAccountRequested,
                        startDate = null,
                        endDate = null
                    ) { newTitle -> title = newTitle }
                }
                NavPosition.PERSONS -> {
                    val laPersona = stringResource(R.string.la_persona)
                    PersonPage(
                        personList = personList.filter { person ->
                            person.person.id != principalPerson?.id
                        },
                        itemHolderPaddingValues = paddingValues,
                        state = personState,
                        delPerson = { person ->
                            action = { delPerson(person) }
                            nombreItem = laPersona.format(person.name)
                            scope.launch { sheetState.show() }
                        },
                        editPerson = onEditPersonRequested,
                        onTitleSetted = { newTitle -> title = newTitle },
                        principalPerson = personList.firstOrNull { person ->
                            person.person.id == principalPerson?.id
                        },
                        onConfigurePrincipalPersonRequested = onSettingsClicked,
                        transacciones = allTransactionList
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
private fun ModalSheetContentPreview() {
    GazegeTheme(darkTheme = true) {
        ModalSheetContent(
            onSiClicked = { },
            onNoClicked = { },
            titleText = "Título",
            bodyText = "Body"
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DefaultPreview() {
    val personList = getPersonWithAccountsSample()
    val accounts = getAccountSample()
    val transactions = getTransactionSample()
    val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var navPosition by remember {
        mutableStateOf(NavPosition.TRANSACCIONES)
    }
    val snackbarHostState = SnackbarHostState()
    GazegeTheme(darkTheme = true) {
        MainFragment(
            personList = personList,
            accountList = accounts,
            onAddPersonRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Add person requested.")
                }
            },
            onAddAccountRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Add account requested.")
                }
            },
            delPerson = {
                scope.launch {
                    snackbarHostState.showSnackbar("Delete person requested")
                }
            },
            allTransactionList = transactions,
            filteredTransactionList = transactions,
            onAddTransactionRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Add transaction requested")
                }
            },
            delAccount = {
                scope.launch {
                    snackbarHostState.showSnackbar("Del account ${it.name}")
                }
            },
            delTransaction = {
                scope.launch {
                    snackbarHostState.showSnackbar("Del transaction ${it.amount}")
                }
            },
            navPosition = navPosition,
            onNavStatusChanged = {
                navPosition = it
            },
            sheetState = sheetState,
            onEditAccountRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Edit account ${it.name}")
                }
            },
            onEditPersonRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Edit person ${it.name}")
                }
            },
            onEditTransactionRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Edit transaccion ${it.amount}")
                }
            },
            snackbarHostState = snackbarHostState,
            onRangeChanged = { _, _ -> },
            range = Pair(LocalDate.now(), LocalDate.now()),
            onSettingsClicked = {
                scope.launch {
                    snackbarHostState.showSnackbar("Settings clicked")
                }
            },
            principalPerson = Person(name = "?"),
            onSaldoActualClick = {}
        )
    }
}