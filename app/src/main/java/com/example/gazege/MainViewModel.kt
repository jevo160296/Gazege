package com.example.gazege

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.*
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.*
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

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    @Composable
    fun rememberAllPerson() = allPerson.observeAsState(emptyList())

    @Composable
    fun rememberAllAccount() = allAccount.observeAsState(emptyList())

    @Composable
    fun rememberAllTransactions() = allTransactions.observeAsState(emptyList())

    @Composable
    fun rememberCategories() = categories.observeAsState(emptyList())

    @Composable
    fun rememberBudget() = budget.observeAsState(emptyList())

    @Composable
    fun rememberBudgetAndCategoryWithTransactions() =
        budgetAndCategoryWithTransactions.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactions() =
        accountAndOwnerWithTransactions.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactionsUserFirst() =
        accountAndOwnerWithTransactionsUserFirst.observeAsState(emptyList())

    @Composable
    fun rememberAccountAndOwnerWithTransactionsAndPockets() =
        accountAndOwnerWithTransactionsAndPockets.observeAsState(emptyList())

    @Composable
    fun rememberPersonWithAccounts() = personWithAccounts.observeAsState(emptyList())

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
    fun rememberPrincipalPersonWithAccounts() = principalPersonWithAccounts.observeAsState()

    @Composable
    fun rememberFilteredTransactionAndAccountsAndCategory() =
        filteredTransactionAndAccountsAndCategory.observeAsState(emptyList())

    @Composable
    fun rememberAllTransactionAndAccountsAndCategory() =
        allTransactionAndAccountsAndCategory.observeAsState(emptyList())

    private val allPerson = repository.getPersons().asLiveData()
    private val allAccount = repository.getAccounts().asLiveData()
    private val allTransactions = repository.getTransactions(null, null).asLiveData()
    private val categories = repository.getCategories().asLiveData()
    private val budget = repository.getBudgets().asLiveData()

    private val accountAndOwnerWithTransactions: LiveData<List<AccountAndOwnerWithTransactions>> =
        MediatorLiveData<List<AccountAndOwnerWithTransactions>>(listOf())
            .apply {
                val update = {
                    value = AccountAndOwnerWithTransactions.from(
                        allAccount.value ?: emptyList(),
                        allPerson.value ?: emptyList(),
                        allTransactions.value ?: emptyList()
                    )
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
                emit(
                    lista.map { item ->
                        AccountAndOwnerWithTransactionsAndPockets.from(item, lista)
                    })
            }
        }
    private val personWithAccounts: LiveData<List<PersonWithAccounts>> =
        MediatorLiveData<List<PersonWithAccounts>>(listOf())
            .apply {
                val update = {
                    value = PersonWithAccounts.from(
                        allPerson.value ?: emptyList(),
                        accountAndOwnerWithTransactionsAndPockets.value ?: emptyList()
                    )
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
                    value = if (person == null) {
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
                }

                addSource(budget) { update() }
                addSource(categories) { update() }
                //addSource(principalPerson){ update() }
                addSource(accountAndOwnerWithTransactions) { update() }
            }

    fun updatePersonFilterValue(newValue: Boolean) {
        personFilterValue.value = newValue
    }


    private val principalPerson = allPerson.switchMap { persons ->
        liveData { emit(getPrincipalPerson(persons)) }
    }

    private val incomeAccount = allAccount.switchMap { accounts ->
        liveData { emit(getIncomeAccount(accounts)) }
    }
    private val outcomeAccount = allAccount.switchMap { accounts ->
        liveData { emit(getOutcomeAccount(accounts)) }
    }

    private val rangeTransactions = range.switchMap { range ->
        repository.getTransactions(range?.first, range?.second).asLiveData()
    }
    private val accountDetail = MutableLiveData<AccountAndOwnerWithTransactionsAndPockets?>(null)
    private val _accountDetailData = MutableLiveData<AccountDetailData?>(null)
    private val accountDetailData: LiveData<AccountDetailData?> =
        MediatorLiveData<AccountDetailData?>()
            .apply {
                addSource(accountDetail) {
                    calculateAccountDetailData(
                        accountDetail.value,
                        allAccount.value,
                        categories.value,
                        range.value
                    )
                }
                addSource(allAccount) {
                calculateAccountDetailData(
                    accountDetail.value,
                    allAccount.value,
                    categories.value,
                    range.value
                )
            }
            addSource(categories) {
                calculateAccountDetailData(
                    accountDetail.value,
                    allAccount.value,
                    categories.value,
                    range.value
                )
            }
            addSource(range) {
                calculateAccountDetailData(
                    accountDetail.value,
                    allAccount.value,
                    categories.value,
                    range.value
                )
            }
                addSource(_accountDetailData) {
                    value = it
                }
            }
    private val principalPersonWithAccounts = personWithAccounts.switchMap {
        liveData { emit(getPrincipalPersonWithAccounts(it)) }
    }
    private val filteredTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        MediatorLiveData<List<TransactionAndAccountsAndCategory>>(listOf())
            .apply {
                val update = {
                    value = TransactionAndAccountsAndCategory.from(
                        rangeTransactions.value ?: emptyList(),
                        allAccount.value ?: emptyList(),
                        categories.value ?: emptyList()
                    )
                }
                addSource(rangeTransactions) { update() }
                addSource(allAccount) { update() }
                addSource(categories) { update() }
            }
    private val allTransactionAndAccountsAndCategory: LiveData<List<TransactionAndAccountsAndCategory>> =
        MediatorLiveData<List<TransactionAndAccountsAndCategory>>(listOf())
            .apply {
                val update = {
                    value = TransactionAndAccountsAndCategory.from(
                        allTransactions.value ?: emptyList(),
                        allAccount.value ?: emptyList(),
                        categories.value ?: emptyList()
                    )
                }
                addSource(allTransactions) { update() }
                addSource(allAccount) { update() }
                addSource(categories) { update() }
            }

    private fun calculateAccountDetailData(
        account: AccountAndOwnerWithTransactionsAndPockets?,
        allAccount: List<Account>?,
        categories: List<Category>?,
        range: Pair<LocalDate?, LocalDate?>?
    ) {
        if (account?.accountAndOwnerWithTransactions?.account?.id !=
            _accountDetailData.value?.account?.accountAndOwnerWithTransactions?.account?.id
        ) {
            _accountDetailData.postValue(null)
        }
        viewModelScope.launch {
            if (account != null) {
                val result = withContext(Dispatchers.Default) {
                    AccountDetailData.build(
                        account = account,
                        allAccounts = allAccount ?: listOf(),
                        allCategories = categories ?: listOf(),
                        startDate = range?.first,
                        endDate = range?.second
                    )
                }
                _accountDetailData.postValue(result)
            } else {
                _accountDetailData.postValue(null)
            }
        }
    }

    fun updateAccountDetailData(
        account: AccountAndOwnerWithTransactionsAndPockets?,
    ) {
        accountDetail.value = account
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

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}