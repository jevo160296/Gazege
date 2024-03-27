package com.jmml.gazege.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.jmml.gazege.R
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.ModalSheetLayout

data class BottomSheetController(
    val getMsg: @Composable () -> String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntityDetail(
    modalController: BottomSheetController?,
    title: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    sheetState: SheetState,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    ModalSheetLayout(
        modalSheetMsg = modalController?.getMsg?.invoke() ?: "",
        onModalSheetMsgChanged = { },
        action = { modalController?.action?.let { it() } },
        onActionChanged = {},
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
            },
            floatingActionButton = floatingActionButton,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
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