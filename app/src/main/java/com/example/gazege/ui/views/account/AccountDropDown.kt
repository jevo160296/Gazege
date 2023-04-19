package com.example.gazege.ui.views.account

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.ui.views.transaction.AccountAndOwnerNode
import com.example.gazege.ui.widgets.TreeComboBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDropDownMenu(
    accountsList: List<AccountAndOwner>,
    deactivatedAccountList: List<AccountAndOwner>,
    selectedAccountNode: AccountAndOwnerNode?,
    label: @Composable () -> Unit,
    canClearSelection: Boolean,
    onClearSelectionClicked: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
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
                index,
                deactivatedAccountList = deactivatedAccountList,
                null
            )
        }
    var dropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val itemToString = { it: AccountAndOwnerNode? -> it?.content?.account?.name ?: "" }
    TreeComboBox(
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
        },
        options = accountNodes,
        selectedItem = selectedAccountNode,
        itemToString = itemToString,
        label = label,
        onItemClick = onItemClick,
        canClearSelection = canClearSelection,
        onClearSelectionClicked = onClearSelectionClicked,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        nodeEnabled = { it.isActive }
    ) {
        it.content.owner.name
    }
}