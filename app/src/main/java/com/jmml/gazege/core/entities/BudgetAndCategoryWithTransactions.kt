package com.jmml.gazege.core.entities

data class BudgetAndCategoryWithTransactions(
    val budget: Budget,
    val category: Category,
    val inTransactions: List<TransactionAndDetails>,
    val outTransactions: List<TransactionAndDetails>,
    val person: Person
) {
    val categoryName get() = category.name

    val budgetId get() = budget.id
    val budgetStartDate get() = budget.startDate
    val budgetEach get() = budget.each
    val budgetFrequency get() = budget.frequency
    val budgetFrequencyType get() = budget.frequencyType
    val budgetValue get() = budget.value
    val budgetEachClass get() = budget.eachClass
    val budgetDescription get() = budget.description

    companion object {
        fun from(
            budget: List<Budget>,
            category: List<Category>,
            person: Person,
            accountAndOwnerWithTransactions: List<AccountAndOwnerWithTransactions>
        ): List<BudgetAndCategoryWithTransactions> {
            val ownAccountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                .filter { it.account.ownerId == person.id }
            val indexedInTransactions = ownAccountAndOwnerWithTransactions
                .flatMap { it.inTransactions }
                .flatMap { transactionAndDetails ->
                    transactionAndDetails.transactionDetails.map {
                        TransactionAndDetails(
                            transaction = transactionAndDetails.transaction,
                            transactionDetails = it
                        )
                    }
                }
                .groupBy { it.categoryId }
            val indexedOutTransactions = ownAccountAndOwnerWithTransactions
                .flatMap { it.outTransactions }
                .flatMap { transactionAndDetails ->
                    transactionAndDetails.transactionDetails.map {
                        TransactionAndDetails(
                            transaction = transactionAndDetails.transaction,
                            transactionDetails = it
                        )
                    }
                }
                .groupBy { it.categoryId }
            val categoryWithTransactions = category
                .map {
                    Triple(
                        it,
                        indexedInTransactions[it.id] ?: emptyList(),
                        indexedOutTransactions[it.id] ?: emptyList()
                    )
                }
                .associateBy {
                    it.first.id
                }
            return budget
                .mapNotNull {
                    val selectedCategoryWithTransactions = categoryWithTransactions[it.categoryId]
                    selectedCategoryWithTransactions?.first?.let { category ->
                        BudgetAndCategoryWithTransactions(
                            it,
                            category,
                            selectedCategoryWithTransactions.second,
                            selectedCategoryWithTransactions.third,
                            person
                        )
                    }
                }
        }
    }
}