package com.jmml.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PersonWithAccounts
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.core.entities.TransactionAndDetailsAndAccounts
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
    suspend fun updateAll(vararg person: Person)

    @Delete
    suspend fun delete(person: Person): Int

    @Query(
        "DELETE FROM Person"
    )
    suspend fun deleteAll()

    companion object {
        private fun calculateValues(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ) {
            val newRange = Pair(startDate, endDate)
            if (
                !person.totalCache.containsKey(newRange) ||
                !person.ingresosCache.containsKey(newRange) ||
                !person.egresosCache.containsKey(newRange)

            ) {
                val selfAccounts =
                    person.accounts.map { it.accountAndOwnerWithTransactions.account }
                if (!person.totalCache.containsKey(newRange)) {
                    person.totalCache[newRange] = person.accounts
                        .filter { it.accountAndOwnerWithTransactions.account.includedInTotal }
                        .sumOf {
                            AccountDao.getTotal(
                                it.accountAndOwnerWithTransactions,
                                startDate,
                                endDate
                            )
                        }
                }
                if (!person.ingresosCache.containsKey(newRange)) {
                    person.ingresosCache[newRange] = person.accounts
                        .sumOf {
                            AccountDao.calculateIngresos(
                                it.accountAndOwnerWithTransactions,
                                startDate,
                                endDate,
                                selfAccounts
                            )
                        }
                }
                if (!person.egresosCache.containsKey(newRange)) {
                    person.egresosCache[newRange] = person.accounts
                        .sumOf {
                            AccountDao.calculateEgresos(
                                it.accountAndOwnerWithTransactions,
                                startDate,
                                endDate,
                                selfAccounts
                            )
                        }
                }
            }
        }

        fun getTotal(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            calculateValues(person, startDate, endDate)
            return person.totalCache[Pair(startDate, endDate)] ?: 0.0
        }

        fun getIngresos(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            calculateValues(person, startDate, endDate)
            return person.ingresosCache[Pair(startDate, endDate)] ?: 0.0
        }

        fun getEgresos(
            person: PersonWithAccounts,
            startDate: LocalDate?,
            endDate: LocalDate?
        ): Double {
            calculateValues(person, startDate, endDate)
            return person.egresosCache[Pair(startDate, endDate)] ?: 0.0
        }

        fun direction(
            fromPersonId: Int?,
            toPersonId: Int?,
            transaction: TransactionAndDetailsAndAccounts
        ): Int {
            val sameDirection = transaction.transaction.aNombreDe == null &&
                    transaction.sourceAccount.ownerId == fromPersonId &&
                    transaction.destinationAccount.ownerId == toPersonId &&
                    !transaction.sourceAccount.isIncome &&
                    !transaction.destinationAccount.isOutcome ||
                    transaction.transaction.aNombreDe != null &&
                    transaction.sourceAccount.ownerId == fromPersonId &&
                    transaction.transaction.aNombreDe == toPersonId &&
                    !transaction.sourceAccount.isIncome ||
                    transaction.transaction.aNombreDe != null &&
                    transaction.transaction.aNombreDe == fromPersonId &&
                    transaction.destinationAccount.ownerId == toPersonId &&
                    !transaction.destinationAccount.isOutcome
            val differentDirection = transaction.transaction.aNombreDe == null &&
                    transaction.sourceAccount.ownerId == toPersonId &&
                    transaction.destinationAccount.ownerId == fromPersonId &&
                    !transaction.sourceAccount.isIncome &&
                    !transaction.destinationAccount.isOutcome ||
                    transaction.transaction.aNombreDe != null &&
                    transaction.sourceAccount.ownerId == toPersonId &&
                    transaction.transaction.aNombreDe == fromPersonId &&
                    !transaction.sourceAccount.isIncome ||
                    transaction.transaction.aNombreDe != null &&
                    transaction.transaction.aNombreDe == toPersonId &&
                    transaction.destinationAccount.ownerId == fromPersonId &&
                    !transaction.destinationAccount.isOutcome
            return if (sameDirection && !differentDirection) 1
            else if (!sameDirection && differentDirection) -1
            else 0
        }

        fun direction(
            fromPersonId: Int?,
            toPersonId: Int?,
            promissoryNote: PromissoryNote
        ): Int {
            val sameDirection = fromPersonId == promissoryNote.sourceId &&
                    toPersonId == promissoryNote.destinationId
            val differentDirection = fromPersonId == promissoryNote.destinationId &&
                    toPersonId == promissoryNote.sourceId
            return if (sameDirection && !differentDirection) -1
            else if (!sameDirection && differentDirection) 1
            else 0
        }

        private fun calculateFlujo(
            from: Person,
            to: Person,
            transacciones: List<TransactionAndDetailsAndAccounts>,
            promissoryNotes: List<PromissoryNote>
        ): Double {
            val valuesFromTransactions =
                transacciones.map { it to direction(from.id, to.id, it) * it.transaction.amount }
            val valuesFromPromissoryNotes =
                promissoryNotes.map { it to direction(from.id, to.id, it) * it.amount }
            return valuesFromTransactions.sumOf { it.second } +
                    valuesFromPromissoryNotes.sumOf { it.second }
        }

        /**
         * Total entregado por esta persona a la otra persona (Negativo si la otra persona le entregó
         * dinero).
         */
        fun getFlujo(
            person: Person,
            otherPersonWithAccounts: Person,
            transacciones: List<TransactionAndDetailsAndAccounts>,
            promissoryNotes: List<PromissoryNote>
        ): Double {
            return calculateFlujo(person, otherPersonWithAccounts, transacciones, promissoryNotes)
        }
    }
}