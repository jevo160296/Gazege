package com.example.gazege.core

import com.example.gazege.core.dao.BudgetDao
import com.example.gazege.core.dsl.budgetWithCalculatedDataDSL
import com.example.gazege.core.entities.AbsoluteMonthDays
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.BudgetType
import com.example.gazege.core.entities.BudgetWithCalculatedData
import com.example.gazege.core.entities.WeekDays
import com.example.gazege.core.entities.toByteString
import com.example.gazege.core.entities.toList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.random.Random
import kotlin.random.nextInt

class BudgetTests {
    interface IExample {
        val expectedStringRepresentation: String
        val expectedInt: Int
        val calculatedStringRepresentation: String
        val calculatedInt: Int
    }

    private fun IExample.assert() {
        assertEquals(this.expectedStringRepresentation, this.calculatedStringRepresentation)
        assertEquals(this.expectedInt, this.calculatedInt)
    }

    @Test
    fun testWeekDaysStringRepresentation() {
        data class Example(
            val weekDays: WeekDays,
            override val expectedStringRepresentation: String,
            override val expectedInt: Int
        ) : IExample {
            override val calculatedStringRepresentation get() = weekDays.toInt().toByteString(7)
            override val calculatedInt get() = weekDays.toInt()
        }

        val manualExamples = listOf(
            Example(
                WeekDays(emptySet()),
                expectedStringRepresentation = "0000000",
                expectedInt = 0b0000000
            ),
            Example(
                WeekDays(setOf(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)),
                expectedStringRepresentation = "1001001",
                expectedInt = 0b1001001
            ),
            Example(
                WeekDays(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)),
                expectedStringRepresentation = "0100010",
                expectedInt = 0b0100010
            )
        )

        manualExamples.forEach { it.assert() }

