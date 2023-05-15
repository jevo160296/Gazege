package com.example.gazege.ui.views.person

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.personaDeleitionConfirmationBuilder
import com.example.gazege.ui.transactionDeleitionConfirmationBuilder
import com.example.gazege.ui.views.*
import com.example.gazege.ui.views.transaction.TransactionPage
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.SmallBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetail(
    person: Person,
    principalPerson: Person?,
    allTransactions: List<Transaction>,
    allAccounts: List<Account>,
    allCategories: List<Category>,
    deuda: Double,
    onPersonAction: (person: Person, action: PersonAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    var modalController: BottomSheetController? by remember {
        mutableStateOf(null)
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val personAccountsIds = allAccounts
        .filter { it.ownerId == person.id }
        .map { it.id }
    val transactions = allTransactions
        .filter {
            it.aNombreDe == person.id ||
                    it.sourceId in personAccountsIds ||
                    it.destinationId in personAccountsIds
        }
        .sortedByDescending { it.date }
    val transactionListItemDetails = TransactionListItemDetails.from(
        transactions = transactions,
        accounts = allAccounts,
        categories = allCategories,
        principalPersonId = principalPerson?.id
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
        TransactionPage(
            transactionList = transactionListItemDetails,
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
            onTitleSetted = {}
        )
    }
}