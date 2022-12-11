package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val owner: Person,
    val initial_balance: Double
    )
