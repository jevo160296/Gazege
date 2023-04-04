package com.example.gazege.ui.views

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.ui.personaDeleitionConfirmationBuilder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonDetail(
    person: PersonWithAccounts,
    onPersonAction: (person: Person, action: PersonAction) -> Unit
) {
    var modalController: BottomSheetController? by remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    EntityDetail(
        modalController = modalController,
        title = stringResource(id = R.string.persona) + " ${person.person.name}",
        onEditClick = { onPersonAction(person.person, PersonAction.EDIT) },
        onDeleteClick = {
            modalController = BottomSheetController(
                getMsg = {
                    personaDeleitionConfirmationBuilder()(person.person.name)
                },
                action = {
                    onPersonAction(person.person, PersonAction.DELETE)
                }
            )
            scope.launch { sheetState.show() }
        },
        sheetState = sheetState
    ) {

    }
}