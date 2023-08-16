package com.example.gazege

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.*
import com.example.gazege.core.AppRepository
import com.example.gazege.core.entities.*
import com.example.gazege.core.export.readAccountFromCsv
import com.example.gazege.core.export.readBudgetFromCsv
import com.example.gazege.core.export.readCategoryFromCsv
import com.example.gazege.core.export.readPersonsFromCsv
import com.example.gazege.core.export.readTransactionsFromCsv
import com.example.gazege.core.export.writeAccounts
import com.example.gazege.core.export.writeBudget
import com.example.gazege.core.export.writeCategories
import com.example.gazege.core.export.writePersons
import com.example.gazege.core.export.writeTransactions
import com.example.gazege.core.export.writeZipBackup
import com.example.gazege.ui.Settings
import com.example.gazege.ui.navigation.EditarCategoriasState
import com.example.gazege.ui.navigation.LoadedEditarCategoriasState
import com.example.gazege.ui.navigation.LoadedPersonSummaryState
import com.example.gazege.ui.navigation.LoadedTransactionDetailsState
import com.example.gazege.ui.navigation.LoadingTransactionsDetailsState
import com.example.gazege.ui.navigation.loadingPersonSummaryState
import com.example.gazege.ui.navigation.nullCategoriasState
import com.example.gazege.ui.progressStatus.HistoricalProgressStatus
import com.example.gazege.ui.progressStatus.IProgressStatus
import com.example.gazege.ui.progressStatus.Status
import com.example.gazege.ui.views.account.AccountDetailData
import com.example.gazege.ui.widgets.BooleanFilters
import com.example.gazege.ui.widgets.INCOME_FILTER
import com.example.gazege.ui.widgets.OUTCOME_FILTER
import com.example.gazege.ui.widgets.TRANSFER_FILTER
import com.example.gazege.ui.widgets.booleanFilterOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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

fun categoriesMergeBooleanFilter(
    categories: List<CategoryWithSubCategories>,
    booleanFilters: BooleanFilters<Int?, Pair<String, Int>>
) = categories
    .sortedBy { it.category.name }
    .flattenWithLevel()
    .let { categoryWithLevel ->
        booleanFilters.updateWithMetadata(
            categoryWithLevel.map { (it.first.id) to (it.first.name to it.second) }
        )
    }

