package com.example.gazege

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.*
import com.example.gazege.core.AppRepository
import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.*
import com.example.gazege.ui.Settings
import com.example.gazege.ui.views.account.AccountDetailData
import kotlinx.coroutines.*
import java.time.LocalDate

enum class NavPosition {
    PERSONS, CUENTAS, TRANSACCIONES
}

fun CoroutineScope.safeLaunch(
    onErrorAction: (Throwable) -> Unit,
    launchBody: suspend () -> Unit
): Job {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onErrorAction(throwable)
    }
    return this.launch(coroutineExceptionHandler) {
        launchBody.invoke()
    }
}

class MainViewModel(private val repository: AppRepository, private val settings: Settings) :
    ViewModel() {
    fun appInitialized(): Boolean {
        val currentValue = appInitialized
        appInitialized = true
        return currentValue
    }

    @Composable
    fun rememberAllPerson() = allPerson.observeAsState(emptyList())

    @Composable
    fun rememberAllAccount() = allAccount.observeAsState(emptyList())

    @Composable
    fun rememberAllTransactions() = allTransactions.observeAsState(emptyList())

    @Composable
    fun rememberSettingsIncluirPresupuestoEnSaldoActualFlow() =
        incluirPresupuestoEnSaldoActual.observeAsState(false)

    @Composable
    fun rememberSettingsIncluirDeudasEnSaldoActualFlow() =
        incluirDeudasEnSaldoActual.observeAsState(false)

    @Composable
    fun rememberPersonSummaryState() = personSummaryState.observeAsState(null)

    @Composable
    fun rememberCategories() = categories.observeAsState(emptyList())

    @Composable
    fun rememberBudget() = budget.observeAsState(emptyList())

    @Composable
    fun rememberBudgetAndCategoryWithTransactions() =
        budgetAndCategoryWithTransactions.observeAsState(emptyList())

    @Composable
    fun rememberBudgetAndCategoryWithCalculatedData() =
        budgetWithCalculatedDataAndCategory.observeAsState(emptyList())

    @Composable
    fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
        categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactions() =
        accountAndOwnerWithTransactions.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwner() = accountAndOwner.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerUserFirst() =
        accountAndOwnerUserFirst.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactionsAndPockets() =
        accountAndOwnerWithTransactionsAndPockets.observeAsState(emptyList())

    @Composable
    fun rememberRange() = range.observeAsState(Pair(LocalDate.now(), LocalDate.now()))

    @Composable
    fun rememberPersonFilterValue() = personFilterValue.observeAsState(false)

    @Composable
    fun rememberIncomeFilterValue() = incomeFilterValue.observeAsState(true)

    @Composable
    fun rememberOutcomeFilterValue() = outcomeFilterValue.observeAsState(true)

    @Composable
    fun rememberTransferFilterValue() = transferFilterValue.observeAsState(true)

    @Composable
    fun rememberPrincipalPerson() = principalPerson.observeAsState()

    @Composable
    fun rememberIncomeAccount() = incomeAccount.observeAsState()

    @Composable
    fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

    @Composable
    fun rememberAccountDetailData(
        accountId: Int?,
        incomeFilterValue: Boolean,
        outcomeFilterValue: Boolean,
        transferFilterValue: Boolean
    ): State<AccountDetailData?> {
        updateAccountDetailIdIfDifferent(
            accountId,
            incomeFilterValue,
            outcomeFilterValue,
            transferFilterValue
        )
        return accountDetailData.observeAsState()
    }

    @Composable
    fun rememberFilteredTransactionListItemDetails() =
        filteredTransactionListitemDetails.observeAsState(emptyList())

    private inline fun <reified A, reified B, reified X> LiveData<A>.combine(
        otherSource: LiveData<B>,
        crossinline merger: suspend (A, B) -> X
    ) = MediatorLiveData<X>()
        .apply {
            val update = {
                if (this@combine.isInitialized && otherSource.isInitialized) {
                    if (this@combine.value is A && otherSource.value is B) {
                        val a = this@combine.value as A
                        val b = otherSource.value as B
                        viewModelScope.launch {
                            withContext(Dispatchers.Default) {
                                postValue(merger(a, b))
                            }
                        }
                    }
                }
            }
            addSource(this@combine) { update() }
            addSource(otherSource) { update() }
        }
        .distinctUntilChanged()

    private var appInitialized = false
    private val incluirPresupuestoEnSaldoActual =
        settings.getIncluirPresupuestoEnSaldoActualFlow().asLiveData()
    private val incluirDeudasEnSaldoActual =
        settings.getIncluirDeudasEnSaldoActualFlow().asLiveData()
    private val allPerson = repository.getPersons().asLiveData()
    private val allAccount = repository.getAccounts().asLiveData()
    private val allTransactions = repository.getTransactions(null, null).asLiveData()
    private val categories = repository.getCategories().asLiveData()
    private val budget = repository.getBudgets().asLiveData()
    private val principalPerson = allPerson.map { persons -> getPrincipalPerson(persons) }

    private val accountAndOwner: LiveData<List<AccountAndOwner>> =
        allAccount.combine(allPerson) { allAccount, allPerson ->
            AccountAndOwner.from(
                allAccount,
                allPerson
            )
        }

    private val accountAndOwnerWithTransactions: LiveData<List<AccountAndOwnerWithTransactions>> =
        allAccount
            .combine(allPerson) { allAccount, allPerson ->
                object {
                    val allAccount = allAccount
                    val allPerson = allPerson
                }
            }
            .combine(allTransactions) { combined, allTransactions ->
                combined.run {
                    AccountAndOwnerWithTransactions.from(
                        allAccount,
                        allPerson,
                        allTransactions
                    )
                }
            }

    private val accountAndOwnerUserFirst: LiveData<List<AccountAndOwner>> =
        accountAndOwner.map { it.sortedByDescending { acc -> acc.owner.importance } }
    private val accountAndOwnerWithTransactionsAndPockets: LiveData<List<AccountAndOwnerWithTransactionsAndPockets>> =
        accountAndOwnerWithTransactions.map { lista ->
            lista.map { item ->
                AccountAndOwnerWithTransactionsAndPockets.from(
                    item,
                    lista
                )
            }
        }
    private val personWithAccounts: LiveData<List<PersonWithAccounts>> =
        allPerson.combine(accountAndOwnerWithTransactionsAndPockets) { allPerson, accountAndOwnerWithTransactionsAndPockets ->
            PersonWithAccounts.from(allPerson, accountAndOwnerWithTransactionsAndPockets)
        }
    private val categoriesWithSubCategories: LiveData<List<CategoryWithSubCategories>> = categories
        .map { CategoryWithSubCategories.from(it) }

    private val budgetAndCategoryWithTransactions: LiveData<List<BudgetAndCategoryWithTransactions>> =
        budget
            .combine(categories) { budget, categories ->
                object {
                    val budget = budget
                    val categories = categories
                }
            }
            .combine(accountAndOwnerWithTransactions) { combined, accountAndOwnerWithTransactions ->
                object {
                    val budget = combined.budget
                    val categories = combined.categories
                    val accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                }
            }
            .combine(principalPerson) { combined, principalPerson ->
                combined.run {
                    if (principalPerson == null) {
                        emptyList()
                    } else {
                        BudgetAndCategoryWithTransactions.from(
                            budget = budget,
                            category = categories,
                            person = principalPerson,
                            accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                        )
                    }
                }
            }

    private val initialRange = LocalDate.now().withDayOfMonth(1).let {
        Pair(it, it.plusMonths(1L).minusDays(1L))
    }

    private val range: MutableLiveData<Pair<LocalDate?, LocalDate?>> = MutableLiveData(initialRange)

    private val budgetWithCalculatedData: LiveData<List<BudgetWithCalculatedData>> =
        budgetAndCategoryWithTransactions.combine(range) { budgetAndCategoryWithTransactions, range ->
            val startDate = range.first
            val endDate = range.second
            if (startDate != null && endDate != null) {
                BudgetWithCalculatedData.from(
                    budgetAndCategoryWithTransactions,
                    LocalDate.now(),
                    startDate,
                    endDate
                )
            } else {
                emptyList()
            }
        }

    private val budgetWithCalculatedDataAndCategory: LiveData<List<BudgetWithCalculatedDataAndCategory>> =
        budgetWithCalculatedData.combine(categories) { budgetWithCalculatedData, categories ->
            BudgetWithCalculatedDataAndCategory.from(budgetWithCalculatedData, categories)
        }

    private val categoryWithSubcategoriesAndBudgetWithCalculatedData: LiveData<List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>> =
        budgetWithCalculatedDataAndCategory.combine(categoriesWithSubCategories) { budgetWithCalculatedDataAndCategory, categoriesWithSubCategories ->
            CategoryWithSubcategoriesAndBudgetWithCalculatedData.from(
                budgetWithCalculatedDataAndCategory,
                categoriesWithSubCategories
            )
        }

    private val personFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    private val incomeFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    private val outcomeFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    private val transferFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    fun updatePersonFilterValue(newValue: Boolean) {
        personFilterValue.value = newValue
    }

    fun updateIncomeFilterValue(newValue: Boolean) {
        incomeFilterValue.value = newValue
    }

    fun updateOutcomeFilterValue(newValue: Boolean) {
        outcomeFilterValue.value = newValue
    }

    fun updateTransferFilterValue(newValue: Boolean) {
        transferFilterValue.value = newValue
    }

    private val incomeAccount = allAccount.map { accounts -> getIncomeAccount(accounts) }
    private val outcomeAccount = allAccount.map { accounts -> getOutcomeAccount(accounts) }

    private val rangeTransactions = range.switchMap { range ->
        repository.getTransactions(range?.first, range?.second).asLiveData()
    }
    private val accountDetailId = MutableLiveData<Int?>(null)
    private val accountDetail = accountAndOwnerWithTransactionsAndPockets
        .combine(accountDetailId) { accountAndOwnerWithTransactionsAndPockets, accountDetailId ->
            accountAndOwnerWithTransactionsAndPockets.firstOrNull {
                it.accountAndOwnerWithTransactions.account.id == accountDetailId
            }
        }
    private val accountIncomeFilterValue = MutableLiveData(true)
    private val accountOutcomeFilterValue = MutableLiveData(true)
    private val accountTransferFilterValue = MutableLiveData(true)
    private val accountDetailData: LiveData<AccountDetailData?> = accountDetail
        .combine(allAccount) { accountDetail, allAccount ->
            object {
                val accountDetail = accountDetail
                val allAccount = allAccount
            }
        }
        .combine(categories) { combined, categories ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = categories
            }
        }
        .combine(budget) { combined, budget ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = combined.categories
                val budget = budget
            }
        }
        .combine(range) { combined, range ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = combined.categories
                val budget = combined.budget
                val range = range
            }
        }
        .combine(principalPerson) { combined, principalPerson ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = combined.categories
                val budget = combined.budget
                val range = combined.range
                val principalPerson = principalPerson
            }
        }
        .combine(accountIncomeFilterValue) { combined, incomeFilterValue ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = combined.categories
                val budget = combined.budget
                val range = combined.range
                val principalPerson = combined.principalPerson
                val incomeFilterValue = incomeFilterValue
            }
        }
        .combine(accountOutcomeFilterValue) { combined, outcomeFilterValue ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = combined.categories
                val budget = combined.budget
                val range = combined.range
                val principalPerson = combined.principalPerson
                val incomeFilterValue = combined.incomeFilterValue
                val outcomeFilterValue = outcomeFilterValue
            }
        }
        .combine(accountTransferFilterValue) { combined, transferFilterValue ->
            combined.run {
                accountDetail?.let {
                    AccountDetailData.build(
                        account = accountDetail,
                        allAccounts = allAccount,
                        allCategories = categories,
                        budget = budget,
                        startDate = range?.first,
                        endDate = range?.second,
                        principalPerson = principalPerson,
                        incomeFilter = incomeFilterValue,
                        outcomeFilter = outcomeFilterValue,
                        transferFilter = transferFilterValue
                    )
                }
            }
        }
    private val principalPersonWithAccounts =
        personWithAccounts.map { getPrincipalPersonWithAccounts(it) }
    private val filteredTransactionListitemDetails: LiveData<List<TransactionListItemDetails>> =
        rangeTransactions
            .combine(categories) { rangeTransactions, categories ->
                object {
                    val rangeTransactions = rangeTransactions
                    val categories = categories
                }
            }
            .combine(allAccount) { combined, allAccount ->
                object {
                    val rangeTransactions = combined.rangeTransactions
                    val categories = combined.categories
                    val allAccount = allAccount
                }
            }
            .combine(principalPerson) { combined, principalPerson ->
                object {
                    val rangeTransactions = combined.rangeTransactions
                    val categories = combined.categories
                    val allAccount = combined.allAccount
                }.run {
                    TransactionListItemDetails.from(
                        rangeTransactions,
                        categories,
                        allAccount,
                        principalPerson?.id
                    )
                }
            }
            .combine(incomeFilterValue) { filteredTransactions, incomeFilterValue ->
                filteredTransactions.applyIncomeFilter(incomeFilterValue)
            }
            .combine(outcomeFilterValue) { filteredTransactions, outcomeFilterValue ->
                filteredTransactions.applyOutcomeFilter(outcomeFilterValue)
            }
            .combine(transferFilterValue) { filteredTransactions, transferFilterValue ->
                filteredTransactions.applyTransferFilter(transferFilterValue)
            }

    private val allTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        allTransactions
            .combine(allAccount) { allTransactions, allAccount ->
                object {
                    val allTransactions = allTransactions
                    val allAccount = allAccount
                }
            }
            .combine(categories) { combined, categories ->
                combined.run {
                    TransactionAndAccountsAndCategory.from(
                        allTransactions,
                        allAccount,
                        categories
                    )
                }
            }

    private val personSummaryState: LiveData<PersonSummaryState?> =
        principalPersonWithAccounts
            .combine(range) { principalPersonWithAccounts, range ->
                object {
                    val principalPersonWithAccounts = principalPersonWithAccounts
                    val range = range
                }
            }
            .combine(personWithAccounts) { combined, personWithAccounts ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = personWithAccounts
                }
            }
            .combine(allTransactionAndAccountsAndCategory) { combined, allTransactionAndAccountsAndCategory ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionAndAccountsAndCategory = allTransactionAndAccountsAndCategory
                }
            }
            .combine(budgetWithCalculatedDataAndCategory) { combined, budgetWithCalculatedDataAndCategory ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionAndAccountsAndCategory =
                        combined.allTransactionAndAccountsAndCategory
                    val budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory
                }
            }
            .combine(incluirPresupuestoEnSaldoActual) { combined, incluirPresupuestoEnSaldoActual ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionAndAccountsAndCategory =
                        combined.allTransactionAndAccountsAndCategory
                    val budgetWithCalculatedDataAndCategory =
                        combined.budgetWithCalculatedDataAndCategory
                    val incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual
                }
            }
            .combine(incluirDeudasEnSaldoActual) { combined, incluirDeudasEnSaldoActual ->
                combined.run {
                    principalPersonWithAccounts?.let { pp ->
                        PersonSummaryState.from(
                            pp,
                            range?.first,
                            range?.second,
                            allPersons = personWithAccounts,
                            allTransactions = allTransactionAndAccountsAndCategory
                                .map {
                                    TransactionAndAccounts(
                                        it.transaction,
                                        it.sourceAccount,
                                        it.destinationAccount
                                    )
                                },
                            budgetWithCalculatedDatumAndCategories = budgetWithCalculatedDataAndCategory,
                            includeBudget = incluirPresupuestoEnSaldoActual,
                            includeDebts = incluirDeudasEnSaldoActual
                        )
                    }
                }
            }

    fun updateRange(startDate: LocalDate?, endDate: LocalDate?) {
        range.value = Pair(startDate, endDate)
    }

    fun insertPerson(vararg person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.insertPerson(*person)
        }

    fun updatePerson(person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.updatePerson(person)
        }

    fun deletePerson(person: Person) = viewModelScope.launch {
        repository.deletePerson(person)
    }

    fun insertAccount(
        vararg account: Account,
        onErrorAction: (Throwable) -> Unit,
        onCompleitionAction: (Long) -> Unit
    ): Job =
        viewModelScope.safeLaunch(onErrorAction) {
            val addedIds = repository.insertAccount(*account)
            onCompleitionAction(addedIds.first())
        }

    fun updateAccount(
        account: Account,
        onErrorAction: (Throwable) -> Unit,
        onCompleitionAction: (Long) -> Unit
    ) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.updateAccount(account)
            val id = account.id
            if (id != null) {
                onCompleitionAction(id.toLong())
            }
        }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun realizarAjuste(
        accountId: Int,
        amount: Double,
        incomeAccountId: Int,
        outcomeAccountId: Int
    ) = viewModelScope.launch {
        if (amount != 0.0) {
            val transaccionAjuste = if (amount > 0) {
                Transaction(
                    amount = amount,
                    description = "Ajuste",
                    sourceId = incomeAccountId,
                    destinationId = accountId,
                    date = LocalDate.now(),
                    aNombreDe = null,
                    categoryId = null
                )
            } else {
                Transaction(
                    amount = -amount,
                    description = "Ajuste",
                    sourceId = accountId,
                    destinationId = outcomeAccountId,
                    date = LocalDate.now(),
                    aNombreDe = null,
                    categoryId = null
                )
            }
            repository.insertTransaction(transaccionAjuste)
        }
    }

    private fun updateAccountDetailIdIfDifferent(
        newId: Int?,
        incomeFilterValue: Boolean,
        outcomeFilterValue: Boolean,
        transferFilterValue: Boolean
    ) {
        if (newId != accountDetailId.value) {
            accountDetailId.value = newId
        }
        if (incomeFilterValue != accountIncomeFilterValue.value) {
            accountIncomeFilterValue.value = incomeFilterValue
        }
        if (outcomeFilterValue != accountOutcomeFilterValue.value) {
            accountOutcomeFilterValue.value = outcomeFilterValue
        }
        if (transferFilterValue != accountTransferFilterValue.value) {
            accountTransferFilterValue.value = transferFilterValue
        }
    }

    fun insertTransaction(
        vararg transaction: Transaction,
        onErrorAction: (Throwable) -> Unit = {}
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.insertTransaction(*transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun insertCategory(
        vararg category: Category,
        onCompleitionAction: (Long?) -> Unit,
        onErrorAction: (Throwable) -> Unit
    ) =
        viewModelScope.safeLaunch(onErrorAction) {
            val ids = repository.insertCategory(*category)
            onCompleitionAction(ids.firstOrNull())
        }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun updateCategory(
        vararg newCategory: Category,
        onCompleitionAction: () -> Unit,
        onErrorAction: (Throwable) -> Unit,
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.updateCategory(*newCategory)
        onCompleitionAction()
    }

    fun insertBudget(
        vararg budget: Budget,
        onCompleitionAction: () -> Unit,
        onErrorAction: (Throwable) -> Unit
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.insertBudget(*budget)
        onCompleitionAction()
    }

    fun updateBudget(
        budget: Budget,
        onCompleitionAction: () -> Unit,
        onErrorAction: (Throwable) -> Unit
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.updateBudget(budget)
        onCompleitionAction()
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    fun settingsIncluirPresupuestoEnSaldoActualFlow(newValue: Boolean) = viewModelScope.launch {
        settings.setIncluirPresupuestoEnSaldoActualFlow(newValue)
    }

    fun settingsIncluirDeudasEnSaldoActualFlow(newValue: Boolean) = viewModelScope.launch {
        settings.setIncluirDeudasEnSaldoActualFlow(newValue)
    }

    private fun getPrincipalPerson(personList: List<Person>): Person? {
        return if (personList.isEmpty()) {
            null
        } else {
            personList
                .filter { it.importance != null }
                .sortedBy { it.id }
                .sortedBy { it.importance }
                .firstOrNull()
        }
    }

    private fun getPrincipalPersonWithAccounts(personWithAccountsList: List<PersonWithAccounts>):
            PersonWithAccounts? {
        return if (personWithAccountsList.isEmpty()) {
            null
        } else {
            personWithAccountsList
                .filter { it.person.importance != null }
                .sortedBy { it.person.id }
                .sortedBy { it.person.importance }
                .firstOrNull()
        }
    }

    private fun getIncomeAccount(accountList: List<Account>): Account? {
        return if (accountList.isEmpty()) {
            null
        } else {
            accountList
                .filter { it.isIncome }
                .sortedBy { it.id }
                .firstOrNull()
        }
    }

    private fun getOutcomeAccount(accountList: List<Account>): Account? {
        return if (accountList.isEmpty()) {
            null
        } else {
            accountList
                .filter { it.isOutcome }
                .sortedBy { it.id }
                .firstOrNull()
        }
    }

    companion object {
        suspend fun List<TransactionListItemDetails>.applyIncomeFilter(
            incomeFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            filter {
                it.transactionType != TransactionType.INCOME || incomeFilterValue
            }
        }

        suspend fun List<TransactionListItemDetails>.applyOutcomeFilter(
            outcomeFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            filter {
                it.transactionType != TransactionType.OUTCOME || outcomeFilterValue
            }
        }

        suspend fun List<TransactionListItemDetails>.applyTransferFilter(
            transferFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            filter {
                it.transactionType != TransactionType.TRANSFER || transferFilterValue
            }
        }
    }
}

data class PersonSummaryState(
    val person: Person,
    val saldoActual: Double,
    val ingresos: Double,
    val egresos: Double,
    val deudasFlujo: Map<Person, Double>
) {
    val flujo: Double get() = ingresos - egresos

    companion object {
        fun from(
            personWithAccounts: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?,
            allPersons: List<PersonWithAccounts>,
            allTransactions: List<TransactionAndAccounts>,
            budgetWithCalculatedDatumAndCategories: List<BudgetWithCalculatedDataAndCategory>,
            includeBudget: Boolean,
            includeDebts: Boolean
        ): PersonSummaryState {
            val deudasFlujo = allPersons.associate { otherPerson ->
                otherPerson.person to PersonDao.getFlujo(
                    personWithAccounts,
                    otherPerson,
                    allTransactions
                )
            }
            return PersonSummaryState(
                person = personWithAccounts.person,
                saldoActual = personWithAccounts.let {
                    PersonDao.getTotal(
                        it,
                        null,
                        null
                    )
                } + if (includeBudget) {
                    budgetWithCalculatedDatumAndCategories.sumOf { it.budgetLeftToPay }
                } else {
                    0.0
                } + if (includeDebts) {
                    deudasFlujo
                        .toList()
                        .sumOf { it.second }
                } else {
                    0.0
                },
                ingresos = personWithAccounts.let { PersonDao.getIngresos(it, startDate, endDate) },
                egresos = personWithAccounts.let { PersonDao.getEgresos(it, startDate, endDate) },
                deudasFlujo = deudasFlujo
            )
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository, private val settings: Settings) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, settings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}