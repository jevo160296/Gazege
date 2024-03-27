package com.jmml.gazege.ui.views.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.personaDeleitionConfirmationBuilder
import com.jmml.gazege.ui.transactionDeleitionConfirmationBuilder
import com.jmml.gazege.ui.views.BottomSheetController
import com.jmml.gazege.ui.views.EntityDetail
import com.jmml.gazege.ui.views.PersonAction
import com.jmml.gazege.ui.views.TransactionAction
import com.jmml.gazege.ui.views.transaction.LoadedTransactionPage
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.SmallBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetail(
    person: Person,
    viewModelPersonDetail: MainViewModel.ViewModelPersonDetail,
    deuda: Double,
    onPersonAction: (person: Person, action: PersonAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    var modalController: BottomSheetController? by remember {
        mutableStateOf(null)
    }
    var justPendingTransactions: Boolean by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val transactionListItemDetails by viewModelPersonDetail.rememberPeopleTransactionListItemDetails(
        person.id,
        justPendingTransactions,
        deuda
    )
    EntityDetail(
        modalController = modalController,
        title = person.name,
        onEditClick = { onPersonAction(person, PersonAction.EDIT) },
        onDeleteClick = {
            modalController = BottomSheetController(
                getMsg = {
                    personaDeleitionConfirmationBuilder()(person.name)
                },
                action = {
                    onPersonAction(person, PersonAction.DELETE)
                }
            )
            scope.launch { sheetState.show() }
        },
        sheetState = sheetState
    ) {
        SmallBody(
            text = "${personDeudaString(deuda)}: ${doubleToMoneyString(deuda)}", Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen.DefaultPadding
                )
            )
        )
        MediumHeadline(
            text = stringResource(id = R.string.transacciones), modifier = Modifier.padding(
                horizontal = dimensionResource(
                    id = R.dimen.DefaultPadding
                )
            )
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            Switch(
                checked = justPendingTransactions,
                onCheckedChange = { justPendingTransactions = it },
            )
            Text(text = stringResource(id = R.string.justPendingTransactions))
        }
        LoadedTransactionPage(
            transactionList = transactionListItemDetails ?: emptyList(),
            delTransaction = {
                modalController = BottomSheetController(
                    getMsg = {
                        transactionDeleitionConfirmationBuilder()()
                    },
                    action = {
                        onTransactionAction(it, TransactionAction.DELETE)
                    }
                )
                scope.launch { sheetState.show() }
            },
            editTransaction = { onTransactionAction(it, TransactionAction.EDIT) },
            state = rememberLazyListState(),
            itemHolderPaddingValues = PaddingValues(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            onTitleSetted = {},
            onZeroElementsChanged = {},
            onFirstElementVisibleChanged = {}
        )
    }
}