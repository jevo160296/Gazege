package com.jmml.gazege.extensions.map

fun <K, U, V, W> Map<K, U>.merge(
    other: Map<K, V>,
    merger: (first: U?, second: V?) -> W
): Map<K, W> =
    (this.keys + other.keys).associateWith { merger(this[it], other[it]) }

operator fun <K> Map<K, Double>.plus(other: Map<K, Double>) = merge(other) { first, second ->
    (first ?: 0.0) + (second ?: 0.0)
}