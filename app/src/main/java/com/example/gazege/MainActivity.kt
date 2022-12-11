package com.example.gazege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Page()
                }
            }
        }
    }
}

@Composable
fun Page() {
    var personList by remember {
        mutableStateOf(
            listOf(
            Person(name = "Persona1"),
            Person(name = "Persona2")
        ))
    }
    Column {
        PersonRecyclerView(personList = personList)
        Button(
            onClick = {
                personList = listOf(
                    *personList.toTypedArray(),
                    Person(name = "Added Persona")
                )
             }) {
            Text("Add")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GazegeTheme {
        Page()
    }
}