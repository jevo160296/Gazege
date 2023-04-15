package com.example.gazege.ui

import androidx.compose.runtime.Composable
import com.example.gazege.PersonSummaryState
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwner
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetAndCategoryWithCalculatedData
import com.example.gazege.core.entities.BudgetAndCategoryWithTransactions
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.FrequencyType
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.core.entities.TransactionAndAccountsAndCategory
import com.example.gazege.core.entities.WeekDays
import java.time.LocalDate
import kotlin.random.Random

@Composable
fun DatabaseSample(
    content: @Composable DatabaseSampleScope.() -> Unit
) {
    val scope = DatabaseSampleScope()
    scope.content()
}

fun databaseSample(
    content: DatabaseSampleScope.() -> Unit
) {
    val scope = DatabaseSampleScope()
    scope.content()
}

class DatabaseSampleScope {
    val personSample by lazy { getPersonSample() }
    val accountSample by lazy { getAccountSample(personSample) }
    val categorieSample by lazy { getCategoriesSample() }
    val transactionSample by lazy { getTransactionSample(accountSample, categorieSample) }
    val transactionsAndAccountAndCategorySample by lazy {
        getTransactionAndAccountsAndCategorySample(
            transactionSample,
            accountSample,
            categorieSample
        )
    }
    val budgetSample by lazy { getBudgetSample(categorieSample) }
    val accountAndOwnerWithTransactionsSample by lazy {
        getAccountAndOwnerWithTransactionsSample(
            accountSample,
            personSample,
            transactionSample
        )
    }
    val accountAndOwnerSample by lazy {
        getAccountAndOwnerSample(accountSample, personSample)
    }
    val accountAndOwnerWithTransactionsAndPocketsSample by lazy {
        getAccountAndOwnerWithTransactionsAndPocketsSample(
            accountAndOwnerWithTransactionsSample
        )
    }
    val budgetAndCategoryWithTransactionSample by lazy {
        getBudgetAndCategoryWithTransactionsSample(
            budgetSample,
            categorieSample,
            personSample.first(),
            accountAndOwnerWithTransactionsSample
        )
    }
    val currentDateSample by lazy { getCurrentDateSample() }
    val startDateSample by lazy { getStartDateSample() }
    val endDateSample by lazy { getEndDateSample() }
    val budgetAndCategoryWithCalculatedDataSample by lazy {
        getBudgetAndCategoryWithCalculatedData(
            budgetAndCategoryWithTransactionSample,
            currentDateSample,
            startDateSample,
            endDateSample
        )
    }
    val personWithAccountsSample by lazy {
        getPersonWithAccountsSample(
            personSample,
            accountAndOwnerWithTransactionsAndPocketsSample
        )
    }
    val transactionAndAccountsSample: List<TransactionAndAccounts> by lazy {
        TransactionAndAccounts.from(transactionSample, accountSample)
    }
    val personSummaryStateSample: PersonSummaryState by lazy {
        PersonSummaryState.from(
            personWithAccountsSample.first(),
            startDateSample,
            endDateSample,
            personWithAccountsSample,
            transactionAndAccountsSample,
            budgetAndCategoryWithCalculatedDataSample,
            includeBudgetSample
        )
    }
    val includeBudgetSample: Boolean = false
}

private fun getBudgetAndCategoryWithTransactionsSample(
    budget: List<Budget>,
    category: List<Category>,
    person: Person,
    accountAndOwnerWithTransactions: List<AccountAndOwnerWithTransactions>
): List<BudgetAndCategoryWithTransactions> = BudgetAndCategoryWithTransactions.from(
    budget,
    category,
    person,
    accountAndOwnerWithTransactions
)

private fun getBudgetSample(
    categorySample: List<Category>
): List<Budget> {
    val random = Random(3)
    val categorySize = categorySample.size
    val frequencyTypeSize = FrequencyType.values().size
    val startDate = LocalDate.of(2023, 1, 1)
    return (0..20).map {
        val categoryIndex = random.nextInt(categorySize)
        val frequencyTypeOrdinal = random.nextInt(frequencyTypeSize)
        val frequencyType = FrequencyType.values()[frequencyTypeOrdinal]
        val budgetType = BudgetType.values().toList().shuffled(random).first()
        val frequency = random.nextInt(1, 5)
        val value = random.nextDouble(100.0, 500000.0)
        val categoryId = categorySample[categoryIndex].id!!
        when (frequencyType) {
            FrequencyType.DAILY -> Budget.fromDaily(
                id = it,
                categoryId = categoryId,
                value = value,
                frequency = frequency,
                startDate = startDate,
                budgetType = budgetType
            )
            FrequencyType.WEEKLY -> Budget.fromWeekly(
                id = it,
                categoryId = categoryId,
                value = value,
                frequency = frequency,
                startDate = startDate,
                each = WeekDays.from(random.nextInt(until = (0b1111111 + 1))),
                budgetType = budgetType
            )
            FrequencyType.MONTHLY -> Budget.fromMonthly(
                id = it,
                categoryId = categoryId,
                value = value,
                budgetType = budgetType
            )
        }
    }
}

