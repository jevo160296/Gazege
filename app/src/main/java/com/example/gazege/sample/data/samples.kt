package com.example.gazege.sample.data

import androidx.lifecycle.viewModelScope
import com.example.gazege.MainViewModel
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.ui.databaseSample
import com.example.gazege.ui.doubleToPercentageString
import com.example.gazege.ui.progressStatus.HistoricalProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SampleId {
    BigSample,
    SmallSample,
    CategoriesSample,
    CategoriesMultipleBudgetSample
}

fun sample(sampleId: SampleId, viewModel: MainViewModel) {
    when (sampleId) {
        SampleId.BigSample -> bigSample(viewModel)
        SampleId.SmallSample -> smallSample(viewModel)
        SampleId.CategoriesSample -> categoriesSample(viewModel)
        SampleId.CategoriesMultipleBudgetSample -> categoriesMultipleBudgetSample(viewModel)
    }
}

private fun bigSample(
    viewModel: MainViewModel
) {
    databaseSample {
        buildSample(
            viewModel,
            { personSample },
            { accountSample },
            { categorieSample },
            { budgetSample }
        ) { transactionSample }
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
            { personSample },
            { accountSample },
            { categorieSample },
            { budgetSample },
            { transactionSample }
        )
    }
}

private fun categoriesSample(
    viewModel: MainViewModel
) {
    val today = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)
    buildSample(
        viewModel,
        {
            listOf(
                Person(0, "Pedro", 0),
                Person(1, "Hortensia"),
                Person(2, "Pablo"),
                Person(3, "__ESPECIAL__")
            )
        },
        {
            listOf(
                Account(0, "Pedro", 0),
                Account(1, "Hortensia", 1),
                Account(2, "Pablo", 2),
                Account(3, "__INGRESO__", 3, isIncome = true),
                Account(4, "__GASTO__", 3, isOutcome = true)
            )
        },
        {
            listOf(
                Category(0, "Ingreso", BudgetType.FIXED, null),
                Category(1, "Hogar", BudgetType.FIXED, null),
                Category(2, "Renta", BudgetType.FIXED, 1),
                Category(3, "Servicios", BudgetType.FIXED, null),
                Category(4, "Luz", BudgetType.FIXED, 3),
                Category(5, "Agua", BudgetType.FIXED, 3)
            )
        },
        {
            listOf(
                Budget.fromMonthly(0, 0, 4000000.0, BudgetType.FIXED),
                Budget.fromMonthly(1, 2, -600000.0, BudgetType.FIXED),
                Budget.fromMonthly(2, 4, -50000.0, BudgetType.FIXED)
            )
        },
        {
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
        }
    )
}

private fun categoriesMultipleBudgetSample(
    viewModel: MainViewModel
) {
    val today = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)
    buildSample(
        viewModel,
        personSample = {
            listOf(
                Person(0, "Pedro", 0),
                Person(1, "Hortensia"),
                Person(2, "Juan"),
                Person(3, "__ESPECIAL__")
            )
        },
        accountSample = {
            listOf(
                Account(0, "Pedro", 0),
                Account(1, "Hortensia", 1),
                Account(2, "Pablo", 2),
                Account(3, "__INGRESO__", 3, isIncome = true),
                Account(4, "__GASTO__", 3, isOutcome = true)
            )
        },
        categorieSample = {
            listOf(
                Category(0, "Ingreso", BudgetType.FIXED, null),
                Category(1, "Hogar", BudgetType.FIXED, null),
                Category(2, "Renta", BudgetType.FIXED, 1),
                Category(3, "Servicios", BudgetType.FIXED, null),
                Category(4, "Luz", BudgetType.FIXED, 3),
                Category(5, "Agua", BudgetType.FIXED, 3)
            )
        },
        {
            listOf(
                Budget.fromMonthly(0, 0, 1000000.0, BudgetType.FIXED),
                Budget.fromMonthly(1, 0, 1000000.0, BudgetType.FIXED),
                Budget.fromMonthly(2, 0, 1000000.0, BudgetType.FIXED)
            )
        },
        {
            listOf(
                Transaction(
                    0,
                    1000000.0,
                    sourceId = 3,
                    destinationId = 0,
                    categoryId = 0,
                    date = startOfMonth,
                    aNombreDe = null,
                    description = ""
                )
            )
        }
    )
}

private fun buildSample(
    viewModel: MainViewModel,
    personSample: () -> List<Person>,
    accountSample: () -> List<Account>,
    categorieSample: () -> List<Category>,
    budgetSample: () -> List<Budget>,
    transactionSample: () -> List<Transaction>
) {
    val progressStatus = HistoricalProgressStatus.start(
        "Building sample",
        5.0,
        1.0
    ) {
        viewModel.importStatePostValue(
            MainViewModel.ProgressStatusState(
                it.message,
                it.progress,
                it.status,
                MainViewModel.Type.IMPORT
            )
        )
    }
    viewModel.viewModelScope.launch(Dispatchers.Default) {
        viewModel.insertPerson(*personSample().toTypedArray()) {}
        progressStatus.incrementProgress(
            "Inserting account... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        viewModel.insertAccount(
            *accountSample().toTypedArray(),
            onErrorAction = {}) {}
        progressStatus.incrementProgress(
            "Inserting categories... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        viewModel.insertCategory(*categorieSample().map { it.copy(parentId = null) }
            .toTypedArray(), onErrorAction = {}, onCompleitionAction = {})
        viewModel.updateCategory(
            *categorieSample().toTypedArray(),
            onErrorAction = {},
            onCompleitionAction = {})
        progressStatus.incrementProgress(
            "Inserting Transactions... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        viewModel.insertTransaction(*transactionSample().toTypedArray()) {}
        progressStatus.incrementProgress(
            "Inserting budget... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        viewModel.insertBudget(
            *budgetSample().toTypedArray(),
            onCompleitionAction = {},
            onErrorAction = {})
        progressStatus.finish("Finish inserting data 100%")
    }
}