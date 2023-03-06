package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.Shapes
import com.example.gazege.ui.widgets.DropDownMenu
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun SettingsFragment(
    personList: List<Person>,
    principalPerson: Person?,
    onPrincipalPersonChanged: (Person) -> Unit,
    onNavigateUpRequested: () -> Unit
) {
    var principalPersonExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    var personIdSelected by rememberSaveable {
        mutableStateOf(principalPerson?.id)
    }
    val personSelected = personList.firstOrNull { it.id == personIdSelected }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                onClick = {
                    if (personSelected != null) {
                        onPrincipalPersonChanged(personSelected)
                    }
                    onNavigateUpRequested()
                }, shape = Shapes.small
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.round_save_24),
                    contentDescription = "Save"
                )
            }
        },
        topBar = { MediumHeadline(text = "Ajustes") },
    ) { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(8.dp)) {
            DropDownMenu(
                dropDownExpanded = principalPersonExpanded,
                onExpandedChange = { principalPersonExpanded = it },
                options = personList,
                selectedItem = personSelected,
                itemToString = { it?.name ?: "" },
                onItemClick = { personIdSelected = it.id },
                label = { Text("Principal person") }
            )
        }
    }
}