        (0..0b1111111).forEach {
            val byteString = it.toString(2).padStart(7, '0')
            val example = Example(
                WeekDays.from(it),
                expectedStringRepresentation = byteString,
                expectedInt = it
            )
            example.assert()
        }
    }

    @Test
    fun testAbsoluteMonthDays() {
        data class Example(
            val absoluteMonthDays: AbsoluteMonthDays,
            override val expectedStringRepresentation: String,
            override val expectedInt: Int
        ) : IExample {
            override val calculatedStringRepresentation
                get() = absoluteMonthDays.toInt().toByteString(31)
            override val calculatedInt get() = absoluteMonthDays.toInt()
        }

        val manualExamples = listOf(
            Example(
                AbsoluteMonthDays(setOf(1, 5, 28, 30)),
                expectedStringRepresentation = "1000100000000000000000000001010",
                expectedInt = 0b1000100000000000000000000001010
            ),
            Example(
                AbsoluteMonthDays(setOf()),
                expectedStringRepresentation = "0000000000000000000000000000000",
                expectedInt = 0b0000000000000000000000000000000
            ),
            Example(
                AbsoluteMonthDays((1..31).toSet()),
                expectedStringRepresentation = "1111111111111111111111111111111",
                expectedInt = 0b1111111111111111111111111111111
            )
        )

        manualExamples.forEach { it.assert() }

        val shouldThrowError = arrayOf(
            { AbsoluteMonthDays(setOf(0)) },
            { AbsoluteMonthDays(setOf(32)) },
            { AbsoluteMonthDays(setOf(0, 32)) }
        )

        shouldThrowError.forEach { assertThrows(AssertionError::class.java) { it() } }

        val random = Random(3)

        (0..10000)
            .map { random.nextInt(0..0b1111111111111111111111111111111) }
            .forEach {
                val byteString = it.toString(2).padStart(31, '0')
                val example = Example(
                    AbsoluteMonthDays.from(it),
                    expectedStringRepresentation = byteString,
                    expectedInt = it
                )
                example.assert()
            }
    }

    @Test
    fun testDailyFrequency() {
        data class Example(
            val frequency: Int,
            val budgetStartDate: LocalDate,
            val startDate: LocalDate,
            val endDate: LocalDate,
            val expectedCantRepetitions: Int
        ) {
            val calculatedCantRepetitions
                get() = BudgetDao.calculateCantRepetitions(
                    Budget.fromDaily(
                        0, 0, 0.0, frequency, budgetStartDate,
                        budgetType = BudgetType.VARIABLE
                    ), startDate, endDate
                )

            fun assert() {
                assertEquals(expectedCantRepetitions, calculatedCantRepetitions)
            }
        }

        val examples = listOf(
            Example(
                frequency = 1,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 31
            ),
            Example(
                frequency = 2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 16
            ),
            Example(
                frequency = 3,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 11
            ),
            Example(
                frequency = 2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 2, 1),
                endDate = LocalDate.of(2023, 2, 28),
                expectedCantRepetitions = 14
            ),
            Example(
                frequency = 2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 2, 28),
                expectedCantRepetitions = 30
            ),
            Example(
                frequency = 31,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 1
            ),
            Example(
                frequency = 32,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 1
            ),
            Example(
                frequency = 100,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 1
            )
        )

        examples.forEach { it.assert() }
    }

    @Test
    fun testWeeklyFrequency() {
        data class Example(
            val id: Int,
            val frequency: Int,
            val budgetStartDate: LocalDate,
            val each: WeekDays,
            val startDate: LocalDate,
            val endDate: LocalDate,
            val expectedCantRepetitions: Int
        ) {
            val calculatedCantRepetitions
                get() = BudgetDao.calculateCantRepetitions(
                    budget, startDate, endDate
                )

            private val budget
                get() = Budget.fromWeekly(
                    id = id,
                    categoryId = 0,
                    value = 0.0,
                    frequency = frequency,
                    startDate = budgetStartDate,
                    each = each,
                    budgetType = BudgetType.VARIABLE
                )

            fun assert() {
                assertEquals(budget.toString(), expectedCantRepetitions, calculatedCantRepetitions)
            }
        }

        val examples: List<Example> = listOf(
            Example(
                0,
                1,
                LocalDate.of(2023, 1, 1),
                WeekDays((DayOfWeek.MONDAY..DayOfWeek.SUNDAY).toList().toSet()),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 31
            ),
            Example(
                1,
                2,
                LocalDate.of(2023, 1, 1),
                WeekDays((DayOfWeek.MONDAY..DayOfWeek.SUNDAY).toList().toSet()),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 17
            ),
            Example(
                2,
                2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 3
            ),
            Example(
                3,
                2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 1, 2),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 2
            ),
            Example(
                4,
                2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 1, 8),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 2
            ),
            Example(
                5,
                2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 1, 16),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 1
            ),
            Example(
                6,
                2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 2, 1),
                endDate = LocalDate.of(2023, 2, 28),
                expectedCantRepetitions = 2
            ),
            Example(
                7,
                2,
                budgetStartDate = LocalDate.of(2023, 1, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 2, 28),
                expectedCantRepetitions = 5
            ),
            Example(
                8,
                2,
                budgetStartDate = LocalDate.of(2023, 4, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 4, 1),
                endDate = LocalDate.of(2023, 4, 30),
                expectedCantRepetitions = 3
            ),
            Example(
                9,
                2,
                budgetStartDate = LocalDate.of(2023, 4, 1),
                each = WeekDays(setOf(DayOfWeek.FRIDAY)),
                startDate = LocalDate.of(2023, 4, 1),
                endDate = LocalDate.of(2023, 4, 30),
                expectedCantRepetitions = 2
            ),
            Example(
                10,
                2,
                budgetStartDate = LocalDate.of(2023, 3, 25),
                each = WeekDays(setOf(DayOfWeek.SUNDAY)),
                startDate = LocalDate.of(2023, 4, 1),
                endDate = LocalDate.of(2023, 4, 30),
                expectedCantRepetitions = 2
            ),
            Example(
                11,
                2,
                budgetStartDate = LocalDate.of(2023, 3, 25),
                each = WeekDays(setOf(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY)),
                startDate = LocalDate.of(2023, 4, 1),
                endDate = LocalDate.of(2023, 4, 30),
                expectedCantRepetitions = 4
            ),
            Example(
                11,
                2,
                budgetStartDate = LocalDate.of(2023, 4, 1),
                each = WeekDays(setOf(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY)),
                startDate = LocalDate.of(2023, 4, 1),
                endDate = LocalDate.of(2023, 4, 30),
                expectedCantRepetitions = 5
            )
        )

        examples.forEach { it.assert() }
    }

    @Test
    fun testMonthlyFrequency() {
        data class Example(
            val id: Int,
            val startDate: LocalDate,
            val endDate: LocalDate,
            val expectedCantRepetitions: Int
        ) {
            val calculatedCantRepetitions
                get() = BudgetDao.calculateCantRepetitions(
                    budget, startDate, endDate
                )

            private val budget
                get() = Budget.fromMonthly(
                    id = id,
                    categoryId = 0,
                    value = 0.0,
                    budgetType = BudgetType.VARIABLE
                )

            fun assert() {
                assertEquals(budget.toString(), expectedCantRepetitions, calculatedCantRepetitions)
            }
        }

        val examples: List<Example> = listOf(
            Example(
                id = 0,
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 1
            ),
            Example(
                id = 1,
                startDate = LocalDate.of(2023, 1, 2),
                endDate = LocalDate.of(2023, 1, 31),
                expectedCantRepetitions = 0
            ),
            Example(
                id = 2,
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 2, 1),
                expectedCantRepetitions = 2
            ),
            Example(
                id = 3,
                startDate = LocalDate.of(2023, 1, 1),
                endDate = LocalDate.of(2023, 12, 31),
                expectedCantRepetitions = 12
            ),
            Example(
                id = 4,
                startDate = LocalDate.of(2023, 1, 2),
                endDate = LocalDate.of(2023, 12, 31),
                expectedCantRepetitions = 11
            ),
            Example(
                id = 5,
                startDate = LocalDate.of(2023, 1, 2),
                endDate = LocalDate.of(2023, 12, 1),
                expectedCantRepetitions = 11
            )
        )

        examples.forEach { it.assert() }
    }
}

