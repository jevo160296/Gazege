package com.jmml.gazege.ui.savers

interface PartialEntity<T> {
    fun isComplete(): Boolean
    fun toFull(): T
}