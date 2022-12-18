package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.R
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.savers.accountAndOwnerSaver
import com.example.gazege.ui.theme.GazegeTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonForm(
    modifier: Modifier = Modifier,
    person: Person? = null,
    onPersonChanged: (Person) -> Unit
) {
    val name = person?.name ?: ""
    Column(modifier = modifier) {
        TextField(
            value = name,
            onValueChange = {
                onPersonChanged(
                    person?.copy(name = it) ?: Person(name = it)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountAndOwnerForm(
    modifier: Modifier = Modifier,
    accountAndOwner: AccountAndOwner? = null,
    personList: List<Person>,
    onAccountAndOwnerChanged: (AccountAndOwner) -> Unit
) {
    val id: Int? = accountAndOwner?.account?.id
    val name: String = accountAndOwner?.account?.name ?: ""
    val owner: Person? = accountAndOwner?.owner ?: personList.firstOrNull()
    Column(modifier = modifier) {
        TextField(
            value = name,
            onValueChange = {
                if (owner?.id != null) {
                    onAccountAndOwnerChanged(
                        AccountAndOwner(
                            account = Account(
                                id = id, name = it, initial_balance = 0.0,
                                ownerId = owner.id
                            ),
                            owner = owner
                        )
                    )
                }
            },
            label = { Text("Nombre") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )
        if (owner?.id != null) {
            var dropDownExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            Box {
                TextField(
                    value = owner.name,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { dropDownExpanded = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_arrow_drop_down_24),
                                contentDescription = ""
                            )
                        }
                    },
                    label = { Text("Owner") },
                )
                DropdownMenu(
                    expanded = dropDownExpanded,
                    onDismissRequest = {
                        dropDownExpanded = false
                    }) {
                    personList.map {
                        DropdownMenuItem(
                            text = {Text(it.name)},
                            onClick = {
                                dropDownExpanded = false
                                if(it.id != null){
                                    onAccountAndOwnerChanged(
                                        AccountAndOwner(
                                            account = Account(
                                                id = id, name = name, initial_balance = 0.0,
                                                ownerId = it.id
                                            ),
                                            owner = it
                                        )
                                    )
                                }
                            })
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 320, heightDp = 400, showBackground = true)
@Composable
private fun Preview() {
    GazegeTheme {
        val personList = (0..10).map {
            Person(it, "Person $it")
        }
        val owner = personList.first()
        var accountAndOwner by rememberSaveable(
            stateSaver = accountAndOwnerSaver
        ) {
            mutableStateOf(
                AccountAndOwner(
                    Account(name = "", ownerId = owner.id ?: -1, initial_balance = 0.0),
                    owner
                )
            )
        }
        val scaffoldState: ScaffoldState = rememberScaffoldState()
        val scope = rememberCoroutineScope()
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
            scaffoldState = scaffoldState,
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    scope.launch {
                        val accountAndOwnerAdded = accountAndOwner
                        scaffoldState.snackbarHostState.showSnackbar(
                            "Added $accountAndOwnerAdded"
                        )
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_check_24),
                        contentDescription = ""
                    )
                }
            }
        ) {
            AccountAndOwnerForm(
                modifier = Modifier.padding(it),
                accountAndOwner = accountAndOwner,
                personList = personList
            ) { changedAccount ->
                accountAndOwner = changedAccount
            }
        }
    }
}