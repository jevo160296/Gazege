package com.example.gazege.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.PersonRecyclerView
import com.example.gazege.ui.views.getPersonWithAccountsSample
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun PersonPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    personList: List<PersonWithAccounts>,
    state: LazyListState,
    onPersonDeleted: (Person) -> Unit
) {
    Column(modifier = modifier) {
        MediumHeadline(text = "Persons")
        PersonRecyclerView(
            personList = personList, onItemTapped = {
                onPersonDeleted(it.person)
            }, itemHolderPaddingValues = itemHolderPaddingValues, state = state
        )
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 620)
@Composable
fun DefaultPreview() {
    GazegeTheme {
        PersonPage(
            personList = getPersonWithAccountsSample(), state = LazyListState()
        ) {}
    }
}
