package com.jmml.gazege.core.entities

import androidx.room.Entity
import java.time.LocalDate

@Entity
data class ExtendedTransaction(
    val id: Int,
    val sourceId: Int,
    val destinationId: Int,
    val date: LocalDate,
    val transactionDetailId: Int,
    val amount: Double,
    val description: String,
    val categoryId: Int?,
    val aNombreDe: Int?,
    val budgetDate: LocalDate?
)