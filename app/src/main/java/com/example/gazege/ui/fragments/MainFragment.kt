package com.example.gazege.ui.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FabPosition
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.NavPosition
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.core.firstDayOfMonth
import com.example.gazege.core.lastDayOfMonth
import com.example.gazege.core.stableMinusMonths
import com.example.gazege.core.stablePlusMonths
import com.example.gazege.ui.DateFormat
import com.example.gazege.ui.localDateToString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.views.AccountPage
import com.example.gazege.ui.views.PersonPage
import com.example.gazege.ui.views.TransactionPage
import com.example.gazege.ui.views.getAccountSample
import com.example.gazege.ui.views.getPersonWithAccountsSample
import com.example.gazege.ui.views.getTransactionSample
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.PersonMonthSummaryView
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ModalSheetContent(
    onSiClicked: () -> Unit,
    onNoClicked: () -> Unit,
    titleText: String,
    bodyText: String
) {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp)
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = bodyText
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            androidx.compose.material.TextButton(
                onClick = {
                    onSiClicked()
                }) {
                Text(
                    text = "Si"
                )
            }
            androidx.compose.material.TextButton(
                onClick = {
                    onNoClicked()
                }
            ) {
                Text(
                    text = "No"
                )
            }
        }
    }
}

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
    transactionList: List<TransactionAndAccounts>,
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
    val accountState = rememberLazyListState()
    val personState = rememberLazyListState()
    val isFiltered = range.first != null || range.second != null
    val startDate = range.first
    val endDate = range.second
    val dateString =
        if (startDate == null && endDate == null) "Todo"
        else if (startDate != null && endDate != null)
            localDateToString(startDate, DateFormat.YEARMONTHNAME)
        else "?"

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

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = Shapes.medium,
        sheetContent = {
            ModalSheetContent(
                titleText = "Confirmar eliminación",
                bodyText = "¿Confirma la eliminación de $nombreItem?",
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (startDate != null && endDate != null) {
                                    val newRange = Pair(
                                        stableMinusMonths(startDate, 1L),
                                        stableMinusMonths(endDate, 1L)
                                    )
                                    onRangeChanged(newRange.first, newRange.second)
                                }
                            },
                            enabled = startDate != null && endDate != null
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.round_arrow_left_24
                                ), contentDescription = "Left"
                            )
                        }
                        Text(
                            dateString,
                            modifier = Modifier.clickable {
                                val newRange = LocalDate.now().let {
                                    Pair(
                                        firstDayOfMonth(it),
                                        lastDayOfMonth(it)
                                    )
                                }
                                onRangeChanged(newRange.first, newRange.second)
                            })
                        IconButton(
                            onClick = {
                                if (startDate != null && endDate != null) {
                                    val newRange = Pair(
                                        stablePlusMonths(startDate, 1L),
                                        stablePlusMonths(endDate, 1L)
                                    )
                                    onRangeChanged(newRange.first, newRange.second)
                                }
                            },
                            enabled = startDate != null && endDate != null
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.round_arrow_right_24
                                ), contentDescription = "Right"
                            )
                        }
                        IconButton(
                            onClick = {
                                val newRange = Pair(
                                    null,
                                    null
                                )
                                onRangeChanged(newRange.first, newRange.second)
                            },
                            enabled = isFiltered
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.round_filter_list_off_24
                                ), contentDescription = "Clear filters"
                            )
                        }
                    }
                    PersonMonthSummaryView(
                        saldoActual = principalPersonWithAccounts?.getTotal(null, null) ?: 0.0,
                        ingresos = principalPersonWithAccounts?.getIngresos(
                            range.first,
                            range.second
                        ) ?: 0.0,
                        egresos = principalPersonWithAccounts?.getEgresos(range.first, range.second)
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
                    Column {
                        TransactionPage(
                            transactionList = transactionList,
                            itemHolderPaddingValues = paddingValues,
                            state = transactionState,
                            delTransaction = { transaction ->
                                action = { delTransaction(transaction) }
                                nombreItem = "la transacción"
                                scope.launch { sheetState.show() }
                            },
                            editTransaction = onEditTransactionRequested,
                            onTitleSetted = { newTitle -> title = newTitle }
                        )
                    }
                }
                NavPosition.CUENTAS -> {
                    AccountPage(
                        accountList = accountList.filter { person ->
                            person.owner.id == principalPerson?.id
                        },
                        itemHolderPaddingValues = paddingValues,
                        state = accountState,
                        delAccount = { account ->
                            action = { delAccount(account) }
                            nombreItem =
                                "la cuenta ${account.name} y sus transacciones asociadas"
                            scope.launch { sheetState.show() }
                        },
                        editAccount = onEditAccountRequested,
                        startDate = null,
                        endDate = null
                    ) { newTitle -> title = newTitle }
                }
                NavPosition.PERSONS -> {
                    PersonPage(
                        personList = personList.filter { person ->
                            person.person.id != principalPerson?.id
                        },
                        itemHolderPaddingValues = paddingValues,
                        state = personState,
                        delPerson = { person ->
                            action = { delPerson(person) }
                            nombreItem =
                                "${person.name} sus cuentas y transacciones asociadas"
                            scope.launch { sheetState.show() }
                        },
                        editPerson = onEditPersonRequested,
                        onTitleSetted = { newTitle -> title = newTitle },
                        principalPerson = personList.firstOrNull { person ->
                            person.person.id == principalPerson?.id
                        },
                        onConfigurePrincipalPersonRequested = onSettingsClicked
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ModalSheetContent() {
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
fun DefaultPreview() {
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
            transactionList = transactions,
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