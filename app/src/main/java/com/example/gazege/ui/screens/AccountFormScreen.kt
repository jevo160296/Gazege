package com.example.gazege.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.PartialAccount
import com.example.gazege.ui.savers.PartialAccountAndOwner
import com.example.gazege.ui.savers.accountAndOwnerSaver
import com.example.gazege.ui.views.account.AccountAndOwnerForm
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.toSignedBigDecimal
import kotlinx.coroutines.launch

fun NavGraphBuilder.screenAddAccount(
    viewModel: MainViewModel,
    onNavigateToAddPerson: () -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable("addAccount") {
        val allPerson by viewModel.allPerson.observeAsState(emptyList())
        val allAccount by viewModel.allAccount.observeAsState(emptyList())
        val incomeAccount by viewModel.incomeAccount.observeAsState()
        val outcomeAccount by viewModel.outcomeAccount.observeAsState()
        val accountAndOwnerWithTransactions by viewModel.accountAndOwnerWithTransactions.observeAsState(
            emptyList()
        )

        val coroutineScope = rememberCoroutineScope()
        AccountFormScreen(
            contentPadding = PaddingValues(8.dp),
            itemSpacing = 8.dp,
            personList = allPerson,
            onPersonAddRequested = onNavigateToAddPerson,
            onAccountAndOwnerAdd = { account, newBalance, snackBarHostState, incomeAccountId, outcomeAccountId ->
                val accountOwnerIdList = allAccount.map {
                    Pair(it.name, it.ownerId)
                }
                val accountOwnerId = Pair(account.name, account.ownerId)
                val sePuedeAgregar = accountOwnerId !in accountOwnerIdList
                if (sePuedeAgregar) {
                    viewModel.insertAccount(
                        account,
                        onErrorAction = {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Error añadiento cuenta $it")
                            }
                        },
                        onCompleitionAction = { addedId ->
                            if (incomeAccountId != null && outcomeAccountId != null) {
                                val valorAjuste = newBalance
                                viewModel.realizarAjuste(
                                    accountId = addedId.toInt(),
                                    amount = valorAjuste,
                                    incomeAccountId = incomeAccountId,
                                    outcomeAccountId = outcomeAccountId
                                )
                            }
                        }).invokeOnCompletion {
                        if (it == null) {
                            onNavigateUp()
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("Las personas no pueden tener cuentas con nombres repetidos")
                    }
                }
            },
            currentBalance = 0.0,
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onSetIncomeOutcomeAccount = onNavigateToSettings,
            accountAndOwnerList = accountAndOwnerWithTransactions.map {
                AccountAndOwner(it.account, it.owner)
            }
        )
    }
}

fun NavController.navigateToAddAccount() {
    navigate("addAccount")
}

@Composable
fun AccountFormScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    accountAndOwner: AccountAndOwner? = null,
    accountAndOwnerList: List<AccountAndOwner>,
    personList: List<Person>,
    currentBalance: Double,
    incomeAccount: Account?,
    outcomeAccount: Account?,
    onSetIncomeOutcomeAccount: () -> Unit,
    onPersonAddRequested: () -> Unit,
    onAccountAndOwnerAdd: (Account, Double, SnackbarHostState, Int?, Int?) -> Unit
) {
    var currentBalanceState by rememberSaveable {
        mutableStateOf(currentBalance.toSignedBigDecimal())
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
                            ownerId = it.ownerId,
                            includedInTotal = it.includedInTotal,
                            isIncome = it.isIncome,
                            isOutcome = it.isOutcome,
                            parentId = it.parentId
                        )
                    },
                    owner = accountAndOwner.owner
                )
            } else {
                PartialAccountAndOwner.blankEntity()
            }
        )
    }
    val completeState = accountAndOwnerState.isComplete()
    val snackbarHostState = SnackbarHostState()
    val saveAccount = {
        val fullAccountAndOwner = accountAndOwnerState.toFull()
        onAccountAndOwnerAdd(
            fullAccountAndOwner.account,
            currentBalanceState.toDouble(),
            snackbarHostState,
            incomeAccount?.id,
            outcomeAccount?.id
        )
    }
    Form(
        modifier = modifier,
        onSaveClicked = saveAccount,
        isSavedButtonEnabled = completeState,
        snackbarHostState = snackbarHostState,
        title = "Account"
    ) {
        AccountAndOwnerForm(
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            accountAndOwner = accountAndOwnerState,
            accountAndOwnerList = accountAndOwnerList,
            personList = personList,
            currentBalance = currentBalanceState,
            onCurrentBalanceChanged = { currentBalanceState = it },
            onPersonAddRequested = onPersonAddRequested,
            onAccountAndOwnerChanged = {
                accountAndOwnerState = it
            },
            incomeAccount = incomeAccount,
            outcomeAccount = outcomeAccount,
            onSetIncomeOutcomeAccount = onSetIncomeOutcomeAccount,
            onDoneAction = saveAccount,
            isComplete = completeState
        )
    }
}