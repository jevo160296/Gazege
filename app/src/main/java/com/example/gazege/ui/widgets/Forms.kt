package com.example.gazege.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.ui.theme.Shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Form(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit,
    isSavedButtonEnabled: Boolean,
    title: String,
    snackbarHostState: SnackbarHostState? = null,
    itemSpacing: Dp = 0.dp,
    itemsColumnsModifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        snackbarHost = {
            if (snackbarHostState == null) {
                SnackbarHost(hostState = it)
            } else {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isSavedButtonEnabled) {
                FloatingActionButton(
                    onClick = onSaveClicked,
                    shape = Shapes.small
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_check_24),
                        contentDescription = ""
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { MediumHeadline(title) }
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                itemsColumnsModifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                content()
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}