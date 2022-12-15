package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation

data class PersonWithAccounts (
    @Embedded val person: Person,
    @Relation(
        entity = Account::class,
        parentColumn = "id",
        entityColumn = "ownerId"
    )
    val accounts: List<AccountAndOwnerWithTransactions>
){
    @Ignore private var total: Double = Double.NaN
    fun getTotal(): Double{
        if(total.isNaN()){
            total = accounts.sumOf { it.getTotal() }
        }
        return total
    }
}