package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Snackbar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.personSaver
import com.example.gazege.ui.theme.GazegeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonForm(
    person: Person = Person(name=""),
    onPersonChanged: (Person) -> Unit
) {
    Column {
        TextField(
            value = person.name,
            onValueChange = {
                onPersonChanged(
                    person.copy(name = it)
                )
            },
            label = { Text("Nombre") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
    }
}


@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun Preview() {
    GazegeTheme {
        var showSnackbar by rememberSaveable {
            mutableStateOf(false)
        }
        var person by rememberSaveable(
            stateSaver = personSaver
        ) {
            mutableStateOf(Person(1, "Persona inicial"))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            PersonForm(person) {
                person = it
            }
            Button(onClick = {
                showSnackbar = true
            }) {
                Text("Save")
            }
            if (showSnackbar) {
                Snackbar {
                    Text("Persona agregada: $person", color = SnackbarDefaults.contentColor)
                }
            }
        }
    }
}