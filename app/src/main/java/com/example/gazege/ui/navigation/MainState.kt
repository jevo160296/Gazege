package com.example.gazege.ui.navigation

import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.TransactionAndAccounts
import com.example.gazege.core.entities.TransactionListItemDetails
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
            allTransactions: List<TransactionAndAccounts>,
            budgetWithCalculatedDatumAndCategories: List<BudgetWithCalculatedDataAndCategory>,
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
                        budgetWithCalculatedDatumAndCategories,
                        includeBudget,
                        includeDebts
                    )
                }
            }
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
            allTransactions: List<TransactionAndAccounts>,
            budgetWithCalculatedDatumAndCategories: List<BudgetWithCalculatedDataAndCategory>,
            includeBudget: Boolean,
            includeDebts: Boolean
        ): FullPersonSummaryState {
            val deudasFlujo = allPersons.associate { otherPerson ->
                otherPerson.person to PersonDao.getFlujo(
                    personWithAccounts,
                    otherPerson,
                    allTransactions
                )
            }
            val deudasTotal = deudasFlujo.toList().sumOf { it.second }
            val presupuestoTotal =
                budgetWithCalculatedDatumAndCategories.sumOf { it.budgetLeftToPayFromToday }
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
    val transactionList: List<TransactionListItemDetails>
) : TransactionDetailsState

fun loadingTransactionDetailsState(): TransactionDetailsState = LoadingTransactionsDetailsState