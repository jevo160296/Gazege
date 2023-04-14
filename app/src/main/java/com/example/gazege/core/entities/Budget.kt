package com.example.gazege.core.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val categoryId: Int,
    val value: Double,
    val each: Int,
    val frequency: Int,
    val frequencyType: FrequencyType,
    val budgetType: BudgetType,
    val startDate: LocalDate
) {
    @Ignore
    val eachClass = when (frequencyType) {
        FrequencyType.DAILY -> null
        FrequencyType.MONTHLY -> AbsoluteMonthDays.from(each)
        FrequencyType.WEEKLY -> WeekDays.from(each)
    }

    companion object {
        fun fromDaily(
            id: Int? = null,
            categoryId: Int,
            value: Double,
            frequency: Int,
            startDate: LocalDate,
            budgetType: BudgetType
        ): Budget = Budget(
            id = id,
            categoryId = categoryId,
            value = value,
            each = 1,
            frequency = frequency,
            frequencyType = FrequencyType.DAILY,
            startDate = startDate,
            budgetType = budgetType
        )

        fun fromWeekly(
            id: Int? = null,
            categoryId: Int,
            value: Double,
            each: WeekDays,
            frequency: Int,
            startDate: LocalDate,
            budgetType: BudgetType
        ): Budget = Budget(
            id = id,
            categoryId = categoryId,
            value = value,
            each = each.toInt(),
            frequency = frequency,
            frequencyType = FrequencyType.WEEKLY,
            startDate = startDate,
            budgetType = budgetType
        )

        @Deprecated(
            message = "Esta función aún no está lista",
            replaceWith = ReplaceWith("Expresion"),
            level = DeprecationLevel.HIDDEN
        )
        fun fromMonthlyAbsoluteDays(
            id: Int? = null,
            categoryId: Int,
            value: Double,
            each: AbsoluteMonthDays,
            frequency: Int,
            startDate: LocalDate,
            budgetType: BudgetType
        ): Budget = Budget(
            id = id,
            categoryId = categoryId,
            value = value,
            each = each.toInt(),
            frequency = frequency,
            frequencyType = FrequencyType.MONTHLY,
            startDate = startDate,
            budgetType = budgetType
        )

        fun fromMonthly(
            id: Int? = null,
            categoryId: Int,
            value: Double,
            budgetType: BudgetType
        ): Budget = Budget(
            id = id,
            categoryId = categoryId,
            value = value,
            startDate = LocalDate.of(1900, 1, 1),
            each = AbsoluteMonthDays(setOf(1)).toInt(),
            frequency = 1,
            frequencyType = FrequencyType.MONTHLY,
            budgetType = budgetType
        )
    }
}

enum class FrequencyType {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class BudgetType {
    FIXED,
    VARIABLE
}

fun Boolean.toByte(position: Int) = (if (this) {
    0b1
} else {
    0b0
}).shl(position)

fun Int.toByteString(length: Int) = this.toString(2).padStart(length, '0')

interface IEach {
    fun toInt(): Int
    fun toByteString(): String
}

data class WeekDays(
    val days: Set<DayOfWeek>
) : IEach {
    override fun toInt() = days.sumOf { it.toByte() }

    override fun toByteString(): String = this.toInt().toByteString(7)

    companion object {
        fun from(value: Int): WeekDays = value
            .toByteString(7)
            .map {
                when (it) {
                    '1' -> true
                    '0' -> false
                    else -> true
                }
            }
            .toTypedArray()
            .let {
                WeekDays(it
                    .zip(
                        listOf(
                            DayOfWeek.SUNDAY,
                            DayOfWeek.MONDAY,
                            DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY,
                            DayOfWeek.THURSDAY,
                            DayOfWeek.FRIDAY,
                            DayOfWeek.SATURDAY
                        )
                    )
                    .filter { pair -> pair.first }
                    .map { pair -> pair.second }
                    .toSet())
            }

        private fun DayOfWeek.toByte() = when (this) {
            DayOfWeek.SUNDAY -> 0b1000000
            DayOfWeek.MONDAY -> 0b0100000
            DayOfWeek.TUESDAY -> 0b0010000
            DayOfWeek.WEDNESDAY -> 0b0001000
            DayOfWeek.THURSDAY -> 0b0000100
            DayOfWeek.FRIDAY -> 0b0000010
            DayOfWeek.SATURDAY -> 0b0000001
        }
    }
}

data class AbsoluteMonthDays(
    val days: Set<Int>
) : IEach {
    init {
        var isValid = days.all { it in 1..31 }
        // TODO La siguiente linea se ingresa para desactivar los multiples días
        isValid = isValid && days.size == 1
        if (!isValid) {
            throw AssertionError("Los días deben estar entre 1 y 31")
        }
    }

    override fun toInt() = (0..30)
        .reversed()
        .sumOf { index ->
            val day = 31 - index
            (day in days).toByte(index)
        }

    override fun toByteString(): String = this.toInt().toByteString(31)

    companion object {
        fun from(value: Int): AbsoluteMonthDays = value
            .toByteString(31)
            .mapIndexedNotNull { index, char ->
                val day = index + 1
                when (char) {
                    '1' -> day
                    else -> null
                }
            }
            .let {
                AbsoluteMonthDays(it.toSet())
            }
    }
}

inline fun <reified T : Enum<T>> ClosedRange<T>.toList(): List<T> {
    val values = enumValues<T>()
    return (this.start.ordinal..this.endInclusive.ordinal)
        .map { values[it] }
}
