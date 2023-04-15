package com.example.gazege.core.entities

import androidx.room.Embedded
import androidx.room.Relation

data class AccountAndOwner(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "ownerId",
        entityColumn = "id"
    )
    val owner: Person
){
    companion object {
        fun from(account: List<Account>, persons: List<Person>): List<AccountAndOwner> {
            val indexedPersons = persons.associateBy { it.id }
            return account.map {
                AccountAndOwner(it, indexedPersons[it.ownerId]!!)
            }
        }
    }
}