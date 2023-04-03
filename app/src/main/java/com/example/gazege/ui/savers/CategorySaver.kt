package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.example.gazege.core.entities.Category
import kotlinx.parcelize.Parcelize

data class PartialCategory(
    var id: Int?,
    var name: String?,
    var parentId: Int?
) : PartialEntity<Category> {
    override fun isComplete(): Boolean {
        return name != null
    }

    override fun toFull(): Category {
        if (isComplete()) {
            return Category(
                id = id,
                name = name!!,
                parentId = parentId
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun blankEntity(): PartialCategory {
            return PartialCategory(null, null, null)
        }

        fun from(category: Category): PartialCategory = category
            .run { PartialCategory(id, name, parentId) }
    }
}

@Parcelize
data class ParcelableCategory(
    val id: Int?,
    val name: String?,
    val parentId: Int?
) : Parcelable {
    fun toPartial(): PartialCategory {
        return PartialCategory(
            id = id,
            name = name,
            parentId = parentId
        )
    }
}

val categorySaver = Saver<PartialCategory, ParcelableCategory>(
    save = { state ->
        ParcelableCategory(id = state.id, name = state.name, parentId = state.parentId)
    },
    restore = {
        it.toPartial()
    }
)