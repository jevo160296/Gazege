package com.jmml.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.jmml.gazege.core.entities.PromissoryNote
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class PartialPromissoryNote(
    var id: Int?,
    var amount: Double?,
    var date: LocalDate?,
    var sourceId: Int?,
    var destinationId: Int?,
    var description: String?
) : PartialEntity<PromissoryNote> {
    override fun isComplete(): Boolean {
        return amount != null &&
                date != null &&
                sourceId != null &&
                destinationId != null &&
                description != null
    }

    override fun toFull(): PromissoryNote {
        if (isComplete()) {
            return PromissoryNote(
                id = id,
                amount = amount!!,
                date = date!!,
                sourceId = sourceId!!,
                destinationId = destinationId!!,
                description = description!!
            )
        } else {
            throw Exception("PromissoryNote conversion failed.")
        }
    }

    companion object {
        fun from(promissoryNote: PromissoryNote): PartialPromissoryNote = promissoryNote.run {
            PartialPromissoryNote(
                id = id,
                amount = amount,
                date = date,
                sourceId = sourceId,
                destinationId = destinationId,
                description = description
            )
        }
    }
}

@Parcelize
data class ParcelablePromissoryNote(
    var id: Int?,
    var amount: Double?,
    var date: LocalDate?,
    var sourceId: Int?,
    var destinationId: Int?,
    var description: String?
) : Parcelable

val promissoryNoteSaver = Saver<PartialPromissoryNote, ParcelablePromissoryNote>(
    save = { state ->
        ParcelablePromissoryNote(
            id = state.id,
            date = state.date,
            amount = state.amount,
            description = state.description,
            destinationId = state.destinationId,
            sourceId = state.sourceId
        )
    },
    restore = { state ->
        PartialPromissoryNote(
            id = state.id,
            date = state.date,
            amount = state.amount,
            description = state.description,
            destinationId = state.destinationId,
            sourceId = state.sourceId
        )
    }
)