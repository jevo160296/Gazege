package com.jmml.gazege.ui.views.promissorynote

import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PromissoryNote

data class PromissoryNoteViewModel(
    val principalPersonId: Int?,
    val promissoryNote: PromissoryNote,
    val sourcePerson: Person,
    val destinationPerson: Person
){
    companion object{
        fun from(
            promissoryNotes: List<PromissoryNote>,
            personList: List<Person>,
            principalPersonId: Int?
        ): List<PromissoryNoteViewModel> =
            personList.associateBy { it.id }.let{ personMap ->
                promissoryNotes.map { promissoryNote ->
                    val sourcePerson = personMap[promissoryNote.sourceId]
                    val destinationPerson = personMap[promissoryNote.destinationId]
                    if(sourcePerson != null && destinationPerson != null){
                        PromissoryNoteViewModel(
                            principalPersonId = principalPersonId,
                            promissoryNote = promissoryNote,
                            sourcePerson = sourcePerson,
                            destinationPerson = destinationPerson
                        )
                    }
                    else {
                        throw NoSuchElementException("sourcePerson or destinationPerson not found.")
                    }
                }
            }
    }
}