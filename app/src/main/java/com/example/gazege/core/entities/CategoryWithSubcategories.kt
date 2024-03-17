package com.example.gazege.core.entities


fun categories(
    categoryWithSubCategories: CategoryWithSubCategoriesScope.() -> Unit
): List<CategoryWithSubCategories> = categories(null, categoryWithSubCategories)

fun List<CategoryWithSubCategories>.flattenWithLevel(level: Int = 0): List<Pair<Category, Int>> =
    flatMap {
        listOf(
            Pair(it.category, level),
            *it.subCategories.flattenWithLevel(level + 1).toTypedArray()
        )
    }

data class CategoryWithSubCategories(
    val category: Category,
    val subCategories: List<CategoryWithSubCategories>
) {
    companion object {
        private fun from(
            allCategories: List<Category>,
            parentCategories: List<Category>
        ): List<CategoryWithSubCategories> {
            return parentCategories
                .map { parent ->
                    CategoryWithSubCategories(
                        parent,
                        from(
                            allCategories,
                            allCategories
                                .filter { child -> child.parentId != child.id }
                                .filter { child -> child.parentId == parent.id }
                        )
                    )
                }
        }

        fun from(allCategories: List<Category>) = from(
            allCategories,
            allCategories.filter { it.parentId == null || it.parentId == it.id }
        )
    }
}

data class CategoryWithSubCategoriesScope(
    val category: Category?,
    val subCategories: MutableList<CategoryWithSubCategories> = mutableListOf()
) {
    fun category(
        id: Int,
        name: String,
        budgetType: BudgetType,
        categoryWithSubCategories: CategoryWithSubCategoriesScope.() -> Unit
    ) {
        subCategories.add(
            Category(id, name, budgetType, category?.id).run {
                CategoryWithSubCategories(
                    this,
                    categories(this) {
                        this.categoryWithSubCategories()
                    }
                )
            }
        )
    }
}

internal fun categories(
    parent: Category? = null,
    categoryWithSubCategories: CategoryWithSubCategoriesScope.() -> Unit
): List<CategoryWithSubCategories> =
    CategoryWithSubCategoriesScope(parent)
        .run {
            categoryWithSubCategories()
            this
        }.subCategories.toList()