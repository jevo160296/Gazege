package com.example.gazege

import androidx.lifecycle.*
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.*
import com.example.gazege.ui.views.AccountDetailData
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
    private val initialRange = LocalDate.now().withDayOfMonth(1).let {
        Pair(it, it.plusMonths(1L).minusDays(1L))
    }
    val range: MutableLiveData<Pair<LocalDate?, LocalDate?>> = MutableLiveData(initialRange)

    val allPerson = repository.getPersons().asLiveData()
    val principalPerson = allPerson.switchMap { persons ->
        MutableLiveData(getPrincipalPerson(persons))
    }
    val allAccount = repository.getAccounts().asLiveData()
    val incomeAccount = allAccount.switchMap { accounts ->
        MutableLiveData(getIncomeAccount(accounts))
    }
    val outcomeAccount = allAccount.switchMap { accounts ->
        MutableLiveData(getOutcomeAccount(accounts))
    }
    val allTransactions = repository.getTransactions(null, null).asLiveData()
    val rangeTransactions = range.switchMap { range ->
        repository.getTransactions(range?.first, range?.second).asLiveData()
    }
    val categories = repository.getCategories().asLiveData()
    private val _accountDetailData = MutableLiveData<AccountDetailData?>(null)
    val accountDetailData: LiveData<AccountDetailData?> = _accountDetailData

    fun updateAccountDetailData(
        initialState: AccountDetailData?,
        account: AccountAndOwnerWithTransactionsAndPockets,
        allAccounts: List<Account>,
        allCategories: List<Category>,
        startDate: LocalDate?,
        endDate: LocalDate?
    ) = viewModelScope.launch {
        _accountDetailData.postValue(initialState)
        val result = withContext(Dispatchers.Default) {
            AccountDetailData.build(
                account = account,
                allAccounts = allAccounts,
                allCategories = allCategories,
                startDate = startDate,
                endDate = endDate
            )
        }
        _accountDetailData.postValue(result)
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