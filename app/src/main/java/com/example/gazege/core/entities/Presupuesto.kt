package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val categoryId: Int,
    val value: Double,
    val frequency: Int,
    val frequencyType: FrequencyType,
    val startDate: LocalDate
)

enum class FrequencyType {
    DAILY,
    WEEKLY,
    MONTHLY
}