package com.example.gazege.core.entities

data class TransactionListItemDetails(
    val transaction: Transaction,
    val category: Category?,
    val sourceAccount: Account,
    val destinationAccount: Account,
    val transactionType: TransactionType
) {
    fun toTransactionAndAccounts(): TransactionAndAccounts = TransactionAndAccounts(
        transaction, sourceAccount, destinationAccount
    )

    companion object {
        fun from(
            transactions: List<Transaction>,
            categories: List<Category>,
            accounts: List<Account>,
            principalPersonId: Int?
        ): List<TransactionListItemDetails> {
            val categoryMap = categories.associateBy { it.id }
            val accountMap = accounts.associateBy { it.id }
            return transactions.mapNotNull { transaction ->
                val category = categoryMap[transaction.categoryId]
                val sourceAccount = accountMap[transaction.sourceId]
                val destinationAccount = accountMap[transaction.destinationId]
                if (sourceAccount != null && destinationAccount != null) {
                    val sourceAccountIsExternal = sourceAccount.ownerId != principalPersonId
                    val destinationAccountIsExternal =
                        destinationAccount.ownerId != principalPersonId

                    val transactionType =
                        when (Pair(sourceAccountIsExternal, destinationAccountIsExternal)) {
                            Pair(true, true) -> TransactionType.TRANSFER
                            Pair(true, false) -> TransactionType.INCOME
                            Pair(false, true) -> TransactionType.OUTCOME
                            Pair(false, false) -> TransactionType.TRANSFER
                            else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                        }
                    TransactionListItemDetails(
                        transaction = transaction,
                        category = category,
                        sourceAccount = sourceAccount,
                        destinationAccount = destinationAccount,
                        transactionType = transactionType
                    )
                } else {
                    null
                }
            }
        }
    }
}

enum class TransactionType {
    INCOME,
    OUTCOME,
    TRANSFER
}