package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionAndAccounts(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "sourceId",
        entityColumn = "id"
    )
    val sourceAccount: Account,
    @Relation(
        parentColumn = "destinationId",
        entityColumn = "id"
    )
    val destinationAccount: Account
)