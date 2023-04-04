package com.example.gazege.ui.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.gazege.core.entities.PersonWithAccounts

@Composable
fun PersonDetail(
    person: PersonWithAccounts
) {
    Text(text = "Person detail ${person.person.name}")
}