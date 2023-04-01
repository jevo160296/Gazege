package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val parentId: Int?
)