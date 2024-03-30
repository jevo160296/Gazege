package com.jmml.gazege.extensions.list

import com.jmml.gazege.extensions.map.plus
import java.time.LocalDate

fun <E> List<E>.mapSumOf(function: (E) -> Map<LocalDate, Double>): Map<LocalDate, Double> =
    fold(emptyMap()) { acc, e -> acc + function(e) }