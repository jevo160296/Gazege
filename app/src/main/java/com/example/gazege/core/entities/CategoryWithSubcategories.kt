package com.example.gazege.core.entities

fun categories(
    categoryWithSubCategories: CategoryWithSubCategoriesScope.() -> Unit
): List<CategoryWithSubCategories> = categories(null, categoryWithSubCategories)

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
                            allCategories.filter { child -> parent.id == child.parentId }
                        )
                    )
                }
        }

        fun from(allCategories: List<Category>) = from(
            allCategories,
            allCategories.filter { it.parentId == null }
        )
    }
}

data class CategoryWithSubCategoriesScope(
    val category: Category?,
    val subCategories: MutableList<CategoryWithSubCategories> = mutableListOf()
) {
    fun category(
        id: Int,
        categoryWithSubCategories: CategoryWithSubCategoriesScope.() -> Unit
    ) {
        subCategories.add(
            Category(id, category?.id).run {
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