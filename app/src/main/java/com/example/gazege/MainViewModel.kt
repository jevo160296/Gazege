package com.example.gazege

import android.util.Log
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

    private fun logPrintln(name: String, source: String? = null) = Log.println(
        Log.INFO,
        "Mediator",
        "$name: runningUpdate ${source?.let { "from $it" } ?: ""}")

    private fun <T, A : Any, B : Any> MediatorLiveData<T>.mergeTwoSources(
        name: String,
        sourceA: LiveData<A>,
        sourceB: LiveData<B>,
        merger: (A, B) -> T
    ) = apply {
        val update = { source: String ->
            if (sourceA.isInitialized && sourceB.isInitialized) {
                logPrintln(name, source)
                val a = sourceA.value!!
                val b = sourceB.value!!
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        postValue(merger(a, b))
                    }
                }
            }
        }
        addSource(sourceA) { update("Source A") }
        addSource(sourceB) { update("Source B") }
    }
        .distinctUntilChanged()

    private fun <T, A : Any?, B : Any?> MediatorLiveData<T>.mergeTwoNullableSources(
        name: String,
        sourceA: LiveData<A>,
        sourceB: LiveData<B>,
        merger: (A?, B?) -> T
    ) = apply {
        val update = {
            if (sourceA.isInitialized && sourceB.isInitialized) {
                logPrintln(name)
                val a = sourceA.value
                val b = sourceB.value
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        postValue(merger(a, b))
                    }
                }
            }
        }
        addSource(sourceA) { update() }
        addSource(sourceB) { update() }
    }
        .distinctUntilChanged()

    private fun <T, A : Any, B : Any, C : Any> MediatorLiveData<T>.mergeThreeSources(
        name: String,
        sourceA: LiveData<A>,
        sourceB: LiveData<B>,
        sourceC: LiveData<C>,
        merger: (A, B, C) -> T
    ) = apply {
        val update = {
            if (sourceA.isInitialized && sourceB.isInitialized && sourceC.isInitialized) {
                logPrintln(name)
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        postValue(merger(sourceA.value!!, sourceB.value!!, sourceC.value!!))
                    }
                }
            }
        }
        addSource(sourceA) { update() }
        addSource(sourceB) { update() }
        addSource(sourceC) { update() }
    }
        .distinctUntilChanged()

    private fun <T, A : Any?, B : Any?, C : Any?, D : Any?> MediatorLiveData<T>.mergeFourNullableSources(
        name: String,
        sourceA: LiveData<A>,
        sourceB: LiveData<B>,
        sourceC: LiveData<C>,
        sourceD: LiveData<D>,
        merger: (A?, B?, C?, D?) -> T
    ) = apply {
        val update = {
            if (sourceA.isInitialized && sourceB.isInitialized && sourceC.isInitialized && sourceD.isInitialized) {
                logPrintln(name)
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        postValue(
                            merger(
                                sourceA.value,
                                sourceB.value,
                                sourceC.value,
                                sourceD.value
                            )
                        )
                    }
                }
            }
        }
        addSource(sourceA) { update() }
        addSource(sourceB) { update() }
        addSource(sourceC) { update() }
        addSource(sourceD) { update() }
    }
        .distinctUntilChanged()

    private fun <T, A, B, C, D, E, F> MediatorLiveData<T>.mergeSixNullableSources(
        name: String,
        sourceA: LiveData<A>,
        sourceB: LiveData<B>,
        sourceC: LiveData<C>,
        sourceD: LiveData<D>,
        sourceE: LiveData<E>,
        sourceF: LiveData<F>,
        merger: (A?, B?, C?, D?, E?, F?) -> T
    ) = apply {
        val update = { source: String ->
            if (sourceA.isInitialized && sourceB.isInitialized && sourceC.isInitialized && sourceD.isInitialized && sourceE.isInitialized && sourceF.isInitialized) {
                logPrintln(name, source)
                viewModelScope.launch {
                    withContext(Dispatchers.Default) {
                        postValue(
                            merger(
                                sourceA.value,
                                sourceB.value,
                                sourceC.value,
                                sourceD.value,
                                sourceE.value,
                                sourceF.value
                            )
                        )
                    }
                }
            }
        }
        addSource(sourceA) { update("SourceA") }
        addSource(sourceB) { update("SourceB") }
        addSource(sourceC) { update("SourceC") }
        addSource(sourceD) { update("SourceD") }
        addSource(sourceE) { update("SourceE") }
        addSource(sourceF) { update("SourceF") }
    }
        .distinctUntilChanged()

    private val incluirPresupuestoEnSaldoActual =
        settings.getIncluirPresupuestoEnSaldoActualFlow().asLiveData()
    private val allPerson = repository.getPersons().asLiveData()
    private val allAccount = repository.getAccounts().asLiveData()
    private val allTransactions = repository.getTransactions(null, null).asLiveData()
    private val categories = repository.getCategories().asLiveData()
    private val budget = repository.getBudgets().asLiveData()
    private val principalPerson = allPerson.map { persons -> getPrincipalPerson(persons) }

    private val accountAndOwner: LiveData<List<AccountAndOwner>> =
        MediatorLiveData<List<AccountAndOwner>>()
            .mergeTwoSources("accountAndOwner", allAccount, allPerson) { allAccount, allPerson ->
                AccountAndOwner.from(
                    allAccount,
                    allPerson
                )
            }

    private val accountAndOwnerWithTransactions: LiveData<List<AccountAndOwnerWithTransactions>> =
        MediatorLiveData<List<AccountAndOwnerWithTransactions>>()
            .mergeThreeSources(
                "accountAndOwnerWithTransactions",
                allAccount,
                allPerson,
                allTransactions
            ) { a, b, c ->
                AccountAndOwnerWithTransactions.from(a, b, c)
            }

    private val accountAndOwnerWithTransactionsUserFirst: LiveData<List<AccountAndOwnerWithTransactions>> =
        accountAndOwnerWithTransactions.map { it.sortedByDescending { acc -> acc.owner.importance } }
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
        MediatorLiveData<List<PersonWithAccounts>>()
            .mergeTwoSources(
                "personWithAccounts",
                allPerson,
                accountAndOwnerWithTransactionsAndPockets
            ) { a, b ->
                PersonWithAccounts.from(a, b)
            }
    private val categoriesWithSubCategories: LiveData<List<CategoryWithSubCategories>> = categories
        .map { CategoryWithSubCategories.from(it) }


    private val initialRange = LocalDate.now().withDayOfMonth(1).let {
        Pair(it, it.plusMonths(1L).minusDays(1L))
    }
    private val range: MutableLiveData<Pair<LocalDate?, LocalDate?>> = MutableLiveData(initialRange)

    private val personFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    private val budgetAndCategoryWithTransactions: LiveData<List<BudgetAndCategoryWithTransactions>> =
        MediatorLiveData<List<BudgetAndCategoryWithTransactions>>()
            .mergeFourNullableSources(
                "budgetAndCategoryWithTransactions",
                budget,
                categories,
                accountAndOwnerWithTransactions,
                principalPerson
            ) { budget, categories, accountAndOwnerWithTransactions, person ->
                if (person == null) {
                    emptyList()
                } else {
                    BudgetAndCategoryWithTransactions.from(
                        budget = budget ?: emptyList(),
                        category = categories ?: emptyList(),
                        person = person,
                        accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                            ?: emptyList()
                    )
                }
            }

    private val budgetAndCategoryWithCalculatedData: LiveData<List<BudgetAndCategoryWithCalculatedData>> =
        MediatorLiveData<List<BudgetAndCategoryWithCalculatedData>>()
            .mergeTwoSources(
                "budgetAndCategoryWithCalculatedData",
                budgetAndCategoryWithTransactions,
                range
            ) { a, b ->
                val startDate = b.first
                val endDate = b.second
                if (startDate != null && endDate != null) {
                    BudgetAndCategoryWithCalculatedData.from(
                        a,
                        LocalDate.now(),
                        startDate,
                        endDate
                    )
                } else {
                    emptyList()
                }
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
    private val accountDetail = MediatorLiveData<AccountAndOwnerWithTransactionsAndPockets?>()
        .mergeTwoNullableSources(
            "accountDetail",
            accountAndOwnerWithTransactionsAndPockets,
            accountDetailId
        ) { a, b ->
            a?.firstOrNull {
                it.accountAndOwnerWithTransactions.account.id == b
            }
        }
    private val accountDetailData: LiveData<AccountDetailData?> =
        MediatorLiveData<AccountDetailData?>()
            .mergeFourNullableSources(
                "accountDetailData",
                accountDetail,
                allAccount,
                categories,
                range
            ) { a, b, c, d ->
                a?.let {
                    AccountDetailData.build(
                        account = a,
                        allAccounts = b ?: emptyList(),
                        allCategories = c ?: emptyList(),
                        startDate = d?.first,
                        endDate = d?.second
                    )
                }
            }
    private val principalPersonWithAccounts =
        personWithAccounts.map { getPrincipalPersonWithAccounts(it) }
    private val filteredTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        MediatorLiveData<List<TransactionAndAccountsAndCategory>>()
            .mergeThreeSources(
                "filteredTransactionAndAccountsAndCategory",
                rangeTransactions,
                allAccount,
                categories
            ) { a, b, c ->
                TransactionAndAccountsAndCategory.from(a, b, c)
            }
    private val allTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        MediatorLiveData<List<TransactionAndAccountsAndCategory>>()
            .mergeThreeSources(
                "allTransactionAndAccountsAndCategory",
                allTransactions,
                allAccount,
                categories
            ) { a, b, c ->
                TransactionAndAccountsAndCategory.from(a, b, c)
            }

    private val personSummaryState: LiveData<PersonSummaryState?> =
        MediatorLiveData<PersonSummaryState?>()
            .mergeSixNullableSources(
                "personSummaryState",
                principalPersonWithAccounts,
                range,
                personWithAccounts,
                allTransactionAndAccountsAndCategory,
                budgetAndCategoryWithCalculatedData,
                incluirPresupuestoEnSaldoActual
            ) { principalPersonWithAccounts, range, personWithAccounts, allTransactionAndAccountsAndCategory, budgetAndCategoryWithCalculatedData, incluirPresupuestoEnSaldoActual ->
                principalPersonWithAccounts?.let { pp ->
                    PersonSummaryState.from(
                        pp,
                        range?.first,
                        range?.second,
                        allPersons = personWithAccounts ?: emptyList(),
                        allTransactions = allTransactionAndAccountsAndCategory
                            ?.map {
                                TransactionAndAccounts(
                                    it.transaction,
                                    it.sourceAccount,
                                    it.destinationAccount
                                )
                            }
                            ?: emptyList(),
                        budgetAndCategoryWithCalculatedData = budgetAndCategoryWithCalculatedData
                            ?: emptyList(),
                        includeBudget = incluirPresupuestoEnSaldoActual ?: false
                    )
                }
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