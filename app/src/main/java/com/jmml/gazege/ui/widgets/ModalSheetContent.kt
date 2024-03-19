package com.jmml.gazege.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalSheetLayout(
    modalSheetMsg: String,
    onModalSheetMsgChanged: (String) -> Unit,
    action: () -> Unit,
    onActionChanged: (() -> Unit) -> Unit,
    sheetState: SheetState,
    content: @Composable () -> Unit
) {
    val internalSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    Box {
        content()
        if (sheetState.targetValue in setOf(SheetValue.Expanded, SheetValue.PartiallyExpanded) ||
            sheetState.currentValue in setOf(SheetValue.Expanded, SheetValue.PartiallyExpanded) ||
            internalSheetState.targetValue in setOf(
                SheetValue.Expanded,
                SheetValue.PartiallyExpanded
            ) ||
            internalSheetState.currentValue in setOf(
                SheetValue.Expanded,
                SheetValue.PartiallyExpanded
            )
        ) {
            ModalBottomSheet(
                onDismissRequest = { scope.launch { sheetState.hide() } },
                sheetState = internalSheetState,
                dragHandle = {}
            ) {
                ModalSheetContent(
                    titleText = stringResource(id = R.string.confirmar_eliminacion),
                    bodyText = modalSheetMsg,
                    onSiClicked = {
                        action()
                        onActionChanged {}
                        onModalSheetMsgChanged("")
                        scope.launch { sheetState.hide() }
                    },
                    onNoClicked = {
                        onActionChanged {}
                        onModalSheetMsgChanged("")
                        scope.launch { sheetState.hide() }
                    }
                )
            }
        }
    }
    LaunchedEffect(sheetState.targetValue) {
        when (sheetState.targetValue) {
            SheetValue.Expanded, SheetValue.PartiallyExpanded -> internalSheetState.show()
            SheetValue.Hidden -> internalSheetState.hide()
        }
    }
}

@Composable
fun ModalSheetContent(
    onSiClicked: () -> Unit,
    onNoClicked: () -> Unit,
    titleText: String,
    bodyText: String
) {
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp)
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = bodyText
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    onSiClicked()
                }) {
                Text(
                    text = stringResource(R.string.Si)
                )
            }
            TextButton(
                onClick = {
                    onNoClicked()
                }
            ) {
                Text(
                    text = stringResource(R.string.No)
                )
            }
        }
    }
}