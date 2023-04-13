package com.example.gazege.ui.views.budget

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.example.gazege.core.entities.BudgetAndCategoryWithTransactions

@Composable
fun BudgetDetailView(budget: BudgetAndCategoryWithTransactions) {
    //TODO diseñar interfaz
    Text("Detalles para el presupuesto: ${budget.categoryName}")
}

@Composable
fun EmptyBudgetDetailView() {
    //TODO diseñar interfaz
    Text("Presupuesto no encontrado en la base de datos")
}