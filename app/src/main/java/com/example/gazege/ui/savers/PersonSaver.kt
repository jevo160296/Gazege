package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Person
import kotlinx.parcelize.Parcelize

data class PartialPerson(
    var id: Int?,
    var name: String?,
    var importance: Int?
): PartialEntity<Person>
{
    override fun isComplete(): Boolean{
        return name != null
    }

    override fun toFull(): Person {
        if(isComplete()){
            return Person(
                id = id,
                name = name!!,
                importance = importance
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun blankEntity(): PartialPerson {
            return PartialPerson(null, null, null)
        }
    }
}

@Parcelize
data class ParcelablePerson(
    val id: Int?,
    val name: String?,
    val importance: Int?
) : Parcelable {
    fun toPartial(): PartialPerson {
        return PartialPerson(
            id = id,
            name = name,
            importance = importance
        )
    }
}

val personSaver = Saver<PartialPerson, ParcelablePerson>(
    save = { state ->
        ParcelablePerson(id = state.id, name = state.name, importance = state.importance)
    },
    restore = {
        PartialPerson(id = it.id, name = it.name, importance = it.importance)
    }
)