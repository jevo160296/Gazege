package com.example.gazege.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.widgets.DropDownTreeMenu

@Composable
fun AccountDropDownMenu(
    accountsList: List<AccountAndOwner>,
    selectedAccountNode: AccountAndOwnerNode?,
    label: @Composable () -> Unit,
    onItemClick: (AccountAndOwnerNode) -> Unit
) {
    val accountNodes = accountsList
        .filter {
            it.account.parentId == null
        }
        .mapIndexed { index, it ->
            AccountAndOwnerNode(
                it,
                accountsList,
                0,
                index
            )
        }
    var dropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    DropDownTreeMenu(
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
        },
        options = accountNodes,
        selectedItem = selectedAccountNode,
        itemToString = { it?.content?.account?.name ?: "" },
        onItemClick = {
            dropDownExpanded = false
            onItemClick(it)
        },
        label = label
    ) {
        it.content.owner.name
    }
}