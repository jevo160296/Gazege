package com.jmml.gazege.ui.fragments

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactions
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.navigation.FullPersonSummaryState
import com.jmml.gazege.ui.navigation.LoadingPersonSummaryState
import com.jmml.gazege.ui.navigation.PersonSummaryState
import com.jmml.gazege.ui.templates.StickyHeaderLayout
import com.jmml.gazege.ui.views.account.AccountSelectionPage
import com.jmml.gazege.ui.views.person.PersonSelectionPage
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.treeview.TreeState
import com.jmml.gazege.ui.widgets.treeview.rememberTreeState

@Composable
fun SaldoActualSettings(
    accountList: List<AccountAndOwnerWithTransactions>,
    personList: List<Person>,
    summaryState: PersonSummaryState,
    saving: Int,
    incluirPresupuestoEnSaldoActual: Boolean,
    incluirDeudasEnSaldoActual: Boolean,
    onIncluirPresupuestoEnSaldoActualChanged: (Boolean) -> Unit,
    onIncluirDeudasEnSaldoActualChanged: (Boolean) -> Unit,
    onPersonStateChanged: (person: Person, nuevoValor: Boolean) -> Unit,
    onUpdateSeleccion: (account: Account, nuevoEstado: Boolean) -> Unit
) {
    val (selectedPage, onSelectedPageChange) = rememberSaveable { mutableIntStateOf(0) }
    val accountListState = rememberTreeState()
    val personListState = rememberLazyListState()
    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = summaryState,
        label = "CrossFade"
    ) {
        when (it) {
            is FullPersonSummaryState -> {
                LoadedSaldoActualSettings(
                    accountList = accountList,
                    summaryState = it,
                    personList = personList,
                    saving = saving,
                    selectedPage = selectedPage,
                    incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
                    incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual,
                    accountListState = accountListState,
                    personListState = personListState,
                    onSelectedPageChange = onSelectedPageChange,
                    onIncluirPresupuestoEnSaldoActualChanged = onIncluirPresupuestoEnSaldoActualChanged,
                    onIncluirDeudasEnSaldoActualChanged = onIncluirDeudasEnSaldoActualChanged,
                    onPersonStateChanged = onPersonStateChanged,
                    onUpdateSeleccion = onUpdateSeleccion
                )
            }

            is LoadingPersonSummaryState -> {
                LoadingSaldoActualSettings()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadedSaldoActualSettings(
    accountList: List<AccountAndOwnerWithTransactions>,
    summaryState: FullPersonSummaryState,
    personList: List<Person>,
    saving: Int,
    selectedPage: Int,
    incluirPresupuestoEnSaldoActual: Boolean,
    incluirDeudasEnSaldoActual: Boolean,
    accountListState: TreeState = rememberTreeState(),
    personListState: LazyListState = rememberLazyListState(),
    onSelectedPageChange: (Int) -> Unit,
    onIncluirPresupuestoEnSaldoActualChanged: (Boolean) -> Unit,
    onIncluirDeudasEnSaldoActualChanged: (Boolean) -> Unit,
    onPersonStateChanged: (person: Person, nuevoValor: Boolean) -> Unit,
    onUpdateSeleccion: (account: Account, nuevoEstado: Boolean) -> Unit
) {
    val contentCanScrollBack = remember(selectedPage) {
        if (selectedPage == 0) {
            { accountListState.listState.run { firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 10 } }
        } else {
            { personListState.run { firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 10 } }
        }
    }
    val headerScrollEnabled = remember(selectedPage) {
        if (selectedPage == 0) {
            { accountList.isNotEmpty() }
        } else {
            { personList.isNotEmpty() }
        }
    }
    Column(Modifier.zIndex(0f)) {
        TopAppBar(
            modifier = Modifier
                .zIndex(1f)
                .background(MaterialTheme.colorScheme.background),
            title = {
                MediumHeadline(text = stringResource(R.string.Ajustes_saldo_actual))
            }
        )
        StickyHeaderLayout(
            contentCanScrollBack = contentCanScrollBack,
            headerScrollEnabled = headerScrollEnabled,
            header = {
                Column {
                    Box(Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))) {
                        LargeBody(text = stringResource(R.string.Ajustes_saldo_actual_desc))
                    }
                    if (saving > 0) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .height(4.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Column(
                        Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
                        ) {
                            Switch(
                                checked = incluirPresupuestoEnSaldoActual,
                                onCheckedChange = onIncluirPresupuestoEnSaldoActualChanged
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(id = R.string.Incluir_presupuesto))
                                Text(text = doubleToMoneyString(summaryState.presupuestoTotal))
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
                        ) {
                            Switch(
                                checked = incluirDeudasEnSaldoActual,
                                onCheckedChange = onIncluirDeudasEnSaldoActualChanged
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(id = R.string.Incluir_deudas))
                                Text(text = doubleToMoneyString(summaryState.deudasTotal))
                            }
                        }
                    }
                }
            }) {
            Column {
                TabRow(selectedTabIndex = selectedPage) {
                    Tab(selected = selectedPage == 0, onClick = { onSelectedPageChange(0) },
                        text = { Text(text = stringResource(id = R.string.cuentas)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_account_balance_wallet_24),
                                "Cuenta"
                            )
                        }
                    )
                    Tab(selected = selectedPage == 1, onClick = { onSelectedPageChange(1) },
                        text = { Text(text = stringResource(id = R.string.personas)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_person_24),
                                "Persona"
                            )
                        }
                    )
                }
                Crossfade(
                    targetState = selectedPage,
                    label = "Crossfade",
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                onSelectedPageChange(if (dragAmount > 0) 0 else 1)
                            }
                        }
                ) {
                    if (it == 0) {
                        AccountSelectionPage(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .nestedScroll(nestedScrollConnection),
                            accountList = accountList,
                            itemHolderPaddingValues = PaddingValues(
                                horizontal = dimensionResource(
                                    id = R.dimen.DefaultPadding
                                )
                            ),
                            treeState = accountListState,
                            onAccountStateChanged = { account, nuevoEstado ->
                                onUpdateSeleccion(account, nuevoEstado)
                            },
                            startDate = null,
                            endDate = null,
                        )
                    } else {
                        PersonSelectionPage(
                            modifier = Modifier.nestedScroll(nestedScrollConnection),
                            principalPersonSummaryState = summaryState,
                            personList = personList,
                            onPersonStateChanged = onPersonStateChanged,
                            state = personListState,
                            enabled = incluirDeudasEnSaldoActual,
                            contentPadding = PaddingValues(dimensionResource(id = R.dimen.DefaultPadding))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingSaldoActualSettings() {
    // TODO Develop UI for loading saldo actual
    Text(text = "Loading")
}