package com.example.gazege.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.TransactionRecyclerView
import com.example.gazege.ui.views.getTransactionSample
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun TransactionPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    transactionList: List<TransactionAndAccounts>,
    state: LazyListState,
    delTransaction: (Transaction) -> Unit
) {
    Column(modifier = modifier) {
        MediumHeadline("Transacciones")
        TransactionRecyclerView(
            transactionList = transactionList, onItemTapped = { transactionAndAccounts ->
                delTransaction(transactionAndAccounts.transaction)
            }, itemHolderPaddingValues = itemHolderPaddingValues, state = state
        )
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
private fun Preview() {
    GazegeTheme(darkTheme = true) {
        TransactionPage(transactionList = getTransactionSample(), state = LazyListState()) {}
    }
}