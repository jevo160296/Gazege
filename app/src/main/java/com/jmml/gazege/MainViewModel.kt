package com.jmml.gazege

import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
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
import com.jmml.gazege.core.entities.ExtendedTransaction
import com.jmml.gazege.core.entities.ITransactionListDetail
import com.jmml.gazege.core.entities.ITransactionListDetailGrouped
import com.jmml.gazege.core.entities.NewTransactionWithDetails
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PersonWithAccounts
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.Transaction
import com.jmml.gazege.core.entities.TransactionAndDetails
import com.jmml.gazege.core.entities.TransactionDetails
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.core.entities.TransactionListItemDetailsWithSign
import com.jmml.gazege.core.entities.TransactionType
import com.jmml.gazege.core.entities.TransactionWithDetails
import com.jmml.gazege.core.entities.TransactionWithDetailsAndAccounts
import com.jmml.gazege.core.entities.TransactionWithDetailsAndAccounts.Companion.toTransactionAndDetailsAndAccounts
import com.jmml.gazege.core.entities.TransactionWithDetailsAndAccountsAndCategory
import com.jmml.gazege.core.entities.flattenWithLevel
import com.jmml.gazege.core.export.readAccountFromCsv
import com.jmml.gazege.core.export.readBudgetFromCsv
import com.jmml.gazege.core.export.readCategoryFromCsv
import com.jmml.gazege.core.export.readPersonsFromCsv
import com.jmml.gazege.core.export.readPromissoryNotesFromCsv
import com.jmml.gazege.core.export.readTransactionsFromCsv
import com.jmml.gazege.core.export.writeAccounts
import com.jmml.gazege.core.export.writeBudget
import com.jmml.gazege.core.export.writeCategories
import com.jmml.gazege.core.export.writeCategoriesWithCalculatedData
import com.jmml.gazege.core.export.writePersons
import com.jmml.gazege.core.export.writePromissoryNotes
import com.jmml.gazege.core.export.writeTransactions
import com.jmml.gazege.core.export.writeZipBackup
import com.jmml.gazege.ui.Settings
import com.jmml.gazege.ui.clockFlow
import com.jmml.gazege.ui.fragments.EditarCategoriasShowType
import com.jmml.gazege.ui.navigation.CurrentCashSettingsState
import com.jmml.gazege.ui.navigation.EmptyPersonSummaryState
import com.jmml.gazege.ui.navigation.FullPersonSummaryState
import com.jmml.gazege.ui.navigation.ICategoriesView
import com.jmml.gazege.ui.navigation.LoadedCategoriesDataView
import com.jmml.gazege.ui.navigation.LoadedCategoriesWithBudgetDataView
import com.jmml.gazege.ui.navigation.LoadedPersonSummaryState
import com.jmml.gazege.ui.navigation.LoadedTransactionDetailsState
import com.jmml.gazege.ui.navigation.LoadingPersonSummaryState
import com.jmml.gazege.ui.navigation.PersonSummaryState
import com.jmml.gazege.ui.navigation.ReloadingCategoriesDataView
import com.jmml.gazege.ui.navigation.ReloadingCategoriesWithBudgetDataView
import com.jmml.gazege.ui.navigation.ReloadingPersonSummaryState
import com.jmml.gazege.ui.navigation.loadingPersonSummaryState
import com.jmml.gazege.ui.progressStatus.HistoricalProgressStatus
import com.jmml.gazege.ui.progressStatus.IProgressStatus
import com.jmml.gazege.ui.progressStatus.Status
import com.jmml.gazege.ui.savers.listStringSaver
import com.jmml.gazege.ui.views.account.AccountDetailData
import com.jmml.gazege.ui.views.category.CompactShow
import com.jmml.gazege.ui.views.document.PromissoryNoteDocumentWithSignViewModel
import com.jmml.gazege.ui.views.document.TransactionDocumentWithSignViewModel
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteViewModel
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteWithSignViewModel
import com.jmml.gazege.ui.widgets.BooleanFilters
import com.jmml.gazege.ui.widgets.DoubleFilter
import com.jmml.gazege.ui.widgets.INCOME_FILTER
import com.jmml.gazege.ui.widgets.OUTCOME_FILTER
import com.jmml.gazege.ui.widgets.PROMISSORY_NOTE_FILTER
import com.jmml.gazege.ui.widgets.TRANSFER_FILTER
import com.jmml.gazege.ui.widgets.TextFilter
import com.jmml.gazege.ui.widgets.booleanFilterOf
import com.jmml.zoo.clases.Result
import com.jmml.zoo.extensions.coroutines.safeLaunch
import com.jmml.zoo.extensions.flow.asResult
import com.jmml.zoo.extensions.flow.collectAsState
import com.jmml.zoo.extensions.flow.shareInViewModel
import com.jmml.zoo.extensions.flow.zDistinctUntilChanged
import com.jmml.zoo.extensions.localdate.isBetween
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
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

fun <T1, T2, R> Flow<T1>.combineDefault(
    flow: Flow<T2>,
    transform: suspend (a: T1, b: T2) -> R
): Flow<R> =
    combine(flow) { value1, value2 ->
        val result = withContext(Dispatchers.Default) { transform(value1, value2) }
        result
    }

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
            categoryWithLevel
                .map { (it.first.id) to (it.first.name to it.second) }
                .plus(null to ("" to 0))
        )
    }

