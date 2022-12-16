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
}