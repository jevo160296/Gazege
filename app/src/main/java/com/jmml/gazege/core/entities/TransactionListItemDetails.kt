package com.jmml.gazege.core.entities

interface ITransactionListDetail {
    val transaction: Transaction
    val category: Category?
    val sourceAccount: Account
    val destinationAccount: Account
    val transactionType: TransactionType
}

data class TransactionListItemDetails(
    override val transaction: Transaction,
    override val category: Category?,
    override val sourceAccount: Account,
    override val destinationAccount: Account,
    override val transactionType: TransactionType
) : ITransactionListDetail {
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

data class TransactionListItemDetailsWithAccount(
    override val transaction: Transaction,
    override val category: Category?,
    override val sourceAccount: Account,
    override val destinationAccount: Account,
    override val transactionType: TransactionType,
    val transactionAccountType: TransactionType
) : ITransactionListDetail {
    companion object {
        fun from(
            transactions: List<Transaction>,
            categories: List<Category>,
            accounts: List<Account>,
            principalPersonId: Int?,
            includedAccountsId: Set<Int>
        ): List<TransactionListItemDetailsWithAccount> {
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
                    val sourceAccountNotInChildren: Boolean =
                        sourceAccount.id !in includedAccountsId
                    val destinationAccountNotInChildren: Boolean =
                        destinationAccount.id !in includedAccountsId

                    val transactionType =
                        when (Pair(sourceAccountIsExternal, destinationAccountIsExternal)) {
                            Pair(true, true) -> TransactionType.TRANSFER
                            Pair(true, false) -> TransactionType.INCOME
                            Pair(false, true) -> TransactionType.OUTCOME
                            Pair(false, false) -> TransactionType.TRANSFER
                            else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                        }
                    val transactionAccountType =
                        when (sourceAccountNotInChildren to destinationAccountNotInChildren) {
                            true to true -> TransactionType.TRANSFER
                            true to false -> TransactionType.INCOME
                            false to true -> TransactionType.OUTCOME
                            false to false -> TransactionType.TRANSFER
                            else -> TODO("Unkown state ${sourceAccount.isIncome}, ${destinationAccount.isOutcome}")
                        }
                    TransactionListItemDetailsWithAccount(
                        transaction = transaction,
                        category = category,
                        sourceAccount = sourceAccount,
                        destinationAccount = destinationAccount,
                        transactionType = transactionType,
                        transactionAccountType = transactionAccountType
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