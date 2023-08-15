package com.example.gazege.ui.fragments

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.NavPosition
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.sample.data.SampleId
import com.example.gazege.ui.DatabaseSample
import com.example.gazege.ui.accountDeleitionConfirmationBuilder
import com.example.gazege.ui.navigation.EmptyPersonSummaryState
import com.example.gazege.ui.navigation.FullPersonSummaryState
import com.example.gazege.ui.navigation.LoadedPersonSummaryState
import com.example.gazege.ui.navigation.LoadedTransactionDetailsState
import com.example.gazege.ui.navigation.LoadingPersonSummaryState
import com.example.gazege.ui.navigation.PersonSummaryState
import com.example.gazege.ui.navigation.TransactionDetailsState
import com.example.gazege.ui.navigation.loadingTransactionDetailsState
import com.example.gazege.ui.personaDeleitionConfirmationBuilder
import com.example.gazege.ui.templates.DynamicAddEntityFAB
import com.example.gazege.ui.theme.AppMode
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.transactionDeleitionConfirmationBuilder
import com.example.gazege.ui.views.*
import com.example.gazege.ui.views.account.LoadedAccountPage
import com.example.gazege.ui.views.account.LoadingAccountPage
import com.example.gazege.ui.views.person.LoadedPersonPage
import com.example.gazege.ui.views.person.NoPrincipalPersonPersonPage
import com.example.gazege.ui.views.transaction.LoadedTransactionPage
import com.example.gazege.ui.views.transaction.LoadingTransactionPage
import com.example.gazege.ui.widgets.*
import com.example.gazege.ui.widgets.treeview.TreeState
import com.example.gazege.ui.widgets.treeview.rememberTreeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFragment(
    allPerson: List<Person>,
    accountList: List<AccountAndOwnerWithTransactions>,
    filteredTransactionList: TransactionDetailsState,
    principalPersonSummaryState: PersonSummaryState,
    navPosition: NavPosition,
    range: Pair<LocalDate?, LocalDate?>,
    personFilterValue: Boolean,
    transactionFilters: BooleanFilters<String, Nothing>,
    categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
    sheetState: SheetState,
    drawerState: DrawerState,
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
    onOpenCategoriesRequested: () -> Unit,
    onOpenBudgetRequested: () -> Unit,
    onTransactionFiltersChanged: (newValue: BooleanFilters<String, Nothing>) -> Unit,
    onCategoriesFilterChanged: (newValue: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    onInitDatabaseSample: (sampleId: SampleId) -> Unit,
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
    var debugMenuExpanded: Boolean by remember {
        mutableStateOf(false)
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                windowInsets = dimensionResource(id = R.dimen.DefaultPadding)
                    .let {
                        WindowInsets(it, it + 24.dp, it, it)
                    }
            ) {
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.Categorias)) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.categorias),
                            contentDescription = "Categorías"
                        )
                    },
                    selected = false,
                    onClick = onOpenCategoriesRequested
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.Presupuesto)) },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.presupuesto),
                            contentDescription = "Presupuesto"
                        )
                    },
                    selected = false,
                    onClick = onOpenBudgetRequested
                )
            }
        },
        drawerState = drawerState
    ) {
        ModalSheetLayout(
            modalSheetMsg = modalSheetMsg,
            onModalSheetMsgChanged = { modalSheetMsg = it },
            action = action,
            onActionChanged = { action = it },
            sheetState = sheetState
        ) {
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
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = navPosition == NavPosition.CUENTAS,
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
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.menu),
                                    contentDescription = "Open menu"
                                )
                            }
                        },
                        actions = {
                            val uriHandler = LocalUriHandler.current
                            if (GazegeTheme.appMode == AppMode.DEBUG) {
                                Box(Modifier.wrapContentSize(Alignment.TopStart)) {
                                    IconButton(
                                        onClick = { debugMenuExpanded = true }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_baseline_add_24),
                                            contentDescription = "Add sample data"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = debugMenuExpanded,
                                        onDismissRequest = { debugMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(text = "Small") },
                                            onClick = { onInitDatabaseSample(SampleId.SmallSample) })
                                        DropdownMenuItem(
                                            text = { Text(text = "Big") },
                                            onClick = { onInitDatabaseSample(SampleId.BigSample) })
                                        DropdownMenuItem(
                                            text = { Text(text = "Category sample") },
                                            onClick = { onInitDatabaseSample(SampleId.CategoriesSample) })
                                        DropdownMenuItem(
                                            text = { Text(text = "Category with miultiple budget sample") },
                                            onClick = { onInitDatabaseSample(SampleId.CategoriesMultipleBudgetSample) })
                                    }
                                }
                                IconButton(onClick = {
                                    uriHandler.openUri("https://forms.gle/Qb1aek3QX9r24Gw26")
                                }) {
                                    Icon(
                                        painter = painterResource(
                                            id = R.drawable.bug_report
                                        ), contentDescription = "Report bug"
                                    )
                                }
                            }
                            IconButton(onClick = onSettingsClicked) {
                                Icon(
                                    painter = painterResource(
                                        id = R.drawable.baseline_settings_24
                                    ), contentDescription = "Settings"
                                )
                            }
                        }
                    )
                },
                contentWindowInsets = WindowInsets.statusBars
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
                    principalPersonSummaryState = principalPersonSummaryState,
                    transactionFilters = transactionFilters,
                    onTransactionFiltersChanged = onTransactionFiltersChanged,
                    categoriesFilter = categoriesFilter,
                    onCategoriesFilterChanged = onCategoriesFilterChanged
                )
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        drawerState.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainFragmentResponsiveContent(
    layoutPaddingValues: PaddingValues,
    allPerson: List<Person>,
    accountList: List<AccountAndOwnerWithTransactions>,
    filteredTransactionList: TransactionDetailsState,
    principalPersonSummaryState: PersonSummaryState,
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
    sheetState: SheetState,
    onTitleChanged: (String) -> Unit,
    onSettingsClicked: () -> Unit,
    transactionFilters: BooleanFilters<String, Nothing>,
    onTransactionFiltersChanged: (newValue: BooleanFilters<String, Nothing>) -> Unit,
    categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
    onCategoriesFilterChanged: (newValue: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    showVertical: Boolean
) {
    val paddingValues = PaddingValues(
        top = 8.dp,
        bottom = dimensionResource(id = R.dimen.FABDefaultSpace),
        start = 8.dp,
        end = 8.dp
    )
    val startDate = range.first
    val endDate = range.second

    val filter = @Composable {
        Filter(
            Modifier.fillMaxWidth(),
            startDate = startDate,
            endDate = endDate,
            onRangeChanged = onRangeChanged,
            personFilterVisible = navPosition == NavPosition.PERSONS,
            personFilterValue = personFilterValue,
            onPersonFilterValueChanged = onPersonFilterValueChanged,
            transactionsFilterVisible = navPosition == NavPosition.TRANSACCIONES,
            transactionFilters = transactionFilters,
            onTransactionFiltersChanged = onTransactionFiltersChanged,
            categoriesFilter = categoriesFilter,
            onCategoriesFilterChanged = onCategoriesFilterChanged
        )
    }

    val personMonthSummaryView = @Composable {
        Crossfade(targetState = principalPersonSummaryState, label = "CrossFadePerson") {
            when (it) {
                is LoadedPersonSummaryState -> {
                    LoadedPersonMonthSummaryView(
                        modifier = Modifier.fillMaxWidth(),
                        saldoActual = it.saldoActual,
                        ingresos = it.ingresos,
                        egresos = it.egresos,
                        flujo = it.flujo,
                        onSaldoActualClick = onSaldoActualClick
                    )
                }

                is LoadingPersonSummaryState -> {
                    EmptyPersonMonthSummaryView(
                        modifier = Modifier.fillMaxWidth(),
                        onSaldoActualClick = onSaldoActualClick
                    )
                }
            }
        }
    }

    val transactionPage = @Composable {
        val template = transactionDeleitionConfirmationBuilder()
        Column {
            Crossfade(targetState = filteredTransactionList, label = "CrosFade transactions") {
                when (it) {
                    is LoadedTransactionDetailsState -> {
                        LoadedTransactionPage(
                            transactionList = it.transactionList,
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

                    loadingTransactionDetailsState() -> {
                        LoadingTransactionPage(
                            onTitleSetted = { newTitle -> onTitleChanged(newTitle) },
                            itemHolderPaddingValues = paddingValues
                        )
                    }
                }
            }
        }
    }

    val cuentasPage = @Composable {
        val template = accountDeleitionConfirmationBuilder()
        when (principalPersonSummaryState) {
            is FullPersonSummaryState -> {
                LoadedAccountPage(
                    accountList = accountList.filter { person ->
                        person.owner.id == principalPersonSummaryState.person.id
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

            is LoadingPersonSummaryState -> {
                onTitleChanged(stringResource(id = R.string.cuentas))
                LoadingAccountPage()
            }
        }
    }

    val personsPage = @Composable {
        val template = personaDeleitionConfirmationBuilder()
        Crossfade(targetState = principalPersonSummaryState, label = "CrossFade") {
            when (it) {
                is FullPersonSummaryState -> {
                    LoadedPersonPage(
                        allPerson = allPerson.filter { person ->
                            person.id != it.person.id
                        }.filter { personWithAccounts ->
                            if (personFilterValue) {
                                val flujo =
                                    it.deudasFlujo[personWithAccounts]
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
                        detailPerson = onPersonDetailRequested,
                        principalPersonSummaryState = it
                    )
                }

                EmptyPersonSummaryState -> {
                    NoPrincipalPersonPersonPage(
                        onConfigurePrincipalPersonRequested = onSettingsClicked,
                        onTitleSetted = { newTitle -> onTitleChanged(newTitle) }
                    )
                }

                is LoadingPersonSummaryState -> {
                    onTitleChanged(stringResource(id = R.string.personas))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.DefaultPadding)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GazegeIndefiniteCircularProgressIndicator()
                        Text(stringResource(id = R.string.LoadingPersonSummaryView))
                    }
                }
            }
        }
    }

    val navigationView = @Composable {
        Crossfade(targetState = navPosition, label = "navigationView") {
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
        Column(Modifier.padding(layoutPaddingValues)) {
            filter()
            personMonthSummaryView()
            navigationView()
        }
    } else {
        Row(Modifier.padding(layoutPaddingValues)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DefaultPreview() {
    val sheetState = rememberModalBottomSheetState()
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
                filteredTransactionList = LoadedTransactionDetailsState(
                    transactionListItemDetailsSample
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
                showVertical = true,
                principalPersonSummaryState = personSummaryStateSample,
                drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
                onOpenBudgetRequested = {},
                onOpenCategoriesRequested = {},
                transactionFilters = booleanFilterOf(emptyList()),
                onTransactionFiltersChanged = {},
                onInitDatabaseSample = {},
                categoriesFilter = booleanFilterOf(emptyList()),
                onCategoriesFilterChanged = {}
            )
        }
    }
}