class MainViewModel(
    private val repository: AppRepository,
    private val settings: Settings,
    private val resultLauncherSaveData: ActivityResultLauncher<String>,
    private val resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>,
    private val resultLauncherExportDetails: ActivityResultLauncher<String>,
    private val getLifecycle: () -> Lifecycle
) :
    ViewModel() {
    fun appInitialized(): Boolean {
        val currentValue = appInitialized
        appInitialized = true
        return currentValue
    }

    private val lifecycle get() = getLifecycle()

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

    private val _today = MutableStateFlow(LocalDate.now())
    private val _calendarDay = clockFlow()
        .map { it.toLocalDate() }
        .zDistinctUntilChanged { old, new -> old == new }
        .asResult()
        .transform {
            emit(it)
            emit(Result.Loading)
        }

    private val today = _today
        .combine(_calendarDay) { today, calendarDay ->
            if (calendarDay is Result.Success) _today.updateAndGet { calendarDay.data }
            else today
        }
        .zDistinctUntilChanged { old, new -> old == new }
        .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
        .shareInViewModel()

    fun updateToday(newDate: LocalDate) {
        _today.value = newDate
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

    private var appInitialized = false
    private val incluirPresupuestoEnSaldoActual =
        settings.getIncluirPresupuestoEnSaldoActualFlow().shareInViewModel()
    private val incluirDeudasEnSaldoActual =
        settings
            .getIncluirDeudasEnSaldoActualFlow()
            .distinctUntilChanged()
            .shareInViewModel()
    val categoryIdToExportFlow =
        settings.getCategoryIdToExportFlow().shareInViewModel()
    val useDynamicColor = settings.getUseDynamicColor().shareInViewModel()
    val showOnBoarding = settings.getShowOnBoardingFlow().shareInViewModel()
    val compactShow = settings.getCompactShow().shareInViewModel()
    private val allPerson = repository.getPersons().shareInViewModel()
    private val allAccount = repository.getAccounts().shareInViewModel()
    private val allTransactions = repository.getTransactions(null, null).shareInViewModel()
    private val allPromissoryNotes = repository.getPromissoryNotes().shareInViewModel()
    private val categories = repository.getCategories().shareInViewModel()
    private val budget = repository.getBudgets().shareInViewModel()
    private val principalPerson = allPerson
        .map { persons -> getPrincipalPerson(persons) }
        .shareInViewModel()

    private val accountAndOwner: SharedFlow<List<AccountAndOwner>> =
        allAccount
            .combineDefault(allPerson) { allAccount, allPerson ->
                AccountAndOwner.from(
                    allAccount,
                    allPerson
                )
            }
            .shareInViewModel()

    private val accountAndOwnerWithTransactions: SharedFlow<Result<List<AccountAndOwnerWithTransactions>>> =
        allAccount
            .combineDefault(allPerson) { allAccount, allPerson ->
                object {
                    val allAccount = allAccount
                    val allPerson = allPerson
                }
            }
            .combineDefault(allTransactions) { combined, allTransactions ->
                combined.run {
                    Result.Success(
                        AccountAndOwnerWithTransactions.from(
                            allAccount,
                            allPerson,
                            allTransactions
                        )
                    )
                }
            }
            .shareInViewModel()

    private val accountAndOwnerUserFirst: SharedFlow<List<AccountAndOwner>> =
        accountAndOwner
            .map { it.sortedByDescending { acc -> acc.owner.importance } }
            .shareInViewModel()
    private val accountAndOwnerWithTransactionsAndPockets: SharedFlow<List<AccountAndOwnerWithTransactionsAndPockets>> =
        accountAndOwnerWithTransactions.mapNotNull { result ->
            if (result is Result.Success) {
                val lista = result.data
                lista.map { item ->
                    AccountAndOwnerWithTransactionsAndPockets.from(
                        item,
                        lista
                    )
                }
            } else null
        }
            .shareInViewModel()
    private val personWithAccounts: SharedFlow<List<PersonWithAccounts>> =
        allPerson
            .combineDefault(accountAndOwnerWithTransactionsAndPockets) { allPerson, accountAndOwnerWithTransactionsAndPockets ->
                PersonWithAccounts.from(allPerson, accountAndOwnerWithTransactionsAndPockets)
            }
            .shareInViewModel()
    private val categoriesWithSubCategories: SharedFlow<List<CategoryWithSubCategories>> =
        categories
            .map { CategoryWithSubCategories.from(it) }
            .shareInViewModel()

    private val budgetAndCategoryWithTransactions: SharedFlow<List<BudgetAndCategoryWithTransactions>> =
        budget
            .combineDefault(categories) { budget, categories ->
                object {
                    val budget = budget
                    val categories = categories
                }
            }
            .combineDefault(accountAndOwnerWithTransactions) { combined, accountAndOwnerWithTransactions ->
                object {
                    val budget = combined.budget
                    val categories = combined.categories
                    val accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                }
            }
            .combineDefault(principalPerson) { combined, principalPerson ->
                combined.run {
                    if (principalPerson == null) {
                        emptyList()
                    } else {
                        if (accountAndOwnerWithTransactions is Result.Success) {
                            BudgetAndCategoryWithTransactions.from(
                                budget = budget,
                                category = categories,
                                person = principalPerson,
                                accountAndOwnerWithTransactions = accountAndOwnerWithTransactions.data
                            )
                        } else null
                    }
                }
            }
            .filterNotNull()
            .shareInViewModel()

    private val initialRange = today.map { today ->
        today.withDayOfMonth(1).let {
            Pair(it, it.plusMonths(1L).minusDays(1L))
        }
    }

    private val variableRange = MutableStateFlow<Pair<LocalDate?, LocalDate?>?>(null)

    private val range: SharedFlow<Pair<LocalDate?, LocalDate?>> = initialRange
        .combineDefault(variableRange) { initialRange, variableRange ->
            variableRange ?: initialRange
        }
        .shareInViewModel()

    private val budgetWithCalculatedData: SharedFlow<Result<List<BudgetWithCalculatedData>?>> =
        budgetAndCategoryWithTransactions
            .combine(range) { budgetAndCategoryWithTransactions, range ->
                object {
                    val budgetAndCategoryWithTransactions = budgetAndCategoryWithTransactions
                    val range = range
                }
            }
            .combineTransform(today) { combined, today ->
                emit(Result.Loading)
                val result = withContext(Dispatchers.Default) {
                    val range = combined.range
                    val budgetAndCategoryWithTransactions =
                        combined.budgetAndCategoryWithTransactions
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
                        null
                    }
                }
                emit(Result.Success(result))
            }
            .shareInViewModel()

    private val budgetTransactions: SharedFlow<List<ExtendedTransaction>> =
        repository.getBudgetTransactions(null, null).shareInViewModel()

    private val categoryWithTransactions: SharedFlow<List<CategoryWithTransactions>> =
        categories
            .combineDefault(principalPerson) { categories, principalPerson ->
                object {
                    val categories = categories
                    val principalPerson = principalPerson
                }
            }
            .combineDefault(allAccount) { combined, allAccount ->
                object {
                    val categories = combined.categories
                    val principalPerson = combined.principalPerson
                    val accounts = allAccount
                }
            }
            .combineDefault(budgetTransactions) { combined, budgetTransactions ->
                object {
                    val categories = combined.categories
                    val principalPerson = combined.principalPerson
                    val accounts = combined.accounts
                    val extendedTransactions = budgetTransactions
                }
            }
            .map { combined ->
                if (combined.principalPerson != null) {
                    CategoryWithTransactions.from(
                        category = combined.categories,
                        person = combined.principalPerson,
                        accounts = combined.accounts,
                        extendedTransactions = combined.extendedTransactions
                    )
                } else {
                    emptyList()
                }
            }
            .filterNotNull()
            .shareInViewModel()

    private val categoryWithCalculatedData: SharedFlow<Result<List<CategoryWithCalculatedData>?>> =
        categoryWithTransactions
            .combine(range) { categoryWithTransactions, range ->
                object {
                    val categoryWithTransactions = categoryWithTransactions
                    val range = range
                }
            }
            .combineTransform(today) { combined, today ->
                emit(Result.Loading)
                val result = withContext(Dispatchers.Default) {
                    val range = combined.range
                    val categoryWithTransactions = combined.categoryWithTransactions
                    val startDate = range.first
                    val endDate = range.second
                    Result.Success(
                        if (startDate != null && endDate != null) {
                            CategoryWithCalculatedData.from(
                                categoryWithTransactions = categoryWithTransactions,
                                currentDate = today,
                                startDate = startDate,
                                endDate = endDate
                            )
                        } else {
                            null
                        }
                    )
                }
                emit(result)
            }
            .shareInViewModel()

    private val budgetWithCalculatedDataAndCategory: SharedFlow<Result<List<BudgetWithCalculatedDataAndCategory>?>> =
        budgetWithCalculatedData
            .combineDefault(categories) { budgetWithCalculatedData, categories ->
                when (budgetWithCalculatedData) {
                    is Result.Success -> Result.Success(
                        budgetWithCalculatedData.data?.let {
                            BudgetWithCalculatedDataAndCategory.from(
                                it,
                                categories
                            )
                        }
                    )

                    is Result.Error -> budgetWithCalculatedData
                    Result.Loading -> Result.Loading
                }
            }
            .shareInViewModel()

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryWithSubcategoriesAndBudgetWithCalculatedData: SharedFlow<Result<List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>?>> =
        categoryWithCalculatedData
            .combine(budgetWithCalculatedDataAndCategory) { categoryWithCalculatedData, budgetWithCalculatedDataAndCategory ->
                object {
                    val categoryWithCalculatedData = categoryWithCalculatedData
                    val budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory
                }
            }
            .combine(categoriesWithSubCategories) { combined, categoriesWithSubcategories ->
                object {
                    val categoryWithCalculatedData = combined.categoryWithCalculatedData
                    val budgetWithCalculatedDataAndCategory =
                        combined.budgetWithCalculatedDataAndCategory
                    val categoriesWithSubcategories = categoriesWithSubcategories
                }
            }
            .mapLatest { combined ->
                if (combined.categoryWithCalculatedData is Result.Success && combined.budgetWithCalculatedDataAndCategory is Result.Success) {
                    val categoryWithCategoryDataData = combined.categoryWithCalculatedData.data
                    val budgetWithCalculatedDataAndCategoryData =
                        combined.budgetWithCalculatedDataAndCategory.data
                    if (categoryWithCategoryDataData != null && budgetWithCalculatedDataAndCategoryData != null) {
                        Result.Success(
                            CategoryWithSubcategoriesAndBudgetWithCalculatedData.from(
                                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategoryData,
                                categoriesWithSubcategories = combined.categoriesWithSubcategories,
                                categoriesWithCalculatedData = categoryWithCategoryDataData.associateBy {
                                    it.category.id ?: 0
                                }
                            )
                        )
                    } else {
                        Result.Success(null)
                    }
                } else Result.Loading
            }
            .shareInViewModel()

    private var cachedReloadingCategoriesWithBudgetDataView: ReloadingCategoriesWithBudgetDataView? =
        null

    private var cachedReloadingCategoriesDataView: ReloadingCategoriesDataView? = null

    private val editarCategoriasState: SharedFlow<Result<ICategoriesView>> =
        budgetWithCalculatedDataAndCategory
            .combineDefault(categoriesWithSubCategories) { budgetWithCalculatedDataAndCategory, categoriesWithSubCategories ->
                object {
                    val budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory
                    val categoriesWithSubCategories = categoriesWithSubCategories
                }
            }
            .combineDefault(categoryWithCalculatedData) { combined, categoryWithCalculatedData ->
                if (categoryWithCalculatedData is Result.Success && combined.budgetWithCalculatedDataAndCategory is Result.Success) {
                    val categoryWithCalculatedDataData = categoryWithCalculatedData.data
                    val budgetWithCalculatedDataAndCategoryData =
                        combined.budgetWithCalculatedDataAndCategory.data

                    if (categoryWithCalculatedDataData != null && budgetWithCalculatedDataAndCategoryData != null) {
                        val categoryWithCalculatedDataMap =
                            categoryWithCalculatedDataData.associateBy { it.category.id ?: 0 }
                        val state = LoadedCategoriesWithBudgetDataView.from(
                            CategoryWithSubcategoriesAndBudgetWithCalculatedData.from(
                                budgetWithCalculatedDataAndCategoryData,
                                combined.categoriesWithSubCategories,
                                categoryWithCalculatedDataMap
                            )
                        )
                        cachedReloadingCategoriesWithBudgetDataView =
                            ReloadingCategoriesWithBudgetDataView.from(state)
                        cachedReloadingCategoriesDataView = null
                        Result.Success(state)
                    } else {
                        val state = LoadedCategoriesDataView(combined.categoriesWithSubCategories)
                        cachedReloadingCategoriesWithBudgetDataView = null
                        cachedReloadingCategoriesDataView = ReloadingCategoriesDataView.from(state)
                        Result.Success(state)
                    }
                } else {
                    cachedReloadingCategoriesWithBudgetDataView
                        ?.let {
                            Result.Success(it)
                        } ?: cachedReloadingCategoriesDataView?.let {
                        Result.Success(it)
                    }
                }
            }
            .filterNotNull()
            .shareInViewModel()

    private val transactionFilters: MutableStateFlow<BooleanFilters<String, Nothing>> =
        MutableStateFlow(
            booleanFilterOf(
                listOf(INCOME_FILTER, TRANSFER_FILTER, OUTCOME_FILTER, PROMISSORY_NOTE_FILTER),
                true
            )
        )


    private val _categoriesFiltersValue: MutableStateFlow<BooleanFilters<Int?, Pair<String, Int>>?> =
        MutableStateFlow(null)

    private val categoriesFiltersValue: SharedFlow<BooleanFilters<Int?, Pair<String, Int>>> =
        _categoriesFiltersValue
            .combineDefault(categoriesWithSubCategories) { oldValue, categories ->
                val orderedCategories = categories
                    .sortedBy { it.category.name }
                    .flattenWithLevel(0)
                val updatedValue = if (oldValue != null) {
                    categoriesMergeBooleanFilter(categories, oldValue)

                } else {
                    booleanFilterOf(
                        filterNames = orderedCategories
                            .map { it.first.id }
                            .plus(null),
                        metadata = orderedCategories
                            .associate {
                                (it.first.id) to (it.first.name to it.second)
                            }
                            .plus(null to ("" to 0)),
                        defaultValue = true
                    )
                }
                updatedValue
            }
            .shareInViewModel()

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
        _categoriesFiltersValue.value = newValue
    }

    fun updatePersonFilterValue(newValue: Boolean) {
        personFilterValue.value = newValue
    }

    fun updateValueFilterValue(newValue: DoubleFilter) {
        _valueFilterValue.value = newValue
    }

    fun updateDescriptionFilterValue(newValue: TextFilter) {
        descriptionFilterValue.value = newValue
    }

    private val incomeAccount =
        allAccount.map { accounts -> getIncomeAccount(accounts) }.shareInViewModel()
    private val outcomeAccount =
        allAccount.map { accounts -> getOutcomeAccount(accounts) }.shareInViewModel()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rangeTransactions = range.transformLatest { range ->
        repository
            .getTransactions(range.first, range.second)
            .onStart { emit(Result.Loading) }
            .collect { emit(Result.Success(it)) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rangePromissoryNotes = range.transformLatest { range ->
        repository
            .getPromissoryNotes(range.first, range.second)
            .onStart { emit(Result.Loading) }
            .collect { emit(Result.Success(it)) }
    }
    private val transactionAmountRangeValue =
        rangeTransactions.mapNotNull { transactionResult ->
            if (transactionResult is Result.Success) {
                val transactions = transactionResult.data
                val amountList =
                    transactions.flatMap { transaction -> transaction.transactionDetails.map { it.amount } }
                        .distinct()
                val max = (amountList.maxOrNull() ?: 0.0).toFloat()
                val min = (amountList.minOrNull() ?: 0.0).toFloat()
                min..max
            } else null
        }
    private val _valueFilterValue = MutableStateFlow<DoubleFilter?>(null)
    private val valueFilterValue: SharedFlow<DoubleFilter> =
        _valueFilterValue
            .combineDefault(transactionAmountRangeValue) { oldValue, transactionAmountRangeValue ->
                DoubleFilter(
                    value = oldValue?.value,
                    range = transactionAmountRangeValue
                )
            }
            .shareInViewModel()
    private val descriptionFilterValue: MutableStateFlow<TextFilter> =
        MutableStateFlow(TextFilter(null))
    private val principalPersonWithAccounts =
        personWithAccounts
            .map { getPrincipalPersonWithAccounts(it) }
            .shareInViewModel()

    private val filteredDocuments: SharedFlow<Result.Success<LoadedTransactionDetailsState>> =
        rangeTransactions
            .combineDefault(categories) { rangeTransactions, categories ->
                object {
                    val rangeTransactions = rangeTransactions
                    val categories = categories
                }
            }
            .combineDefault(allAccount) { combined, allAccount ->
                object {
                    val rangeTransactions = combined.rangeTransactions
                    val categories = combined.categories
                    val allAccount = allAccount
                }
            }
            .combineDefault(allPerson) { combined, allPerson ->
                object {
                    val rangeTransactions = combined.rangeTransactions
                    val categories = combined.categories
                    val allAccount = combined.allAccount
                    val allPerson = allPerson
                }
            }
            .combineDefault(rangePromissoryNotes) { combined, rangePromissoryNotes ->
                object {
                    val rangeTransactions = combined.rangeTransactions
                    val categories = combined.categories
                    val allAccount = combined.allAccount
                    val allPerson = combined.allPerson
                    val rangePromissoryNotes = rangePromissoryNotes
                }
            }
            .combineDefault(principalPerson) { combined, principalPerson ->
                object {
                    val rangeTransactions = combined.rangeTransactions
                    val categories = combined.categories
                    val allAccount = combined.allAccount
                    val allPerson = combined.allPerson
                    val rangePromissoryNotes = combined.rangePromissoryNotes
                }.run {
                    if (rangeTransactions is Result.Success && rangePromissoryNotes is Result.Success) {
                        Result.Success(
                            object {
                                val transactionItemDetails = TransactionListItemDetails.from(
                                    rangeTransactions.data,
                                    categories,
                                    allAccount,
                                    principalPerson?.id
                                )
                                val promissoryNotesViewModel = PromissoryNoteViewModel.from(
                                    promissoryNotes = rangePromissoryNotes.data,
                                    personList = allPerson,
                                    principalPersonId = principalPerson?.id
                                )
                            }
                        )
                    } else Result.Loading
                }
            }
            .combineDefault(transactionFilters) { filteredTransactions, filtersValue ->
                if (filteredTransactions is Result.Success) {
                    val transactionsWithFilters = filteredTransactions
                        .data
                        .transactionItemDetails
                        .applyIncomeFilter(filtersValue[INCOME_FILTER])
                        .applyOutcomeFilter(filtersValue[OUTCOME_FILTER])
                        .applyTransferFilter(filtersValue[TRANSFER_FILTER])
                    val promissoryNotes = filteredTransactions
                        .data
                        .promissoryNotesViewModel
                        .applyPromissoryNoteFilter(filtersValue[PROMISSORY_NOTE_FILTER])
                    Result.Success(
                        object {
                            val transactionsWithFilters = transactionsWithFilters
                            val promissoryNotesViewModel = promissoryNotes
                        }
                    )
                } else Result.Loading
            }
            .combineDefault(categoriesFiltersValue) { filteredTransactions, filtersValue ->
                if (filteredTransactions is Result.Success) {
                    val transactionsWithFilters =
                        filteredTransactions.data.transactionsWithFilters.applyCategoriesFilter(
                            filtersValue
                        )
                    val promissoryNotes = filteredTransactions.data.promissoryNotesViewModel
                    Result.Success(
                        object {
                            val transactionsWithFilters = transactionsWithFilters
                            val promissoryNotesViewModel = promissoryNotes
                        }
                    )
                } else Result.Loading
            }
            .combineDefault(valueFilterValue) { filteredTransactions, valueFilterValue ->
                if (filteredTransactions is Result.Success) {
                    val transactionsWithFilters =
                        filteredTransactions.data.transactionsWithFilters.applyValueFilter(
                            valueFilterValue
                        )
                    val promissoryNotes = filteredTransactions.data.promissoryNotesViewModel
                    Result.Success(
                        object {
                            val transactionsWithFilters = transactionsWithFilters
                            val promissoryNotesViewModel = promissoryNotes
                        }
                    )
                } else Result.Loading
            }
            .combineDefault(descriptionFilterValue) { filteredTransactions, descriptionFilterValue ->
                if (filteredTransactions is Result.Success) {
                    val transactionsWithFilters =
                        filteredTransactions.data.transactionsWithFilters.applyDescriptionFilter(
                            descriptionFilterValue
                        )
                    val promissoryNotes = filteredTransactions
                        .data
                        .promissoryNotesViewModel
                        .applyPromissoryDescriptionFilter(descriptionFilterValue)
                    Result.Success(
                        LoadedTransactionDetailsState(
                            transactionList = transactionsWithFilters,
                            promissoryNotesList = promissoryNotes
                        )
                    )
                } else Result.Loading
            }
            .mapNotNull {
                if (it is Result.Success) it
                else null
            }
            .shareInViewModel()

    private val allTransactionWithDetailsAndAccountsAndCategory: SharedFlow<List<TransactionWithDetailsAndAccountsAndCategory>> =
        allTransactions
            .combineDefault(allAccount) { allTransactions, allAccount ->
                object {
                    val allTransactions = allTransactions
                    val allAccount = allAccount
                }
            }
            .combineDefault(categories) { combined, categories ->
                combined.run {
                    TransactionWithDetailsAndAccountsAndCategory.from(
                        allTransactions,
                        allAccount,
                        categories
                    )
                }
            }
            .shareInViewModel()

    private var cachedPersonSummaryState: PersonSummaryState = loadingPersonSummaryState()

    private val personSummaryState: SharedFlow<PersonSummaryState> =
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
            .combine(allTransactionWithDetailsAndAccountsAndCategory) { combined, allTransactionWithDetailsAndAccountsAndCategory ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionWithDetailsAndAccountsAndCategory =
                        allTransactionWithDetailsAndAccountsAndCategory
                }
            }
            .combine(categoryWithSubcategoriesAndBudgetWithCalculatedData) { combined, budgetWithCalculatedDataAndCategory ->
                if (budgetWithCalculatedDataAndCategory is Result.Success) {
                    object {
                        val principalPersonWithAccounts = combined.principalPersonWithAccounts
                        val range = combined.range
                        val personWithAccounts = combined.personWithAccounts
                        val allTransactionWithDetailsAndAccountsAndCategory =
                            combined.allTransactionWithDetailsAndAccountsAndCategory
                        val budgetWithCalculatedDataAndCategory =
                            budgetWithCalculatedDataAndCategory.data
                    }
                } else null
            }
            .filterNotNull()
            .combine(incluirPresupuestoEnSaldoActual) { combined, incluirPresupuestoEnSaldoActual ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionWithDetailsAndAccountsAndCategory =
                        combined.allTransactionWithDetailsAndAccountsAndCategory
                    val budgetWithCalculatedDataAndCategory =
                        combined.budgetWithCalculatedDataAndCategory
                    val incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual
                }
            }
            .combine(incluirDeudasEnSaldoActual) { combined, incluirDeudasEnSaldoActual ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionWithDetailsAndAccountsAndCategory =
                        combined.allTransactionWithDetailsAndAccountsAndCategory
                    val budgetWithCalculatedDataAndCategory =
                        combined.budgetWithCalculatedDataAndCategory
                    val incluirPresupuestoEnSaldoActual = combined.incluirPresupuestoEnSaldoActual
                    val incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual
                }
            }
            .combine(allPromissoryNotes) { combined, allPromissoryNotes ->
                object {
                    val principalPersonWithAccounts = combined.principalPersonWithAccounts
                    val range = combined.range
                    val personWithAccounts = combined.personWithAccounts
                    val allTransactionWithDetailsAndAccountsAndCategory =
                        combined.allTransactionWithDetailsAndAccountsAndCategory
                    val budgetWithCalculatedDataAndCategory =
                        combined.budgetWithCalculatedDataAndCategory
                    val incluirPresupuestoEnSaldoActual = combined.incluirPresupuestoEnSaldoActual
                    val incluirDeudasEnSaldoActual = combined.incluirDeudasEnSaldoActual
                    val allPromissoryNotes = allPromissoryNotes
                }
            }
            .zDistinctUntilChanged { old, new ->
                old === new ||
                        (
                                old.principalPersonWithAccounts == new.principalPersonWithAccounts &&
                                        old.range == new.range &&
                                        old.personWithAccounts == new.personWithAccounts &&
                                        old.allTransactionWithDetailsAndAccountsAndCategory == new.allTransactionWithDetailsAndAccountsAndCategory &&
                                        old.budgetWithCalculatedDataAndCategory == new.budgetWithCalculatedDataAndCategory &&
                                        old.incluirPresupuestoEnSaldoActual == new.incluirPresupuestoEnSaldoActual &&
                                        old.incluirDeudasEnSaldoActual == new.incluirDeudasEnSaldoActual &&
                                        old.allPromissoryNotes == new.allPromissoryNotes
                                )
            }
            .transform { combined ->
                emit(cachedPersonSummaryState)
                val result = withContext(Dispatchers.Default) {
                    combined.run {
                        val summaryState = LoadedPersonSummaryState.from(
                            principalPersonWithAccounts,
                            range.first,
                            range.second,
                            allPersons = personWithAccounts,
                            allTransactions = allTransactionWithDetailsAndAccountsAndCategory
                                .map {
                                    TransactionWithDetailsAndAccounts(
                                        it.transaction,
                                        it.sourceAccount,
                                        it.destinationAccount
                                    )
                                }
                                .toTransactionAndDetailsAndAccounts(),
                            allPromissoryNotes = allPromissoryNotes,
                            budgetWithCalculatedDatumAndCategories = budgetWithCalculatedDataAndCategory
                                ?: emptyList(),
                            includeBudget = incluirPresupuestoEnSaldoActual,
                            includeDebts = incluirDeudasEnSaldoActual
                        )
                        cachedPersonSummaryState = ReloadingPersonSummaryState.from(summaryState)
                        summaryState
                    }
                }
                emit(result)
            }
            .shareInViewModel()

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
                TransactionWithDetails.new(
                    amount = amount,
                    description = "Ajuste",
                    sourceId = incomeAccountId,
                    destinationId = accountId,
                    date = today,
                    aNombreDe = null,
                    categoryId = null,
                    budgetDate = null
                )
            } else {
                TransactionWithDetails.new(
                    amount = -amount,
                    description = "Ajuste",
                    sourceId = accountId,
                    destinationId = outcomeAccountId,
                    date = today,
                    aNombreDe = null,
                    categoryId = null,
                    budgetDate = null
                )
            }
            repository.insertTransaction(transaccionAjuste)
        }
    }

    fun insertTransaction(
        vararg transaction: NewTransactionWithDetails,
        onErrorAction: (Throwable) -> Unit = {}
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.insertTransaction(*transaction)
    }

    fun insertTransaction(
        vararg transaction: TransactionWithDetails,
        onErrorAction: (Throwable) -> Unit = {}
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.insertTransaction(*transaction)
    }

    fun insertPromissoryNote(
        vararg promissorNote: PromissoryNote,
        onErrorAction: (Throwable) -> Unit = {}
    ) = viewModelScope.safeLaunch(onErrorAction) {
        repository.insertPromissoryNote(*promissorNote)
    }

    fun updateTransaction(transaction: NewTransactionWithDetails) = viewModelScope.launch {
        repository.upsertTransaction(transaction)
    }

    fun updatePromissoryNote(promissoryNote: PromissoryNote) = viewModelScope.launch {
        repository.updatePromissoryNote(promissoryNote)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun deleteTransactionDetails(transactionDetails: TransactionDetails) = viewModelScope.launch {
        repository.deleteTransactionDetails(transactionDetails)
    }

    fun deletePromissoryNote(promissoryNote: PromissoryNote) = viewModelScope.launch {
        repository.deletePromissoryNote(promissoryNote)
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

    fun settingsCompactShowFlow(newValue: CompactShow) = viewModelScope.launch {
        settings.setCompactShow(newValue)
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

        private suspend fun getPromissoryNotes() =
            repository.getPromissoryNotes()
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
                totalWork = 9.0,
                defaultIncrement = 1.0
            ) { loadingDataState.postValue(it.toState(Type.EXPORT)) }
            viewModelScope.safeLaunch(
                onErrorAction = {
                    progressStatus.error("Error: ${it.message}")
                }
            ) {
                withContext(Dispatchers.IO) {
                    progressStatus.incrementProgress("Converting persons")
                    val persons = getPersons()
                    val personsOutputStream = ByteArrayOutputStream()
                    personsOutputStream.use {
                        writePersons(it, persons)
                    }

                    progressStatus.incrementProgress("Converting promissory notes")
                    val promissoryNotes = getPromissoryNotes()
                    val promissoryNotesOutputStream = ByteArrayOutputStream()
                    promissoryNotesOutputStream.use {
                        writePromissoryNotes(it, promissoryNotes)
                    }

                    progressStatus.incrementProgress("Converting categories")
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
                    val promissoryNotesInputStream =
                        ByteArrayInputStream(promissoryNotesOutputStream.toByteArray())
                    progressStatus.incrementProgress("Compressing files")
                    ZipOutputStream(outputStream)
                        .use { zipOurpurStream ->
                            writeZipBackup(
                                transactionsInputStream,
                                promissoryNotesInputStream,
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
            var transactions: List<TransactionWithDetails>? = null
            var promissoryNotes: List<PromissoryNote> = emptyList()
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

                                        "promissorynotes.csv" -> {
                                            progressStatus.incrementProgress("Loading promissory notes")
                                            zipInputStream.readBytes()
                                                .run {
                                                    inputStream().run {
                                                        promissoryNotes =
                                                            readPromissoryNotesFromCsv(this)
                                                    }
                                                }
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
                                    (promissoryNotes.size) +
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
                            promissoryNotes.also { promissoryNotes ->
                                insertPromissoryNote(*promissoryNotes.toTypedArray()) {}
                            }
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
            vararg transaction: TransactionWithDetails,
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
        fun rememberShowOnBoarding() = showOnBoarding.collectAsState(null)

        @Composable
        fun rememberPrincipalPersonId() = remember {
            principalPerson
                .map { it?.id }
                .shareInViewModel()
        }
            .collectAsState(-1)

        @Composable
        fun rememberPrincipalAccounts(principalPersonId: Int?) = remember(principalPersonId) {
            allAccount
                .map { it.filter { account -> account.ownerId == principalPersonId } }
                .shareInViewModel()
        }
            .collectAsState(null)

        fun setShowOnBoarding(value: Boolean) = this@MainViewModel.setShowOnBoarding(value)
    }

    inner class ViewModelOnBoarding {
        @Composable
        fun rememberOnBoardingStep() = rememberSaveable { mutableIntStateOf(-1) }

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
        fun rememberIncomeAccount() = incomeAccount.collectAsState(null)

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.collectAsState(null)

        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(null)

        @Composable
        fun rememberAllAccounts() = allAccount.collectAsState(null)

        @Composable
        fun rememberAllCategories() = categories.collectAsState(null)

        @Composable
        fun rememberPrincipalPerson() = principalPerson.collectAsState(null)

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
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            remember {
                accountAndOwnerWithTransactions
                    .mapNotNull { if (it is Result.Success) it.data else null }
                    .shareInViewModel()
            }
                .collectAsState(emptyList())

        @Composable
        fun rememberFilteredTransactionListItemDetails() =
            filteredDocuments.collectAsState(Result.Loading)

        @Composable
        fun rememberPersonSummaryState() =
            personSummaryState.collectAsState(cachedPersonSummaryState)

        @Composable
        fun rememberPrincipalPerson() = principalPerson.collectAsState(null)

        @Composable
        fun rememberRange() = range.collectAsState(Pair(null, null))

        @Composable
        fun rememberTransactionFiltersValue() = transactionFilters
            .collectAsState(
                booleanFilterOf(
                    listOf(INCOME_FILTER, TRANSFER_FILTER, OUTCOME_FILTER, PROMISSORY_NOTE_FILTER),
                    true
                )
            )

        @Composable
        fun rememberCategoriesFiltersValue() =
            categoriesFiltersValue.collectAsState(booleanFilterOf(emptyList(), true))

        @Composable
        fun rememberPersonFilterValue() = personFilterValue.observeAsState(false)

        @Composable
        fun rememberValueFilterValue() =
            valueFilterValue.collectAsState(DoubleFilter(0.0f..0.0f, 0.0f..0.0f))

        @Composable
        fun rememberDescriptionFilterValue() =
            descriptionFilterValue.collectAsState(TextFilter(null))

        @Composable
        fun rememberToday(): State<LocalDate> = today.collectAsState(initial = LocalDate.now())

        fun deletePerson(person: Person) = this@MainViewModel.deletePerson(person)

        fun deleteAccount(account: Account) = this@MainViewModel.deleteAccount(account)

        fun deleteTransaction(transaction: Transaction) =
            this@MainViewModel.deleteTransaction(transaction)

        fun delTransactionDetails(transactionDetails: TransactionDetails) =
            this@MainViewModel.deleteTransactionDetails(transactionDetails)

        fun deletePromissoryNote(promissoryNote: PromissoryNote) =
            this@MainViewModel.deletePromissoryNote(promissoryNote)

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
        fun rememberCompactShow() =
            compactShow.collectAsState(null)

        @Composable
        fun rememberEditarCategoriasState() =
            editarCategoriasState.collectAsState(Result.Loading)

        fun updateShowType(newValue: EditarCategoriasShowType) = _showPlot.postValue(newValue)

        fun updateCompactShow(newValue: CompactShow) =
            this@MainViewModel.settingsCompactShowFlow(newValue)

        fun deleteCategory(category: Category) = this@MainViewModel.deleteCategory(category)
    }

    inner class ViewModelAddAccount {
        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberAllAccount() = allAccount.collectAsState(emptyList())

        @Composable
        fun rememberIncomeAccount() = incomeAccount.collectAsState(null)

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.collectAsState(null)

        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.collectAsState(Result.Loading)

        @Composable
        fun rememberToday() = today.collectAsState(LocalDate.now())

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
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

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
        fun rememberIncomeAccount() = incomeAccount.collectAsState(null)

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.collectAsState(null)

        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberAllAccount() = allAccount.collectAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.collectAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() = remember {
            categoryWithSubcategoriesAndBudgetWithCalculatedData
                .filter { it !is Result.Loading }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)

        @Composable
        fun rememberAccountAndOwner() = accountAndOwner
            .collectAsState(emptyList())

        @Composable
        fun rememberAccountAndOwnerUserFirst() =
            accountAndOwnerUserFirst
                .collectAsState(emptyList())

        fun insertTransaction(
            vararg transaction: NewTransactionWithDetails,
            onErrorAction: (Throwable) -> Unit = {}
        ) = this@MainViewModel.insertTransaction(
            *transaction,
            onErrorAction = onErrorAction
        )
    }

    inner class ViewModelAddPromissoryNote {
        @Composable
        fun rememberAllPerson() = remember {
            allPerson
                .map { Result.Success(it) }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)

        fun insertPromissoryNote(
            promissoryNote: PromissoryNote,
            onErrorAction: (Throwable) -> Unit = {}
        ) =
            this@MainViewModel.insertPromissoryNote(
                promissoryNote,
                onErrorAction = onErrorAction
            )
    }

    inner class ViewModelEditAccount {
        @Composable
        fun rememberAccountAndOwnerWithTransactionsAndPockets() =
            accountAndOwnerWithTransactionsAndPockets.collectAsState(emptyList())

        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberAllAccount() = allAccount.collectAsState(emptyList())

        @Composable
        fun rememberIncomeAccount() = incomeAccount.collectAsState(null)

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.collectAsState(null)

        @Composable
        fun rememberAccountAndOwnerWithTransactions() =
            accountAndOwnerWithTransactions.collectAsState(Result.Loading)

        @Composable
        fun rememberToday() = today.collectAsState(LocalDate.now())

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
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

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
        fun rememberTransactionAndAccounts(transactionId: Int?):
                State<TransactionWithDetailsAndAccounts?> = remember(transactionId) {
            allTransactionWithDetailsAndAccountsAndCategory
                .map { transactionAndAccountAndCategory ->
                    transactionAndAccountAndCategory
                        .firstOrNull { it.transaction.transaction.id == transactionId }
                        ?.toTransactionWithDetailsAndAccounts()
                }
                .shareInViewModel()
        }
            .collectAsState(null)

        @Composable
        fun rememberAccountAndOwnerWithTransactions() = remember {
            accountAndOwnerWithTransactions
                .filter { it !is Result.Loading }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)

        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.collectAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() = remember {
            categoryWithSubcategoriesAndBudgetWithCalculatedData
                .filter { it !is Result.Loading }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)

        fun updateTransaction(transaction: NewTransactionWithDetails) =
            this@MainViewModel.updateTransaction(transaction)
    }

    inner class ViewModelShareTransaction {
        @Composable
        fun rememberTransaction(transactionId: Int?):
                State<Result<TransactionWithDetails>> = remember(transactionId) {
            allTransactions
                .map { transactions ->
                    transactions
                        .firstOrNull { it.transaction.id == transactionId }
                        ?.let { Result.Success(it) }
                        ?: Result.Error(Throwable("Transaction not found"))
                }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)
    }

    inner class ViewModelEditPromissoryNote {
        @Composable
        fun rememberPromissoryNote(promissoryNoteId: Int?) = remember(promissoryNoteId) {
            allPromissoryNotes
                .map {
                    it
                        .firstOrNull { it.id == promissoryNoteId }
                        ?.let { Result.Success(it) }
                        ?: Result.Error(Throwable("PromissoryNote not found"))
                }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)

        @Composable
        fun rememberAllPerson() = remember {
            allPerson
                .map { Result.Success(it) }
                .shareInViewModel()
        }
            .collectAsState(Result.Loading)

        fun updatePromissoryNote(promissoryNote: PromissoryNote) =
            this@MainViewModel.updatePromissoryNote(promissoryNote)
    }

    inner class ViewModelSettings {
        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberPrincipalPerson() = principalPerson.collectAsState(null)

        @Composable
        fun rememberAccountAndOwner() = accountAndOwner.collectAsState(emptyList())

        @Composable
        fun rememberIncomeAccount() = incomeAccount.collectAsState(null)

        @Composable
        fun rememberOutcomeAccount() = outcomeAccount.collectAsState(null)

        @Composable
        fun rememberShowOnBoarding() = showOnBoarding.collectAsState(null)

        @Composable
        fun rememberUseDynamicColor() = useDynamicColor.collectAsState(null)

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
        fun rememberCurrentCashSettingsState() = remember {
            accountAndOwnerWithTransactions
                .combine(principalPerson) { accountAndOwnerWithTransactions, principalPerson ->
                    object {
                        val accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
                        val principalPerson = principalPerson
                    }
                }
                .combine(allPerson) { combined, allPerson ->
                    val principalPersonId = combined.principalPerson?.id
                    object {
                        val accountAndOwnerWithTransactions =
                            combined.accountAndOwnerWithTransactions
                        val principalPerson = combined.principalPerson
                        val allPerson =
                            allPerson.filter { it.id == null || it.id != principalPersonId }
                    }
                }
                .combine(personSummaryState) { combined, personSummaryState ->
                    object {
                        val accountAndOwnerWithTransactions =
                            combined.accountAndOwnerWithTransactions
                        val principalPerson = combined.principalPerson
                        val allPerson = combined.allPerson
                        val personSummaryState = personSummaryState
                    }
                }
                .combine(incluirPresupuestoEnSaldoActual) { combined, incluirPresupuestoEnSaldoActual ->
                    object {
                        val accountAndOwnerWithTransactions =
                            combined.accountAndOwnerWithTransactions
                        val allPerson = combined.allPerson
                        val personSummaryState = combined.personSummaryState
                        val principalPerson = combined.principalPerson
                        val incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual
                    }
                }
                .combine(incluirDeudasEnSaldoActual) { combined, incluirDeudasEnSaldoActual ->
                    object {
                        val accountAndOwnerWithTransactions =
                            combined.accountAndOwnerWithTransactions
                        val allPerson = combined.allPerson
                        val personSummaryState = combined.personSummaryState
                        val principalPerson = combined.principalPerson
                        val incluirPresupuestoEnSaldoActual =
                            combined.incluirPresupuestoEnSaldoActual
                    }.run {
                        when (accountAndOwnerWithTransactions) {
                            is Result.Error -> Result.Error(accountAndOwnerWithTransactions.exception)
                            Result.Loading -> null
                            is Result.Success -> when (personSummaryState) {
                                EmptyPersonSummaryState -> null
                                is FullPersonSummaryState -> Result.Success(
                                    CurrentCashSettingsState(
                                        accountAndOwnerWithTransactions = accountAndOwnerWithTransactions.data,
                                        personList = allPerson,
                                        personSummaryState = personSummaryState,
                                        principalPerson = principalPerson,
                                        incluirPresupuestoEnSaldoActual = incluirPresupuestoEnSaldoActual,
                                        incluirDeudasEnSaldoActual = incluirDeudasEnSaldoActual
                                    )
                                )

                                is ReloadingPersonSummaryState -> null
                                LoadingPersonSummaryState -> null
                            }
                        }
                    }
                }
                .filterNotNull()
                .shareInViewModel()
        }.collectAsState(Result.Loading)

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
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.collectAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.collectAsState(Result.Loading)

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
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.collectAsState(emptyList())

        @Composable
        fun rememberBudgetAndCategoryWithCalculatedData() =
            remember {
                budgetWithCalculatedDataAndCategory
                    .map { if (it is Result.Success) it.data else null }
                    .shareInViewModel()
            }
                .collectAsState(null)

        @Composable
        fun rememberBudgetAndCategoryWithTransactions() =
            remember {
                budgetAndCategoryWithTransactions
                    .map { Result.Success(it) }
                    .shareInViewModel()
            }
                .collectAsState(Result.Loading)

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.collectAsState(Result.Loading)

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
        private val _accountDetailId = MutableStateFlow<Int?>(null)
        private val accountDetailId = _accountDetailId
            .filterNotNull()
            .shareInViewModel()
        private val accountDetail = accountAndOwnerWithTransactionsAndPockets
            .combineDefault(accountDetailId) { accountAndOwnerWithTransactionsAndPockets, accountDetailId ->
                accountAndOwnerWithTransactionsAndPockets.firstOrNull {
                    it.accountAndOwnerWithTransactions.account.id == accountDetailId
                }
            }
        private val _descriptionFilter: MutableStateFlow<TextFilter> =
            MutableStateFlow(TextFilter(null))
        private val descriptionFilter: SharedFlow<TextFilter> =
            _descriptionFilter.shareInViewModel()
        private val _accountFilterValue: MutableStateFlow<BooleanFilters<String, Nothing>> =
            MutableStateFlow(
                booleanFilterOf(
                    listOf(
                        TRANSFER_FILTER, INCOME_FILTER, OUTCOME_FILTER
                    ), true
                )
            )
        private val accountFilterValue: SharedFlow<BooleanFilters<String, Nothing>> =
            _accountFilterValue.shareInViewModel()
        private val _accountCategoryFilterValue: MutableStateFlow<BooleanFilters<Int?, Pair<String, Int>>?> =
            MutableStateFlow(null)
        private val accountCategoryFilterValue: SharedFlow<BooleanFilters<Int?, Pair<String, Int>>> =
            _accountCategoryFilterValue
                .combineDefault(categoriesWithSubCategories) { filterValue, categories ->
                    if (filterValue == null) {
                        val flattenCategories = categories.flattenWithLevel()
                        booleanFilterOf(
                            filterNames = flattenCategories
                                .map { it.first.id }
                                .plus(null),
                            defaultValue = true,
                            metadata = flattenCategories
                                .associate { it.first.id to (it.first.name to it.second) }
                                .plus(null to ("" to 0))
                        )
                    } else {
                        categoriesMergeBooleanFilter(
                            categories = categories,
                            booleanFilters = filterValue
                        )
                    }
                }
                .shareInViewModel()
        private val _accountValueFilter = MutableStateFlow(DoubleFilter(null, 0f..0f))
        private val accountValueFilter = _accountValueFilter
            .combine(range) { accountValueFilter, range ->
                object {
                    val accountValueFilter = accountValueFilter
                    val range = range
                }
            }
            .combine(accountDetail) { combined, accountDetail ->
                combined.run {
                    if (accountDetail != null) {
                        val transactions = accountDetail.allTransactionsWithPocketTransactions
                        val min = transactions
                            .filter { it.date.isBetween(range.first, range.second) }
                            .minOfOrNull { it.totalAmount.toFloat() } ?: 0f
                        val max = transactions
                            .filter { it.date.isBetween(range.first, range.second) }
                            .maxOfOrNull { it.totalAmount.toFloat() } ?: 0f
                        _accountValueFilter
                            .updateAndGet {
                                accountValueFilter.copy(range = min..max)
                            }
                    } else accountValueFilter
                }
            }
            .shareInViewModel()
        private val accountDetailData: SharedFlow<AccountDetailData?> = accountDetail
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
            .combine(accountValueFilter) { combined, accountValueFilter ->
                object {
                    val accountDetail = combined.accountDetail
                    val allAccount = combined.allAccount
                    val categories = combined.categories
                    val budget = combined.budget
                    val range = combined.range
                    val principalPerson = combined.principalPerson
                    val accountFilterValue = combined.accountFilterValue
                    val accountCategoryFilterValue = combined.accountCategoryFilterValue
                    val accountValueFilter = accountValueFilter
                }
            }
            .combineDefault(descriptionFilter) { combined, descriptionFilter ->
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
                            transactionFilters = accountFilterValue,
                            categoriesFilter = accountCategoryFilterValue,
                            descriptionFilter = descriptionFilter,
                            valueFilter = accountValueFilter
                        )
                    }
                }
            }
            .shareInViewModel()

        @Composable
        fun rememberCategoriesFilter(accountId: Int?) =
            rememberSaveable(accountId) {
                _accountDetailId.update { accountId }
                accountId ?: -1
            }
                .let {
                    accountCategoryFilterValue
                        .collectAsState(booleanFilterOf(emptyList()))
                }

        @Composable
        fun rememberDescriptionFilter(accountId: Int?) = rememberSaveable(accountId) {
            _accountDetailId.update { accountId }
            accountId ?: -1
        }.let {
            descriptionFilter.collectAsState(TextFilter(null))
        }

        @Composable
        fun rememberAccountValueFilter(accountId: Int?) = rememberSaveable(accountId) {
            _accountDetailId.update { accountId }
            accountId ?: -1
        }.let {
            accountValueFilter.collectAsState(DoubleFilter(null, 0f..0f))
        }

        @Composable
        fun rememberAccountFilterValue(accountId: Int?) = rememberSaveable(accountId) {
            _accountDetailId.update { accountId }
            accountId ?: -1
        }.let {
            accountFilterValue
                .collectAsState(
                    booleanFilterOf(
                        listOf(
                            TRANSFER_FILTER, INCOME_FILTER, OUTCOME_FILTER
                        ), true
                    )
                )
        }

        @Composable
        fun rememberAccountDetailData(
            accountId: Int?
        ): State<AccountDetailData?> = rememberSaveable(accountId) {
            _accountDetailId.update { accountId }
            accountId ?: -1
        }.let {
            accountDetailData.collectAsState(null)
        }

        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberAccountAndOwner() = accountAndOwner.collectAsState(emptyList())

        @Composable
        fun rememberDateRange() = range.collectAsState(initial = null to null)

        fun deleteAccount(account: Account) = this@MainViewModel.deleteAccount(account)

        fun updateAccountFilter(newValue: BooleanFilters<String, Nothing>) =
            _accountFilterValue.apply {
                value = newValue
            }

        fun updateDescriptionFilter(newValue: TextFilter) = _descriptionFilter.apply {
            value = newValue
        }

        fun updateValueFilter(newValue: DoubleFilter) = _accountValueFilter.apply {
            value = newValue
        }

        fun updateCategoryFilter(newValue: BooleanFilters<Int?, Pair<String, Int>>) =
            _accountCategoryFilterValue.apply {
                value = newValue
            }

        fun deleteTransaction(transaction: Transaction) =
            this@MainViewModel.deleteTransaction(transaction)

        fun onDateRangeChange(startDate: LocalDate?, endDate: LocalDate?) {
            this@MainViewModel.updateRange(startDate, endDate)
        }
    }

    inner class ViewModelPersonDetail {
        @Composable
        fun rememberPrincipalPerson() = principalPerson.collectAsState(null)

        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberPersonSummaryState() =
            personSummaryState.collectAsState(cachedPersonSummaryState)

        @Composable
        fun rememberPeopleTransactionListItemDetails(
            principalPersonId: Int?,
            otherPersonId: Int?,
            justPendingTransactions: Boolean,
            debt: Double
        ) = remember(principalPersonId, otherPersonId, justPendingTransactions, debt) {
            allTransactions
                .combineDefault(allPromissoryNotes) { allTransactions, allPromissoryNotes ->
                    object {
                        val transactions = allTransactions
                        val promissoryNotes = allPromissoryNotes
                    }
                }
                .combineDefault(allAccount) { combined, allAccount ->
                    val personAccountsIds = allAccount
                        .filter { it.ownerId == otherPersonId }
                        .map { it.id }
                        .toSet()
                    object {
                        val transactions = TransactionAndDetails.from(combined.transactions)
                            .filter {
                                it.aNombreDe == otherPersonId ||
                                        it.sourceId in personAccountsIds ||
                                        it.destinationId in personAccountsIds
                            }
                            .sortedByDescending { it.date }
                        val promissoryNotes = combined.promissoryNotes
                            .filter {
                                it.sourceId == principalPersonId && it.destinationId == otherPersonId ||
                                        it.destinationId == principalPersonId && it.sourceId == otherPersonId
                            }
                        val accounts = allAccount
                    }
                }
                .combineDefault(allPerson) { combined, allPerson ->
                    object {
                        val transactions = combined.transactions
                        val promissoryNotes = combined.promissoryNotes
                        val accounts = combined.accounts
                        val personList = allPerson
                    }
                }
                .combineDefault(categories) { combined, categories ->
                    object {
                        val transactions = combined.transactions
                        val accounts = combined.accounts
                        val promissoryNotes = combined.promissoryNotes
                        val personList = combined.personList
                    }.run {
                        val allDocuments = (TransactionListItemDetailsWithSign.from(
                            transactions,
                            categories,
                            accounts,
                            principalPersonId,
                            otherPersonId
                        )
                            .map { TransactionDocumentWithSignViewModel(it) } + PromissoryNoteWithSignViewModel.from(
                            promissoryNotes,
                            personList,
                            principalPersonId,
                            otherPersonId
                        ).map { PromissoryNoteDocumentWithSignViewModel(it) })
                            .sortedByDescending { it.id }
                            .sortedByDescending { it.date }
                            .filter { it.sign != 0 }
                        if (justPendingTransactions) {
                            var cumSum = 0.0
                            val filteredDocuments = allDocuments
                                .takeWhile {
                                    val condition = cumSum != debt
                                    cumSum += it.amount * it.sign
                                    condition
                                }
                            filteredDocuments
                        } else {
                            allDocuments
                        }
                    }
                }
                .shareInViewModel()
        }
            .collectAsState(null)

        fun deletePerson(person: Person) = this@MainViewModel.deletePerson(person)

        fun deleteTransaction(transaction: Transaction) =
            this@MainViewModel.deleteTransaction(transaction)

        fun deleteTransactionDetails(transactionDetails: TransactionDetails) =
            this@MainViewModel.deleteTransactionDetails(transactionDetails)

        fun deletePromissoryNote(promissoryNote: PromissoryNote) =
            this@MainViewModel.deletePromissoryNote(promissoryNote)
    }

    inner class ViewModelEditBudget {
        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberBudgetAndCategoryWithCalculatedData() =
            remember {
                budgetWithCalculatedDataAndCategory
                    .mapNotNull { if (it is Result.Success) it.data else null }
                    .shareInViewModel()
            }
                .collectAsState(emptyList())

        fun deleteBudget(budget: Budget) = this@MainViewModel.deleteBudget(budget)
    }

    inner class ViewModelAddOneBudget {
        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.collectAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.collectAsState(Result.Loading)

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
            budgetAndCategoryWithTransactions.collectAsState(emptyList())
    }

    inner class ViewModelEditOneBudget {
        @Composable
        fun rememberAllPerson() = allPerson.collectAsState(emptyList())

        @Composable
        fun rememberCategories() = categories.collectAsState(emptyList())

        @Composable
        fun rememberBudget() = budget.collectAsState(emptyList())

        @Composable
        fun rememberCategoryWithSubcategoriesAndBudgetWithCalculatedData() =
            categoryWithSubcategoriesAndBudgetWithCalculatedData.collectAsState(Result.Loading)

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
    val viewModelAddPromissoryNote = ViewModelAddPromissoryNote()
    val viewModelEditAccount = ViewModelEditAccount()
    val viewModelEditPerson = ViewModelEditPerson()
    val viewModelEditTransaction = ViewModelEditTransaction()
    val viewModelShareTransaction = ViewModelShareTransaction()
    val viewModelEditPromissoryNote = ViewModelEditPromissoryNote()
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
        suspend fun <T : ITransactionListDetail> List<T>.applyIncomeFilter(
            incomeFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            filter {
                it.transactionType != TransactionType.INCOME || incomeFilterValue
            }
        }

        suspend fun <T : ITransactionListDetail> List<T>.applyOutcomeFilter(
            outcomeFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            filter {
                it.transactionType != TransactionType.OUTCOME || outcomeFilterValue
            }
        }

        suspend fun <T : ITransactionListDetail> List<T>.applyTransferFilter(
            transferFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            filter {
                it.transactionType != TransactionType.TRANSFER || transferFilterValue
            }
        }

        suspend fun List<PromissoryNoteViewModel>.applyPromissoryNoteFilter(
            promissoryNoteFilterValue: Boolean
        ) = withContext(Dispatchers.Default) {
            if (promissoryNoteFilterValue) {
                this@applyPromissoryNoteFilter
            } else {
                emptyList<PromissoryNoteViewModel>()
            }
        }

        suspend fun <T : ITransactionListDetailGrouped> List<T>.applyCategoriesFilter(
            filters: BooleanFilters<Int?, Pair<String, Int>>
        ) = withContext(Dispatchers.Default) {
            filter { transaction ->
                transaction.categories
                    .takeIf { it.isNotEmpty() }
                    ?.any {
                        filters.values.getOrDefault(it.id, filters.defaultValue)
                    } ?: filters.values.getOrDefault(null, filters.defaultValue)
            }
        }

        suspend fun <T : ITransactionListDetailGrouped> List<T>.applyDescriptionFilter(
            descriptionFilter: TextFilter
        ) = withContext(Dispatchers.Default) {
            val locale = Locale.getDefault()
            val searchTokens = descriptionFilter.value
                ?.lowercase(locale)
                ?.split(" ")
                ?.toSet()
                ?.map { ".*$it.*".toRegex() }
            filter { transaction ->
                val descriptionTokens = transaction.descriptions.joinToString(separator = " ")
                    .lowercase(locale)
                searchTokens == null ||
                        searchTokens
                            .any { it.containsMatchIn(descriptionTokens) }
            }
        }

        suspend fun List<PromissoryNoteViewModel>.applyPromissoryDescriptionFilter(
            descriptionFilter: TextFilter
        ) = withContext(Dispatchers.Default) {
            val locale = Locale.getDefault()
            val searchTokens = descriptionFilter.value
                ?.lowercase(locale)
                ?.split(" ")
                ?.toSet()
                ?.map { ".*$it.*".toRegex() }
            filter { document ->
                val descriptionTokens = document.promissoryNote.description
                    .lowercase(locale)
                searchTokens == null ||
                        searchTokens
                            .any { it.containsMatchIn(descriptionTokens) }
            }
        }

        suspend fun <T : ITransactionListDetailGrouped> List<T>.applyValueFilter(
            filterValue: DoubleFilter
        ) = withContext(Dispatchers.Default) {
            filterValue
                .value?.let {
                    filter { transaction ->
                        filterValue.value.contains(transaction.totalAmount.toFloat())
                    }
                } ?: this@applyValueFilter
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val settings: Settings,
    private val resultLauncherSaveTransaction: ActivityResultLauncher<String>,
    private val resultLauncherOpenDocument: ActivityResultLauncher<Array<String>>,
    private val resultLauncherExportDetails: ActivityResultLauncher<String>,
    private val getLifecycle: () -> Lifecycle
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
                resultLauncherExportDetails,
                getLifecycle
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}