package com.jmml.gazege.core.entities

data class TransactionWithDetailsAndAccountsAndCategory(
    val transaction: TransactionWithDetails,
    val sourceAccount: Account,
    val destinationAccount: Account,
    val category: List<Category>
) {
    fun toTransactionWithDetailsAndAccounts(): TransactionWithDetailsAndAccounts =
        TransactionWithDetailsAndAccounts(transaction, sourceAccount, destinationAccount)

    fun toTransactionAndDetailsAndAccountsAndCategory(): List<TransactionAndDetailsAndAccountsAndCategory> =
        transaction.transactionDetails.map { transactionDetails ->
            TransactionAndDetailsAndAccountsAndCategory(
                TransactionAndDetails(
                    transaction.transaction,
                    transactionDetails
                ),
                sourceAccount,
                destinationAccount,
                category.first { it.id == transactionDetails.categoryId }
            )
        }

    companion object {
        fun List<TransactionWithDetailsAndAccountsAndCategory>.toTransactionAndDetailsAndAccountsAndCategory() {
            TODO()
        }

        fun from(
            transactions: List<TransactionWithDetails>,
            accounts: List<Account>,
            categories: List<Category>
        ): List<TransactionWithDetailsAndAccountsAndCategory> {
            val accountMap: Map<Int?, Account> = accounts.associateBy { it.id }
            val categoriesMap: Map<Int?, Category> = categories.associateBy { it.id }
            return transactions.mapNotNull { transaction: TransactionWithDetails ->
                val sourceAccount = accountMap[transaction.sourceId]
                val destinationAccount = accountMap[transaction.destinationId]
                if (sourceAccount != null && destinationAccount != null) {
                    TransactionWithDetailsAndAccountsAndCategory(
                        transaction,
                        sourceAccount = sourceAccount,
                        destinationAccount = destinationAccount,
                        category = transaction.transactionDetails.mapNotNull {
                            categoriesMap[it.categoryId]
                        }
                    )
                } else {
                    null
                }
            }
        }
    }
}