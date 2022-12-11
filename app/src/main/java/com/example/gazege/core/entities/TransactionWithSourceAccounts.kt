package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithSourceAccounts(
    @Embedded(prefix = "source_") val sourceAccount: Account,
    @Relation(
        parentColumn = "source_id",
        entityColumn = "id"
    )
    val transaction: List<Transaction>
)