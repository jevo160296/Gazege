package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionAndSourceAccounts(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "id"
    )
    val account: Account
)