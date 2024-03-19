package com.jmml.gazege.ui.templates

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jmml.gazege.NavPosition
import com.jmml.gazege.R
import com.jmml.gazege.ui.views.AddTransactionAction
import com.jmml.gazege.ui.widgets.fab.ExpandableFAB
import com.jmml.gazege.ui.widgets.fab.ExtendedFAB
import com.jmml.gazege.ui.widgets.menu.DropDownMenuItem

@Composable
fun DynamicAddEntityFAB(
    fabExpanded: Boolean,
    onFabExpandedChanged: (Boolean) -> Unit,
    navPosition: NavPosition,
    onAddPersonRequested: () -> Unit,
    onAddAccountRequested: () -> Unit,
    onAddTransactionRequested: (AddTransactionAction) -> Unit,
    onAddCategoryRequested: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (fabExpanded) {
            45f
        } else {
            0f
        },
        label = "rotation"
    )
    val nuevaTransaccionString = stringResource(id = R.string.Nueva_transaccion)
    val nuevaCuentaStrirng = stringResource(id = R.string.Nueva_cuenta)
    val nuevaPersonaString = stringResource(id = R.string.Nueva_persona)
    val nuevaCategoriaString = stringResource(id = R.string.Nueva_categoria)

    val text by rememberSaveable(navPosition) {
        mutableStateOf(
            when (navPosition) {
                NavPosition.PERSONS -> nuevaPersonaString
                NavPosition.CUENTAS -> nuevaCuentaStrirng
                NavPosition.TRANSACCIONES -> nuevaTransaccionString
                NavPosition.CATEGORIAS -> nuevaCategoriaString
            }
        )
    }

    ExpandableFAB(
        modifier = Modifier,
        columnModifier = Modifier.width(IntrinsicSize.Max),
        isExpanded = fabExpanded,
        onClick = {
            when (navPosition) {
                NavPosition.PERSONS -> onAddPersonRequested()
                NavPosition.CUENTAS -> onAddAccountRequested()
                NavPosition.TRANSACCIONES -> onFabExpandedChanged(true)
                NavPosition.CATEGORIAS -> onAddCategoryRequested()
            }
        },
        onDismissRequest = { onFabExpandedChanged(false) },
        fab = {
            ExtendedFAB(onClick = it, text = text) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "Add",
                    modifier = Modifier.rotate(rotation),
                )
            }
        }
    ) {
        DropDownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAddTransactionRequested(AddTransactionAction.ADD_TRANSFER)
                onFabExpandedChanged(false)
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.transfer_icon),
                    contentDescription = "Add"
                )
            },
            label = { Text(text = stringResource(id = R.string.Transferencia)) }
        )
        DropDownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAddTransactionRequested(AddTransactionAction.ADD_EXPENSE)
                onFabExpandedChanged(false)
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.gasto_icon),
                    contentDescription = "Add"
                )
            },
            label = { Text(text = stringResource(id = R.string.Gasto)) }
        )
        DropDownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAddTransactionRequested(AddTransactionAction.ADD_INCOME)
                onFabExpandedChanged(false)
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ingreso_icon),
                    contentDescription = "Add"
                )
            },
            label = { Text(text = stringResource(id = R.string.Ingreso)) }
        )
    }
}