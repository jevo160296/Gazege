package com.jmml.gazege.ui.views.account

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.ui.views.transaction.AccountAndOwnerNode
import com.jmml.gazege.ui.widgets.MutableTreeComboBox
import com.jmml.zoo.ui.input.ButtonField

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
    onAccountAddRequested: (() -> Unit)?,
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
    val contentWhenDisabled: @Composable (() -> Unit)? = if (onAccountAddRequested != null) {
        @Composable {
            ButtonField(onClick = onAccountAddRequested) {
                Text(stringResource(R.string.Nueva_cuenta))
            }
        }
    } else {
        null
    }
    MutableTreeComboBox(
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = { dropDownExpanded = !dropDownExpanded },
        options = accountNodes,
        selectedItem = selectedAccountNode,
        itemToString = itemToString,
        label = label,
        onItemClick = onItemClick,
        nodeEnabled = { it.isActive },
        enabled = accountsList.isNotEmpty() && accountsList.size > deactivatedAccountList.size,
        canClearSelection = canClearSelection,
        onClearSelectionClicked = onClearSelectionClicked,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        groupByKeySelector = { it.content.owner.name },
        contentWhenDisabled = contentWhenDisabled
    )
}