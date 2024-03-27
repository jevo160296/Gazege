package com.jmml.gazege.ui.fragments

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jmml.gazege.NavPosition
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactions
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.data.SampleId
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.accountDeleitionConfirmationBuilder
import com.jmml.gazege.ui.navigation.EditarCategoriasState
import com.jmml.gazege.ui.navigation.EmptyEditarCategoriasState
import com.jmml.gazege.ui.navigation.EmptyPersonSummaryState
import com.jmml.gazege.ui.navigation.FullPersonSummaryState
import com.jmml.gazege.ui.navigation.LoadedEditarCategoriasState
import com.jmml.gazege.ui.navigation.LoadedTransactionDetailsState
import com.jmml.gazege.ui.navigation.LoadingPersonSummaryState
import com.jmml.gazege.ui.navigation.PersonSummaryState
import com.jmml.gazege.ui.navigation.ReloadingPersonSummaryState
import com.jmml.gazege.ui.navigation.TransactionDetailsState
import com.jmml.gazege.ui.navigation.loadingTransactionDetailsState
import com.jmml.gazege.ui.personaDeleitionConfirmationBuilder
import com.jmml.gazege.ui.templates.DynamicAddEntityFAB
import com.jmml.gazege.ui.theme.AppMode
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.transactionDeleitionConfirmationBuilder
import com.jmml.gazege.ui.views.AddTransactionAction
import com.jmml.gazege.ui.views.account.LoadedAccountPage
import com.jmml.gazege.ui.views.account.LoadingAccountPage
import com.jmml.gazege.ui.views.person.LoadedPersonPage
import com.jmml.gazege.ui.views.person.NoPrincipalPersonPersonPage
import com.jmml.gazege.ui.views.transaction.LoadedTransactionPage
import com.jmml.gazege.ui.views.transaction.LoadingTransactionPage
import com.jmml.gazege.ui.widgets.BooleanFilters
import com.jmml.gazege.ui.widgets.DatePicker
import com.jmml.gazege.ui.widgets.DoubleFilter
import com.jmml.gazege.ui.widgets.EmptyPersonMonthSummaryView
import com.jmml.gazege.ui.widgets.Filter
import com.jmml.gazege.ui.widgets.GIndefiniteCircularProgressIndicator
import com.jmml.gazege.ui.widgets.LoadedPersonMonthSummaryView
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.ModalSheetContent
import com.jmml.gazege.ui.widgets.TextFilter
import com.jmml.gazege.ui.widgets.booleanFilterOf
import com.jmml.gazege.ui.widgets.treeview.TreeState
import com.jmml.gazege.ui.widgets.treeview.rememberTreeState
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
    valueFilterState: DoubleFilter,
    snackbarHostState: SnackbarHostState,
    today: LocalDate,
    delPerson: (Person) -> Unit,
    delAccount: (Account) -> Unit,
    delTransaction: (Transaction) -> Unit,
    delCategory: (Category) -> Unit,
    onAddPersonRequested: () -> Unit,
    onEditPersonRequested: (Person) -> Unit,
    onPersonDetailRequested: (Person) -> Unit,
    onAddAccountRequested: () -> Unit,
    onEditAccountRequested: (Account) -> Unit,
    onAccountDetailRequested: (Account) -> Unit,
    onAddTransactionRequested: (action: AddTransactionAction) -> Unit,
    onEditTransactionRequested: (Transaction) -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit,
    onNavStatusChanged: (NavPosition) -> Unit,
    onRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onSettingsClicked: () -> Unit,
    onSaldoActualClick: () -> Unit,
    onPersonFilterValueChanged: (Boolean) -> Unit,
    onTransactionFiltersChanged: (newValue: BooleanFilters<String, Nothing>) -> Unit,
    onCategoriesFilterChanged: (newValue: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    descriptionFilterState: TextFilter,
    onDescriptionFilterStateChanged: (TextFilter) -> Unit,
    onValueFilterStateChanged: (DoubleFilter) -> Unit,
    onShowTypeChanged: (EditarCategoriasShowType) -> Unit,
    onInitDatabaseSample: (sampleId: SampleId) -> Unit,
    onTodayChangeRequested: (newDate: LocalDate) -> Unit,
    onExportCategoryRequested: (Category) -> Unit,
    showVertical: Boolean,
    showType: EditarCategoriasShowType,
    categoriasState: EditarCategoriasState
) {
    val transactionState = rememberLazyListState()
    val accountState = rememberTreeState()
    val personState = rememberLazyListState()
    val categoryListState = rememberTreeState()

    val scope = rememberCoroutineScope()
    var title: String by rememberSaveable {
        mutableStateOf("Gazege")
    }
    var fabExpanded: Boolean by remember {
        mutableStateOf(false)
    }
    var debugMenuExpanded: Boolean by remember {
        mutableStateOf(false)
    }
    val transactionMessageBuilder = transactionDeleitionConfirmationBuilder()
    val accountMessageBuilder = accountDeleitionConfirmationBuilder()
    val personaMessageBuilder = personaDeleitionConfirmationBuilder()
    val categoryMessageBuilder = stringResource(id = R.string.confirma_la_eliminacion_de)
        .let { categoryTemplate ->
            { categoryName: String -> categoryTemplate.format(categoryName) }
        }


    val yesLabel = stringResource(id = R.string.Si)

    Scaffold(
        floatingActionButton = {
            DynamicAddEntityFAB(
                fabExpanded = fabExpanded,
                onFabExpandedChanged = { fabExpanded = it },
                navPosition = navPosition,
                onAddPersonRequested = onAddPersonRequested,
                onAddAccountRequested = onAddAccountRequested,
                onAddTransactionRequested = onAddTransactionRequested,
                onAddCategoryRequested = onNavigateToAddCategory
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = navPosition == NavPosition.TRANSACCIONES,
                    onClick = { onNavStatusChanged(NavPosition.TRANSACCIONES) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.transaccion),
                            contentDescription = "Transactions"
                        )
                    })
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
                NavigationBarItem(
                    selected = navPosition == NavPosition.CATEGORIAS,
                    onClick = { onNavStatusChanged(NavPosition.CATEGORIAS) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.categorias),
                            contentDescription = "Categorías"
                        )
                    })
                NavigationBarItem(
                    selected = navPosition == NavPosition.PERSONS,
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
                                DropdownMenuItem(
                                    text = { Text(text = "Variable fixed category sample") },
                                    onClick = { onInitDatabaseSample(SampleId.VariableFixedCategorySample) })
                                DatePicker(
                                    value = today,
                                    onValueChange = onTodayChangeRequested
                                )
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
            principalPersonSummaryState = principalPersonSummaryState,
            personFilterValue = personFilterValue,
            delPerson = {
                scope.launch {
                    val response = snackbarHostState.showSnackbar(
                        message = personaMessageBuilder(it.name),
                        actionLabel = "Yes",
                        withDismissAction = true
                    )
                    if (response == SnackbarResult.ActionPerformed) {
                        delPerson(it)
                    }
                }
            },
            delAccount = {
                scope.launch {
                    val response = snackbarHostState.showSnackbar(
                        message = accountMessageBuilder(it.name),
                        actionLabel = "Yes",
                        withDismissAction = true
                    )
                    if (response == SnackbarResult.ActionPerformed) {
                        delAccount(it)
                    }
                }
            },
            delTransaction = {
                scope.launch {
                    val response = snackbarHostState.showSnackbar(
                        message = transactionMessageBuilder(),
                        actionLabel = "Yes",
                        withDismissAction = true
                    )
                    if (response == SnackbarResult.ActionPerformed) {
                        delTransaction(it)
                    }
                }
            },
            delCategory = {
                scope.launch {
                    val response = snackbarHostState.showSnackbar(
                        message = categoryMessageBuilder(it.name),
                        actionLabel = yesLabel,
                        withDismissAction = true
                    )
                    if (response == SnackbarResult.ActionPerformed) {
                        delCategory(it)
                    }
                }
            },
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
            onTitleChanged = { newTitle -> title = newTitle },
            onSettingsClicked = onSettingsClicked,
            transactionFilters = transactionFilters,
            onTransactionFiltersChanged = onTransactionFiltersChanged,
            categoriesFilter = categoriesFilter,
            onCategoriesFilterChanged = onCategoriesFilterChanged,
            valueFilterState = valueFilterState,
            onValueFilterStateChanged = onValueFilterStateChanged,
            descriptionFilterState = descriptionFilterState,
            onDescriptionFilterStateChanged = onDescriptionFilterStateChanged,
            showType = showType,
            categoriasState = categoriasState,
            categoryState = categoryListState,
            onShowTypeChanged = onShowTypeChanged,
            onNavigateToEditCategory = onNavigateToEditCategory,
            onNavigateToAddBudget = onNavigateToAddBudget,
            onExportCategoryRequested = onExportCategoryRequested,
            showVertical = showVertical
        )
    }
}

