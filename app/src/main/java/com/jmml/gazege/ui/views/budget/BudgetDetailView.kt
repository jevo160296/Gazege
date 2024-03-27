package com.jmml.gazege.ui.views.budget

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.jmml.gazege.core.entities.BudgetAndCategoryWithTransactions

@Composable
fun BudgetDetailView(budget: BudgetAndCategoryWithTransactions) {
    //TODO diseñar UI
    Text("Detalles para el presupuesto: ${budget.categoryName}")
}

@Composable
fun EmptyBudgetDetailView() {
    //TODO diseñar UI
    Text("Presupuesto no encontrado en la base de datos")
}