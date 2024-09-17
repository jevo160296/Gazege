package com.jmml.gazege.ui.views.person

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionListItemDetailsWithSign
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.personaDeleitionConfirmationBuilder
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.transactionDeleitionConfirmationBuilder
import com.jmml.gazege.ui.views.BottomSheetController
import com.jmml.gazege.ui.views.EntityDetail
import com.jmml.gazege.ui.views.PersonAction
import com.jmml.gazege.ui.views.TransactionAction
import com.jmml.gazege.ui.views.transaction.LoadedTransactionPage
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.SmallBody
import com.jmml.zoo.extensions.numerical.toMoneyString
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun PersonDetail(
    principalPerson: Person,
    person: Person,
    viewModelPersonDetail: MainViewModel.ViewModelPersonDetail,
    deuda: Double,
    onPersonAction: (person: Person, action: PersonAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    var justPendingTransactions: Boolean by remember { mutableStateOf(true) }
    val transactionListItemDetails by viewModelPersonDetail.rememberPeopleTransactionListItemDetails(
        principalPerson.id,
        person.id,
        justPendingTransactions,
        deuda
    )

    PersonDetailUI(
        person = person,
        deuda = deuda,
        transactionListItemDetails = transactionListItemDetails,
        justPendingTransactions = justPendingTransactions,
        onJustPendingTransactionsChange = { justPendingTransactions = it },
        onPersonAction = onPersonAction,
        onTransactionAction = onTransactionAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonDetailUI(
    person: Person,
    deuda: Double,
    transactionListItemDetails: List<TransactionListItemDetailsWithSign>?,
    justPendingTransactions: Boolean,
    onJustPendingTransactionsChange: (Boolean) -> Unit,
    onPersonAction: (person: Person, action: PersonAction) -> Unit,
    onTransactionAction: (transaction: Transaction, action: TransactionAction) -> Unit
) {
    var modalController: BottomSheetController? by remember {
        mutableStateOf(null)
    }
    var simplifyView: Boolean by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val simplifyViewButton = @Composable {
        IconButton(onClick = { simplifyView = !simplifyView }) {
            Icon(
                painter = painterResource(id = R.drawable.eye),
                contentDescription = "Edit"
            )
        }
    }

    Crossfade(simplifyView, label = "Crossfade") {
        if (!it) {
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
                aditionalItem = simplifyViewButton,
                sheetState = sheetState
            ) {
                SmallBody(
                    text = "${personDeudaString(deuda)}: ${doubleToMoneyString(deuda)}",
                    Modifier.padding(
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
                        onCheckedChange = { onJustPendingTransactionsChange(it) },
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
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { MediumHeadline(person.name) },
                        actions = {
                            simplifyViewButton()
                        }
                    )
                },
                bottomBar = {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MediumHeadline("Total:")
                        MediumHeadline(
                            deuda.absoluteValue.toMoneyString(),
                            color = when {
                                deuda < 0 -> GazegeTheme.gazegeColorScheme.income
                                deuda > 0 -> GazegeTheme.gazegeColorScheme.outcome
                                else -> MaterialTheme.colorScheme.onBackground
                            }
                        )
                    }
                }
            ) {
                LoadedTransactionPage(
                    modifier = Modifier.padding(it),
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
                    onFirstElementVisibleChanged = {},
                    transactionViewHolder = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                it.transaction.description,
                                Modifier.weight(2f / 3f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                it.transaction.amount.toMoneyString(),
                                Modifier.weight(1f / 3f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                color = (it.sign * it.transaction.amount).let { value ->
                                    when {
                                        value < 0 -> GazegeTheme.gazegeColorScheme.income
                                        value > 0 -> GazegeTheme.gazegeColorScheme.outcome
                                        else -> MaterialTheme.colorScheme.onBackground
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewPersonDetail() {
    GazegeTheme {
        val (justPendingTransactions, onJustPendingTransactionsChange) = remember {
            mutableStateOf(
                true
            )
        }
        Box(
            Modifier
                .navigationBarsPadding()
                .statusBarsPadding()) {
            DatabaseSample {
                val person = remember { this.personSample.firstOrNull() }
                val person2 = remember { this.personSample.getOrNull(1) }
                val transactionListItemDetails = remember {
                    TransactionListItemDetailsWithSign.from(
                        this.transactionSample,
                        this.categorieSample,
                        this.accountSample,
                        person?.id,
                        person2?.id
                    )
                }
                if (person != null) {
                    PersonDetailUI(
                        person = person,
                        deuda = 1000.0,
                        transactionListItemDetails = transactionListItemDetails,
                        justPendingTransactions = justPendingTransactions,
                        onJustPendingTransactionsChange = onJustPendingTransactionsChange,
                        onPersonAction = { _, _ -> },
                        onTransactionAction = { _, _ -> }
                    )
                }
            }
        }
    }
}