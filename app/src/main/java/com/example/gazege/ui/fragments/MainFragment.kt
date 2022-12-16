package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.NavPosition
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.views.AccountPage
import com.example.gazege.ui.views.PersonPage
import com.example.gazege.ui.views.TransactionPage
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.views.getAccountSample
import com.example.gazege.ui.views.getPersonWithAccountsSample
import com.example.gazege.ui.views.getTransactionSample
import java.util.*
import kotlin.random.Random

@Composable
fun MainFragment(
    personList: List<PersonWithAccounts>,
    addPerson: (Person) -> Unit,
    delPerson: (Person) -> Unit,
    accountList: List<AccountAndOwnerWithTransactions>,
    addAccount: (Account) -> Unit,
    delAccount: (Account) -> Unit,
    transactionList: List<TransactionAndAccounts>,
    addTransaction: (Transaction) -> Unit,
    delTransaction: (Transaction) -> Unit,
    navPosition: NavPosition,
    onNavStatusChanged: (NavPosition) -> Unit
) {
    val transactionState = rememberLazyListState()
    val accountState = rememberLazyListState()
    val personState = rememberLazyListState()
    Scaffold(floatingActionButton = {
        FloatingActionButton(
            onClick = {
                val random = Random.nextInt(3)
                val personAdd = random == 0
                val transAdd = random == 1
                val cuentaAdd = random == 2
                val maxPersonasId = personList.maxOfOrNull { it.person.id ?: -1 } ?: -1
                val maxAccountsId = accountList.maxOfOrNull { it.account.id ?: -1 } ?: -1
                val maxTransactionsId =
                    transactionList.maxOfOrNull { it.transaction.id ?: -1 } ?: -1
                if (personAdd) {
                    addPerson(Person(name = "Persona ${maxPersonasId + 1}"))
                } else if (cuentaAdd && personList.isNotEmpty()) {
                    val cantPersonas = personList.size
                    val selectedPerson = Random.nextInt(cantPersonas)
                    addAccount(
                        Account(
                            name = "Account ${maxAccountsId + 1}",
                            initial_balance = 1.0,
                            ownerId = personList[selectedPerson].person.id ?: -1
                        )
                    )
                } else if (transAdd && accountList.size >= 2) {
                    val cantAccounts = accountList.size
                    val selectedSourceAccount = Random.nextInt(cantAccounts)
                    val selectedDestinationAccount = Random.nextInt(cantAccounts)
                    addTransaction(
                        Transaction(
                            amount = Random.nextDouble(0.0, 200.0),
                            description = "Trans ${maxTransactionsId + 1}",
                            sourceId = accountList[selectedSourceAccount].account.id ?: -1,
                            destinationId = accountList[selectedDestinationAccount].account.id
                                ?: -1,
                            date = Date()
                        )
                    )
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
        }) {
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
                TransactionPage(
                    transactionList = transactionList,
                    itemHolderPaddingValues = paddingValues,
                    state = transactionState
                ) { transaction -> delTransaction(transaction) }
            }
            NavPosition.CUENTAS -> {
                AccountPage(
                    accountList = accountList,
                    itemHolderPaddingValues = paddingValues,
                    state = accountState
                ) { account -> delAccount(account) }
            }
            NavPosition.PERSONS -> {
                PersonPage(
                    personList = personList,
                    itemHolderPaddingValues = paddingValues,
                    state = personState
                ) { person -> delPerson(person) }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val personList = getPersonWithAccountsSample()
    val accounts = getAccountSample()
    val transactions = getTransactionSample()
    GazegeTheme(darkTheme = true) {
        MainFragment(personList = personList,
            accountList = accounts,
            addPerson = {},
            addAccount = {},
            delPerson = {},
            transactionList = transactions,
            addTransaction = {},
            delAccount = {},
            delTransaction = {},
            navPosition = NavPosition.TRANSACCIONES,
            onNavStatusChanged = {})
    }
}