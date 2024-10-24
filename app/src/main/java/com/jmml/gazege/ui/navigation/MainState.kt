package com.jmml.gazege.ui.navigation

import com.jmml.gazege.core.dao.PersonDao
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PersonWithAccounts
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.TransactionAndDetailsAndAccounts
import com.jmml.gazege.core.entities.TransactionListItemDetails
import com.jmml.gazege.ui.views.document.IDocumentViewModel
import com.jmml.gazege.ui.views.document.PromissoryNoteDocumentViewModel
import com.jmml.gazege.ui.views.document.TransactionDocumentViewModel
import com.jmml.gazege.ui.views.promissorynote.PromissoryNoteViewModel
import java.time.LocalDate

interface PersonSummaryState

object LoadingPersonSummaryState : PersonSummaryState
object EmptyPersonSummaryState : LoadedPersonSummaryState {
    override val saldoActual: Double
        get() = 0.0
    override val ingresos: Double
        get() = 0.0
    override val egresos: Double
        get() = 0.0
    override val deudasFlujo: Map<Person, Double>
        get() = emptyMap()
    override val deudasTotal: Double
        get() = 0.0
    override val presupuestoTotal: Double
        get() = 0.0

}

interface LoadedPersonSummaryState : PersonSummaryState {
    val saldoActual: Double
    val ingresos: Double
    val egresos: Double
    val deudasFlujo: Map<Person, Double>
    val deudasTotal: Double
    val presupuestoTotal: Double
    val flujo: Double get() = ingresos - egresos

    companion object {
        fun from(
            personWithAccounts: PersonWithAccounts?,
            startDate: LocalDate?,
            endDate: LocalDate?,
            allPersons: List<PersonWithAccounts>,
            allTransactions: List<TransactionAndDetailsAndAccounts>,
            allPromissoryNotes: List<PromissoryNote>,
            budgetWithCalculatedDatumAndCategories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
            includeBudget: Boolean,
            includeDebts: Boolean
        ): LoadedPersonSummaryState {
            when (personWithAccounts) {
                null -> {
                    return EmptyPersonSummaryState
                }

                else -> {
                    return FullPersonSummaryState.from(
                        personWithAccounts,
                        startDate,
                        endDate,
                        allPersons,
                        allTransactions,
                        allPromissoryNotes,
                        budgetWithCalculatedDatumAndCategories,
                        includeBudget,
                        includeDebts
                    )
                }
            }
        }
    }
}

data class ReloadingPersonSummaryState(
    override val saldoActual: Double,
    override val ingresos: Double,
    override val egresos: Double,
    override val deudasFlujo: Map<Person, Double>,
    override val deudasTotal: Double,
    override val presupuestoTotal: Double
) : LoadedPersonSummaryState {
    companion object {
        fun from(
            loadedPersonSummaryState: LoadedPersonSummaryState
        ) = loadedPersonSummaryState.run {
            ReloadingPersonSummaryState(
                saldoActual = saldoActual,
                ingresos = ingresos,
                egresos = egresos,
                deudasFlujo = deudasFlujo,
                deudasTotal = deudasTotal,
                presupuestoTotal = presupuestoTotal
            )
        }
    }
}

data class FullPersonSummaryState(
    val person: Person,
    override val saldoActual: Double,
    override val ingresos: Double,
    override val egresos: Double,
    override val deudasFlujo: Map<Person, Double>,
    override val deudasTotal: Double,
    override val presupuestoTotal: Double
) : LoadedPersonSummaryState {
    companion object {
        fun from(
            personWithAccounts: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?,
            allPersons: List<PersonWithAccounts>,
            allTransactions: List<TransactionAndDetailsAndAccounts>,
            allPromissoryNotes: List<PromissoryNote>,
            budgetWithCalculatedDatumAndCategories: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
            includeBudget: Boolean,
            includeDebts: Boolean
        ): FullPersonSummaryState {
            val deudasFlujo = allPersons.associate { otherPerson ->
                otherPerson.person to PersonDao.getFlujo(
                    personWithAccounts.person,
                    otherPerson.person,
                    allTransactions,
                    allPromissoryNotes
                )
            }
            val deudasTotal =
                deudasFlujo.filterKeys { it.debtsIncludedInTotal }.toList().sumOf { it.second }
            val presupuestoTotal =
                budgetWithCalculatedDatumAndCategories.sumOf { it.leftToPay + it.childrenLeftToPay }
            return FullPersonSummaryState(
                person = personWithAccounts.person,
                saldoActual = personWithAccounts.let {
                    PersonDao.getTotal(
                        it,
                        null,
                        null
                    )
                }
                        + if (includeBudget) {
                    presupuestoTotal
                } else {
                    0.0
                }
                        + if (includeDebts) {
                    deudasTotal
                } else {
                    0.0
                },
                ingresos = personWithAccounts.let { PersonDao.getIngresos(it, startDate, endDate) },
                egresos = personWithAccounts.let { PersonDao.getEgresos(it, startDate, endDate) },
                deudasFlujo = deudasFlujo,
                deudasTotal = deudasTotal,
                presupuestoTotal = presupuestoTotal
            )
        }
    }
}

fun loadingPersonSummaryState(): PersonSummaryState = LoadingPersonSummaryState

interface TransactionDetailsState

object LoadingTransactionsDetailsState : TransactionDetailsState

data class LoadedTransactionDetailsState(
    val transactionList: List<TransactionListItemDetails>,
    val promissoryNotesList: List<PromissoryNoteViewModel> = emptyList()
) : TransactionDetailsState {
    val documentList: List<IDocumentViewModel>

    init {
        val transactionListView = transactionList.map { TransactionDocumentViewModel(it) }
        val promissoryNoteListView = promissoryNotesList.map { PromissoryNoteDocumentViewModel(it) }
        documentList = (transactionListView + promissoryNoteListView).sortedByDescending { it.date }
    }
}

fun loadingTransactionDetailsState(): TransactionDetailsState = LoadingTransactionsDetailsState