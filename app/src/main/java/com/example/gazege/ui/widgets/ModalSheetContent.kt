package com.example.gazege.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R

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