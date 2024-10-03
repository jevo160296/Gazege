package com.jmml.gazege.ui.views.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.core.entities.TransactionType
import com.jmml.gazege.ui.DateFormat
import com.jmml.gazege.ui.localDateToString
import com.jmml.gazege.ui.templates.GroupedLazyList
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.views.promissorynote.DefaultPromissoryNoteViewHolder
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteViewModel
import com.jmml.gazege.ui.views.transaction.TransactionGroupItemViewHolder
import com.jmml.gazege.ui.widgets.DefaultGroupViewHolder
import com.jmml.gazege.ui.widgets.PulsatingCard
import java.time.LocalDate

@Composable
fun LoadingDocumentListView(
    modifier: Modifier = Modifier,
    onTitleSetted: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    onTitleSetted(stringResource(id = R.string.transacciones))
    GroupedLazyList(
        modifier = modifier,
        state = LazyListState(),
        contentPadding = contentPadding,
        items = (0..10).toList(),
        groupSelector = { "" },
        groupViewHolder = {
            PulsatingCard(
                modifier = Modifier
                    .width(90.dp)
                    .height(18.dp)
                    .clip(shape = MaterialTheme.shapes.small),
                color = MaterialTheme.colorScheme.scrim,
                minAlpha = 0.0F,
                maxAlpha = 0.2F
            )
        }
    ) {
        DefaultDocumentViewHolder(
            document = null,
            editDocument = {},
            delDocument = {}
        )
    }
}

@Composable
fun LoadedDocumentListView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    documents: List<IDocumentViewModel>,
    delDocument: (IDocumentViewModel) -> Unit,
    editDocument: (IDocumentViewModel) -> Unit,
    state: LazyListState = LazyListState(),
    nestedScrollConnection: NestedScrollConnection? = null,
    onTitleSetted: (String) -> Unit,
    onZeroElementsChanged: (Boolean) -> Unit,
    onFirstElementVisibleChanged: (isVisible: Boolean) -> Unit,
    dateViewHolder: @Composable (String) -> Unit = { DefaultGroupViewHolder(it) },
    documentViewHolder: @Composable (IDocumentViewModel) -> Unit = {
        DefaultDocumentViewHolder(
            document = it,
            editDocument = editDocument,
            delDocument = delDocument
        )
    }
) {
    LaunchedEffect(documents.isEmpty()) { onZeroElementsChanged(documents.isEmpty()) }
    val firstElementIsVisible by remember { derivedStateOf { state.firstVisibleItemIndex == 0 } }
    LaunchedEffect(firstElementIsVisible) { onFirstElementVisibleChanged(firstElementIsVisible) }
    onTitleSetted(stringResource(id = R.string.transacciones))

    GroupedLazyList(
        modifier = nestedScrollConnection?.let { modifier.nestedScroll(nestedScrollConnection) }
            ?: modifier,
        state = state,
        contentPadding = contentPadding,
        items = documents,
        groupSelector = { localDateToString(it.date, DateFormat.DAYMONTHYEAR) },
        groupViewHolder = dateViewHolder,
        itemViewHolder = documentViewHolder
    )
}

@Composable
fun DefaultDocumentViewHolder(
    document: IDocumentViewModel?,
    editDocument: (IDocumentViewModel) -> Unit,
    delDocument: (IDocumentViewModel) -> Unit
) {
    if (document != null) {
        when (document) {
            is TransactionDocumentViewModel -> TransactionGroupItemViewHolder(
                transaction = document.transactionListItemDetails,
                delTransaction = { delDocument(TransactionDocumentViewModel(it)) },
                editTransaction = { editDocument(TransactionDocumentViewModel(it)) }
            )

            is PromissoryNoteDocumentViewModel -> DefaultPromissoryNoteViewHolder(
                promissoryNote = document.promissoryNoteViewModel,
                editPromissoryNote = { editDocument(PromissoryNoteDocumentViewModel(it)) },
                delPromissoryNote = { delDocument(PromissoryNoteDocumentViewModel(it)) }
            )

            is PromissoryNoteDocumentWithSignViewModel -> DefaultPromissoryNoteViewHolder(
                promissoryNote = document.promissoryNoteWithSignViewModel,
                editPromissoryNote = { editDocument(PromissoryNoteDocumentWithSignViewModel(it)) },
                delPromissoryNote = { delDocument(PromissoryNoteDocumentWithSignViewModel(it)) }
            )

            is TransactionDocumentWithSignViewModel -> TransactionGroupItemViewHolder(
                transaction = document.transactionListItemWithSign,
                delTransaction = { delDocument(TransactionDocumentWithSignViewModel(it)) },
                editTransaction = { editDocument(TransactionDocumentWithSignViewModel(it)) }
            )
        }
    } else {
        PulsatingCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(shape = MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.scrim,
            minAlpha = 0.0f,
            maxAlpha = 0.2f
        )
    }
}

@Preview
@Composable
private fun LoadingDocumentListViewPreview() {
    GazegeTheme {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            LoadingDocumentListView()
        }
    }
}

@Preview
@Composable
private fun LoadedDocumentListViewPreview() {
    val documentList = listOf(
        TransactionDocumentViewModel(
            TransactionListItemDetails(
                transaction = Transaction(
                    amount = 100.0, date =
                    LocalDate.now(),
                    description = "",
                    sourceId = 0,
                    destinationId = 0,
                    aNombreDe = 0,
                    categoryId = 0
                ),
                sourceAccount = Account(
                    name = "Source account",
                    ownerId = 0,
                ),
                destinationAccount = Account(
                    name = "Destination account",
                    ownerId = 0,
                ),
                category = null,
                transactionType = TransactionType.INCOME
            )
        ),
        PromissoryNoteDocumentViewModel(
            PromissoryNoteViewModel(
                principalPersonId = 0,
                promissoryNote = PromissoryNote(
                    amount = 100.0,
                    date = LocalDate.now(),
                    sourceId = 0,
                    destinationId = 1,
                    description = "Promissory note"
                ),
                sourcePerson = Person(
                    name = "Source person",
                ),
                destinationPerson = Person(
                    name = "Destination person"
                )
            )
        ),
        PromissoryNoteDocumentViewModel(
            PromissoryNoteViewModel(
                principalPersonId = 0,
                promissoryNote = PromissoryNote(
                    amount = 100.0,
                    date = LocalDate.now(),
                    sourceId = 1,
                    destinationId = 0,
                    description = "Promissory note"
                ),
                sourcePerson = Person(
                    name = "Source person",
                ),
                destinationPerson = Person(
                    name = "Destination person"
                )
            )
        ),
        PromissoryNoteDocumentViewModel(
            PromissoryNoteViewModel(
                principalPersonId = 0,
                promissoryNote = PromissoryNote(
                    amount = 100.0,
                    date = LocalDate.now(),
                    sourceId = 2,
                    destinationId = 3,
                    description = "Promissory note"
                ),
                sourcePerson = Person(
                    name = "Source person",
                ),
                destinationPerson = Person(
                    name = "Destination person"
                )
            )
        )
    )
    GazegeTheme {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            LoadedDocumentListView(
                documents = documentList,
                delDocument = {},
                editDocument = {},
                onTitleSetted = { _ -> },
                onZeroElementsChanged = { _ -> },
                onFirstElementVisibleChanged = { _ -> }
            )
        }
    }
}