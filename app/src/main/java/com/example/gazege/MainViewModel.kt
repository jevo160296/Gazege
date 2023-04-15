package com.example.gazege

import androidx.compose.runtime.Composable
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
        budgetAndCategoryWithCalculatedData.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactions() =
        accountAndOwnerWithTransactions.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwner() = accountAndOwner.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactionsUserFirst() =
        accountAndOwnerWithTransactionsUserFirst.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactionsAndPockets() =
        accountAndOwnerWithTransactionsAndPockets.observeAsState(emptyList())

    @Composable
    fun rememberCategoriesWithSubCategories() =
        categoriesWithSubCategories.observeAsState(emptyList())

    @Composable
    fun rememberRange() = range.observeAsState(Pair(LocalDate.now(), LocalDate.now()))

    @Composable
    fun rememberPersonFilterValue() = personFilterValue.observeAsState(false)

    @Composable
    fun rememberPrincipalPerson() = principalPerson.observeAsState()

    @Composable
    fun rememberIncomeAccount() = incomeAccount.observeAsState()

    @Composable
    fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

    @Composable
    fun rememberAccountDetailData() = accountDetailData.observeAsState()

    @Composable
    fun rememberFilteredTransactionAndAccountsAndCategory() =
        filteredTransactionAndAccountsAndCategory.observeAsState(emptyList())


    private val incluirPresupuestoEnSaldoActual =
        settings.getIncluirPresupuestoEnSaldoActualFlow().asLiveData()
    private val allPerson = repository.getPersons().asLiveData()
    private val allAccount = repository.getAccounts().asLiveData()
    private val allTransactions = repository.getTransactions(null, null).asLiveData()
    private val categories = repository.getCategories().asLiveData()
    private val budget = repository.getBudgets().asLiveData()
    private val principalPerson = allPerson.map { persons -> getPrincipalPerson(persons) }

    private val accountAndOwner: LiveData<List<AccountAndOwner>> =
        MediatorLiveData<List<AccountAndOwner>>(emptyList())
            .apply {
                val update = {
                    value = AccountAndOwner.from(
                        allAccount.value ?: emptyList(),
                        allPerson.value ?: emptyList()
                    )
                }

                addSource(allAccount) { update() }
                addSource(allPerson) { update() }
            }

    private val accountAndOwnerWithTransactions: LiveData<List<AccountAndOwnerWithTransactions>> =
        MediatorLiveData<List<AccountAndOwnerWithTransactions>>(listOf())
            .apply {
                val update = {
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            postValue(
                                AccountAndOwnerWithTransactions.from(
                                    allAccount.value ?: emptyList(),
                                    allPerson.value ?: emptyList(),
                                    allTransactions.value ?: emptyList()
                                )
                            )
                        }
                    }
                }
                addSource(allAccount) { update() }
                addSource(allPerson) { update() }
                addSource(allTransactions) { update() }
            }
    private val accountAndOwnerWithTransactionsUserFirst: LiveData<List<AccountAndOwnerWithTransactions>> =
        accountAndOwnerWithTransactions.map { it.sortedByDescending { acc -> acc.owner.importance } }
    private val accountAndOwnerWithTransactionsAndPockets: LiveData<List<AccountAndOwnerWithTransactionsAndPockets>> =
        accountAndOwnerWithTransactions.switchMap { lista ->
            liveData {
                emit(lista.map { item ->
                    AccountAndOwnerWithTransactionsAndPockets.from(
                        item,
                        lista
                    )
                })
            }
        }
    private val personWithAccounts: LiveData<List<PersonWithAccounts>> =
        MediatorLiveData<List<PersonWithAccounts>>(listOf())
            .apply {
                val update = {
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            postValue(
                                PersonWithAccounts.from(
                                    allPerson.value ?: emptyList(),
                                    accountAndOwnerWithTransactionsAndPockets.value ?: emptyList()
                                )
                            )
                        }
                    }
                }
                addSource(allPerson) { update() }
                addSource(accountAndOwnerWithTransactionsAndPockets) { update() }
            }
    private val categoriesWithSubCategories: LiveData<List<CategoryWithSubCategories>> = categories
        .switchMap { liveData { emit(CategoryWithSubCategories.from(it)) } }


    private val initialRange = LocalDate.now().withDayOfMonth(1).let {
        Pair(it, it.plusMonths(1L).minusDays(1L))
    }
    private val range: MutableLiveData<Pair<LocalDate?, LocalDate?>> = MutableLiveData(initialRange)

    private val personFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    private val budgetAndCategoryWithTransactions: LiveData<List<BudgetAndCategoryWithTransactions>> =
        MediatorLiveData<List<BudgetAndCategoryWithTransactions>>(emptyList())
            .apply {
                val update = {
                    val person = principalPerson.value
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            postValue(
                                if (person == null) {
                                    emptyList()
                                } else {
                                    BudgetAndCategoryWithTransactions.from(
                                        budget = budget.value ?: emptyList(),
                                        category = categories.value ?: emptyList(),
                                        person = person,
                                        accountAndOwnerWithTransactions = accountAndOwnerWithTransactions.value
                                            ?: emptyList()
                                    )
                                }
                            )
                        }
                    }
                }

                addSource(budget) { update() }
                addSource(categories) { update() }
                addSource(principalPerson) { update() }
                addSource(accountAndOwnerWithTransactions) { update() }
            }

    private val budgetAndCategoryWithCalculatedData: LiveData<List<BudgetAndCategoryWithCalculatedData>> =
        MediatorLiveData<List<BudgetAndCategoryWithCalculatedData>>(emptyList())
            .apply {
                val update = {
                    val budget = budgetAndCategoryWithTransactions.value
                    val startDate = range.value?.first
                    val endDate = range.value?.second

                    if (budget != null && startDate != null && endDate != null) {
                        viewModelScope.launch {
                            withContext(Dispatchers.Default) {
                                postValue(
                                    BudgetAndCategoryWithCalculatedData.from(
                                        budget,
                                        LocalDate.now(),
                                        startDate,
                                        endDate
                                    )
                                )
                            }
                        }
                    }
                }

                addSource(budgetAndCategoryWithTransactions) { update() }
                addSource(range) { update() }
            }

    fun updatePersonFilterValue(newValue: Boolean) {
        personFilterValue.value = newValue
    }

    private val incomeAccount = allAccount.map { accounts -> getIncomeAccount(accounts) }
    private val outcomeAccount = allAccount.map { accounts -> getOutcomeAccount(accounts) }

    private val rangeTransactions = range.switchMap { range ->
        repository.getTransactions(range?.first, range?.second).asLiveData()
    }
    private val accountDetailId = MutableLiveData<Int?>(null)
    private val accountDetail = MediatorLiveData<AccountAndOwnerWithTransactionsAndPockets?>(null)
        .apply {
            val update = {
                val newId = accountDetailId.value
                value = accountAndOwnerWithTransactionsAndPockets.value?.firstOrNull {
                    it.accountAndOwnerWithTransactions.account.id == newId
                }
            }

            addSource(accountAndOwnerWithTransactionsAndPockets) { update() }
            addSource(accountDetailId) { update() }
        }
    private val accountDetailData: LiveData<AccountDetailData?> =
        MediatorLiveData<AccountDetailData?>()
            .apply {
                val update = {
                    val account = accountDetail.value
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            val value = account?.let {
                                AccountDetailData.build(
                                    account = account,
                                    allAccounts = allAccount.value ?: emptyList(),
                                    allCategories = categories.value ?: emptyList(),
                                    startDate = range.value?.first,
                                    endDate = range.value?.second
                                )
                            }
                            postValue(value)
                        }
                    }
                }
                addSource(accountDetail) { update() }
                addSource(allAccount) { update() }
                addSource(categories) { update() }
                addSource(range) { update() }
            }
    private val principalPersonWithAccounts = personWithAccounts.switchMap {
        liveData { emit(getPrincipalPersonWithAccounts(it)) }
    }
    private val filteredTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        MediatorLiveData<List<TransactionAndAccountsAndCategory>>(listOf())
            .apply {
                val update = {
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            postValue(
                                TransactionAndAccountsAndCategory.from(
                                    rangeTransactions.value ?: emptyList(),
                                    allAccount.value ?: emptyList(),
                                    categories.value ?: emptyList()
                                )
                            )
                        }
                    }
                }
                addSource(rangeTransactions) { update() }
                addSource(allAccount) { update() }
                addSource(categories) { update() }
            }
    private val allTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        MediatorLiveData<List<TransactionAndAccountsAndCategory>>(listOf())
            .apply {
                val update = {
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            postValue(
                                TransactionAndAccountsAndCategory.from(
                                    allTransactions.value ?: emptyList(),
                                    allAccount.value ?: emptyList(),
                                    categories.value ?: emptyList()
                                )
                            )
                        }
                    }
                }
                addSource(allTransactions) { update() }
                addSource(allAccount) { update() }
                addSource(categories) { update() }
            }

    private val personSummaryState: LiveData<PersonSummaryState?> =
        MediatorLiveData<PersonSummaryState?>(null)
            .apply {
                val update = {
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            val state = principalPersonWithAccounts.value?.let { pp ->
                                PersonSummaryState.from(
                                    pp,
                                    range.value?.first,
                                    range.value?.second,
                                    allPersons = personWithAccounts.value ?: emptyList(),
                                    allTransactions = allTransactionAndAccountsAndCategory.value
                                        ?.map {
                                            TransactionAndAccounts(
                                                it.transaction,
                                                it.sourceAccount,
                                                it.destinationAccount
                                            )
                                        }
                                        ?: emptyList(),
                                    budgetAndCategoryWithCalculatedData = budgetAndCategoryWithCalculatedData.value
                                        ?: emptyList(),
                                    includeBudget = incluirPresupuestoEnSaldoActual.value ?: false
                                )
                            }
                            postValue(state)
                        }
                    }
                }

                addSource(principalPersonWithAccounts) { update() }
                addSource(range) { update() }
                addSource(personWithAccounts) { update() }
                addSource(allTransactionAndAccountsAndCategory) { update() }
                addSource(budgetAndCategoryWithCalculatedData) { update() }
                addSource(incluirPresupuestoEnSaldoActual) { update() }
            }

    fun updateRange(startDate: LocalDate?, endDate: LocalDate?) {
        range.value = Pair(startDate, endDate)
    }

    fun insertPerson(person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.insertPerson(person)
        }

    fun updatePerson(person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.updatePerson(person)
        }

    fun deletePerson(person: Person) = viewModelScope.launch {
        repository.deletePerson(person)
    }

    fun insertAccount(
        account: Account,
        onErrorAction: (Throwable) -> Unit,
        onCompleitionAction: (Long) -> Unit
    ): Job =
        viewModelScope.safeLaunch(onErrorAction) {
            val addedIds = repository.insertAccount(account)
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

    fun updateAccountDetailIdIfDifferent(newId: Int?) {
        if (newId != accountDetailId.value) {
            accountDetailId.value = newId
        }
    }

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insertTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun insertCategory(
        category: Category,
        onCompleitionAction: (Long?) -> Unit,
        onErrorAction: (Throwable) -> Unit
    ) =
        viewModelScope.safeLaunch(onErrorAction) {
            val ids = repository.insertCategory(category)
            onCompleitionAction(ids.firstOrNull())
        }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun updateCategory(
        newCategory: Category,
        onCompleitionAction: () -> Unit,
        onErrorAction: (Throwable) -> Unit,
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.updateCategory(newCategory)
        onCompleitionAction()
    }

    fun insertBudget(
        budget: Budget,
        onCompleitionAction: () -> Unit,
        onErrorAction: (Throwable) -> Unit
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.insertBudget(budget)
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
        withContext(Dispatchers.Default) {
            settings.setIncluirPresupuestoEnSaldoActualFlow(newValue)
        }
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
            budgetAndCategoryWithCalculatedData: List<BudgetAndCategoryWithCalculatedData>,
            includeBudget: Boolean
        ): PersonSummaryState = PersonSummaryState(
            person = personWithAccounts.person,
            saldoActual = personWithAccounts.let {
                PersonDao.getTotal(
                    it,
                    null,
                    null
                )
            } + if (includeBudget) {
                budgetAndCategoryWithCalculatedData.sumOf { it.budgetLeftToPay }
            } else {
                0.0
            },
            ingresos = personWithAccounts.let { PersonDao.getIngresos(it, startDate, endDate) },
            egresos = personWithAccounts.let { PersonDao.getEgresos(it, startDate, endDate) },
            deudasFlujo = allPersons.associate { otherPerson ->
                otherPerson.person to PersonDao.getFlujo(
                    personWithAccounts,
                    otherPerson,
                    allTransactions
                )
            }
        )
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