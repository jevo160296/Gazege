package com.example.gazege.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.AccountRecyclerView
import com.example.gazege.ui.views.getAccountSample
import com.example.gazege.ui.widgets.MediumHeadline

@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    itemHolderPaddingValues: PaddingValues = PaddingValues(),
    accountList: List<AccountAndOwnerWithTransactions>,
    state: LazyListState,
    delAccount: (Account) -> Unit
) {
    Column(modifier = modifier) {
        MediumHeadline(text = "Cuentas")
        AccountRecyclerView(
            accountList = accountList,
            onItemTapped = { accountAndOwner ->
                delAccount(accountAndOwner.account)
            },
            itemHolderPaddingValues = itemHolderPaddingValues,
            state = state
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    heightDp = 640
)
@Composable
private fun Preview() {
    GazegeTheme(darkTheme = true) {
        AccountPage(accountList = getAccountSample(), state = LazyListState()) {}
    }
}