package com.jmml.gazege.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.ui.theme.Shapes

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
    Form(
        modifier = modifier,
        onSaveClicked = onSaveClicked,
        isSavedButtonEnabled = isSavedButtonEnabled,
        title = {
            TopAppBar(
                title = { MediumHeadline(title) }
            )
        },
        snackbarHostState = snackbarHostState,
        itemSpacing = itemSpacing,
        itemsColumnsModifier = itemsColumnsModifier,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Form(
    modifier: Modifier = Modifier,
    onSaveClicked: () -> Unit,
    isSavedButtonEnabled: Boolean,
    title: @Composable () -> Unit = { TopAppBar(title = { MediumHeadline("Form") }) },
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
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
        topBar = title
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .then(itemsColumnsModifier),
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                content()
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}