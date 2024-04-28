package com.jmml.gazege.ui.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.ui.savers.PartialAccount
import com.jmml.gazege.ui.savers.PartialAccountAndOwner
import com.jmml.gazege.ui.savers.accountAndOwnerSaver
import com.jmml.gazege.ui.views.account.AccountAndOwnerForm
import com.jmml.gazege.ui.widgets.Form
import com.jmml.zoo.ui.state.ZIndefiniteCircularProgressIndicator


@Composable
fun AccountFormFragment(
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
            currentBalanceState,
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
        title = stringResource(R.string.cuenta)
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

@Composable
fun LoadingAccountFormFragment() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ZIndefiniteCircularProgressIndicator()
    }
}