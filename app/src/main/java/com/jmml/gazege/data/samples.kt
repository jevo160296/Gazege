package com.jmml.gazege.data

import com.jmml.gazege.MainViewModel
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.BudgetType
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.ui.databaseSample
import com.jmml.gazege.ui.doubleToPercentageString
import com.jmml.gazege.ui.progressStatus.HistoricalProgressStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SampleId {
    BigSample,
    SmallSample,
    CategoriesSample,
    CategoriesMultipleBudgetSample,
    VariableFixedCategorySample
}

fun sample(sampleId: SampleId, sampleModule: MainViewModel.SampleModule) {
    when (sampleId) {
        SampleId.BigSample -> bigSample(sampleModule)
        SampleId.SmallSample -> smallSample(sampleModule)
        SampleId.CategoriesSample -> categoriesSample(sampleModule)
        SampleId.CategoriesMultipleBudgetSample -> categoriesMultipleBudgetSample(sampleModule)
        SampleId.VariableFixedCategorySample -> variableFixedCategorySample(sampleModule)
    }
}

private fun bigSample(
    sampleModule: MainViewModel.SampleModule
) {
    databaseSample {
        buildSample(
            sampleModule,
            { personSample },
            { accountSample },
            { categorieSample },
            { budgetSample }
        ) {
            TransactionWithDetails.from(
                transactionSample,
                transactionDetailsSample
            )
        }
    }
}

private fun smallSample(
    sampleModule: MainViewModel.SampleModule
) {
    databaseSample(
        categoriesAmount = 0,
        transactionAmount = 0,
        budgetAmount = 0,
        principalPersonAccountAmount = 5
    ) {
        buildSample(
            sampleModule,
            { personSample },
            { accountSample },
            { categorieSample },
            { budgetSample },
            {
                TransactionWithDetails.from(
                    transactionSample,
                    transactionDetailsSample
                )
            }
        )
    }
}

private fun categoriesSample(
    sampleModule: MainViewModel.SampleModule
) {
    val today = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)
    buildSample(
        sampleModule,
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
                Budget.fromMonthly(0, 0, 4000000.0),
                Budget.fromMonthly(1, 2, -600000.0),
                Budget.fromMonthly(2, 4, -50000.0)
            )
        },
        {
            listOf(
                TransactionWithDetails(
                    transaction = Transaction(
                        id = 0,
                        sourceId = 3,
                        destinationId = 0,
                        date = startOfMonth
                    ),
                    transactionDetails = listOf(
                        TransactionDetails(
                            id = 0,
                            transactionId = 0,
                            amount = 2500000.0,
                            description = "",
                            categoryId = 0,
                            aNombreDe = null,
                            budgetDate = null
                        )
                    )
                ),
                TransactionWithDetails(
                    transaction = Transaction(
                        id = 1,
                        sourceId = 0,
                        destinationId = 4,
                        date = startOfMonth
                    ),
                    transactionDetails = listOf(
                        TransactionDetails(
                            id = 1,
                            transactionId = 1,
                            amount = 600000.0,
                            description = "",
                            categoryId = 2,
                            aNombreDe = null,
                            budgetDate = null
                        )
                    )
            ),
                TransactionWithDetails(
                    transaction = Transaction(
                        id = 2,
                        sourceId = 0,
                        destinationId = 4,
                        date = startOfMonth
                    ),
                    transactionDetails = listOf(
                        TransactionDetails(
                            id = 2,
                            transactionId = 2,
                            amount = 20000.0,
                            description = "",
                            categoryId = 4,
                            aNombreDe = null,
                            budgetDate = null
                        )
                    )
                )
            )
        }
    )
}

