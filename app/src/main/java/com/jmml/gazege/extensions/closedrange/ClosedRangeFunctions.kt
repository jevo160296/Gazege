package com.jmml.gazege.extensions.closedrange

inline fun <reified T : Enum<T>> ClosedRange<T>.toList(): List<T> {
    val values = enumValues<T>()
    return (this.start.ordinal..this.endInclusive.ordinal)
        .map { values[it] }
}

fun <T : Comparable<T>> ClosedRange<T>.toSequence(next: (T) -> T): Sequence<T> =
    generateSequence(start) { next(it).takeIf { current -> contains(current) } }

operator fun <T : Comparable<T>> ClosedRange<T>?.plus(other: ClosedRange<T>?): ClosedRange<T>? =
    if (this != null && other != null) {
        minOf(start, other.start)..maxOf(endInclusive, other.endInclusive)
    } else {
        this ?: other
    }