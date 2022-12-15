package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation

data class AccountAndOwnerWithTransactions(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id"
    )
    val owner: Person,
    @Relation(
        parentColumn = "id",
        entityColumn = "sourceId"
    )
    val outTransactions: List<Transaction>,
    @Relation(
        parentColumn = "id",
        entityColumn = "destinationId"
    )
    val inTransactions: List<Transaction>,
){
    @Ignore private var total: Double = Double.NaN
    fun getTotal(): Double {
        if (total.isNaN()){
            total = inTransactions.sumOf { it.amount } - outTransactions.sumOf { it.amount }
        }
        return total
    }
}