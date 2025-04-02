package com.jmml.gazege.core.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["aNombreDe"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class TransactionDetails(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val transactionId: Int,
    val amount: Double,
    val description: String,
    val categoryId: Int?,
    val aNombreDe: Int?,
    val budgetDate: LocalDate?
)