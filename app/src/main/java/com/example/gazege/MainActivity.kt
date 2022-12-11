package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.AccountRecyclerView
import com.example.gazege.ui.views.PersonRecyclerView
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    private val repository: AppRepository by lazy { AppRepository(
        personDao = database.personDao(),
        accountDao = database.accountDao(),
        transactionDao = database.transactionDao()
    ) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GazegeTheme {
                var personList by remember {mutableStateOf(emptyList<Person>()) }
                mainViewModel.allPerson.observe(this) { persons ->
                    persons?.let { personList = it }
                }
                var accountList by remember { mutableStateOf(emptyList<AccountAndOwner>())}
                mainViewModel.allAccount.observe(this){ accounts ->
                    accounts?.let{ accountList = it }
                }
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
    addAccount: (Account) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            Button(
                onClick = {
                    val personAdd = Random.nextBoolean()
                    val maxPersonasId = personList.maxOfOrNull { it.id ?: -1} ?: -1
                    val maxAccountsId = accountList.maxOfOrNull { it.account.id ?: -1 } ?: -1
                    if(personAdd || personList.isEmpty()){
                        addPerson(Person(name = "Persona ${maxPersonasId+1}"))
                    }
                    if(!personAdd){
                        val cantPersonas = personList.size
                        val selectedPerson = Random.nextInt(cantPersonas)
                        addAccount(Account(
                            name="Account ${maxAccountsId+1}",
                            initial_balance = 1.0,
                            ownerId = personList[selectedPerson].id ?: -1
                        ))
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
            Column(modifier = Modifier.weight(1F)){
                Text("Personas")
                PersonRecyclerView(personList = personList)
            }
            Column(modifier = Modifier.weight(1F)){
                Text("Cuentas")
                AccountRecyclerView(accountList = accountList)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val personList = listOf(
        Person(id=0, name = "Persona 1"),
        Person(id=1, name = "Persona 2")
    )
    val accounts = personList.map { person ->
        listOf(1, 2, 3, 4, 5).map { index ->
            val account = Account(
                name="Cuenta $index - ${person.id}",
                ownerId = person.id ?: -1,
                initial_balance = 0.0
            )
            AccountAndOwner(account = account, owner = person)
        }
    }.flatten()
    GazegeTheme {
        Page(
            personList = personList,
            accountList = accounts,
            addPerson = {},
            addAccount = {},
            delPerson = {}
        )
    }
}