private fun Density.toDp(valuePx: Float): Dp = valuePx.div(this.density).dp

private fun Density.toPx(valueDp: Dp): Float = valueDp.times(this.density).value

private fun Density.toPx(valueDp: Float): Float = valueDp.times(this.density)

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
    delCategory: (Category) -> Unit,
    onEditPersonRequested: (Person) -> Unit,
    onPersonDetailRequested: (Person) -> Unit,
    onEditAccountRequested: (Account) -> Unit,
    onAccountDetailRequested: (Account) -> Unit,
    onEditTransactionRequested: (Transaction) -> Unit,
    onRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onSaldoActualClick: () -> Unit,
    onPersonFilterValueChanged: (Boolean) -> Unit,
    transactionState: LazyListState,
    categoryState: TreeState,
    accountState: TreeState,
    personState: LazyListState,
    navPosition: NavPosition,
    range: Pair<LocalDate?, LocalDate?>,
    onTitleChanged: (String) -> Unit,
    onSettingsClicked: () -> Unit,
    transactionFilters: BooleanFilters<String, Nothing>,
    onTransactionFiltersChanged: (newValue: BooleanFilters<String, Nothing>) -> Unit,
    categoriesFilter: BooleanFilters<Int?, Pair<String, Int>>,
    onCategoriesFilterChanged: (newValue: BooleanFilters<Int?, Pair<String, Int>>) -> Unit,
    valueFilterState: DoubleFilter,
    onValueFilterStateChanged: (DoubleFilter) -> Unit,
    descriptionFilterState: TextFilter,
    onDescriptionFilterStateChanged: (TextFilter) -> Unit,
    showType: EditarCategoriasShowType,
    categoriasState: EditarCategoriasState,
    onShowTypeChanged: (EditarCategoriasShowType) -> Unit,
    onNavigateToEditCategory: (Int?) -> Unit,
    onNavigateToAddBudget: (Int?) -> Unit,
    onExportCategoryRequested: (Category) -> Unit,
    showVertical: Boolean
) {
    val d = LocalDensity.current
    var offsetYDP by rememberSaveable { mutableFloatStateOf(0f) }

    val animatedOffsetYDP by animateFloatAsState(targetValue = offsetYDP, label = "Size")
    var maxOffsetYDP by remember { mutableFloatStateOf(0f) }
    val paddingValues = PaddingValues(
        top = 8.dp,
        bottom = dimensionResource(id = R.dimen.FABDefaultSpace),
        start = 8.dp,
        end = 8.dp
    )
    var isFirstElementVisible by rememberSaveable { mutableStateOf(false) }
    var hasZeroElements: Boolean? by rememberSaveable { mutableStateOf(null) }
    val innerNestedScrollConnection = remember(isFirstElementVisible) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (
                    !((0f..20f).contains(available.y)) ||
                    source == NestedScrollSource.Wheel ||
                    isFirstElementVisible
                ) {
                    val newOffset =
                        (offsetYDP + d.toDp(available.y).value).coerceIn(-maxOffsetYDP..0f)
                    val returnedOffset = if (newOffset != offsetYDP) {
                        available
                    } else {
                        Offset.Zero
                    }
                    offsetYDP = newOffset
                    returnedOffset
                } else {
                    Offset.Zero
                }
            }
        }
    }
    val startDate = range.first
    val endDate = range.second
    val onZeroElementsChanged = remember {
        { newHasZeroElements: Boolean ->
            if (hasZeroElements != newHasZeroElements) {
                hasZeroElements = newHasZeroElements
            }
            if (newHasZeroElements && offsetYDP != 0f) {
                offsetYDP = 0f
            }
        }
    }
    val onFirstElementVisibleChanged =
        remember { { isVisible: Boolean -> isFirstElementVisible = isVisible } }

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
            onCategoriesFilterChanged = onCategoriesFilterChanged,
            valueFilterState = valueFilterState,
            onValueFilterStateChanged = onValueFilterStateChanged,
            descriptionFilterState = descriptionFilterState,
            onDescriptionFilterStateChanged = onDescriptionFilterStateChanged
        )
    }

    val personMonthSummaryView = @Composable {
        Crossfade(targetState = principalPersonSummaryState, label = "CrossFadePerson") {
            when (it) {
                is ReloadingPersonSummaryState -> {
                    EmptyPersonMonthSummaryView(
                        modifier = Modifier.fillMaxWidth(),
                        saldoActual = it.saldoActual,
                        ingresos = it.ingresos,
                        egresos = it.egresos,
                        flujo = it.flujo,
                        onSaldoActualClick = onSaldoActualClick
                    )
                }

                is FullPersonSummaryState -> {
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

    val categoriasPage = @Composable {
        onTitleChanged(stringResource(id = R.string.Categorias))
        when (categoriasState) {
            is LoadedEditarCategoriasState -> LoadedEditarCategorias(
                editarCategoriasState = categoriasState,
                paddingValues = paddingValues,
                onEditCategoryRequested = { onNavigateToEditCategory(it.id) },
                onDeleteCategoryRequested = { delCategory(it) },
                onSetBudgetRequested = { onNavigateToAddBudget(it.id) },
                onExportCategoryRequested = onExportCategoryRequested,
                showType = showType,
                onShowTypeChanged = onShowTypeChanged,
                nestedScrollConnection = innerNestedScrollConnection,
                onZeroElementsChanged = onZeroElementsChanged,
                state = categoryState,
                onFirstElementVisibleChanged = onFirstElementVisibleChanged
            )

            is EmptyEditarCategoriasState -> EmptyEditarCategorias(
                editarCategoriasState = categoriasState,
                paddingValues = paddingValues
            )
        }
    }

    val transactionPage = @Composable {
        Column {
            Crossfade(targetState = filteredTransactionList, label = "CrosFade transactions") {
                when (it) {
                    is LoadedTransactionDetailsState -> {
                        LoadedTransactionPage(
                            transactionList = it.transactionList,
                            itemHolderPaddingValues = paddingValues,
                            state = transactionState,
                            delTransaction = delTransaction,
                            editTransaction = onEditTransactionRequested,
                            onTitleSetted = { newTitle -> onTitleChanged(newTitle) },
                            nestedScrollConnection = innerNestedScrollConnection,
                            onZeroElementsChanged = onZeroElementsChanged,
                            onFirstElementVisibleChanged = onFirstElementVisibleChanged
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
        when (principalPersonSummaryState) {
            is FullPersonSummaryState -> {
                LoadedAccountPage(
                    accountList = accountList.filter { person ->
                        person.owner.id == principalPersonSummaryState.person.id
                    },
                    itemHolderPaddingValues = paddingValues,
                    treeState = accountState,
                    delAccount = delAccount,
                    editAccount = onEditAccountRequested,
                    startDate = null,
                    endDate = null,
                    detailAccount = onAccountDetailRequested,
                    nestedScrollConnection = innerNestedScrollConnection,
                    onZeroElementsChanged = onZeroElementsChanged,
                    onFirstElementVisibilityChanged = onFirstElementVisibleChanged
                ) { newTitle -> onTitleChanged(newTitle) }
            }

            is LoadingPersonSummaryState -> {
                onTitleChanged(stringResource(id = R.string.cuentas))
                LoadingAccountPage()
            }
        }
    }

    val personsPage = @Composable {
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
                        delPerson = delPerson,
                        editPerson = onEditPersonRequested,
                        onTitleSetted = { newTitle -> onTitleChanged(newTitle) },
                        detailPerson = onPersonDetailRequested,
                        principalPersonSummaryState = it,
                        nestedScrollConnection = innerNestedScrollConnection,
                        onZeroElementsChanged = onZeroElementsChanged,
                        onFirstElementVisibleChanged = onFirstElementVisibleChanged
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
                        GIndefiniteCircularProgressIndicator()
                        Text(stringResource(id = R.string.LoadingPersonSummaryView))
                    }
                }
            }
        }
    }

    val navigationView = @Composable {
        Crossfade(targetState = navPosition, label = "navigationView") {
            when (it) {
                NavPosition.CATEGORIAS -> {
                    categoriasPage()
                }

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
        Box(Modifier.padding(layoutPaddingValues)) {
            Box(Modifier.padding(top = (maxOffsetYDP.dp + animatedOffsetYDP.dp).coerceAtLeast(0.dp))) {
                navigationView()
            }
            Column(Modifier
                .onGloballyPositioned { maxOffsetYDP = with(d) { it.size.height.div(density) } }
                .offset {
                    IntOffset(
                        0,
                        d
                            .toPx(animatedOffsetYDP)
                            .toInt()
                    )
                }
                .pointerInput(1) {
                    detectDragGestures { change, dragAmount ->
                        if (hasZeroElements?.not() == true) {
                            change.consume()
                            innerNestedScrollConnection.onPreScroll(
                                dragAmount,
                                NestedScrollSource.Wheel
                            )
                        }
                    }
                }
            ) {
                filter()
                personMonthSummaryView()
            }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DefaultPreview() {
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
                principalPersonSummaryState = personSummaryStateSample,
                navPosition = navPosition,
                range = Pair(LocalDate.now(), LocalDate.now()),
                personFilterValue = false,
                transactionFilters = booleanFilterOf(emptyList()),
                categoriesFilter = booleanFilterOf(emptyList()),
                valueFilterState = DoubleFilter(0.0f..0.0f, 0.0f..0.0f),
                snackbarHostState = snackbarHostState,
                today = LocalDate.now(),
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
                delCategory = {},
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
                onNavigateToAddCategory = {},
                onNavigateToEditCategory = {},
                onNavigateToAddBudget = {},
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
                onTransactionFiltersChanged = {},
                onCategoriesFilterChanged = {},
                descriptionFilterState = TextFilter(null),
                onDescriptionFilterStateChanged = {},
                onValueFilterStateChanged = {},
                onShowTypeChanged = {},
                onInitDatabaseSample = {},
                onTodayChangeRequested = {},
                onExportCategoryRequested = {},
                showVertical = true,
                showType = EditarCategoriasShowType.COMPACT,
                categoriasState = EmptyEditarCategoriasState
            )
        }
    }
}