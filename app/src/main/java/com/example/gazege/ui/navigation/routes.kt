package com.example.gazege.ui.navigation

import com.example.gazege.ui.views.AddTransactionAction
import java.time.LocalDate

fun addTransactionRoute(date: LocalDate, transactionAction: AddTransactionAction) =
    "addTransaction/${date.let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }}/${transactionAction.name}"