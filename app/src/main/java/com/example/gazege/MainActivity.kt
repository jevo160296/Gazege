package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.*
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.AccountRecyclerView
import com.example.gazege.ui.views.PersonRecyclerView
import com.example.gazege.ui.views.TransactionRecyclerView
import java.util.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val repository: AppRepository by lazy {
        AppRepository(
            personDao = database.personDao(),
            accountDao = database.accountDao(),
            transactionDao = database.transactionDao()
        )
    }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GazegeTheme {
                val personList by mainViewModel.allPerson.observeAsState(emptyList())
                val accountList by mainViewModel.allAccount.observeAsState(emptyList())
                val transactionList by mainViewModel.allTransactions.observeAsState(emptyList())
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Page(
                        personList,
                        addPerson = {
                            mainViewModel.insertPerson(it)
                        },
                        delPerson = {
                            mainViewModel.deletePerson(it)
                        },
                        accountList,
                        addAccount = {
                            mainViewModel.insertAccount(it)
                        },
                        transactionList,
                        addTransaction = {
                            mainViewModel.insertTransaction(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Page(
    personList: List<Person>,
    addPerson: (Person) -> Unit,
    delPerson: (Person) -> Unit,
    accountList: List<AccountAndOwner>,
    addAccount: (Account) -> Unit,
    transactionList: List<TransactionAndAccounts>,
    addTransaction: (Transaction) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            Button(
                onClick = {
                    val random = Random.nextInt(3)
                    val personAdd = random == 0
                    val transAdd = random == 1
                    val cuentaAdd = random == 2
                    val maxPersonasId = personList.maxOfOrNull { it.id ?: -1 } ?: -1
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
                                ownerId = personList[selectedPerson].id ?: -1
                            )
                        )
                    } else if (transAdd && accountList.size >= 2) {
                        val cantAccounts = accountList.size
                        val selectedSourceAccount = Random.nextInt(cantAccounts)
                        val selectedDestinationAccount = Random.nextInt(cantAccounts)
                        addTransaction(
                            Transaction(
                                amount = 0.0,
                                description = "Trans ${maxTransactionsId + 1}",
                                sourceId = accountList[selectedSourceAccount].account.id ?: -1,
                                destinationId = accountList[selectedDestinationAccount].account.id
                                    ?: -1,
                                date = Date()
                            )
                        )
                    }
                }) {
                Text("Add")
            }
        },
        bottomBar = {
            Button(onClick = {
                if (personList.isNotEmpty()) {
                    delPerson(personList[0])
                }
            }) {
                Text(text = "Delete")
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.weight(1F)) {
                Text("Personas")
                PersonRecyclerView(personList = personList)
            }
            Column(modifier = Modifier.weight(1F)) {
                Text("Cuentas")
                AccountRecyclerView(accountList = accountList)
            }
            Column(modifier = Modifier.weight(1F)) {
                Text("Transacciones")
                TransactionRecyclerView(transactionList = transactionList)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val personList = listOf(
        Person(id = 0, name = "Persona 1"),
        Person(id = 1, name = "Persona 2")
    )
    val accounts = personList.map { person ->
        listOf(1, 2, 3, 4, 5).map { index ->
            val account = Account(
                name = "Cuenta $index - ${person.id}",
                ownerId = person.id ?: -1,
                initial_balance = 0.0
            )
            AccountAndOwner(account = account, owner = person)
        }
    }.flatten()
    val transactions = accounts.map { sourceAccount ->
        accounts.map { destinationAccount ->
            TransactionAndAccounts(
                Transaction(
                    amount = 0.0, description = "Desc",
                    sourceId = sourceAccount.account.id ?: -1,
                    destinationId = destinationAccount.account.id ?: -1,
                    date = Date()
                ),
                sourceAccount.account, destinationAccount.account
            )
        }
    }.flatten()
    GazegeTheme {
        Page(
            personList = personList,
            accountList = accounts,
            addPerson = {},
            addAccount = {},
            delPerson = {},
            transactionList = transactions,
            addTransaction = {}
        )
    }
}