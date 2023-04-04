package com.example.gazege.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.gazege.R

@Composable
fun accountDeleitionConfirmationBuilder(): (accountName: String) -> String {
    val itemTemplate = stringResource(id = R.string.la_cuenta)
    val template = stringResource(id = R.string.confirma_la_eliminacion_de).format(itemTemplate)
    return { accountName ->
        template.format(accountName)
    }
}

@Composable
fun personaDeleitionConfirmationBuilder(): (personName: String) -> String {
    val itemTemplate = stringResource(R.string.la_persona)
    val template = stringResource(id = R.string.confirma_la_eliminacion_de).format(itemTemplate)
    return { personName ->
        template.format(personName)
    }
}

@Composable
fun transactionDeleitionConfirmationBuilder(): () -> String {
    val itemTemplate = stringResource(id = R.string.la_transaccion)
    val template = stringResource(id = R.string.confirma_la_eliminacion_de).format(itemTemplate)
    return {
        template
    }
}