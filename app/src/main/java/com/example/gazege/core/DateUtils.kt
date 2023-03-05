package com.example.gazege.core

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): LocalDate? {
        val instant = dateLong?.let { Instant.ofEpochMilli(it) }
        return instant?.atOffset(ZoneOffset.ofHours(-5))?.toLocalDate()
    }

    @TypeConverter
    fun fromDate(date: LocalDate?): Long? {
        return date
            ?.atStartOfDay()
            ?.toInstant(ZoneOffset.ofHours(-5))
            ?.toEpochMilli()
    }
}

/**
 * Devuelve una copia de date con months reducidos, si es el último día del mes
 * actual devuelve el último día del mes anterior.
 */
fun stableMinusMonths(date: LocalDate, months: Long): LocalDate {
    return date.plusDays(1L).minusMonths(months).minusDays(1L)
}

/**
 * Devuelve una copia de date con months añadidos.
 */
fun stablePlusMonths(date: LocalDate, months: Long): LocalDate {
    return date.plusMonths(months)
}


/**
 * Devuelve una copia de date con el último dia del mes.
 */
fun lastDayOfMonth(date: LocalDate): LocalDate {
    return date.withDayOfMonth(1).plusMonths(1L).minusDays(1L)
}

fun firstDayOfMonth(date: LocalDate): LocalDate {
    return date.withDayOfMonth(1)
}

fun dateBetween(date: LocalDate, startDate: LocalDate?, endDate: LocalDate?): Boolean {
    return date >= startDate && date <= endDate
}