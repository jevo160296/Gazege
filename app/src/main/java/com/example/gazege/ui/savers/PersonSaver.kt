package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Person
import kotlinx.parcelize.Parcelize

data class PartialPerson(
    var id: Int? = null,
    var name: String? = null
): PartialEntity<Person>
{
    override fun isComplete(): Boolean{
        return name != null
    }

    override fun toFull(): Person {
        if(isComplete()){
            return Person(
                id = id,
                name = name!!
            )
        }
        else{
            throw Exception()
        }
    }
}

@Parcelize
data class ParcelablePerson(
    val id: Int?,
    val name: String?
) : Parcelable {
    fun toPartial(): PartialPerson {
        return PartialPerson(
            id = id,
            name = name
        )
    }
}

val personSaver = Saver<PartialPerson, ParcelablePerson>(
    save = { state ->
        ParcelablePerson(id = state.id, name = state.name)
    },
    restore = {
        PartialPerson(id = it.id, name = it.name)
    }
)