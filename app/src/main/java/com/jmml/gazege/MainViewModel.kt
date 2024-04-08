package com.jmml.gazege

import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.jmml.gazege.core.AppRepository
import com.jmml.gazege.core.entities.Account
import com.jmml.gazege.core.entities.AccountAndOwner
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactions
import com.jmml.gazege.core.entities.AccountAndOwnerWithTransactionsAndPockets
import com.jmml.gazege.core.entities.Budget
import com.jmml.gazege.core.entities.BudgetAndCategoryWithTransactions
import com.jmml.gazege.core.entities.BudgetType
import com.jmml.gazege.core.entities.BudgetWithCalculatedData
import com.jmml.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithCalculatedData
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.CategoryWithTransactions
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PersonWithAccounts
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndAccounts
import com.jmml.gazege.core.entities.TransactionAndAccountsAndCategory
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.core.entities.TransactionType
import com.jmml.gazege.core.entities.flattenWithLevel
import com.jmml.gazege.core.export.readAccountFromCsv
import com.jmml.gazege.core.export.readBudgetFromCsv
import com.jmml.gazege.core.export.readCategoryFromCsv
import com.jmml.gazege.core.export.readPersonsFromCsv
import com.jmml.gazege.core.export.readTransactionsFromCsv
import com.jmml.gazege.core.export.writeAccounts
import com.jmml.gazege.core.export.writeBudget
import com.jmml.gazege.core.export.writeCategories
import com.jmml.gazege.core.export.writeCategoriesWithCalculatedData
import com.jmml.gazege.core.export.writePersons
import com.jmml.gazege.core.export.writeTransactions
import com.jmml.gazege.core.export.writeZipBackup
import com.jmml.gazege.extensions.coroutines.safeLaunch
import com.jmml.gazege.ui.Settings
import com.jmml.gazege.ui.fragments.EditarCategoriasShowType
import com.jmml.gazege.ui.navigation.EditarCategoriasState
import com.jmml.gazege.ui.navigation.LoadedEditarCategoriasState
import com.jmml.gazege.ui.navigation.LoadedPersonSummaryState
import com.jmml.gazege.ui.navigation.LoadedTransactionDetailsState
import com.jmml.gazege.ui.navigation.LoadingTransactionsDetailsState
import com.jmml.gazege.ui.navigation.PersonSummaryState
import com.jmml.gazege.ui.navigation.ReloadingPersonSummaryState
import com.jmml.gazege.ui.navigation.loadingPersonSummaryState
import com.jmml.gazege.ui.navigation.nullCategoriasState
import com.jmml.gazege.ui.progressStatus.HistoricalProgressStatus
import com.jmml.gazege.ui.progressStatus.IProgressStatus
import com.jmml.gazege.ui.progressStatus.Status
import com.jmml.gazege.ui.savers.listStringSaver
import com.jmml.gazege.ui.views.account.AccountDetailData
import com.jmml.gazege.ui.widgets.BooleanFilters
import com.jmml.gazege.ui.widgets.DoubleFilter
import com.jmml.gazege.ui.widgets.INCOME_FILTER
import com.jmml.gazege.ui.widgets.OUTCOME_FILTER
import com.jmml.gazege.ui.widgets.TRANSFER_FILTER
import com.jmml.gazege.ui.widgets.TextFilter
import com.jmml.gazege.ui.widgets.booleanFilterOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.util.Locale
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.collections.set

enum class NavPosition {
    PERSONS, CUENTAS, TRANSACCIONES, CATEGORIAS
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
    private val resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>,
    private val resultLauncherExportDetails: ActivityResultLauncher<String>
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

    fun startActivityToExportDetails(
        suggestedName: String,
        categoryId: Int
    ) {
        settingsCategoryIdToExportFlow(categoryId)
        resultLauncherExportDetails.launch(suggestedName)
    }

    fun startActivityToLoadData() =
        resultLauncherOpenDocument.launch(arrayOf("*/*"))

    fun setShowOnBoarding(value: Boolean) =
        viewModelScope.launch { settings.setShowOnBoarding(value) }

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

    private val today = MutableLiveData(LocalDate.now())

