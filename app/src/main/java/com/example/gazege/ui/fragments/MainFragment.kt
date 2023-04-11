package com.example.gazege.ui.fragments

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.NavPosition
import com.example.gazege.R
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.*
import com.example.gazege.ui.accountDeleitionConfirmationBuilder
import com.example.gazege.ui.personaDeleitionConfirmationBuilder
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.transactionDeleitionConfirmationBuilder
import com.example.gazege.ui.views.*
import com.example.gazege.ui.views.account.AccountPage
import com.example.gazege.ui.views.account.getAccountSample
import com.example.gazege.ui.views.person.PersonPage
import com.example.gazege.ui.views.person.getPersonWithAccountsSample
import com.example.gazege.ui.views.transaction.TransactionPage
import com.example.gazege.ui.views.transaction.getTransactionSample
import com.example.gazege.ui.widgets.*
import com.example.gazege.ui.widgets.fab.ExpandableFAB
import com.example.gazege.ui.widgets.menu.DropDownMenuItem
import com.example.gazege.ui.widgets.treeview.TreeState
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainFragment(
    personList: List<PersonWithAccounts>,
    accountList: List<AccountAndOwnerWithTransactions>,
    allTransactionList: List<TransactionAndAccountsAndCategory>,
    filteredTransactionList: List<TransactionAndAccountsAndCategory>,
    principalPersonWithAccounts: PersonWithAccounts?,
    navPosition: NavPosition,
    range: Pair<LocalDate?, LocalDate?>,
    personFilterValue: Boolean,
    sheetState: ModalBottomSheetState,
    snackbarHostState: SnackbarHostState,
    delPerson: (Person) -> Unit,
    delAccount: (Account) -> Unit,
    delTransaction: (Transaction) -> Unit,
    onAddPersonRequested: () -> Unit,
    onEditPersonRequested: (Person) -> Unit,
    onPersonDetailRequested: (Person) -> Unit,
    onAddAccountRequested: () -> Unit,
    onEditAccountRequested: (Account) -> Unit,
    onAccountDetailRequested: (Account) -> Unit,
    onAddTransactionRequested: (action: AddTransactionAction) -> Unit,
    onEditTransactionRequested: (Transaction) -> Unit,
    onNavStatusChanged: (NavPosition) -> Unit,
    onRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onSettingsClicked: () -> Unit,
    onSaldoActualClick: () -> Unit,
    onPersonFilterValueChanged: (Boolean) -> Unit,
    showVertical: Boolean
) {
    val transactionState = rememberLazyListState()
    val accountState = rememberTreeState()
    val personState = rememberLazyListState()

    val scope = rememberCoroutineScope()
    var action by remember {
        mutableStateOf({})
    }
    var modalSheetMsg by remember {
        mutableStateOf("")
    }
    var title: String by rememberSaveable {
        mutableStateOf("Gazege")
    }
    var fabExpanded: Boolean by remember {
        mutableStateOf(false)
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = Shapes.medium,
        sheetContent = {
            ModalSheetContent(
                titleText = stringResource(id = R.string.confirmar_eliminacion),
                bodyText = modalSheetMsg,
                onSiClicked = {
                    action()
                    action = {}
                    modalSheetMsg = ""
                    scope.launch { sheetState.hide() }
                },
                onNoClicked = {
                    action = {}
                    modalSheetMsg = ""
                    scope.launch { sheetState.hide() }
                }
            )
        }) {
        Scaffold(
            floatingActionButton = {
                val rotation by animateFloatAsState(
                    targetValue = if (fabExpanded) {
                        45f
                    } else {
                        0f
                    }
                )
                ExpandableFAB(
                    columnModifier = Modifier.width(IntrinsicSize.Max),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_add_24),
                            contentDescription = "Add",
                            modifier = Modifier.rotate(rotation),
                        )
                    },
                    isExpanded = fabExpanded,
                    onClick = {
                        when (navPosition) {
                            NavPosition.PERSONS -> onAddPersonRequested()
                            NavPosition.CUENTAS -> onAddAccountRequested()
                            NavPosition.TRANSACCIONES -> fabExpanded = true
                        }
                    },
                    onDismissRequest = { fabExpanded = false }
                ) {
                    DropDownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onAddTransactionRequested(AddTransactionAction.ADD_TRANSFER)
                            fabExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.transfer_icon),
                                contentDescription = "Add"
                            )
                        },
                        label = { Text(text = stringResource(id = R.string.Transferencia)) }
                    )
                    DropDownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onAddTransactionRequested(AddTransactionAction.ADD_EXPENSE)
                            fabExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.gasto_icon),
                                contentDescription = "Add"
                            )
                        },
                        label = { Text(text = stringResource(id = R.string.Gasto)) }
                    )
                    DropDownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onAddTransactionRequested(AddTransactionAction.ADD_INCOME)
                            fabExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ingreso_icon),
                                contentDescription = "Add"
                            )
                        },
                        label = { Text(text = stringResource(id = R.string.Ingreso)) }
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
                TopAppBar(
                    title = { MediumHeadline(text = title) },
                    actions = {
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.baseline_settings_24
                                ), contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        ) {
            MainFragmentResponsiveContent(
                it,
                personList = personList,
                accountList = accountList,
                allTransactionList = allTransactionList,
                filteredTransactionList = filteredTransactionList,
                principalPersonWithAccounts = principalPersonWithAccounts,
                personFilterValue = personFilterValue,
                delPerson = delPerson,
                delAccount = delAccount,
                delTransaction = delTransaction,
                onEditPersonRequested = onEditPersonRequested,
                onPersonDetailRequested = onPersonDetailRequested,
                onEditAccountRequested = onEditAccountRequested,
                onAccountDetailRequested = onAccountDetailRequested,
                onEditTransactionRequested = onEditTransactionRequested,
                onRangeChanged = onRangeChanged,
                onSaldoActualClick = onSaldoActualClick,
                onPersonFilterValueChanged = onPersonFilterValueChanged,
                transactionState = transactionState,
                accountState = accountState,
                personState = personState,
                navPosition = navPosition,
                range = range,
                onActionChanged = { action = it },
                onModalSheetMsgChanged = { modalSheetMsg = it },
                scope = scope,
                sheetState = sheetState,
                onTitleChanged = { title = it },
                onSettingsClicked = onSettingsClicked,
                showVertical = showVertical
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainFragmentResponsiveContent(
    layoutPaddingValues: PaddingValues,
    personList: List<PersonWithAccounts>,
    accountList: List<AccountAndOwnerWithTransactions>,
    allTransactionList: List<TransactionAndAccountsAndCategory>,
    filteredTransactionList: List<TransactionAndAccountsAndCategory>,
    principalPersonWithAccounts: PersonWithAccounts?,
    personFilterValue: Boolean,
    delPerson: (Person) -> Unit,
    delAccount: (Account) -> Unit,
    delTransaction: (Transaction) -> Unit,
    onEditPersonRequested: (Person) -> Unit,
    onPersonDetailRequested: (Person) -> Unit,
    onEditAccountRequested: (Account) -> Unit,
    onAccountDetailRequested: (Account) -> Unit,
    onEditTransactionRequested: (Transaction) -> Unit,
    onRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onSaldoActualClick: () -> Unit,
    onPersonFilterValueChanged: (Boolean) -> Unit,
    transactionState: LazyListState,
    accountState: TreeState,
    personState: LazyListState,
    navPosition: NavPosition,
    range: Pair<LocalDate?, LocalDate?>,
    onActionChanged: (() -> Unit) -> Unit,
    onModalSheetMsgChanged: (String) -> Unit,
    scope: CoroutineScope,
    sheetState: ModalBottomSheetState,
    onTitleChanged: (String) -> Unit,
    onSettingsClicked: () -> Unit,
    showVertical: Boolean
) {
    val paddingValues = layoutPaddingValues.let {
        PaddingValues(
            top = it.calculateTopPadding() + 8.dp,
            bottom = it.calculateBottomPadding() + dimensionResource(id = R.dimen.FABDefaultSpace),
            start = it.calculateStartPadding(LocalLayoutDirection.current) + 8.dp,
            end = it.calculateEndPadding(LocalLayoutDirection.current) + 8.dp
        )
    }
    val startDate = range.first
    val endDate = range.second

    val filter = @Composable {
        Filter(
            Modifier.fillMaxWidth(),
            startDate,
            endDate,
            onRangeChanged = onRangeChanged,
            personFilterVisible = navPosition == NavPosition.PERSONS,
            personFilterValue = personFilterValue,
            onPersonFilterValueChanged = onPersonFilterValueChanged
        )
    }

    val personMonthSummaryView = @Composable {
        PersonMonthSummaryView(
            modifier = Modifier.fillMaxWidth(),
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

    val navigationView = @Composable {
        when (navPosition) {
            NavPosition.TRANSACCIONES -> {
                val template = transactionDeleitionConfirmationBuilder()
                Column {
                    TransactionPage(
                        transactionList = filteredTransactionList,
                        itemHolderPaddingValues = paddingValues,
                        state = transactionState,
                        delTransaction = { transaction ->
                            onActionChanged { delTransaction(transaction) }
                            onModalSheetMsgChanged(template())
                            scope.launch { sheetState.show() }
                        },
                        editTransaction = onEditTransactionRequested,
                        onTitleSetted = { newTitle -> onTitleChanged(newTitle) }
                    )
                }
            }
            NavPosition.CUENTAS -> {
                val template = accountDeleitionConfirmationBuilder()
                AccountPage(
                    accountList = accountList.filter { person ->
                        person.owner.id == principalPersonWithAccounts?.person?.id
                    },
                    itemHolderPaddingValues = paddingValues,
                    treeState = accountState,
                    delAccount = { account ->
                        onActionChanged { delAccount(account) }
                        onModalSheetMsgChanged(template(account.name))
                        scope.launch { sheetState.show() }
                    },
                    editAccount = onEditAccountRequested,
                    startDate = null,
                    endDate = null,
                    detailAccount = onAccountDetailRequested
                ) { newTitle -> onTitleChanged(newTitle) }
            }
            NavPosition.PERSONS -> {
                val template = personaDeleitionConfirmationBuilder()
                val transacciones =
                    allTransactionList.map { trans -> trans.toTransactionAndAccounts() }
                PersonPage(
                    personList = personList.filter { person ->
                        person.person.id != principalPersonWithAccounts?.person?.id
                    }.filter { personWithAccounts ->
                        if (personFilterValue) {
                            val flujo = principalPersonWithAccounts
                                ?.let {
                                    PersonDao.getFlujo(
                                        principalPersonWithAccounts,
                                        personWithAccounts,
                                        transacciones
                                    )
                                }
                                ?: 0.0
                            flujo != 0.0
                        } else {
                            true
                        }
                    },
                    itemHolderPaddingValues = paddingValues,
                    state = personState,
                    delPerson = { person ->
                        onActionChanged { delPerson(person) }
                        onModalSheetMsgChanged(template(person.name))
                        scope.launch { sheetState.show() }
                    },
                    editPerson = onEditPersonRequested,
                    onTitleSetted = { newTitle -> onTitleChanged(newTitle) },
                    principalPerson = principalPersonWithAccounts,
                    onConfigurePrincipalPersonRequested = onSettingsClicked,
                    transacciones = allTransactionList
                        .map { trans -> trans.toTransactionAndAccounts() },
                    detailPerson = onPersonDetailRequested
                )
            }
        }
    }

    if (showVertical) {
        Column {
            filter()
            personMonthSummaryView()
            navigationView()
        }
    } else {
        Row {
            Column(modifier = Modifier.widthIn(max = 350.dp)) {
                filter()
                personMonthSummaryView()
            }
            navigationView()
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
            allTransactionList = transactions,
            filteredTransactionList = transactions,
            principalPersonWithAccounts = PersonWithAccounts(
                person = Person(name = "?"),
                emptyList()
            ),
            navPosition = navPosition,
            range = Pair(LocalDate.now(), LocalDate.now()),
            personFilterValue = false,
            sheetState = sheetState,
            snackbarHostState = snackbarHostState,
            delPerson = {
                scope.launch {
                    snackbarHostState.showSnackbar("Delete person requested")
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
            onAddPersonRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Add person requested.")
                }
            },
            onEditPersonRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Edit person ${it.name}")
                }
            },
            onPersonDetailRequested = {},
            onAddAccountRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Add account requested.")
                }
            },
            onEditAccountRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Edit account ${it.name}")
                }
            },
            onAccountDetailRequested = {},
            onAddTransactionRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Add transaction requested")
                }
            },
            onEditTransactionRequested = {
                scope.launch {
                    snackbarHostState.showSnackbar("Edit transaccion ${it.amount}")
                }
            },
            onNavStatusChanged = {
                navPosition = it
            },
            onRangeChanged = { _, _ -> },
            onSettingsClicked = {
                scope.launch {
                    snackbarHostState.showSnackbar("Settings clicked")
                }
            },
            onSaldoActualClick = {},
            onPersonFilterValueChanged = {},
            showVertical = true
        )
    }
}