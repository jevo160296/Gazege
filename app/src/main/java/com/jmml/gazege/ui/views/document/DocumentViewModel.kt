package com.jmml.gazege.ui.views.document

import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.core.entities.TransactionListItemDetailsWithSign
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteViewModel
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteWithSignViewModel
import java.time.LocalDate

sealed interface IDocumentViewModel {
    val id: Int?
    val date: LocalDate
    val amount: Double
    val description: String
}

data class TransactionDocumentViewModel(
    val transactionListItemDetails: TransactionListItemDetails
) : IDocumentViewModel {
    override val date: LocalDate
        get() = transactionListItemDetails.transaction.date
    override val id: Int?
        get() = transactionListItemDetails.transaction.transaction.id
    override val amount: Double
        get() = transactionListItemDetails.transaction.totalAmount
    override val description: String
        get() = transactionListItemDetails.transaction.descriptionString
}

data class PromissoryNoteDocumentViewModel(
    val promissoryNoteViewModel: PromissoryNoteViewModel
) : IDocumentViewModel {
    override val date: LocalDate
        get() = promissoryNoteViewModel.promissoryNote.date
    override val id: Int?
        get() = promissoryNoteViewModel.promissoryNote.id
    override val amount: Double
        get() = promissoryNoteViewModel.promissoryNote.amount
    override val description: String
        get() = promissoryNoteViewModel.promissoryNote.description
}

sealed interface IDocumentWithSignViewModel : IDocumentViewModel {
    val sign: Int
}

data class TransactionDocumentWithSignViewModel(
    val transactionListItemWithSign: TransactionListItemDetailsWithSign
) : IDocumentWithSignViewModel {
    override val date: LocalDate
        get() = transactionListItemWithSign.transaction.date
    override val id: Int?
        get() = transactionListItemWithSign.transaction.transactionDetailsId
    override val sign: Int
        get() = transactionListItemWithSign.sign
    override val amount: Double
        get() = transactionListItemWithSign.transaction.amount
    override val description: String
        get() = transactionListItemWithSign.transaction.description
}

data class PromissoryNoteDocumentWithSignViewModel(
    val promissoryNoteWithSignViewModel: PromissoryNoteWithSignViewModel
) : IDocumentWithSignViewModel {
    override val date: LocalDate
        get() = promissoryNoteWithSignViewModel.promissoryNote.date
    override val id: Int?
        get() = promissoryNoteWithSignViewModel.promissoryNote.id
    override val sign: Int
        get() = promissoryNoteWithSignViewModel.sign
    override val amount: Double
        get() = promissoryNoteWithSignViewModel.promissoryNote.amount
    override val description: String
        get() = promissoryNoteWithSignViewModel.promissoryNote.description
}