    fun updateToday(newDate: LocalDate) {
        today.value = newDate
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

    private inline fun <reified A, reified B, reified X, reified Y : X> LiveData<A>.combine(
        otherSource: LiveData<B>,
        crossinline whileUpdating: () -> Y,
        crossinline merger: suspend (A, B) -> Y
    ) = MediatorLiveData<X>()
        .apply {
            val update = {
                postValue(whileUpdating())
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
    val categoryIdToExportFlow =
        settings.getCategoryIdToExportFlow().asLiveData()
    val useDynamicColor = settings.getUseDynamicColor().asLiveData()
    val showOnBoarding = settings.getShowOnBoardingFlow().asLiveData()
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

    private val initialRange = today.map { today ->
        today.withDayOfMonth(1).let {
            Pair(it, it.plusMonths(1L).minusDays(1L))
        }
    }

    private val variableRange = MutableLiveData<Pair<LocalDate?, LocalDate?>?>(null)

    private val range: LiveData<Pair<LocalDate?, LocalDate?>> = initialRange
        .combine(variableRange) { initialRange, variableRange ->
            variableRange ?: initialRange
        }

    private val budgetWithCalculatedData: LiveData<List<BudgetWithCalculatedData>> =
        budgetAndCategoryWithTransactions
            .combine(range) { budgetAndCategoryWithTransactions, range ->
                object {
                    val budgetAndCategoryWithTransactions = budgetAndCategoryWithTransactions
                    val range = range
                }
            }
            .combine(today) { combined, today ->
                val range = combined.range
                val budgetAndCategoryWithTransactions = combined.budgetAndCategoryWithTransactions
                val startDate = range.first
                val endDate = range.second
                if (startDate != null && endDate != null) {
                    BudgetWithCalculatedData.from(
                        budgetAndCategoryWithTransactions,
                        today,
                        startDate,
                        endDate
                    )
                } else {
                    emptyList()
                }
            }

    private val categoryWithCalculatedData: LiveData<List<CategoryWithCalculatedData>> =
        categoryWithTransactions
            .combine(range) { categoryWithTransactions, range ->
                object {
                    val categoryWithTransactions = categoryWithTransactions
                    val range = range
                }
            }
            .combine(today) { combined, today ->
                val range = combined.range
                val categoryWithTransactions = combined.categoryWithTransactions
                val startDate = range.first
                val endDate = range.second
                if (startDate != null && endDate != null) {
                    CategoryWithCalculatedData.from(
                        categoryWithTransactions = categoryWithTransactions,
                        currentDate = today,
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

    val categoryWithSubcategoriesAndBudgetWithCalculatedData: LiveData<List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>> =
        budgetWithCalculatedDataAndCategory.combine(categoriesWithSubCategories) { budgetWithCalculatedDataAndCategory, categoriesWithSubcategories ->
            object {
                val budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory
                val categoriesWithSubcategories = categoriesWithSubcategories
            }
        }.combine(categoryWithCalculatedData) { combined, categoryWithCalculatedData ->
            CategoryWithSubcategoriesAndBudgetWithCalculatedData.from(
                budgetWithCalculatedDataAndCategory = combined.budgetWithCalculatedDataAndCategory,
                categoriesWithSubcategories = combined.categoriesWithSubcategories,
                categoriesWithCalculatedData = categoryWithCalculatedData.associateBy {
                    it.category.id ?: 0
                }
            )
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

    fun importStatePostValue(importState: ProgressStatusState) {
        loadingDataState.postValue(importState.toState(importState.type))
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

    fun updateValueFilterValue(newValue: DoubleFilter) {
        valueFilterValue.value = newValue
    }

    fun updateDescriptionFilterValue(newValue: TextFilter) {
        descriptionFilterValue.value = newValue
    }

    private val incomeAccount = allAccount.map { accounts -> getIncomeAccount(accounts) }
    private val outcomeAccount = allAccount.map { accounts -> getOutcomeAccount(accounts) }

    private val rangeTransactions = range.switchMap { range ->
        repository.getTransactions(range.first, range.second).asLiveData()
    }
    private val transactionAmountRangeValue =
        rangeTransactions.map {
            val amountList = it.map { transaction -> transaction.amount }.distinct()
            val max = (amountList.maxOrNull() ?: 0.0).toFloat()
            val min = (amountList.minOrNull() ?: 0.0).toFloat()
            min..max
        }
    private val valueFilterValue: MutableLiveData<DoubleFilter> =
        MediatorLiveData<DoubleFilter>()
            .apply {
                addSource(transactionAmountRangeValue) {
                    val oldValue = value?.value
                    value = DoubleFilter(
                        value = oldValue,
                        range = it
                    )
                }
            }
    private val descriptionFilterValue: MutableLiveData<TextFilter> =
        MutableLiveData(TextFilter(null))
    private val accountDetailId = MutableLiveData<Int?>(null)
    private val accountDetail = accountAndOwnerWithTransactionsAndPockets
        .combine(accountDetailId) { accountAndOwnerWithTransactionsAndPockets, accountDetailId ->
            accountAndOwnerWithTransactionsAndPockets.firstOrNull {
                it.accountAndOwnerWithTransactions.account.id == accountDetailId
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
                filteredTransactions.applyCategoriesFilter(filtersValue)
            }
            .combine(valueFilterValue) { filteredTransactions, valueFilterValue ->
                valueFilterValue?.let {
                    filteredTransactions.applyValueFilter(valueFilterValue)
                } ?: filteredTransactions
            }
            .combine(descriptionFilterValue) { filteredTransactions, descriptionFilterValue ->
                LoadedTransactionDetailsState(
                    filteredTransactions.applyDescriptionFilter(descriptionFilterValue)
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

    private var cachedPersonSummaryState: PersonSummaryState = loadingPersonSummaryState()

    private val personSummaryState: LiveData<PersonSummaryState> =
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
            .combine(categoryWithSubcategoriesAndBudgetWithCalculatedData) { combined, budgetWithCalculatedDataAndCategory ->
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
            .combine(
                incluirDeudasEnSaldoActual,
                whileUpdating = { cachedPersonSummaryState }
            ) { combined, incluirDeudasEnSaldoActual ->
                combined.run {
                    val summaryState = LoadedPersonSummaryState.from(
                        principalPersonWithAccounts,
                        range.first,
                        range.second,
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
                    cachedPersonSummaryState = ReloadingPersonSummaryState.from(summaryState)
                    summaryState
                }
            }

    fun updateRange(startDate: LocalDate?, endDate: LocalDate?) {
        variableRange.value = Pair(startDate, endDate)
    }

    fun insertPerson(vararg person: Person, onErrorAction: (Throwable) -> Unit) =
        viewModelScope.safeLaunch(onErrorAction) {
            repository.insertPerson(*person)
        }

    suspend fun insertPerson(person: Person): List<Long> = repository.insertPerson(person)

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
        outcomeAccountId: Int,
        today: LocalDate
    ) = viewModelScope.launch {
        if (amount != 0.0) {
            val transaccionAjuste = if (amount > 0) {
                Transaction(
                    amount = amount,
                    description = "Ajuste",
                    sourceId = incomeAccountId,
                    destinationId = accountId,
                    date = today,
                    aNombreDe = null,
                    categoryId = null
                )
            } else {
                Transaction(
                    amount = -amount,
                    description = "Ajuste",
                    sourceId = accountId,
                    destinationId = outcomeAccountId,
                    date = today,
                    aNombreDe = null,
                    categoryId = null
                )
            }
            repository.insertTransaction(transaccionAjuste)
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

    fun settingsCategoryIdToExportFlow(newValue: Int) = viewModelScope.launch {
        settings.setCategoryIdToExportFlow(newValue)
    }

    fun settingsUseDynamicColorFlow(newValue: Boolean) = viewModelScope.launch {
        settings.setUseDynamicColor(newValue)
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

    inner class ExportModule {
        private suspend fun getTransactions() =
            repository.getTransactions(null, null)
                .firstOrNull()
                ?: emptyList()

        private suspend fun getPersons() =
            repository.getPersons()
                .firstOrNull()
                ?: emptyList()

        private suspend fun getCategories() =
            repository.getCategories()
                .firstOrNull()
                ?: emptyList()

        private suspend fun getAccounts() =
            repository.getAccounts()
                .firstOrNull()
                ?: emptyList()

        private suspend fun getBudget() =
            repository.getBudgets()
                .firstOrNull()
                ?: emptyList()

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
                    val accountsInputStream =
                        ByteArrayInputStream(accountsOutputStream.toByteArray())
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

        fun exportDetails(
            outputStream: OutputStream,
            categoryToExport: CategoryWithSubcategoriesAndBudgetWithCalculatedData
        ) {
            val progressStatus = HistoricalProgressStatus.start(
                "Exporting category...",
                totalWork = 8.0,
                defaultIncrement = 1.0
            ) { loadingDataState.postValue(it.toState(Type.EXPORT)) }
            viewModelScope.safeLaunch(
                onErrorAction = {
                    progressStatus.error("Error: ${it.message}")
                }
            ) {
                progressStatus.incrementProgress("outputStream use")
                outputStream.use {
                    progressStatus.incrementProgress("writing categories")
                    writeCategoriesWithCalculatedData(it, listOf(categoryToExport))
                    progressStatus.incrementProgress("categories writted")
                }
                progressStatus.finish("Finished")
            }
        }
    }

    inner class SampleModule {
        val viewModelScope get() = this@MainViewModel.viewModelScope
        fun importStatePostValue(importState: ProgressStatusState) =
            this@MainViewModel.importStatePostValue(importState)

        fun deleteAll() = this@MainViewModel.deleteAll()

        fun insertPerson(vararg person: Person, onErrorAction: (Throwable) -> Unit) =
            this@MainViewModel.insertPerson(*person) { onErrorAction(it) }

        fun insertAccount(
            vararg account: Account,
            onErrorAction: (Throwable) -> Unit,
            onCompleitionAction: (Long) -> Unit
        ) =
            this@MainViewModel.insertAccount(
                *account,
                onErrorAction = onErrorAction,
                onCompleitionAction = onCompleitionAction
            )

        fun insertCategory(
            vararg category: Category,
            onCompleitionAction: (Long?) -> Unit,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.insertCategory(
                *category,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction
            )

        fun updateCategory(
            vararg newCategory: Category,
            onCompleitionAction: () -> Unit,
            onErrorAction: (Throwable) -> Unit,
        ) =
            this@MainViewModel.updateCategory(
                *newCategory,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction
            )

        fun insertTransaction(
            vararg transaction: Transaction,
            onErrorAction: (Throwable) -> Unit = {}
        ) =
            this@MainViewModel.insertTransaction(
                *transaction,
                onErrorAction = onErrorAction
            )

        fun insertBudget(
            vararg budget: Budget,
            onCompleitionAction: () -> Unit,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.insertBudget(
                *budget,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction
            )
    }

    inner class ViewModelInitial {
        @Composable
        fun rememberShowOnBoarding() = showOnBoarding.observeAsState()

        @Composable
        fun rememberPrincipalPersonId() = principalPerson
            .map { it?.id }
            .observeAsState(-1)

        @Composable
        fun rememberPrincipalAccounts(principalPersonId: Int?) = remember(principalPersonId) {
            allAccount
                .map { it.filter { account -> account.ownerId == principalPersonId } }
        }
            .observeAsState()

        fun setShowOnBoarding(value: Boolean) = this@MainViewModel.setShowOnBoarding(value)
    }

    inner class ViewModelOnBoarding {
        @Composable
        fun rememberMainPersonName(currentPersonName: String?) =
            rememberSaveable(currentPersonName) { mutableStateOf(currentPersonName) }

        @Composable
        fun rememberSelfAccountNames(selfAccounts: List<String>) = rememberSaveable(
            selfAccounts,
            saver = listStringSaver
        ) { mutableStateListOf(*selfAccounts.toTypedArray()) }

        @Composable
        fun rememberCategoriesNames(currentCategories: List<String>) = rememberSaveable(
            currentCategories,
            saver = listStringSaver
        ) {
            mutableStateListOf(*currentCategories.toTypedArray())
        }

        @Composable
        fun rememberNewPersonNames(nonPrincipalPersons: List<String>) = rememberSaveable(
            nonPrincipalPersons,
            saver = listStringSaver
        ) {
            mutableStateListOf(*nonPrincipalPersons.toTypedArray())
        }

        @Composable
        fun rememberIncomeAccount() = incomeAccount.observeAsState()

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

        @Composable
        fun rememberAllPerson() = allPerson.observeAsState()

        @Composable
        fun rememberAllAccounts() = allAccount.observeAsState()

        @Composable
        fun rememberAllCategories() = categories.observeAsState()

        @Composable
        fun rememberPrincipalPerson() = principalPerson.observeAsState()

        fun onBoardingFinished(
            allPerson: List<Person>,
            allAccounts: List<Account>,
            allCategories: List<Category>,
            incomeAccount: Account?,
            outcomeAccount: Account?,
            mainPersonName: String,
            selfAccountsNames: List<String>,
            categoryNames: List<String>,
            personNames: List<String>
        ) = viewModelScope.launch {
            val principalPersonId = createPrincipalPerson(mainPersonName, allPerson)
            if (principalPersonId != null) {
                createAccounts(selfAccountsNames, principalPersonId, allAccounts)
            }
            createCategories(categories = categoryNames, allCategories)
            createPerson(
                personNames = personNames,
                allPerson = allPerson
                    .map { it.name }
                    .plus(mainPersonName))
            createIncomeOutcomeAccounts(
                allPersonNames = allPerson
                    .map { it.name }
                    .plus(mainPersonName)
                    .toSet(),
                currentIncomeAccount = incomeAccount,
                currentOutComeAccount = outcomeAccount
            )
        }

        private suspend fun createPrincipalPerson(
            personName: String,
            allPerson: List<Person>
        ): Int? {
            val currentPrincipalPerson = getPrincipalPerson(allPerson)
            var personToAdd: Int? = currentPrincipalPerson?.id
            if (currentPrincipalPerson?.name != personName) {
                if (currentPrincipalPerson != null) {
                    updatePerson(currentPrincipalPerson.copy(importance = null)) {}
                }
                val newPrincipalPerson = allPerson.firstOrNull { it.name == personName }
                personToAdd = if (newPrincipalPerson == null) {
                    insertPerson(Person(name = personName, importance = 1)).firstOrNull()?.toInt()
                        ?: -1
                } else {
                    updatePerson(newPrincipalPerson.copy(importance = 1)) {}
                    newPrincipalPerson.id
                }
            }
            return personToAdd
        }

        private fun createAccounts(
            accounts: List<String>,
            principalPersonId: Int,
            allAccounts: List<Account>
        ) {
            val allAccountsNames = allAccounts.map { it.name }.toMutableSet()
            accounts.forEach { accountName ->
                if (accountName !in allAccountsNames) {
                    allAccountsNames.add(accountName)
                    insertAccount(
                        Account(name = accountName, ownerId = principalPersonId),
                        onErrorAction = {},
                        onCompleitionAction = {}
                    )
                }
            }
        }

        private fun createCategories(
            categories: List<String>,
            allCategories: List<Category>
        ) {
            val allCategoriesNames = allCategories.map { it.name }.toMutableSet()
            categories.forEach { categoryName ->
                if (categoryName !in allCategoriesNames) {
                    allCategoriesNames.add(categoryName)
                    insertCategory(
                        Category(
                            name = categoryName,
                            budgetType = BudgetType.FIXED,
                            parentId = null
                        ),
                        onCompleitionAction = {},
                        onErrorAction = {}
                    )
                }
            }
        }

        private suspend fun createPerson(
            personNames: List<String>,
            allPerson: List<String>
        ) {
            val allPersonNames = allPerson.toMutableSet()
            personNames.forEach { personName ->
                if (personName !in allPersonNames) {
                    allPersonNames.add(personName)
                    val personId = insertPerson(Person(name = personName)).firstOrNull()
                    if (personId != null) {
                        insertAccount(
                            Account(name = personName, ownerId = personId.toInt()),
                            onCompleitionAction = {},
                            onErrorAction = {}
                        )
                    }
                }
            }
        }

        private suspend fun createIncomeOutcomeAccounts(
            allPersonNames: Set<String>,
            currentIncomeAccount: Account?,
            currentOutComeAccount: Account?
        ) {
            if (currentIncomeAccount == null || currentOutComeAccount == null) {
                var especialPersonName = "__ESPECIAL__"
                var index = 0
                while (especialPersonName in allPersonNames) {
                    especialPersonName = "__ESPECIAL__ $index"
                    index += 1
                }
                val especialPersonId = insertPerson(Person(name = especialPersonName)).firstOrNull()
                especialPersonId?.let {
                    if (currentIncomeAccount == null) {
                        insertAccount(
                            Account(
                                name = "__INCOME__",
                                ownerId = especialPersonId.toInt(),
                                isIncome = true
                            ),
                            onCompleitionAction = {},
                            onErrorAction = {}
                        )
                    }
                    if (currentOutComeAccount == null) {
                        insertAccount(
                            Account(
                                name = "__OUTCOME__",
                                ownerId = especialPersonId.toInt(),
                                isOutcome = true
                            ),
                            onCompleitionAction = {},
                            onErrorAction = {}
                        )
                    }
                }
            }
        }

        fun onShowOnBoardingChanged(value: Boolean) = setShowOnBoarding(value)
    }

    inner class ViewModelMain {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.observeAsState(emptyList())

        @Composable
        fun rememberFilteredTransactionListItemDetails() =
            filteredTransactionListitemDetails.observeAsState(LoadingTransactionsDetailsState)

        @Composable
        fun rememberPersonSummaryState() =
            personSummaryState.observeAsState(loadingPersonSummaryState())

        @Composable
        fun rememberRange() = range.observeAsState(Pair(null, null))

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
        fun rememberValueFilterValue() =
            valueFilterValue.observeAsState(DoubleFilter(0.0f..0.0f, 0.0f..0.0f))

        @Composable
        fun rememberDescriptionFilterValue() =
            descriptionFilterValue.observeAsState(TextFilter(null))

        @Composable
        fun rememberToday(): State<LocalDate> = today.observeAsState(initial = LocalDate.now())

        fun deletePerson(person: Person) = this@MainViewModel.deletePerson(person)

        fun deleteAccount(account: Account) = this@MainViewModel.deleteAccount(account)

        fun deleteTransaction(transaction: Transaction) =
            this@MainViewModel.deleteTransaction(transaction)

        fun updateRange(startDate: LocalDate?, endDate: LocalDate?) =
            this@MainViewModel.updateRange(startDate, endDate)

        fun updatePersonFilterValue(newValue: Boolean) =
            this@MainViewModel.updatePersonFilterValue(newValue)

        fun updateTransactionFilters(newValue: BooleanFilters<String, Nothing>) =
            this@MainViewModel.updateTransactionFilters(newValue)

        fun updateCategoriasFiltersValue(newValue: BooleanFilters<Int?, Pair<String, Int>>) =
            this@MainViewModel.updateCategoriasFiltersValue(newValue)

        fun updateValueFilterValue(newValue: DoubleFilter) =
            this@MainViewModel.updateValueFilterValue(newValue)

        fun updateDescriptionFilterValue(newValue: TextFilter) =
            this@MainViewModel.updateDescriptionFilterValue(newValue)

        fun updateToday(newDate: LocalDate) = this@MainViewModel.updateToday(newDate)
    }

    inner class ViewModelCategoryList {
        private val _showPlot: MutableLiveData<EditarCategoriasShowType> =
            MutableLiveData(EditarCategoriasShowType.COMPACT)

        @Composable
        fun rememberShowType() =
            _showPlot.observeAsState(initial = EditarCategoriasShowType.COMPACT)

        @Composable
        fun rememberEditarCategoriasState() =
            editarCategoriasState.observeAsState(nullCategoriasState())

        fun updateShowType(newValue: EditarCategoriasShowType) = _showPlot.postValue(newValue)

        fun deleteCategory(category: Category) = this@MainViewModel.deleteCategory(category)
    }

    inner class ViewModelAddAccount {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(initial = emptyList())

        @Composable
        fun rememberAllAccount() = allAccount.observeAsState(emptyList())

        @Composable
        fun rememberIncomeAccount() = incomeAccount.observeAsState()

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.observeAsState(emptyList())

        @Composable
        fun rememberToday() = today.observeAsState(LocalDate.now())

        fun insertAccount(
            vararg account: Account,
            onErrorAction: (Throwable) -> Unit,
            onCompleitionAction: (Long) -> Unit
        ) =
            this@MainViewModel.insertAccount(
                *account,
                onErrorAction = onErrorAction,
                onCompleitionAction = onCompleitionAction
            )

        fun realizarAjuste(
            accountId: Int,
            amount: Double,
            incomeAccountId: Int,
            outcomeAccountId: Int,
            today: LocalDate
        ) =
            this@MainViewModel.realizarAjuste(
                accountId = accountId,
                amount = amount,
                incomeAccountId = incomeAccountId,
                outcomeAccountId = outcomeAccountId,
                today = today
            )
    }

    inner class ViewModelAddPerson {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        fun insertPerson(
            vararg person: Person,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.insertPerson(
                *person,
                onErrorAction = onErrorAction
            )
    }

    inner class ViewModelAddTransaction {
        @Composable
        fun rememberIncomeAccount() = incomeAccount.observeAsState()

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberAllAccount() = allAccount.observeAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.observeAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

        @Composable
        fun rememberAccountAndOwner() = accountAndOwner.observeAsState(emptyList())

        @Composable
        fun rememberAccountAndOwnerUserFirst() =
            accountAndOwnerUserFirst.observeAsState(emptyList())

        fun insertTransaction(
            vararg transaction: Transaction,
            onErrorAction: (Throwable) -> Unit = {}
        ) = this@MainViewModel.insertTransaction(
            *transaction,
            onErrorAction = onErrorAction
        )
    }

    inner class ViewModelEditAccount {
        @Composable
        fun rememberAccountAndOwnerWithTransactionsAndPockets() =
            accountAndOwnerWithTransactionsAndPockets.observeAsState(emptyList())

        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberAllAccount() = allAccount.observeAsState(emptyList())

        @Composable
        fun rememberIncomeAccount() = incomeAccount.observeAsState()

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.observeAsState(emptyList())

        @Composable
        fun rememberToday() = today.observeAsState(LocalDate.now())

        fun updateAccount(
            account: Account,
            onErrorAction: (Throwable) -> Unit,
            onCompleitionAction: (Long) -> Unit
        ) =
            this@MainViewModel.updateAccount(
                account = account,
                onErrorAction = onErrorAction,
                onCompleitionAction = onCompleitionAction
            )

        fun realizarAjuste(
            accountId: Int,
            amount: Double,
            incomeAccountId: Int,
            outcomeAccountId: Int,
            today: LocalDate
        ) =
            this@MainViewModel.realizarAjuste(
                accountId = accountId,
                amount = amount,
                incomeAccountId = incomeAccountId,
                outcomeAccountId = outcomeAccountId,
                today = today
            )
    }

    inner class ViewModelEditPerson {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        fun updatePerson(
            vararg person: Person,
            onErrorAction: (Throwable) -> Unit
        ) = this@MainViewModel.updatePerson(
            *person,
            onErrorAction = onErrorAction
        )
    }

    inner class ViewModelEditTransaction {
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
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.observeAsState(emptyList())

        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.observeAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

        fun updateTransaction(transaction: Transaction) =
            this@MainViewModel.updateTransaction(transaction)
    }

    inner class ViewModelSettings {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberPrincipalPerson() = principalPerson.observeAsState()

        @Composable
        fun rememberAccountAndOwner() = accountAndOwner.observeAsState(emptyList())

        @Composable
        fun rememberIncomeAccount() = incomeAccount.observeAsState()

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.observeAsState()

        @Composable
        fun rememberShowOnBoarding() = showOnBoarding.observeAsState()

        @Composable
        fun rememberUseDynamicColor() = useDynamicColor.observeAsState()

        fun updatePerson(
            vararg person: Person,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.updatePerson(
                *person,
                onErrorAction = onErrorAction
            )

        fun updateAccount(
            account: Account,
            onErrorAction: (Throwable) -> Unit,
            onCompleitionAction: (Long) -> Unit
        ) =
            this@MainViewModel.updateAccount(
                account,
                onErrorAction = onErrorAction,
                onCompleitionAction = onCompleitionAction
            )

        fun setShowOnBoarding(value: Boolean) =
            viewModelScope.launch { settings.setShowOnBoarding(value) }

        fun canUseDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        fun setUseDynamicColor(value: Boolean) =
            this@MainViewModel.settingsUseDynamicColorFlow(value)
    }

    inner class ViewModelSaldoActualSettings {
        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.observeAsState(emptyList())

        @Composable
        fun rememberPersonList(principalPersonId: Int?) = remember(principalPersonId) {
            allPerson
                .map { personList ->
                    personList.filter { it.id == null || it.id != principalPersonId }
                }
        }.observeAsState(initial = emptyList())

        @Composable
        fun rememberPersonSummaryState() =
            personSummaryState.observeAsState(loadingPersonSummaryState())

        @Composable
        fun rememberPrincipalPerson() = principalPerson.observeAsState()

        @Composable
        fun rememberSettingsIncluirPresupuestoEnSaldoActualFlow() =
            incluirPresupuestoEnSaldoActual.observeAsState(false)

        @Composable
        fun rememberSettingsIncluirDeudasEnSaldoActualFlow() =
            incluirDeudasEnSaldoActual.observeAsState(false)

        fun settingsIncluirPresupuestoEnSaldoActualFlow(newValue: Boolean) =
            this@MainViewModel.settingsIncluirPresupuestoEnSaldoActualFlow(newValue)

        fun settingsIncluirDeudasEnSaldoActualFlow(newValue: Boolean) =
            this@MainViewModel.settingsIncluirDeudasEnSaldoActualFlow(newValue)

        fun updateAccount(
            account: Account,
            onErrorAction: (Throwable) -> Unit,
            onCompleitionAction: (Long) -> Unit
        ) =
            this@MainViewModel.updateAccount(
                account = account,
                onErrorAction = onErrorAction,
                onCompleitionAction = onCompleitionAction
            )

        fun updatePerson(
            person: Person,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.updatePerson(
                person,
                onErrorAction = onErrorAction
            )
    }

    inner class ViewModelAddCategory {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.observeAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

        fun insertCategory(
            vararg category: Category,
            onCompleitionAction: (Long?) -> Unit,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.insertCategory(
                *category,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction
            )
    }

    inner class ViewModelEditCategory {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.observeAsState(emptyList())

        @Composable
        fun rememberBudgetAndCategoryWithCalculatedData() =
            budgetWithCalculatedDataAndCategory.observeAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

        fun updateCategory(
            vararg newCategory: Category,
            onCompleitionAction: () -> Unit,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.updateCategory(
                *newCategory,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction
            )

        fun deleteBudget(budget: Budget) = this@MainViewModel.deleteBudget(budget)
    }

    inner class ViewModelAccountDetail {
        private val descriptionFilter: MutableLiveData<Pair<Int?, TextFilter>> = MutableLiveData()
        private val accountFilterValue: MutableLiveData<Pair<Int?, BooleanFilters<String, Nothing>>> =
            MutableLiveData()
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
                object {
                    val accountDetail = combined.accountDetail
                    val allAccount = combined.allAccount
                    val categories = combined.categories
                    val budget = combined.budget
                    val range = combined.range
                    val principalPerson = combined.principalPerson
                    val accountFilterValue = combined.accountFilterValue
                    val accountCategoryFilterValue = accountCategoryFilterValue
                }
            }
            .combine(descriptionFilter) { combined, descriptionFilter ->
                combined.run {
                    accountDetail?.let {
                        AccountDetailData.build(
                            account = accountDetail,
                            allAccounts = allAccount,
                            allCategories = categories,
                            budget = budget,
                            startDate = range.first,
                            endDate = range.second,
                            principalPerson = principalPerson,
                            transactionFilters = accountFilterValue.second,
                            categoriesFilter = accountCategoryFilterValue,
                            descriptionFilter = descriptionFilter.second
                        )
                    }
                }
            }

        @Composable
        fun rememberCategoriesFilter(
            accountId: Int?,
            categories: List<CategoryWithSubCategories>
        ) = remember(accountId) {
            mutableStateOf(
                categories
                    .flattenWithLevel()
                    .let { categories ->
                        booleanFilterOf(
                            defaultValue = true,
                            filterNames = categories
                                .map { it.first.id }
                                .plus(null),
                            metadata = categories.associate {
                                (it.first.id ?: 0) to (it.first.name to it.second)
                            }
                                .plus(null to ("" to 0))
                        )
                    }

            )
        }

        @Composable
        fun rememberDescriptionFilter(accountId: Int?) = remember(accountId) {
            descriptionFilter
                .also {
                    if (it.value?.first != accountId) {
                        it.value = accountId to TextFilter(null)
                    }
                }
                .map { it.second }
        }
            .observeAsState(TextFilter(null))

        @Composable
        fun rememberAccountFilterValue(accountId: Int?) = remember(accountId) {
            accountFilterValue
                .also {
                    if (it.value?.first != accountId) {
                        it.value = accountId to booleanFilterOf(
                            listOf(
                                TRANSFER_FILTER, INCOME_FILTER, OUTCOME_FILTER
                            ), true
                        )
                    }
                }
                .map { it.second }
        }
            .observeAsState(
                booleanFilterOf(
                    listOf(
                        TRANSFER_FILTER, INCOME_FILTER, OUTCOME_FILTER
                    ), true
                )
            )

        @Composable
        fun rememberAccountDetailData(
            accountId: Int?,
            accountFilters: BooleanFilters<String, Nothing>,
            accountCategoryFilters: BooleanFilters<Int?, Pair<String, Int>>,
            descriptionFilter: TextFilter
        ): State<AccountDetailData?> {
            updateAccountDetailIdIfDifferent(
                accountId,
                accountFilters,
                accountCategoryFilters,
                descriptionFilter
            )
            return accountDetailData.observeAsState()
        }

        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberAccountAndOwner() = accountAndOwner.observeAsState(emptyList())

        @Composable
        fun rememberCategoriesWithSubcategories() =
            categoriesWithSubCategories.observeAsState(emptyList())

        fun deleteAccount(account: Account) = this@MainViewModel.deleteAccount(account)

        private fun updateAccountDetailIdIfDifferent(
            newId: Int?,
            accountFilters: BooleanFilters<String, Nothing>,
            accountCategoryFilters: BooleanFilters<Int?, Pair<String, Int>>,
            descriptionFilter: TextFilter
        ) {
            if (newId != accountDetailId.value) {
                accountDetailId.value = newId
            }
            if (accountFilters != accountFilterValue.value) {
                accountFilterValue.value = newId to accountFilters
            }
            if (accountCategoryFilters != accountCategoryFilterValue.value) {
                accountCategoryFilterValue.value = accountCategoryFilters
            }
            if (descriptionFilter != descriptionFilterValue.value) {
                descriptionFilterValue.value = descriptionFilter
            }
        }

        fun updateAccountFilter(newValue: BooleanFilters<String, Nothing>) =
            accountFilterValue.apply {
                value = value?.first to newValue
            }

        fun updateDescriptionFilter(newValue: TextFilter) = descriptionFilter.apply {
            value = value?.first to newValue
        }

        fun deleteTransaction(transaction: Transaction) =
            this@MainViewModel.deleteTransaction(transaction)
    }

    inner class ViewModelPersonDetail {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberPersonSummaryState() =
            personSummaryState.observeAsState(loadingPersonSummaryState())

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

        fun deletePerson(person: Person) = this@MainViewModel.deletePerson(person)

        fun deleteTransaction(transaction: Transaction) =
            this@MainViewModel.deleteTransaction(transaction)
    }

    inner class ViewModelEditBudget {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberBudgetAndCategoryWithCalculatedData() =
            budgetWithCalculatedDataAndCategory.observeAsState(emptyList())

        fun deleteBudget(budget: Budget) = this@MainViewModel.deleteBudget(budget)
    }

    inner class ViewModelAddOneBudget {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.observeAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

        fun insertBudget(
            vararg budget: Budget,
            onCompleitionAction: () -> Unit,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.insertBudget(
                *budget,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction,
            )
    }

    inner class ViewModelOneBudgetDetail {
        @Composable
        fun rememberBudgetAndCategoryWithTransactions() =
            budgetAndCategoryWithTransactions.observeAsState(emptyList())
    }

    inner class ViewModelEditOneBudget {
        @Composable
        fun rememberAllPerson() = allPerson.observeAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.observeAsState(emptyList())

        @Composable
        fun rememberBudget() = budget.observeAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.observeAsState(emptyList())

        fun updateBudget(
            budget: Budget,
            onCompleitionAction: () -> Unit,
            onErrorAction: (Throwable) -> Unit
        ) =
            this@MainViewModel.updateBudget(
                budget = budget,
                onCompleitionAction = onCompleitionAction,
                onErrorAction = onErrorAction
            )
    }

    val exportModule = ExportModule()
    val sampleModule = SampleModule()
    val viewModelInitial = ViewModelInitial()
    val viewModelOnBoarding = ViewModelOnBoarding()
    val viewModelMain = ViewModelMain()
    val viewModelCategoryList = ViewModelCategoryList()
    val viewModelAddAccount = ViewModelAddAccount()
    val viewModelAddPerson = ViewModelAddPerson()
    val viewModelAddTransaction = ViewModelAddTransaction()
    val viewModelEditAccount = ViewModelEditAccount()
    val viewModelEditPerson = ViewModelEditPerson()
    val viewModelEditTransaction = ViewModelEditTransaction()
    val viewModelSettings = ViewModelSettings()
    val viewModelSaldoActualSettings = ViewModelSaldoActualSettings()
    val viewModelAddCategory = ViewModelAddCategory()
    val viewModelEditCategory = ViewModelEditCategory()
    val viewModelAccountDetail = ViewModelAccountDetail()
    val viewModelPersonDetail = ViewModelPersonDetail()
    val viewModelEditBudget = ViewModelEditBudget()
    val viewModelAddOneBudget = ViewModelAddOneBudget()
    val viewModelOneBudgetDetail = ViewModelOneBudgetDetail()
    val viewModelEditOneBudget = ViewModelEditOneBudget()

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

        suspend fun List<TransactionListItemDetails>.applyDescriptionFilter(
            descriptionFilter: TextFilter
        ) = withContext(Dispatchers.Default) {
            filter { transaction ->
                val locale = Locale.getDefault()
                val searchTokens = descriptionFilter.value
                    ?.lowercase(locale)
                    ?.split(" ")
                    ?.toSet()
                    ?.map { ".*$it.*".toRegex() }
                val descriptionTokens = transaction.transaction.description
                    .lowercase(locale)
                searchTokens == null ||
                        searchTokens
                            .any { it.containsMatchIn(descriptionTokens) }
            }
        }

        suspend fun List<TransactionListItemDetails>.applyValueFilter(
            filterValue: DoubleFilter
        ) = withContext(Dispatchers.Default) {
            filterValue.value?.let {
                filter { transaction ->
                    filterValue.value.contains(transaction.transaction.amount.toFloat())
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val settings: Settings,
    private val resultLauncherSaveTransaction: ActivityResultLauncher<String>,
    private val resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>,
    private val resultLauncherExportDetails: ActivityResultLauncher<String>
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                repository,
                settings,
                resultLauncherSaveTransaction,
                resultLauncherOpenDocument,
                resultLauncherExportDetails
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}