package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.RecyclerView
import com.example.gazege.ui.widgets.SmallBody
import com.example.gazege.ui.widgets.SmallEmphasis

@Composable
private fun PersonViewHolder(person: Person) {
    Row{
        SmallEmphasis(text = "Name: ")
        SmallBody(text = person.name)
    }
}

@Composable
fun PersonRecyclerView(
    personList: List<Person>,
    onItemTapped: (Person) -> Unit,
    modifier: Modifier = Modifier) {
    RecyclerView(elements = personList, modifier = modifier, onItemTapped = onItemTapped) {
        PersonViewHolder(person = it)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonItem() {
    val person = Person(name = "Persona")
    PersonViewHolder(person = person)
}

@Preview(showBackground = true)
@Composable
private fun PreviewPersonList() {
    GazegeTheme {
        val personList = listOf(
            Person(name = "Persona1"),
            Person(name = "Persona2"),
            Person(name = "Persona3"),
            Person(name = "Persona4")
        )
        RecyclerView(
            elements = personList,
            viewHolder = { person -> PersonViewHolder(person = person) }
        )
    }
}