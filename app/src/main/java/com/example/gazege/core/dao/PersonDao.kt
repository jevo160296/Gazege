package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


@Dao
interface PersonDao {
    @Transaction
    @Query(
        "SELECT * " +
                "FROM Person " +
                "ORDER BY name"
    )
    fun getAll(): Flow<List<Person>>

    @Insert
    suspend fun insertAll(vararg persons: Person): List<Long>

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person): Int

    companion object {
        private fun calculateValues(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ) {
            val newRange = Pair(startDate, endDate)
            if (newRange != person.range) {
                val selfAccounts =
                    person.accounts.map { it.accountAndOwnerWithTransactions.account }
                person.total = person.accounts
                    .filter { it.accountAndOwnerWithTransactions.account.includedInTotal }
                    .sumOf {
                        AccountDao.getTotal(
                            it.accountAndOwnerWithTransactions,
                            startDate,
                            endDate
                        )
                    }
                person.ingresos = person.accounts
                    .sumOf {
                        AccountDao.calculateIngresos(
                            it.accountAndOwnerWithTransactions,
                            startDate,
                            endDate,
                            selfAccounts
                        )
                    }
                person.egresos = person.accounts
                    .sumOf {
                        AccountDao.calculateEgresos(
                            it.accountAndOwnerWithTransactions,
                            startDate,
                            endDate,
                            selfAccounts
                        )
                    }
                person.range = newRange
            }
        }

        fun getTotal(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            calculateValues(person, startDate, endDate)
            return person.total
        }

        fun getIngresos(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            calculateValues(person, startDate, endDate)
            return person.ingresos
        }

        fun getEgresos(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            calculateValues(person, startDate, endDate)
            return person.egresos
        }

        private fun calculateFlujo(
            from: Person,
            to: Person,
            transacciones: List<TransactionAndAccounts>
        ): Double {
            data class TransactionAndPerson(
                val sourcePersonId: Int,
                val destinationPersonId: Int,
                val sourceAccountIsIncome: Boolean,
                val sourceAccountIsOutcome: Boolean,
                val destinationAccountIsIncome: Boolean,
                val destinationAccountIsOutcome: Boolean,
                val value: Double,
            )

            val mappedTransactions: List<TransactionAndPerson> = listOf(
                transacciones
                    .filter { it.transaction.aNombreDe == null }
                    .map {
                        TransactionAndPerson(
                            sourcePersonId = it.sourceAccount.ownerId,
                            destinationPersonId = it.destinationAccount.ownerId,
                            value = it.transaction.amount,
                            sourceAccountIsIncome = it.sourceAccount.isIncome,
                            sourceAccountIsOutcome = it.sourceAccount.isOutcome,
                            destinationAccountIsIncome = it.destinationAccount.isIncome,
                            destinationAccountIsOutcome = it.destinationAccount.isOutcome
                        )
                    },
                transacciones
                    .filter { it.transaction.aNombreDe != null }
                    .map {
                        TransactionAndPerson(
                            sourcePersonId = it.transaction.aNombreDe ?: -1,
                            destinationPersonId = it.destinationAccount.ownerId,
                            value = it.transaction.amount,
                            sourceAccountIsIncome = false,
                            sourceAccountIsOutcome = false,
                            destinationAccountIsIncome = it.destinationAccount.isIncome,
                            destinationAccountIsOutcome = it.destinationAccount.isOutcome
                        )
                    },
                transacciones
                    .filter { it.transaction.aNombreDe != null }
                    .map {
                        TransactionAndPerson(
                            sourcePersonId = it.sourceAccount.ownerId,
                            destinationPersonId = it.transaction.aNombreDe ?: -1,
                            value = it.transaction.amount,
                            sourceAccountIsIncome = it.sourceAccount.isIncome,
                            sourceAccountIsOutcome = it.sourceAccount.isOutcome,
                            destinationAccountIsIncome = false,
                            destinationAccountIsOutcome = false
                        )
                    }
            ).flatten()
            val inTransactions = mappedTransactions
                .filter { it.sourcePersonId == to.id && it.destinationPersonId == from.id && !it.sourceAccountIsIncome && !it.sourceAccountIsOutcome }
            val outTransactions = mappedTransactions
                .filter { it.sourcePersonId == from.id && it.destinationPersonId == to.id && !it.destinationAccountIsIncome && !it.destinationAccountIsOutcome }

            val totalIn = inTransactions.sumOf { it.value }
            val totalOut = outTransactions.sumOf { it.value }

            return totalOut - totalIn
        }

        /**
         * Total entregado por esta persona a la otra persona (Negativo si la otra persona le entregó
         * dinero).
         */
        fun getFlujo(
            person: PersonWithAccounts,
            otherPersonWithAccounts: PersonWithAccounts,
            transacciones: List<TransactionAndAccounts>
        ): Double {
            val backedFlujo = person.flujos[otherPersonWithAccounts.person]
            val flujo = if (backedFlujo == null) {
                val calculatedFlujo =
                    calculateFlujo(person.person, otherPersonWithAccounts.person, transacciones)
                person.flujos[otherPersonWithAccounts.person] = calculatedFlujo
                calculatedFlujo
            } else {
                backedFlujo
            }
            return flujo
        }
    }
}