package com.example.gazege.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.TransactionAndAccounts
import kotlinx.coroutines.flow.Flow

data class TransactionAndPerson(
    val sourcePersonId: Int,
    val destinationPersonId: Int,
    val sourceAccountIsIncome: Boolean,
    val sourceAccountIsOutcome: Boolean,
    val destinationAccountIsIncome: Boolean,
    val destinationAccountIsOutcome: Boolean,
    val value: Double,
)

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
        fun calculateFlujo(
            from: Person,
            to: Person,
            transacciones: List<TransactionAndAccounts>
        ): Double {
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
    }
}