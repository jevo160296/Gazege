package com.example.gazege

import com.example.gazege.core.dao.PersonDao
import com.example.gazege.core.entities.*
import org.junit.Assert.assertArrayEquals
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
                categoryId = null,
                aNombreDe = null
            )
        }

        val transactionsAndAccounts = TransactionAndAccounts.from(
            transactions, accounts
        )

        val accountAndOwnerWithTransactions = AccountAndOwnerWithTransactions.from(
            accounts, persons, transactions
        )

        val personWithAccounts: List<PersonWithAccounts> = PersonWithAccounts.from(
            persons,
            accountAndOwnerWithTransactions.map {
                AccountAndOwnerWithTransactionsAndPockets.from(it, accountAndOwnerWithTransactions)
            }
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

    @Test
    fun testCategoryWithSubcategories() {
        val expected = listOf(
            Category(0, "", null).let {
                CategoryWithSubCategories(
                    it,
                    listOf(
                        CategoryWithSubCategories(Category(5, "", it.id), listOf()),
                        CategoryWithSubCategories(Category(6, "", it.id), listOf()),
                        Category(7, "", it.id).let {
                            CategoryWithSubCategories(
                                it, listOf(
                                    CategoryWithSubCategories(Category(8, "", it.id), listOf()),
                                    CategoryWithSubCategories(Category(9, "", it.id), listOf())
                                )
                            )
                        }
                    )
                )
            },
            CategoryWithSubCategories(Category(1, "", null), listOf()),
            CategoryWithSubCategories(Category(2, "", null), listOf()),
            CategoryWithSubCategories(Category(3, "", null), listOf()),
            CategoryWithSubCategories(Category(4, "", null), listOf())
        )
        val categoriesDsl = categories {
            category(0, "") {
                category(5, "") {}
                category(6, "") {}
                category(7, "") {
                    category(8, "") {}
                    category(9, "") {}
                }
            }
            category(1, "") {}
            category(2, "") {}
            category(3, "") {}
            category(4, "") {}
        }
        val categories: List<CategoryWithSubCategories> = listOf(
            Category(0, "", null),
            Category(1, "", null),
            Category(2, "", null),
            Category(3, "", null),
            Category(4, "", null),
            Category(5, "", 0),
            Category(6, "", 0),
            Category(7, "", 0),
            Category(8, "", 7),
            Category(9, "", 7)
        )
            .let { CategoryWithSubCategories.from(it) }
        assertArrayEquals(expected.toTypedArray(), categories.toTypedArray())
        assertArrayEquals(expected.toTypedArray(), categoriesDsl.toTypedArray())
    }
}