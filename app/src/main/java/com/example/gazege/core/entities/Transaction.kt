package com.example.gazege.core.entities

import java.util.Date

data class Transaction(
    val id: Int,
    val amount: Double,
    val description: String,
    val source: Account,
    val destination: Account,
    val date: Date
    )
