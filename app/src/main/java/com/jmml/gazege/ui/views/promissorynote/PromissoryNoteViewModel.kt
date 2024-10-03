package com.jmml.gazege.ui.views.promissorynote

import com.jmml.gazege.core.dao.PersonDao
import com.jmml.gazege.core.entities.Person
import com.jmml.gazege.core.entities.PromissoryNote

sealed interface IPromissoryNoteViewModel {
    val principalPersonId: Int?
    val promissoryNote: PromissoryNote
    val sourcePerson: Person
    val destinationPerson: Person
}

data class PromissoryNoteViewModel(
    override val principalPersonId: Int?,
    override val promissoryNote: PromissoryNote,
    override val sourcePerson: Person,
    override val destinationPerson: Person
) : IPromissoryNoteViewModel {
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

data class PromissoryNoteWithSignViewModel(
    override val principalPersonId: Int?,
    override val promissoryNote: PromissoryNote,
    override val sourcePerson: Person,
    override val destinationPerson: Person,
    val sign: Int
) : IPromissoryNoteViewModel {
    companion object {
        fun from(
            promissoryNotes: List<PromissoryNote>,
            personList: List<Person>,
            principalPersonId: Int?,
            toPersonId: Int?
        ): List<PromissoryNoteWithSignViewModel> = PromissoryNoteViewModel.from(
            promissoryNotes = promissoryNotes,
            personList = personList,
            principalPersonId = principalPersonId
        )
            .map { details ->
                val sign = PersonDao.direction(
                    fromPersonId = principalPersonId,
                    toPersonId = toPersonId,
                    promissoryNote = details.promissoryNote
                )
                PromissoryNoteWithSignViewModel(
                    principalPersonId = details.principalPersonId,
                    promissoryNote = details.promissoryNote,
                    sourcePerson = details.sourcePerson,
                    destinationPerson = details.destinationPerson,
                    sign = sign
                )
            }
    }
}