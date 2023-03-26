package com.example.gazege

import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.Account
import com.example.gazege.core.entities.AccountAndOwnerWithTransactions
import com.example.gazege.core.entities.Person
import com.example.gazege.core.entities.PersonWithAccounts
import com.example.gazege.core.entities.Transaction
import com.example.gazege.core.entities.TransactionAndAccounts
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DataModelTests {
    @Test
    fun testDebts() {
        val persons: List<Person> = arrayOf(
            "Pedro",
            "Juana",
            "Andres",
            "Paola"
        ).mapIndexed { index, name -> Person(id = index, name = name) }
        val accounts: List<Account> = arrayOf(
            Pair(0, "Pedro1"),
            Pair(0, "Pedro2"),
            Pair(1, "Juana1"),
            Pair(1, "Juana2"),
            Pair(2, "Andres1"),
            Pair(3, "Paola1")
        ).mapIndexed { index, pair ->
            Account(
                id = index,
                ownerId = pair.first,
                name = pair.second
            )
        }
        val transactions: List<Transaction> = arrayOf(
            //Transaction(source, destination, amount)
            Triple(0, 2, 10000.0),
            Triple(1, 2, 20000.0),
            Triple(2, 4, 5000.0),
            Triple(4, 5, 2000.0),
            Triple(5, 2, 4000.0)
        ).mapIndexed { index, triple ->
            Transaction(
                id = index,
                amount = triple.third,
                description = "",
                sourceId = triple.first,
                destinationId = triple.second,
                date = LocalDate.now(),
                aNombreDe = null
            )
        }

        val transactionsAndAccounts = TransactionAndAccounts.from(
            transactions, accounts
        )

        val personWithAccounts: List<PersonWithAccounts> = PersonWithAccounts.from(
            persons,
            AccountAndOwnerWithTransactions.from(
                accounts,
                persons,
                transactions
            )
        )

        val flows: MutableMap<Pair<String, String>, Double> = mutableMapOf()

        for (principalPerson in personWithAccounts) {
            for (otherPerson in personWithAccounts) {
                val key = Pair(principalPerson.person.name, otherPerson.person.name)
                flows[key] =
                    PersonDao.getFlujo(principalPerson, otherPerson, transactionsAndAccounts)
            }
        }

        val expectedFlows: Map<Pair<String, String>, Double> = mapOf(
            Pair("Pedro", "Pedro") to 0.0,
            Pair("Pedro", "Juana") to 30000.0,
            Pair("Pedro", "Andres") to 0.0,
            Pair("Pedro", "Paola") to 0.0,
            Pair("Juana", "Pedro") to -30000.0,
            Pair("Juana", "Juana") to 0.0,
            Pair("Juana", "Andres") to 5000.0,
            Pair("Juana", "Paola") to -4000.0,
            Pair("Andres", "Pedro") to 0.0,
            Pair("Andres", "Juana") to -5000.0,
            Pair("Andres", "Andres") to 0.0,
            Pair("Andres", "Paola") to 2000.0,
            Pair("Paola", "Pedro") to 0.0,
            Pair("Paola", "Juana") to 4000.0,
            Pair("Paola", "Andres") to -2000.0,
            Pair("Paola", "Paola") to 0.0
        )
        assertTrue(flows == expectedFlows)
    }
}