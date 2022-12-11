package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Person::class,
        parentColumns = ["id"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val ownerId: Int,
    val initial_balance: Double
)
