package com.jmml.gazege.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun clockFlow() = flow<LocalDateTime> {
    while (true) {
        LocalDateTime.now().let { now ->
            emit(now)
            val endOfDay = now.truncatedTo(ChronoUnit.DAYS).plusDays(1).minusNanos(1L)
            delay(ChronoUnit.MILLIS.between(now, endOfDay))
        }
    }
}