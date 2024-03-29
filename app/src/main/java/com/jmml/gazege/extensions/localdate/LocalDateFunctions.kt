package com.jmml.gazege.extensions.localdate

import java.time.LocalDate

/**
 * Devuelve una copia de date con months reducidos, si es el último día del mes
 * actual devuelve el último día del mes anterior.
 */
fun LocalDate.stableMinusMonths(months: Long): LocalDate =
    plusDays(1L)
        .minusMonths(months)
        .minusDays(1L)

/**
 * Devuelve una copia de date con months añadidos, si es el último día del mes actual devuelve
 * el último día del mes siguiente.
 */
fun LocalDate.stablePlusMonths(months: Long): LocalDate =
    plusDays(1L)
        .plusMonths(months)
        .minusDays(1L)

/**
 * Devuelve el último día del mes.
 */
fun LocalDate.endOfMonth(): LocalDate =
    withDayOfMonth(1)
        .plusMonths(1L)
        .minusDays(1L)

/**
 * Devuelve el primer día del mes.
 */
fun LocalDate.startOfMonth(): LocalDate = withDayOfMonth(1)

/**
 * Devuelve true si la fecha está entre startDate y endDate.
 */
fun LocalDate.isBetween(startDate: LocalDate?, endDate: LocalDate?): Boolean =
    (startDate == null || this >= startDate) && (endDate == null || this <= endDate)