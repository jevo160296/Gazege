package com.example.gazege.core

import java.time.LocalDate

/**
 * Devuelve una copia de date con months reducidos, si es el último día del mes
 * actual devuelve el último día del mes anterior.
 */
fun stableMinusMonths(date: LocalDate, months: Long): LocalDate {
    return date.plusDays(1L).minusMonths(months).minusDays(1L)
}

/**
 * Devuelve una copia de date con months añadidos, si es el último día del mes actual devuelve
 * el último día del mes siguiente.
 */
fun stablePlusMonths(date: LocalDate, months: Long): LocalDate {
    return date.plusDays(1L).plusMonths(months).minusDays(1L)
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
    return (startDate == null || date >= startDate) && (endDate == null || date <= endDate)
}