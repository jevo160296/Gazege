package com.jmml.gazege.ui.views

enum class AccountAction {
    EDIT,
    DELETE
}

sealed interface AddAction

enum class AddTransactionAction : AddAction {
    ADD_EXPENSE,
    ADD_INCOME,
    ADD_TRANSFER
}

enum class AddPromissoryNoteAction : AddAction {
    ADD_PROMISSORY_NOTE
}

enum class TransactionAction {
    EDIT,
    DELETE
}

enum class PersonAction {
    EDIT,
    DELETE
}