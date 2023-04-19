package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.ModalSheetContent
import kotlinx.coroutines.launch

data class BottomSheetController(
    val getMsg: @Composable () -> String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun EntityDetail(
    modalController: BottomSheetController?,
    title: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    sheetState: ModalBottomSheetState,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalBottomSheetLayout(
        sheetContent = {
            ModalSheetContent(
                onSiClicked = {
                    modalController?.action?.let { it() }
                    scope.launch { sheetState.hide() }
                },
                onNoClicked = { scope.launch { sheetState.hide() } },
                titleText = stringResource(id = R.string.confirmar_eliminacion),
                bodyText = modalController?.getMsg?.invoke() ?: ""
            )
        },
        sheetState = sheetState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        MediumHeadline(
                            text = title
                        )
                    },
                    actions = {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit),
                                contentDescription = "Edit"
                            )
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Delete"
                            )
                        }
                    }
                )
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding)),
                modifier = Modifier.padding(it)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))) {
                    content()
                }
            }
        }
    }
}