package com.example.gazege.core.dsl

import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetAndCategoryWithTransactions
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.BudgetWithCalculatedData
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.WeekDays
import java.time.LocalDate

class BudgetWithCalculatedDataScope {
    private val budgets: MutableList<Budget> = mutableListOf()
    private var budgetIndex = 0
    private var transIndex = 0
    private var accountIndex = 0
    private var categoryIndex = 0
    private val categories: MutableSet<Category> = mutableSetOf()
    private val owner = Person(0, "OWNER", 1)
    private val other = Person(1, "OTHER", null)
    private val persons = mutableSetOf(owner, other)
    private val incomeAccount = "INGRESO"
    private val outcomeAccount = "GASTO"
    private val accounts: MutableSet<Account> = mutableSetOf()
    private val transactions = mutableListOf<Transaction>()

    private fun onTransactionAdd(transaction: Transaction) {
        transactions.add(transaction)
    }

    inner class CategoryScope(private val category: Category) {
        fun andBudgetDaily(
            value: Double,
            frequency: Int,
            startDate: LocalDate,
            budgetType: BudgetType
        ): BudgetScope = Budget.fromDaily(
            budgetIndex++,
            category.id ?: -1,
            value,
            frequency,
            startDate,
            budgetType
        )
            .run {
                budgets.add(this)
                BudgetScope(category)
            }

        fun andBudgetMonthly(
            value: Double,
            budgetType: BudgetType
        ) = Budget.fromMonthly(
            budgetIndex++,
            category.id ?: -1,
            value,
            budgetType
        ).run {
            budgets.add(this)
            BudgetScope(category)
        }

        fun andBudgetWeekly(
            value: Double,
            each: WeekDays,
            frequency: Int,
            startDate: LocalDate,
            budgetType: BudgetType
        ) = Budget.fromWeekly(
            budgetIndex++,
            category.id ?: -1,
            value,
            each,
            frequency,
            startDate,
            budgetType
        )
            .run {
                budgets.add(this)
                BudgetScope(category)
            }
    }

    inner class BudgetScope(private val category: Category) {
        fun andSourceAccount(account: String): SourceAccountScope =
            SourceAccountScope(category, getAccountByName(account))

        fun andDestinationAccount(account: String): DestinationAccountScope =
            DestinationAccountScope(category, getAccountByName(account))
    }

    open inner class AccountScope(
        protected val category: Category,
        protected val account: Account
    )

    inner class SourceAccountScope(category: Category, account: Account) :
        AccountScope(category, account) {
        fun andDestinationAccount(account: String): SourceDestinationAccountScope =
            SourceDestinationAccountScope(category, this.account, getAccountByName(account))

        fun addExpense(amount: Double, description: String, date: LocalDate) =
            andDestinationAccount(outcomeAccount).addTransaction(amount, description, date)
                .let { this }

        fun finish(): BudgetWithCalculatedDataScope = this@BudgetWithCalculatedDataScope
    }

    inner class DestinationAccountScope(category: Category, account: Account) :
        AccountScope(category, account) {
        fun andSourceAccount(account: String): SourceDestinationAccountScope =
            SourceDestinationAccountScope(category, getAccountByName(account), this.account)

        fun addIncome(amount: Double, description: String, date: LocalDate) =
            andSourceAccount(incomeAccount).addTransaction(amount, description, date).let { this }

        fun finish(): BudgetWithCalculatedDataScope = this@BudgetWithCalculatedDataScope
    }

    inner class SourceDestinationAccountScope(
        private val category: Category,
        private val sourceAccount: Account,
        private val destinationAccount: Account
    ) {
        fun addTransaction(
            amount: Double,
            description: String,
            date: LocalDate
        ): SourceDestinationAccountScope = if (amount >= 0) {
            Transaction(
                transIndex++,
                amount,
                description,
                sourceAccount.id ?: -1,
                destinationAccount.id ?: -1,
                category.id,
                date,
                null
            )
        } else {
            Transaction(
                transIndex++,
                amount,
                description,
                destinationAccount.id ?: -1,
                sourceAccount.id ?: -1,
                category.id,
                date,
                null
            )
        }
            .run {
                onTransactionAdd(this)
                this@SourceDestinationAccountScope
            }

        fun finish(): BudgetWithCalculatedDataScope = this@BudgetWithCalculatedDataScope
    }

    fun withCategory(categoryName: String, parent: String?): CategoryScope {
        val parentAccount = parent?.let { getCategoryByName(parent) }
        val parentId = parentAccount?.id
        val category = Category(categoryIndex++, categoryName, BudgetType.FIXED, parentId)
        categories.add(category)
        return CategoryScope(category)
    }

    fun build(
        currentDate: LocalDate,
        startDate: LocalDate,
        today: LocalDate,
        endDate: LocalDate
    ): List<BudgetWithCalculatedData> {
        val accountAndOwnerWithTransactions: List<AccountAndOwnerWithTransactions> =
            AccountAndOwnerWithTransactions.from(
                accounts.toList(), persons.toList(), transactions
            )
        val budgetAndCategoryWithTransactions: List<BudgetAndCategoryWithTransactions> =
            BudgetAndCategoryWithTransactions.from(
                budgets, categories.toList(), owner, accountAndOwnerWithTransactions
            )
        return BudgetWithCalculatedData.from(
            budgetAndCategoryWithTransactions,
            currentDate,
            startDate,
            endDate
        )
    }

    private fun getCategoryByName(name: String): Category = categories
        .firstOrNull { it.name == name }
        .run {
            this
                ?: Category(categoryIndex++, name, BudgetType.FIXED, null)
        }

    private fun getAccountByName(name: String): Account = accounts
        .firstOrNull { it.name == name }
        .run {
            this
                ?: Account(
                    accountIndex++,
                    name,
                    (if (name == incomeAccount || name == outcomeAccount) other else owner).id
                        ?: -1,
                    null
                )
                    .run {
                        accounts.add(this)
                        this
                    }
        }
}

fun budgetWithCalculatedDataDSL(
    currentDate: LocalDate,
    startDate: LocalDate,
    today: LocalDate,
    endDate: LocalDate,
    builder: BudgetWithCalculatedDataScope.() -> BudgetWithCalculatedDataScope
) = BudgetWithCalculatedDataScope()
    .run {
        builder()
        build(currentDate, startDate, today, endDate)
    }