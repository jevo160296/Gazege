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
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun PersonForm(
    person: Person? = null,
    onPersonAdd: (Person) -> Unit
) {
    var name by rememberSaveable {
        mutableStateOf(person?.name ?: "")
    }
    val modifiedPerson = person?.copy(name = name) ?: Person(name = name)
    Column {
        PersonFormFields(name) {
            name = it
        }
        Button(onClick = {
            onPersonAdd(modifiedPerson)
        }) {
            Text("Save")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormFields(
    name: String,
    onNameChanged: (String) -> Unit
) {
    TextField(
        value = name,
        onValueChange = {
            onNameChanged(it)
        },
        label = { Text("Nombre") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        )
    )
}


@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun Preview() {
    GazegeTheme {
        var showSnackbar by rememberSaveable {
            mutableStateOf(false)
        }
        var persona by remember<MutableState<Person?>> {
            mutableStateOf(Person(1, "Persona inicial"))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            PersonForm(persona) {
                println("Persona agregada: $it")
                persona = it
                showSnackbar = true
            }
            if (showSnackbar) {
                Snackbar {
                    Text("Persona agregada: $persona", color = SnackbarDefaults.contentColor)
                }
            }
        }
    }
}