class BudgetCalculationTests {
    interface IExample {
        val expectedStringRepresentation: String
        val expectedInt: Int
        val calculatedStringRepresentation: String
        val calculatedInt: Int
    }

    @Test
    fun testBudgetCalculatedData() {
        val today = LocalDate.of(2023, 4, 20)
        val startDate = today.withDayOfMonth(1)
        val endDate = today.plusMonths(1L).withDayOfMonth(1).minusDays(1L)
        val budgetWithCalculatedData = budgetWithCalculatedDataDSL(today, startDate, endDate) {
            withCategory("Desayunos", "Alimentacion")
                .andBudgetDaily(-10000.0, 1, startDate, BudgetType.FIXED)
                .andSourceAccount("Efectivo")
                .addExpense(6000.0, "Gasto", startDate)
                .finish() // Total 30000

            withCategory("Almuerzos", "Alimentacion")
                .andBudgetDaily(-11000.0, 1, startDate, BudgetType.VARIABLE)
                .andSourceAccount("Banco")
                .addExpense(10000.0, "Gasto", startDate)
                .addExpense(10000.0, "Gasto", startDate)
                .finish()

            withCategory("Renta", null)
                .andBudgetMonthly(-600000.0, BudgetType.FIXED)
                .andSourceAccount("Banco")
                .addExpense(500000.0, "Pago renta", startDate)
                .finish()

            withCategory("Salario", null)
                .andBudgetMonthly(3000000.0, BudgetType.FIXED)
                .andDestinationAccount("Banco")
                .addIncome(2800000.0, "Salario", startDate)
                .finish()
        }
        val expectedBudgetWithCalculatedData = listOf(
            BudgetWithCalculatedData(
                Budget.fromDaily(0, 0, -10000.0, 1, startDate, BudgetType.FIXED),
                expectedTotalFlow = -300000.0,
                expectedFlowUntilNow = -200000.0,
                leftToPay = -294000.0,
                expectedRemainingFlow = -100000.0,
            ),
            BudgetWithCalculatedData(
                Budget.fromDaily(1, 1, -11000.0, 1, startDate, BudgetType.VARIABLE),
                expectedTotalFlow = -330000.0,
                expectedFlowUntilNow = -220000.0,
                leftToPay = -220000.0,
                expectedRemainingFlow = -110000.0
            ),
            BudgetWithCalculatedData(
                Budget.fromMonthly(2, 2, -600000.0, BudgetType.FIXED),
                expectedTotalFlow = -600000.0,
                expectedFlowUntilNow = -600000.0,
                leftToPay = -100000.0,
                expectedRemainingFlow = -0.0
            ),
            BudgetWithCalculatedData(
                Budget.fromMonthly(3, 3, 3000000.0, BudgetType.FIXED),
                expectedTotalFlow = 3000000.0,
                expectedFlowUntilNow = 3000000.0,
                leftToPay = 200000.0,
                expectedRemainingFlow = 0.0
            )
        )
    }
}
