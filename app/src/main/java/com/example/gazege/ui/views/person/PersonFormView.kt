package com.example.gazege.ui.views.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.ui.savers.PartialPerson
import com.example.gazege.ui.widgets.TextField


@Composable
fun PersonForm(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp = 0.dp,
    person: PartialPerson,
    imeAction: ImeAction,
    onDoneAction: () -> Unit,
    onPersonChanged: (PartialPerson) -> Unit
) {
    val name = person.name ?: ""
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = name,
            onValueChange = {
                onPersonChanged(
                    person.copy(name = it)
                )
            },
            label = { Text(stringResource(R.string.nombre)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    defaultKeyboardAction(ImeAction.Done)
                    onDoneAction()
                }
            )
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}