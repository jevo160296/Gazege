package com.jmml.gazege.core.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.jmml.gazege.core.entities.Account
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["destinationId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val sourceId: Int,
    val destinationId: Int,
    val date: LocalDate,
)
