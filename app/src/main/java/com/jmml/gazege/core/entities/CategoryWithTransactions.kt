package com.jmml.gazege.core.entities

data class CategoryWithTransactions(
    val category: Category,
    val inTransactions: List<ExtendedTransaction>,
    val outTransactions: List<ExtendedTransaction>,
    val person: Person
) {
    val categoryId get() = category.id

    companion object {
        fun from(
            category: List<Category>,
            person: Person,
            accounts: List<Account>,
            extendedTransactions: List<ExtendedTransaction>
        ): List<CategoryWithTransactions> {
            val ownerId = person.id
            val ownAccountIds = accounts
                .filter { it.ownerId == ownerId }
                .map { it.id }
                .toSet()

            val classifiedTransactions = extendedTransactions
                .map {
                    val sourceAccountIsOwned = ownAccountIds.contains(it.sourceId)
                    val destinationAccountIsOwned = ownAccountIds.contains(it.destinationId)

                    val classification =
                        if (sourceAccountIsOwned && destinationAccountIsOwned) {
                            0
                        } else if (!sourceAccountIsOwned && !destinationAccountIsOwned) {
                            0
                        } else if (sourceAccountIsOwned) {
                            -1
                        } else {
                            1
                        }
                    classification to it
                }
                .groupBy { it.first }
                .mapValues { it.value.map { it.second } }
            val inTransactions = (classifiedTransactions[1] ?: emptyList())
                .groupBy { it.categoryId }
            val outTransactions = (classifiedTransactions[-1] ?: emptyList())
                .groupBy { it.categoryId }
            return category.map {
                CategoryWithTransactions(
                    category = it,
                    inTransactions = inTransactions[it.id] ?: emptyList(),
                    outTransactions = outTransactions[it.id] ?: emptyList(),
                    person = person
                )
            }
        }
    }
}