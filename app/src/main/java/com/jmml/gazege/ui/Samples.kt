package com.jmml.gazege.ui

import androidx.compose.runtime.Composable
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactions
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.BudgetAndCategoryWithTransactions
import com.jmml.gazege.core.entities.BudgetType
import com.jmml.gazege.core.entities.BudgetWithCalculatedData
import com.jmml.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithCalculatedData
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.CategoryWithTransactions
import com.jmml.gazege.core.entities.ExtendedTransaction
import com.jmml.gazege.core.entities.FrequencyType
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PersonWithAccounts
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndDetails
import com.jmml.gazege.core.entities.TransactionAndDetailsAndAccounts
import com.jmml.gazege.core.entities.TransactionAndDetailsAndAccountsAndCategory
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.core.entities.TransactionWithDetails.Companion.toTransactionDetails
import com.jmml.gazege.core.entities.WeekDays
import com.jmml.gazege.ui.navigation.FullPersonSummaryState
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
    categoriesAmount: Int = 20,
    transactionAmount: Int = 100000,
    budgetAmount: Int = 50,
    principalPersonAccountAmount: Int = 100,
    content: DatabaseSampleScope.() -> Unit
) {
    val scope = DatabaseSampleScope(
        categoriesAmount,
        transactionAmount,
        budgetAmount,
        principalPersonAccountAmount
    )
    scope.content()
}

