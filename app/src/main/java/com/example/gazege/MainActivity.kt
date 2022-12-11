package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.AppDatabase
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.PersonRecyclerView

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
    addPerson: (Person) -> Unit = {},
    delPerson: (Person) -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            Button(
                onClick = {
                    addPerson(Person(name = "Added Persona"))
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
        PersonRecyclerView(personList = personList, modifier = Modifier.padding(it))
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GazegeTheme {
        Page(
            personList = listOf(
                Person(name = "Persona 1"),
                Person(name = "Persona 2")
            )
        )
    }
}