private fun getStartDateSample(): LocalDate = LocalDate.of(2023, 1, 1)
private fun getEndDateSample(): LocalDate = LocalDate.of(2023, 1, 31)
private fun getCurrentDateSample(): LocalDate = LocalDate.of(2023, 1, 14)
private fun getBudgetAndCategoryWithCalculatedData(
    budget: List<BudgetAndCategoryWithTransactions>,
    currentDate: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate
): List<BudgetAndCategoryWithCalculatedData> =
    BudgetAndCategoryWithCalculatedData.from(
        budget,
        currentDate,
        startDate,
        endDate
    )

private fun getAccountSample(personSample: List<Person>): List<Account> {
    var index = 0
    val random = Random(3)
    return personSample
        .flatMap {
            when (it.name) {
                "Pablo" -> Pair(
                    0, (0..100).map {
                        val pIndex = index++
                        val hasParent = random.nextBoolean()
                        val parentId = if (hasParent && pIndex > 0) {
                            random.nextInt(pIndex)
                        } else {
                            null
                        }
                        Triple(pIndex, "Cuenta $pIndex", parentId)
                    }
                )
                "Banco" -> Pair(
                    1, listOf(
                        Triple(index++, "Banco", null)
                    )
                )
                "Petunia" -> Pair(
                    2, listOf(
                        Triple(index++, "Petunia", null)
                    )
                )
                "Hortensia" -> Pair(
                    3, listOf(
                        Triple(index++, "Hortensia", null)
                    )
                )
                "__ESPECIAL__" -> Pair(
                    4, listOf(
                        Triple(index++, "__INGRESOS__", null),
                        Triple(index++, "__GASTOS__", null)
                    )
                )
                else -> null
            }
                .let { pair ->
                    if (pair != null) {
                        val ownerId = pair.first
                        val accounts = pair.second
                        accounts.map { triple ->
                            val isIncome = triple.second == "__INGRESOS__"
                            val isOutcome = triple.second == "__GASTOS__"
                            Account(
                                triple.first,
                                triple.second,
                                ownerId,
                                triple.third,
                                isIncome = isIncome,
                                isOutcome = isOutcome
                            )
                        }
                    } else {
                        listOf()
                    }
                }
        }
}

private fun getAccountAndOwnerWithTransactionsSample(
    accountSample: List<Account>,
    personSample: List<Person>,
    transactionSample: List<Transaction>
): List<AccountAndOwnerWithTransactions> {
    return AccountAndOwnerWithTransactions.from(accountSample, personSample, transactionSample)
}

private fun getAccountAndOwnerSample(accountSample: List<Account>, personSample: List<Person>)
        : List<AccountAndOwner> {
    return AccountAndOwner.from(accountSample, personSample)
}

private fun getAccountAndOwnerWithTransactionsAndPocketsSample(
    accountAndOwnerWithTransactionsSample: List<AccountAndOwnerWithTransactions>
): List<AccountAndOwnerWithTransactionsAndPockets> {
    return AccountAndOwnerWithTransactionsAndPockets.from(
        accountAndOwnerWithTransactionsSample
    )
}

private fun getCategoriesSample(): List<Category> {
    val random = Random(3)
    return (0..20).map {
        val hasParent = random.nextBoolean()
        val parentId = random.nextInt(19)
            .let { parentId ->
                if (parentId < it) {
                    parentId
                } else {
                    parentId + 1
                }
            }
            .takeIf { hasParent }
        Category(it, "Parent category $parentId.$it", parentId)
    }
}

private fun getPersonSample(): List<Person> {
    return listOf(
        "Pablo",
        "Banco",
        "Petunia",
        "Hortensia",
        "__ESPECIAL__"
    ).mapIndexed { index, s ->
        Person(
            index, s, if (index == 0) {
                1
            } else {
                null
            }
        )
    }
}

private fun getPersonWithAccountsSample(
    personSample: List<Person>,
    accountAndOwnerWithTransactionsAndPocketsSample: List<AccountAndOwnerWithTransactionsAndPockets>
): List<PersonWithAccounts> {
    return PersonWithAccounts.from(
        personSample,
        accountAndOwnerWithTransactionsAndPocketsSample
    )
}

private fun getTransactionSample(
    accountsSample: List<Account>,
    categoriesSample: List<Category>
): List<Transaction> {
    val random = java.util.Random(3)
    return (0..100000).map {
        val selectedAccounts = accountsSample.shuffled(random).take(2)
        val sourceAccount = selectedAccounts[0]
        val destinationAccount = selectedAccounts[1]
        val category = random.nextBoolean()
            .let {
                if (it) {
                    categoriesSample.shuffled(random).first()
                } else {
                    null
                }
            }
        Transaction(
            it,
            (random.nextDouble() * (100000 - 1000)) + 1000,
            "Esta es la transaccion $it, desde " +
                    "${sourceAccount.name} hasta ${destinationAccount.name}, y " +
                    "categoría ${category?.name}",
            sourceAccount.id ?: -1,
            destinationAccount.id ?: -1,
            category?.id,
            date = LocalDate.now().withDayOfYear(1).plusDays(random.nextInt(365).toLong()),
            null
        )
    }
}

private fun getTransactionAndAccountsAndCategorySample(
    transactionSample: List<Transaction>,
    accountsSample: List<Account>,
    categoriesSample: List<Category>
): List<TransactionAndAccountsAndCategory> {
    return TransactionAndAccountsAndCategory.from(
        transactionSample, accountsSample, categoriesSample
    )
}