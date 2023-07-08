package com.example.gazege.sample.data

import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.databaseSample
import java.time.LocalDate

enum class SampleId {
    BigSample,
    SmallSample,
    CategoriesSample
}

fun sample(sampleId: SampleId, viewModel: MainViewModel) {
    when (sampleId) {
        SampleId.BigSample -> bigSample(viewModel)
        SampleId.SmallSample -> smallSample(viewModel)
        SampleId.CategoriesSample -> categoriesSample(viewModel)
    }
}

private fun bigSample(
    viewModel: MainViewModel
) {
    databaseSample {
        buildSample(
            viewModel,
            personSample,
            accountSample,
            categorieSample,
            budgetSample,
            transactionSample
        )
    }
}

private fun smallSample(
    viewModel: MainViewModel
) {
    databaseSample(
        categoriesAmount = 0,
        transactionAmount = 0,
        budgetAmount = 0,
        principalPersonAccountAmount = 5
    ) {
        buildSample(
            viewModel,
            personSample,
            accountSample,
            categorieSample,
            budgetSample,
            transactionSample
        )
    }
}

private fun categoriesSample(
    viewModel: MainViewModel
) {
    val today = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)
    databaseSample(principalPersonAccountAmount = 5) {
        buildSample(
            viewModel,
            listOf(
                Person(0, "Pedro", 0),
                Person(1, "Hortensia"),
                Person(2, "Pablo"),
                Person(3, "__ESPECIAL__")
            ),
            listOf(
                Account(0, "Pedro", 0),
                Account(1, "Hortensia", 1),
                Account(2, "Pablo", 2),
                Account(3, "__INGRESO__", 3, isIncome = true),
                Account(4, "__GASTO__", 3, isOutcome = true)
            ),
            listOf(
                Category(0, "Ingreso", null),
                Category(1, "Hogar", null),
                Category(2, "Renta", 1),
                Category(3, "Servicios", null),
                Category(4, "Luz", 3),
                Category(5, "Agua", 3)
            ),
            listOf(
                Budget.fromMonthly(0, 0, 4000000.0, BudgetType.FIXED),
                Budget.fromMonthly(1, 2, -600000.0, BudgetType.FIXED),
                Budget.fromMonthly(2, 4, -50000.0, BudgetType.FIXED)
            ),
            listOf(
                Transaction(
                    0,
                    2500000.0,
                    sourceId = 3,
                    destinationId = 0,
                    categoryId = 0,
                    date = startOfMonth,
                    aNombreDe = null,
                    description = ""
                ),
                Transaction(
                    1,
                    600000.0,
                    sourceId = 0,
                    destinationId = 4,
                    categoryId = 2,
                    date = startOfMonth,
                    aNombreDe = null,
                    description = ""
                ),
                Transaction(
                    2,
                    20000.0,
                    sourceId = 0,
                    destinationId = 4,
                    categoryId = 4,
                    date = startOfMonth,
                    aNombreDe = null,
                    description = ""
                )
            )
        )
    }
}

private fun buildSample(
    viewModel: MainViewModel,
    personSample: List<Person>,
    accountSample: List<Account>,
    categorieSample: List<Category>,
    budgetSample: List<Budget>,
    transactionSample: List<Transaction>
) {
    viewModel.insertPerson(*personSample.toTypedArray()) {}
    viewModel.insertAccount(
        *accountSample.toTypedArray(),
        onErrorAction = {}) {}
    viewModel.insertCategory(*categorieSample.map { it.copy(parentId = null) }
        .toTypedArray(), onErrorAction = {}, onCompleitionAction = {})
    viewModel.updateCategory(
        *categorieSample.toTypedArray(),
        onErrorAction = {},
        onCompleitionAction = {})
    viewModel.insertTransaction(*transactionSample.toTypedArray()) {}
    viewModel.insertBudget(
        *budgetSample.toTypedArray(),
        onCompleitionAction = {},
        onErrorAction = {})
}