class DatabaseSampleScope(
    val categoriesAmount: Int = 20,
    val transactionAmount: Int = 100000,
    val budgetAmount: Int = 50,
    val principalPersonAccountAmount: Int = 100,
    val promissoryNoteAmount: Int = 20
) {
    val personSample by lazy { getPersonSample() }
    val principalPersonSample by lazy {
        personSample.minByOrNull { it.importance ?: Int.MAX_VALUE }
    }
    val accountSample by lazy { getAccountSample(principalPersonAccountAmount, personSample) }
    val categorieSample by lazy { getCategoriesSample(categoriesAmount) }
    val promissoryNoteSample by lazy { getPromissoryNoteSample(promissoryNoteAmount, personSample) }
    val transactionSample by lazy {
        getTransactionSample(
            accountSample,
            categorieSample,
            transactionAmount
        )
    }
    val transactionDetailsSample by lazy {
        getTransactionDetailsSample(
            accountSample,
            categorieSample,
            transactionAmount
        )
    }
    val transactionsAndAccountAndCategorySample by lazy {
        getTransactionAndAccountsAndCategorySample(
            transactionSample,
            transactionDetailsSample,
            accountSample,
            categorieSample
        )
    }
    val categoryWithSubcategoriesSample by lazy { getCategoryWithSubcategoriesSample(categorieSample) }
    val budgetSample by lazy { getBudgetSample(categorieSample, budgetAmount) }
    val accountAndOwnerWithTransactionsSample by lazy {
        getAccountAndOwnerWithTransactionsSample(
            accountSample,
            personSample,
            transactionSample,
            transactionDetailsSample
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
    val budgetWithCalculatedDataSample by lazy {
        getBudgetWithCalculatedData(
            budgetAndCategoryWithTransactionSample,
            currentDateSample,
            startDateSample,
            endDateSample
        )
    }
    val budgetAndCategoryWithCalculatedDataSample by lazy {
        getBudgetAndCategoryWithCalculatedData(budgetWithCalculatedDataSample, categorieSample)
    }
    val personWithAccountsSample by lazy {
        getPersonWithAccountsSample(
            personSample,
            accountAndOwnerWithTransactionsAndPocketsSample
        )
    }
    val transactionAndDetailsAndAccountsSamples: List<TransactionAndDetailsAndAccounts> by lazy {
        TransactionAndDetailsAndAccounts.from(
            transactionSample,
            transactionDetailsSample,
            accountSample
        )
    }
    val personSummaryStateSample: FullPersonSummaryState by lazy {
        FullPersonSummaryState.from(
            personWithAccountsSample.first(),
            startDateSample,
            endDateSample,
            personWithAccountsSample,
            transactionAndDetailsAndAccountsSamples,
            promissoryNoteSample,
            categoryWithSubcategoriesAndBudgetWithCalculatedDataSample,
            includeBudgetSample,
            includeDebtsSample
        )
    }
    val includeBudgetSample: Boolean = false
    val includeDebtsSample: Boolean = false
    val transactionListItemDetailsSample by lazy {
        getTransactionListItemDetailsSample(
            transactionSample,
            transactionDetailsSample,
            categorieSample,
            accountSample,
            principalPerson = personSample[0]
        )
    }
    val budgetWithCalculatedDataAndCategorySample by lazy {
        getBudgetAndCategoryWithCalculatedData(budgetWithCalculatedDataSample, categorieSample)
    }

    val categoryWithTransactionsSample by lazy {
        getCategoryWithTransactionsSample(
            categorieSample,
            personSample[0],
            accountSample,
            emptyList()
        )
    }

    val categoryWithCalculatedDataSample by lazy {
        getCategoryWithCalculatedDataSample(
            categoryWithTransactionsSample,
            currentDateSample,
            startDateSample,
            endDateSample
        )
    }

    val categoryWithSubcategoriesAndBudgetWithCalculatedDataSample by lazy {
        getCategoryWithSubcategoriesAndBudgetWithCalculatedDataSample(
            budgetWithCalculatedDataAndCategorySample,
            categoryWithSubcategoriesSample,
            categoryWithCalculatedDataSample.associateBy { it.category.id ?: 0 }
        )
    }
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

private fun getCategoryWithTransactionsSample(
    category: List<Category>,
    person: Person,
    accounts: List<Account>,
    extendedTransactions: List<ExtendedTransaction>
) = CategoryWithTransactions.from(
    category = category,
    person = person,
    accounts = accounts,
    extendedTransactions = extendedTransactions
)

private fun getBudgetSample(
    categorySample: List<Category>,
    budgetAmount: Int
): List<Budget> {
    val random = Random(3)
    val categorySize = categorySample.size
    val frequencyTypeSize = FrequencyType.values().size
    val startDate = LocalDate.of(2023, 1, 1)
    return (0..budgetAmount).map {
        val categoryIndex = random.nextInt(categorySize)
        val frequencyTypeOrdinal = random.nextInt(frequencyTypeSize)
        val frequencyType = FrequencyType.values()[frequencyTypeOrdinal]
        val frequency = random.nextInt(1, 5)
        val value = random.nextDouble(100.0, 500000.0)
        val categoryId = categorySample[categoryIndex].id!!
        when (frequencyType) {
            FrequencyType.DAILY -> Budget.fromDaily(
                id = it,
                categoryId = categoryId,
                value = value,
                frequency = frequency,
                startDate = startDate
            )
            FrequencyType.WEEKLY -> Budget.fromWeekly(
                id = it,
                categoryId = categoryId,
                value = value,
                frequency = frequency,
                startDate = startDate,
                each = WeekDays.from(random.nextInt(until = (0b1111111 + 1)))
            )
            FrequencyType.MONTHLY -> Budget.fromMonthly(
                id = it,
                categoryId = categoryId,
                value = value
            )
        }
    }
}

private fun getStartDateSample(): LocalDate = LocalDate.of(2023, 1, 1)
private fun getEndDateSample(): LocalDate = LocalDate.of(2023, 1, 31)
private fun getCurrentDateSample(): LocalDate = LocalDate.of(2023, 1, 14)

private fun getBudgetWithCalculatedData(
    budget: List<BudgetAndCategoryWithTransactions>,
    currentDate: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate
) = BudgetWithCalculatedData.from(budget, currentDate, startDate, endDate)

private fun getBudgetAndCategoryWithCalculatedData(
    budget: List<BudgetWithCalculatedData>,
    categories: List<Category>
): List<BudgetWithCalculatedDataAndCategory> =
    BudgetWithCalculatedDataAndCategory.from(budget, categories)

private fun getCategoryWithCalculatedDataSample(
    categoryWithTransactions: List<CategoryWithTransactions>,
    currentDate: LocalDate,
    startDate: LocalDate,
    endDate: LocalDate
) = CategoryWithCalculatedData.from(
    categoryWithTransactions = categoryWithTransactions,
    currentDate = currentDate,
    startDate = startDate,
    endDate = endDate
)

private fun getAccountSample(
    principalPersonAccountAmount: Int,
    personSample: List<Person>
): List<Account> {
    var index = 0
    val random = Random(3)
    return personSample
        .flatMap {
            when (it.name) {
                "Pablo" -> Pair(
                    0, (0..principalPersonAccountAmount).map {
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
    transactionSample: List<Transaction>,
    transactionDetailsSample: List<TransactionDetails>
): List<AccountAndOwnerWithTransactions> {
    val transactionWithDetails = TransactionWithDetails.from(
        transactionSample,
        transactionDetailsSample
    )
    return AccountAndOwnerWithTransactions.from(accountSample, personSample, transactionWithDetails)
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

private fun getCategoriesSample(amount: Int): List<Category> {
    val random = Random(3)
    return (0..amount).map {
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
        Category(it, "Parent category $parentId.$it", BudgetType.FIXED, parentId)
    }
}

private fun getCategoryWithSubcategoriesSample(categories: List<Category>):
        List<CategoryWithSubCategories> = CategoryWithSubCategories.from(categories)

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

private fun getTransactionAndDetailsSample(
    accountsSample: List<Account>,
    categoriesSample: List<Category>,
    transactionAmount: Int
): List<TransactionWithDetails> {
    val random = java.util.Random(3)
    return (0..transactionAmount).map {
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
        TransactionWithDetails(
            transaction = Transaction(
                id = it,
                sourceId = sourceAccount.id ?: -1,
                destinationId = destinationAccount.id ?: -1,
                date = LocalDate.now().withDayOfYear(1).plusDays(random.nextInt(365).toLong())
            ),
            transactionDetails = listOf(
                TransactionDetails(
                    id = it,
                    transactionId = it,
                    amount = (random.nextDouble() * (100000 - 1000)) + 1000,
                    description = "Esta es la transaccion $it, desde " +
                            "${sourceAccount.name} hasta ${destinationAccount.name}, y " +
                            "categoría ${category?.name}",
                    categoryId = category?.id,
                    aNombreDe = null,
                    budgetDate = null
                )
            )
        )
    }
}

private fun getTransactionSample(
    accountsSample: List<Account>,
    categoriesSample: List<Category>,
    transactionAmount: Int
): List<Transaction> {
    val transactionAndDetails = getTransactionAndDetailsSample(
        accountsSample = accountsSample,
        categoriesSample = categoriesSample,
        transactionAmount = transactionAmount
    )
    return transactionAndDetails.map {
        it.toTransaction()
    }
}

private fun getTransactionDetailsSample(
    accountsSample: List<Account>,
    categoriesSample: List<Category>,
    transactionAmount: Int
): List<TransactionDetails> {
    val transactionAndDetails = getTransactionAndDetailsSample(
        accountsSample = accountsSample,
        categoriesSample = categoriesSample,
        transactionAmount = transactionAmount
    )
    return transactionAndDetails.flatMap { it.toTransactionDetails() }
}

private fun getPromissoryNoteSample(
    promissoryNoteAmount: Int,
    personSample: List<Person>
): List<PromissoryNote>{
    val random = java.util.Random(3)
    return (0..promissoryNoteAmount).map {
        val selectedPersons = personSample.shuffled(random).take(2)
        val sourcePerson = selectedPersons[0]
        val destinationPerson = selectedPersons[1]
        PromissoryNote(
            id = it,
            amount = random.nextDouble() * (100000 - 1000) + 1000,
            date = LocalDate.now().withDayOfYear(1).plusDays(random.nextInt(365).toLong()),
            sourceId = sourcePerson.id ?: -1,
            destinationId = destinationPerson.id ?: -1,
            description = "Promissory note from ${sourcePerson} to ${destinationPerson}"
        )
    }
}

private fun getTransactionAndAccountsAndCategorySample(
    transactionSample: List<Transaction>,
    transactionDetailsSample: List<TransactionDetails>,
    accountsSample: List<Account>,
    categoriesSample: List<Category>
): List<TransactionAndDetailsAndAccountsAndCategory> {
    val transactionWithDetails = TransactionAndDetails.from(
        transactionSample,
        transactionDetailsSample
    )
    return TransactionAndDetailsAndAccountsAndCategory.from(
        transactionWithDetails, accountsSample, categoriesSample
    )
}

private fun getTransactionListItemDetailsSample(
    transactions: List<Transaction>,
    transactionDetails: List<TransactionDetails>,
    categories: List<Category>,
    accounts: List<Account>,
    principalPerson: Person?
): List<TransactionListItemDetails> = TransactionListItemDetails.from(
    TransactionWithDetails.from(
        transactions,
        transactionDetails
    ),
    categories,
    accounts,
    principalPerson?.id
)

private fun getCategoryWithSubcategoriesAndBudgetWithCalculatedDataSample(
    budgetWithCalculatedDataAndCategory: List<BudgetWithCalculatedDataAndCategory>,
    categoriesWithSubcategories: List<CategoryWithSubCategories>,
    categoriesWithCalculatedData: Map<Int, CategoryWithCalculatedData>
) =
    CategoryWithSubcategoriesAndBudgetWithCalculatedData.from(
        budgetWithCalculatedDataAndCategory,
        categoriesWithSubcategories,
        categoriesWithCalculatedData
    )