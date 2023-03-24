package com.example.gazege.core.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.SET_NULL
        )],
    indices = [
        Index(value = ["name", "ownerId"], unique = true)
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val ownerId: Int,
    val parentId: Int? = null,
    @ColumnInfo(defaultValue = "true") val includedInTotal: Boolean = true,
    @ColumnInfo(defaultValue = "false") val isIncome: Boolean = false,
    @ColumnInfo(defaultValue = "false") val isOutcome: Boolean = false
) {
    companion object {
        fun empty(): Account {
            return Account(name = "Null", ownerId = -1)
        }
    }
}
