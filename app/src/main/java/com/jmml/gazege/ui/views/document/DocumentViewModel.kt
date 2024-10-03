package com.jmml.gazege.ui.views.document

import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteViewModel
import java.time.LocalDate

sealed interface IDocumentViewModel {
    val date: LocalDate
}

data class TransactionDocumentViewModel(
    val transactionListItemDetails: TransactionListItemDetails
) : IDocumentViewModel {
    override val date: LocalDate
        get() = transactionListItemDetails.transaction.date
}

data class PromissoryNoteDocumentViewModel(
    val promissoryNoteViewModel: PromissoryNoteViewModel
) : IDocumentViewModel {
    override val date: LocalDate
        get() = promissoryNoteViewModel.promissoryNote.date
}