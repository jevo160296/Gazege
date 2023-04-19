package com.example.gazege.ui.fragments

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.NavPosition
import com.example.gazege.PersonSummaryState
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.accountDeleitionConfirmationBuilder
import com.example.gazege.ui.personaDeleitionConfirmationBuilder
import com.example.gazege.ui.templates.DynamicAddEntityFAB
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.transactionDeleitionConfirmationBuilder
import com.example.gazege.ui.views.*
import com.example.gazege.ui.views.account.AccountPage
import com.example.gazege.ui.views.person.PersonPage
import com.example.gazege.ui.views.transaction.TransactionPage
import com.example.gazege.ui.widgets.*
import com.example.gazege.ui.widgets.treeview.TreeState
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainFragment(
    allPerson: List<Person>,
    accountList: List<AccountAndOwnerWithTransactions>,
    filteredTransactionList: List<TransactionListItemDetails>,
    principalPersonSummaryState: PersonSummaryState?,
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
                DynamicAddEntityFAB(
                    fabExpanded = fabExpanded,
                    onFabExpandedChanged = { fabExpanded = it },
                    navPosition = navPosition,
                    onAddPersonRequested = onAddPersonRequested,
                    onAddAccountRequested = onAddAccountRequested,
                    onAddTransactionRequested = onAddTransactionRequested
                )
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
                allPerson = allPerson,
                accountList = accountList,
                filteredTransactionList = filteredTransactionList,
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
                onActionChanged = { newAction -> action = newAction },
                onModalSheetMsgChanged = { newMsg -> modalSheetMsg = newMsg },
                scope = scope,
                sheetState = sheetState,
                onTitleChanged = { newTitle -> title = newTitle },
                onSettingsClicked = onSettingsClicked,
                showVertical = showVertical,
                principalPersonSummaryState = principalPersonSummaryState
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainFragmentResponsiveContent(
    layoutPaddingValues: PaddingValues,
    allPerson: List<Person>,
    accountList: List<AccountAndOwnerWithTransactions>,
    filteredTransactionList: List<TransactionListItemDetails>,
    principalPersonSummaryState: PersonSummaryState?,
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
            saldoActual = principalPersonSummaryState?.saldoActual ?: 0.0,
            ingresos = principalPersonSummaryState?.ingresos ?: 0.0,
            egresos = principalPersonSummaryState?.egresos ?: 0.0,
            flujo = principalPersonSummaryState?.flujo ?: 0.0,
            onSaldoActualClick = onSaldoActualClick
        )
    }

    val transactionPage = @Composable {
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

    val cuentasPage = @Composable {
        val template = accountDeleitionConfirmationBuilder()
        AccountPage(
            accountList = accountList.filter { person ->
                person.owner.id == principalPersonSummaryState?.person?.id
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

    val personsPage = @Composable {
        val template = personaDeleitionConfirmationBuilder()
        PersonPage(
            allPerson = allPerson.filter { person ->
                person.id != principalPersonSummaryState?.person?.id
            }.filter { personWithAccounts ->
                if (personFilterValue) {
                    val flujo =
                        principalPersonSummaryState?.deudasFlujo?.get(personWithAccounts) ?: 0.0
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
            onConfigurePrincipalPersonRequested = onSettingsClicked,
            detailPerson = onPersonDetailRequested,
            principalPersonSummaryState = principalPersonSummaryState
        )
    }

    val navigationView = @Composable {
        Crossfade(targetState = navPosition) {
            when (it) {
                NavPosition.TRANSACCIONES -> {
                    transactionPage()
                }

                NavPosition.CUENTAS -> {
                    cuentasPage()
                }

                NavPosition.PERSONS -> {
                    personsPage()
                }
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
    val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var navPosition by remember {
        mutableStateOf(NavPosition.TRANSACCIONES)
    }
    val snackbarHostState = SnackbarHostState()
    DatabaseSample {
        GazegeTheme(darkTheme = true) {
            MainFragment(
                allPerson = personSample,
                accountList = accountAndOwnerWithTransactionsSample,
                filteredTransactionList = transactionListItemDetailsSample,
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
                showVertical = true,
                principalPersonSummaryState = personSummaryStateSample
            )
        }
    }
}