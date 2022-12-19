package com.example.gazege.ui.savers

interface PartialEntity<T> {
    fun isComplete(): Boolean
    fun toFull(): T
}