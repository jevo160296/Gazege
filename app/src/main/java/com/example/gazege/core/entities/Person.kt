package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String
)
