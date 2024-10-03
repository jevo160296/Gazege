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
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        ),
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["destinationId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class PromissoryNote(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val amount: Double,
    val date: LocalDate,
    val sourceId: Int,
    val destinationId: Int,
    val description: String
)