package com.jmml.gazege.core.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["name"], unique = true)]
)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val importance: Int? = null
) {
    companion object {
        fun empty(): Person {
            return Person(name = "Null")
        }
    }
}