package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Person
import kotlinx.parcelize.Parcelize

val personSaver = Saver<Person, ParcelablePerson>(
    save = { state ->
        ParcelablePerson(id = state.id, name = state.name)
    },
    restore = {
        Person(id = it.id, name = it.name)
    }
)

@Parcelize
data class ParcelablePerson(
    val id: Int?,
    val name: String
) : Parcelable