private fun categoriesMultipleBudgetSample(
    sampleModule: MainViewModel.SampleModule
) {
    val today = LocalDate.now()
    val startOfMonth = today.withDayOfMonth(1)
    buildSample(
        sampleModule,
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
                Budget.fromMonthly(0, 0, 1000000.0),
                Budget.fromMonthly(1, 0, 1000000.0),
                Budget.fromMonthly(2, 0, 1000000.0)
            )
        },
        {
            listOf(
                TransactionWithDetails(
                    transaction = Transaction(
                        id = 0,
                        sourceId = 3,
                        destinationId = 0,
                        date = startOfMonth
                    ),
                    transactionDetails = listOf(
                        TransactionDetails(
                            id = 0,
                            transactionId = 0,
                            amount = 1000000.0,
                            description = "",
                            categoryId = 0,
                            aNombreDe = null,
                            budgetDate = null
                        )
                    )
                )
            )
        }
    )
}

private fun variableFixedCategorySample(sampleModule: MainViewModel.SampleModule) {
    buildSample(
        sampleModule,
        personSample = {
            listOf(
                Person(id = 0, name = "Principal", importance = 0),
                Person(id = 1, name = "__SPECIAL__")
            )
        },
        accountSample = {
            listOf(
                Account(id = 0, name = "Bank", ownerId = 0),
                Account(id = 1, name = "__INCOME__", ownerId = 1, isIncome = true),
                Account(id = 2, name = "__OUTCOME__", ownerId = 1, isOutcome = true)
            )
        },
        categorieSample = {
            listOf(
                Category(id = 0, name = "C1", budgetType = BudgetType.FIXED, parentId = null),
                Category(id = 1, name = "Fixed", budgetType = BudgetType.FIXED, parentId = 0),
                Category(id = 2, name = "Variable", budgetType = BudgetType.VARIABLE, parentId = 0),
            )
        },
        budgetSample = {
            listOf(
                Budget.fromDaily(id = 0, 1, 1000.0, 3, LocalDate.of(2023, 1, 1)),
                Budget.fromDaily(1, 2, 2000.0, 4, LocalDate.of(2023, 1, 1))
            )
        },
        transactionSample = { emptyList() }
    )
}

private fun buildSample(
    sampleModule: MainViewModel.SampleModule,
    personSample: () -> List<Person>,
    accountSample: () -> List<Account>,
    categorieSample: () -> List<Category>,
    budgetSample: () -> List<Budget>,
    transactionSample: () -> List<TransactionWithDetails>
) {
    val progressStatus = HistoricalProgressStatus.start(
        "Building sample",
        5.0,
        1.0
    ) {
        sampleModule.importStatePostValue(
            MainViewModel.ProgressStatusState(
                it.message,
                it.progress,
                it.status,
                MainViewModel.Type.IMPORT
            )
        )
    }
    sampleModule.viewModelScope.launch(Dispatchers.Default) {
        sampleModule.deleteAll().join()
        sampleModule.insertPerson(*personSample().toTypedArray()) {}.join()
        progressStatus.incrementProgress(
            "Inserting account... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        sampleModule.insertAccount(
            *accountSample().toTypedArray(),
            onErrorAction = {}) {}.join()
        progressStatus.incrementProgress(
            "Inserting categories... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        sampleModule.insertCategory(*categorieSample().map { it.copy(parentId = null) }
            .toTypedArray(), onErrorAction = {}, onCompleitionAction = {}).join()
        sampleModule.updateCategory(
            *categorieSample().toTypedArray(),
            onErrorAction = {},
            onCompleitionAction = {}).join()
        progressStatus.incrementProgress(
            "Inserting Transactions... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        sampleModule.insertTransaction(*transactionSample().toTypedArray()) {}.join()
        progressStatus.incrementProgress(
            "Inserting budget... ${
                doubleToPercentageString(
                    progressStatus.progress
                )
            }"
        )
        sampleModule.insertBudget(
            *budgetSample().toTypedArray(),
            onCompleitionAction = {},
            onErrorAction = {}).join()
        progressStatus.finish("Finish inserting data 100%")
    }
}