class MainViewModel(
    private val repository: AppRepository,
    private val settings: Settings,
    private val resultLauncherSaveData: ActivityResultLauncher<String>,
    private val resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>
) :
    ViewModel() {
    fun appInitialized(): Boolean {
        val currentValue = appInitialized
        appInitialized = true
        return currentValue
    }

    fun startActivityToSaveData(
        suggestedName: String
    ) =
        resultLauncherSaveData.launch(suggestedName)

    fun startActivityToLoadData() =
        resultLauncherOpenDocument.launch(arrayOf("*/*"))

    suspend fun getTransactions() =
        repository.getTransactions(null, null)
            .firstOrNull()
            ?: emptyList()

    suspend fun getPersons() =
        repository.getPersons()
            .firstOrNull()
            ?: emptyList()

    suspend fun getCategories() =
        repository.getCategories()
            .firstOrNull()
            ?: emptyList()

    suspend fun getAccounts() =
        repository.getAccounts()
            .firstOrNull()
            ?: emptyList()

    suspend fun getBudget() =
        repository.getBudgets()
            .firstOrNull()
            ?: emptyList()

    enum class Type {
        IMPORT,
        EXPORT
    }

    private fun IProgressStatus.toState(type: Type) = ProgressStatusState(
        message = this.message,
        progress = this.progress,
        status = this.status,
        type = type
    )

    data class ProgressStatusState(
        override var message: String,
        override val progress: Double,
        override var status: Status,
        val type: Type
    ) : IProgressStatus(message, status)


    private val loadingDataState: MutableLiveData<ProgressStatusState> =
        MutableLiveData(
            ProgressStatusState(
                "Not started",
                0.0,
                Status.NOT_STARTED,
                Type.IMPORT,
            )
        )

    fun exportData(outputStream: OutputStream) {
        val progressStatus = HistoricalProgressStatus.start(
            "Exporting data",
            totalWork = 8.0,
            defaultIncrement = 1.0
        ) { loadingDataState.postValue(it.toState(Type.EXPORT)) }
        viewModelScope.safeLaunch(
            onErrorAction = {
                progressStatus.error("Error: ${it.message}")
            }
        ) {
            withContext(Dispatchers.IO) {
                progressStatus.incrementProgress("ConvertingPersons")
                val persons = getPersons()
                val personsOutputStream = ByteArrayOutputStream()
                personsOutputStream.use {
                    writePersons(it, persons)
                }

                progressStatus.incrementProgress("Converting caategories")
                val categories = getCategories()
                val categoriesOutputStream = ByteArrayOutputStream()
                categoriesOutputStream.use {
                    writeCategories(it, categories)
                }

                progressStatus.incrementProgress("Converting accounts")
                val accounts = getAccounts()
                val accountsOutputStream = ByteArrayOutputStream()
                accountsOutputStream.use {
                    writeAccounts(it, accounts)
                }

                progressStatus.incrementProgress("Converting budget")
                val budget = getBudget()
                val budgetOutputStream = ByteArrayOutputStream()
                budgetOutputStream.use {
                    writeBudget(it, budget)
                }

                progressStatus.incrementProgress("Converting transactions")
                val transactions = getTransactions()
                val transactionsOutputStream = ByteArrayOutputStream()
                transactionsOutputStream.use {
                    writeTransactions(it, transactions)
                }

                progressStatus.incrementProgress("Saving files")
                val personsInputStream = ByteArrayInputStream(personsOutputStream.toByteArray())
                val categoriesInputStream =
                    ByteArrayInputStream(categoriesOutputStream.toByteArray())
                val budgetInputStream = ByteArrayInputStream(budgetOutputStream.toByteArray())
                val accountsInputStream = ByteArrayInputStream(accountsOutputStream.toByteArray())
                val transactionsInputStream =
                    ByteArrayInputStream(transactionsOutputStream.toByteArray())
                progressStatus.incrementProgress("Compressing files")
                ZipOutputStream(outputStream)
                    .use { zipOurpurStream ->
                        writeZipBackup(
                            transactionsInputStream,
                            personsInputStream,
                            categoriesInputStream,
                            accountsInputStream,
                            budgetInputStream,
                            zipOurpurStream
                        )
                    }
                progressStatus.finish("Done")
            }
        }
    }

    fun importData(inputStream: InputStream) {
        val progressStatus = HistoricalProgressStatus.start(
            "Starting data import...",
            1.0,
            0.0
        ) { loadingDataState.postValue(it.toState(Type.IMPORT)) }
        var transactions: List<Transaction>? = null
        var categories: List<Category>? = null
        var persons: List<Person>? = null
        var budget: List<Budget>? = null
        var accounts: List<Account>? = null

        viewModelScope.safeLaunch(
            onErrorAction = {
                progressStatus.error("Error: ${it.message}")
            }
        ) {
            withContext(Dispatchers.IO) {
                ZipInputStream(inputStream)
                    .use { zipInputStream ->
                        generateSequence {
                            val entry = zipInputStream.nextEntry
                            entry
                        }
                            .map {
                                when (it.name) {
                                    "transacciones.csv" -> {
                                        progressStatus.incrementProgress("Loading transactions")
                                        zipInputStream.readBytes()
                                            .run {
                                                inputStream().run {
                                                    transactions = readTransactionsFromCsv(this)
                                                }
                                            }
                                        progressStatus.incrementProgress(
                                            "Transactions loaded",
                                            0.238
                                        )
                                    }

                                    "categorias.csv" -> {
                                        progressStatus.incrementProgress("Loading categories")
                                        zipInputStream.readBytes().run {
                                            inputStream().run {
                                                categories = readCategoryFromCsv(this)
                                            }
                                        }
                                        progressStatus.incrementProgress(
                                            "Categories loaded",
                                            0.048
                                        )
                                    }

                                    "cuentas.csv" -> {
                                        progressStatus.incrementProgress("Loading accounts")
                                        zipInputStream.readBytes().run {
                                            inputStream().run {
                                                accounts = readAccountFromCsv(this)
                                            }
                                        }
                                        progressStatus.incrementProgress(
                                            "Accounts loaded",
                                            0.119
                                        )
                                    }

                                    "presupuesto.csv" -> {
                                        progressStatus.incrementProgress("Loading budget")
                                        zipInputStream.readBytes().run {
                                            inputStream().run {
                                                budget = readBudgetFromCsv(this)
                                            }
                                        }
                                        progressStatus.incrementProgress(
                                            "Budget loaded",
                                            0.048
                                        )
                                    }

                                    "personas.csv" -> {
                                        progressStatus.incrementProgress("Loading persons")
                                        zipInputStream.readBytes().run {
                                            inputStream().run {
                                                persons = readPersonsFromCsv(this)
                                            }
                                        }
                                        progressStatus.incrementProgress(
                                            "Persons loaded",
                                            0.048
                                        )
                                    }

                                    else -> {}
                                }
                            }
                            .toList()
                    }
                progressStatus.incrementProgress("Checking imported data")
                if (transactions == null ||
                    categories == null ||
                    persons == null ||
                    budget == null ||
                    accounts == null
                ) {
                    throw Exception(
                        """Error loading data, parsed data:
                        |transactions: ${transactions?.size}
                        |categories: ${categories?.size}
                        |persons: ${persons?.size}
                        |budget: ${budget?.size}
                        |accounts: ${accounts?.size}
                    """.trimMargin()
                    )
                } else {
                    progressStatus.setCompletedWork("Deleting all data", 0.5)
                    deleteAll().invokeOnCompletion {
                        val totalSize = (persons?.size ?: 0) +
                                (accounts?.size ?: 0) +
                                (categories?.size ?: 0) +
                                (budget?.size ?: 0) +
                                (transactions?.size ?: 0)
                        progressStatus.incrementProgress("Inserting values")
                        persons?.also { persons ->
                            insertPerson(*persons.toTypedArray()) {}
                        }
                        progressStatus.incrementProgress(
                            "Person inserted",
                            (persons?.size ?: 0).toDouble() / totalSize
                        )
                        accounts?.also { accounts ->
                            insertAccount(
                                *accounts.toTypedArray(),
                                onErrorAction = {
                                }
                            ) {}
                        }
                        progressStatus.incrementProgress(
                            "Accounts inserted",
                            (accounts?.size ?: 0).toDouble() / totalSize
                        )
                        categories?.also { categories ->
                            insertCategory(
                                *categories.toTypedArray(),
                                onErrorAction = {
                                },
                                onCompleitionAction = {}
                            )
                        }
                        progressStatus.incrementProgress(
                            "Categories inserted",
                            (categories?.size ?: 0).toDouble() / totalSize
                        )
                        budget?.also { budget ->
                            insertBudget(
                                *budget.toTypedArray(),
                                onCompleitionAction = {},
                                onErrorAction = {
                                }
                            )
                        }
                        progressStatus.incrementProgress(
                            "Budget inserted",
                            (budget?.size ?: 0).toDouble() / totalSize
                        )
                        transactions?.also { transactions ->
                            insertTransaction(*transactions.toTypedArray()) {
                            }
                        }
                        progressStatus.finish("Done")
                    }
                }
            }
        }
    }

    @Composable
    fun rememberImportState(): State<ProgressStatusState> = loadingDataState
        .observeAsState(
            ProgressStatusState(
                "Not started",
                0.0,
                Status.NOT_STARTED,
                Type.IMPORT,
            )
        )

    @Composable
    fun rememberAllPerson() = allPerson.observeAsState(emptyList())

    @Composable
    fun rememberAllAccount() = allAccount.observeAsState(emptyList())

    @Composable
    fun rememberSettingsIncluirPresupuestoEnSaldoActualFlow() =
        incluirPresupuestoEnSaldoActual.observeAsState(false)

    @Composable
    fun rememberSettingsIncluirDeudasEnSaldoActualFlow() =
        incluirDeudasEnSaldoActual.observeAsState(false)

    @Composable
    fun rememberPersonSummaryState() =
        personSummaryState.observeAsState(loadingPersonSummaryState())

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
    fun rememberEditarCategoriasState() =
        editarCategoriasState.observeAsState(nullCategoriasState())

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
    fun rememberCategoriesWithSubcategories() =
        categoriesWithSubCategories.observeAsState(emptyList())

    @Composable
    fun rememberRange() = range.observeAsState(Pair(LocalDate.now(), LocalDate.now()))

    @Composable
    fun rememberTransactionFiltersValue() = transactionFilters
        .observeAsState(
            booleanFilterOf(
                listOf(INCOME_FILTER, TRANSFER_FILTER, OUTCOME_FILTER),
                true
            )
        )

    @Composable
    fun rememberCategoriesFiltersValue() =
        categoriesFiltersValue.observeAsState(booleanFilterOf(emptyList(), true))

    @Composable
    fun rememberPersonFilterValue() = personFilterValue.observeAsState(false)

    @Composable
    fun rememberPrincipalPerson() = principalPerson.observeAsState()

    @Composable
    fun rememberIncomeAccount() = incomeAccount.observeAsState()

    @Composable
    fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

    @Composable
    fun rememberAccountDetailData(
        accountId: Int?,
        accountFilters: BooleanFilters<String, Nothing>,
        accountCategoryFilters: BooleanFilters<Int?, Pair<String, Int>>
    ): State<AccountDetailData?> {
        updateAccountDetailIdIfDifferent(
            accountId,
            accountFilters,
            accountCategoryFilters
        )
        return accountDetailData.observeAsState()
    }

    @Composable
    fun rememberFilteredTransactionListItemDetails() =
        filteredTransactionListitemDetails.observeAsState(LoadingTransactionsDetailsState)

    @Composable
    fun rememberTransactionAndAccounts(transactionId: Int?) = remember(transactionId) {
        allTransactionAndAccountsAndCategory
            .map { transactionAndAccountAndCategory ->
                transactionAndAccountAndCategory
                    .firstOrNull { it.transaction.id == transactionId }
                    ?.toTransactionAndAccounts()
            }
    }
        .observeAsState()

    @Composable
    fun rememberPeopleTransactionListItemDetails(
        otherPersonId: Int?,
        justPendingTransactions: Boolean,
        debt: Double
    ) = remember(otherPersonId, justPendingTransactions, debt) {
        allTransactions
            .combine(allAccount) { allTransactions, allAccount ->
                val personAccountsIds = allAccount
                    .filter { it.ownerId == otherPersonId }
                    .map { it.id }
                    .toSet()
                object {
                    val transactions = allTransactions
                        .filter {
                            it.aNombreDe == otherPersonId ||
                                    it.sourceId in personAccountsIds ||
                                    it.destinationId in personAccountsIds
                        }
                        .sortedByDescending { it.date }
                    val accounts = allAccount
                }
            }
            .combine(categories) { combined, categories ->
                object {
                    val transactions = combined.transactions
                    val accounts = combined.accounts
                    val categories = categories
                }
            }
            .combine(principalPerson) { combined, principalPerson ->
                object {
                    val transactions = combined.transactions
                    val categories = combined.categories
                    val accounts = combined.accounts
                    val principalPersonId = principalPerson?.id
                }.run {
                    val allTransactions = TransactionListItemDetails.from(
                        transactions,
                        categories,
                        accounts,
                        principalPersonId
                    )
                    if (justPendingTransactions) {
                        var cumSum = 0.0
                        val sortedTransactions = allTransactions
                            .sortedByDescending { it.transaction.id }
                            .sortedByDescending { it.transaction.date }
                        val filteredTransactions = sortedTransactions
                            .takeWhile {
                                val sign = when (it.transactionType) {
                                    TransactionType.INCOME -> -1.0
                                    TransactionType.OUTCOME -> 1.0
                                    else -> 0.0
                                }
                                val condition = cumSum != debt
                                cumSum += it.transaction.amount * sign
                                condition
                            }
                            .filter { it.transactionType == TransactionType.INCOME || it.transactionType == TransactionType.OUTCOME }
                        filteredTransactions
                    } else {
                        allTransactions
                    }
                }
            }
    }
        .observeAsState()


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

    private val categoryWithTransactions: LiveData<List<CategoryWithTransactions>> =
        categories
            .combine(principalPerson) { categories, principalPerson ->
                object {
                    val categories = categories
                    val principalPerson = principalPerson
                }
            }
            .combine(accountAndOwnerWithTransactions) { combined, accountAndOwnerWithTransactions ->
                if (combined.principalPerson != null) {
                    CategoryWithTransactions.from(
                        category = combined.categories,
                        person = combined.principalPerson,
                        accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                    )
                } else {
                    emptyList()
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

    private val categoryWithCalculatedData: LiveData<List<CategoryWithCalculatedData>> =
        categoryWithTransactions.combine(range) { categoryWithTransactions, range ->
            val startDate = range.first
            val endDate = range.second
            if (startDate != null && endDate != null) {
                CategoryWithCalculatedData.from(
                    categoryWithTransactions = categoryWithTransactions,
                    startDate = startDate,
                    endDate = endDate
                )
            } else {
                emptyList()
            }
        }

    private val budgetWithCalculatedDataAndCategory: LiveData<List<BudgetWithCalculatedDataAndCategory>> =
        budgetWithCalculatedData.combine(categories) { budgetWithCalculatedData, categories ->
            BudgetWithCalculatedDataAndCategory.from(budgetWithCalculatedData, categories)
        }

    private val editarCategoriasState: LiveData<EditarCategoriasState> =
        budgetWithCalculatedDataAndCategory
            .combine(categoriesWithSubCategories) { budgetWithCalculatedDataAndCategory, categoriesWithSubCategories ->
                object {
                    val budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory
                    val categoriesWithSubCategories = categoriesWithSubCategories
                }
            }
            .combine(categoryWithCalculatedData)
            { combined, categoryWithCalculatedData ->
                val categoryWithCalculatedDataMap =
                    categoryWithCalculatedData.associateBy { it.category.id ?: 0 }
                LoadedEditarCategoriasState.from(
                    CategoryWithSubcategoriesAndBudgetWithCalculatedData.from(
                        combined.budgetWithCalculatedDataAndCategory,
                        combined.categoriesWithSubCategories,
                        categoryWithCalculatedDataMap
                    )
                )
            }

    private val transactionFilters: MutableLiveData<BooleanFilters<String, Nothing>> =
        MutableLiveData(
            booleanFilterOf(
                listOf(INCOME_FILTER, TRANSFER_FILTER, OUTCOME_FILTER),
                true
            )
        )

    private val categoriesFiltersValue: MutableLiveData<BooleanFilters<Int?, Pair<String, Int>>> =
        MediatorLiveData(booleanFilterOf<Int?, Pair<String, Int>>(emptyList(), defaultValue = true))
            .apply {
                addSource(categoriesWithSubCategories) { categories ->
                    viewModelScope.launch {
                        withContext(Dispatchers.Default) {
                            val orderedCategories = categories
                                .sortedBy { it.category.name }
                                .flattenWithLevel(0)
                            val updatedValue = if (isInitialized) {
                                val oldValue = value!!
                                val updatedValue = categoriesMergeBooleanFilter(
                                    categories, oldValue
                                )
                                updatedValue
                                    .copy(
                                        values = updatedValue
                                            .let {
                                                it.values.toMutableMap().apply {
                                                    this[null] = oldValue.values.getOrDefault(
                                                        null,
                                                        updatedValue.defaultValue
                                                    )
                                                }
                                            },
                                        metadata = updatedValue
                                            .let {
                                                it.metadata.toMutableMap().apply {
                                                    this[null] = "" to 0
                                                }
                                            }
                                    )
                            } else {
                                booleanFilterOf(
                                    filterNames = orderedCategories.map { it.first.id },
                                    metadata = orderedCategories.associate {
                                        (it.first.id) to (it.first.name to it.second)
                                    }
                                )
                                    .let {
                                        it.copy(
                                            values = it.values.plus(null to true),
                                            metadata = it.metadata.plus(null to ("" to 0))
                                        )
                                    }
                            }
                            postValue(updatedValue)
                        }
                    }
                }
            }

    private val personFilterValue: MutableLiveData<Boolean> = MutableLiveData(true)

    fun updateImportStateStatus(newState: Status) {
        loadingDataState.value = loadingDataState.value?.copy(status = newState)
    }

    fun updateTransactionFilters(newValue: BooleanFilters<String, Nothing>) {
        transactionFilters.value = newValue
    }

    fun updateCategoriasFiltersValue(newValue: BooleanFilters<Int?, Pair<String, Int>>) {
        categoriesFiltersValue.value = newValue
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
    private val accountDetail = accountAndOwnerWithTransactionsAndPockets
        .combine(accountDetailId) { accountAndOwnerWithTransactionsAndPockets, accountDetailId ->
            accountAndOwnerWithTransactionsAndPockets.firstOrNull {
                it.accountAndOwnerWithTransactions.account.id == accountDetailId
            }
        }
    private val accountFilterValue = MutableLiveData(
        booleanFilterOf<String, Nothing>(
            listOf(
                INCOME_FILTER,
                OUTCOME_FILTER,
                TRANSFER_FILTER
            )
        )
    )
    private val accountCategoryFilterValue = MutableLiveData(
        booleanFilterOf<Int?, Pair<String, Int>>(emptyList())
    )
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
        .combine(accountFilterValue) { combined, accountFilterValue ->
            object {
                val accountDetail = combined.accountDetail
                val allAccount = combined.allAccount
                val categories = combined.categories
                val budget = combined.budget
                val range = combined.range
                val principalPerson = combined.principalPerson
                val accountFilterValue = accountFilterValue
            }
        }
        .combine(accountCategoryFilterValue) { combined, accountCategoryFilterValue ->
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
                        transactionFilters = accountFilterValue,
                        categoriesFilter = accountCategoryFilterValue
                    )
                }
            }
        }
    private val principalPersonWithAccounts =
        personWithAccounts.map { getPrincipalPersonWithAccounts(it) }
    private val filteredTransactionListitemDetails: LiveData<LoadedTransactionDetailsState> =
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
            .combine(transactionFilters) { filteredTransactions, filtersValue ->
                filteredTransactions
                    .applyIncomeFilter(filtersValue[INCOME_FILTER])
                    .applyOutcomeFilter(filtersValue[OUTCOME_FILTER])
                    .applyTransferFilter(filtersValue[TRANSFER_FILTER])
            }
            .combine(categoriesFiltersValue) { filteredTransactions, filtersValue ->
                LoadedTransactionDetailsState(
                    filteredTransactions.applyCategoriesFilter(filtersValue)
                )
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

    private val personSummaryState: LiveData<LoadedPersonSummaryState> =
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
                    LoadedPersonSummaryState.from(
                        principalPersonWithAccounts,
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

    fun updateRange(startDate: LocalDate?, endDate: LocalDate?) {
        range.value = Pair(startDate, endDate)
    }

    fun insertPerson(vararg person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.insertPerson(*person)
        }

    fun updatePerson(vararg person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.updatePerson(*person)
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
        accountFilters: BooleanFilters<String, Nothing>,
        accountCategoryFilters: BooleanFilters<Int?, Pair<String, Int>>
    ) {
        if (newId != accountDetailId.value) {
            accountDetailId.value = newId
        }
        if (accountFilters != accountFilterValue.value) {
            accountFilterValue.value = accountFilters
        }
        if (accountCategoryFilters != accountCategoryFilterValue.value) {
            accountCategoryFilterValue.value = accountCategoryFilters
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

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllData()
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

        suspend fun List<TransactionListItemDetails>.applyCategoriesFilter(
            filters: BooleanFilters<Int?, Pair<String, Int>>
        ) = withContext(Dispatchers.Default) {
            filter { transaction ->
                filters.values.getOrDefault(transaction.category?.id, filters.defaultValue)
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val settings: Settings,
    private val resultLauncherSaveTransaction: ActivityResultLauncher<String>,
    private val resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                repository,
                settings,
                resultLauncherSaveTransaction,
                resultLauncherOpenDocument
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}