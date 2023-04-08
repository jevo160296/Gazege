package com.example.gazege.ui.navigation

import java.time.LocalDate

fun addTransactionRoute(date: LocalDate) =
    "addTransaction/${date.let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }}"