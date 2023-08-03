package com.example.gazege.core.entities

data class CategoryWithTransactions(
    val category: Category,
    val inTransactions: List<Transaction>,
    val outTransactions: List<Transaction>,
    val person: Person
) {
    val categoryId get() = category.id

    companion object {
        fun from(
            category: List<Category>,
            person: Person,
            accountAndOwnerWithTransactions: List<AccountAndOwnerWithTransactions>
        ): List<CategoryWithTransactions> {
            val ownAccountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                .filter { it.account.ownerId == person.id }
            val indexedInTransactions = ownAccountAndOwnerWithTransactions
                .flatMap { it.inTransactions }
                .groupBy { it.categoryId }
            val indexedOutTransactions = ownAccountAndOwnerWithTransactions
                .flatMap { it.outTransactions }
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
            return category
                .mapNotNull {
                    val selectedCategoryWithTransactions = categoryWithTransactions[it.id]
                    selectedCategoryWithTransactions?.first?.let { category ->
                        CategoryWithTransactions(
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