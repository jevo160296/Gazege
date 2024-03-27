package com.jmml.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.saveable.Saver
import com.jmml.gazege.core.entities.BudgetType
import com.jmml.gazege.core.entities.Category
import kotlinx.parcelize.Parcelize

data class PartialCategory(
    var id: Int?,
    var name: String?,
    var budgetType: BudgetType?,
    var parentId: Int?
) : PartialEntity<Category> {
    override fun isComplete(): Boolean {
        return name != null && budgetType != null
    }

    override fun toFull(): Category {
        if (isComplete()) {
            return Category(
                id = id,
                name = name!!,
                budgetType = budgetType!!,
                parentId = parentId
            )
        } else {
            throw Exception()
        }
    }

    companion object {
        fun blankEntity(): PartialCategory {
            return PartialCategory(null, null, null, null)
        }

        fun from(category: Category): PartialCategory = category
            .run { PartialCategory(id, name, budgetType, parentId) }
    }
}

@Parcelize
data class ParcelableCategory(
    val id: Int?,
    val name: String?,
    val budgetType: BudgetType?,
    val parentId: Int?
) : Parcelable {
    fun toPartial(): PartialCategory {
        return PartialCategory(
            id = id,
            name = name,
            budgetType = budgetType,
            parentId = parentId
        )
    }
}

val categorySaver = Saver<PartialCategory, ParcelableCategory>(
    save = { state ->
        ParcelableCategory(
            id = state.id,
            name = state.name,
            budgetType = state.budgetType,
            parentId = state.parentId
        )
    },
    restore = {
        it.toPartial()
    }
)