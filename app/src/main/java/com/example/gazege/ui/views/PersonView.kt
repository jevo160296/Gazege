package com.example.gazege.ui.views

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Person
import com.example.gazege.ui.widgets.RecyclerView

@Composable
private fun PersonViewHolder(person: Person) {
    Text(text = person.name)
}

@Composable
fun PersonRecyclerView(personList: List<Person>, modifier: Modifier = Modifier) {
    RecyclerView(elements = personList, modifier = modifier) {
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