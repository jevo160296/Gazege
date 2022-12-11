package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val amount: Double,
    val description: String,
    val sourceId: Int,
    val destinationId: Int,
    val date: Date
    )
