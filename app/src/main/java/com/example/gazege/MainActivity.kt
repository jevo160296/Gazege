package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.PersonRecyclerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GazegeTheme {
                var personList by remember {
                    mutableStateOf(
                        listOf(
                            Person(id = 0, name = "Persona1"),
                            Person(id = 1, name = "Persona2")
                        )
                    )
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Page(
                        personList,
                        addPerson = {
                            personList =
                                listOf(
                                    *personList.toTypedArray(),
                                    Person(id = personList.size + 1, name = "New Person")
                                )
                        },
                        delPerson = { deletingPerson ->
                            personList = personList.filter {
                                it != deletingPerson
                            }
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