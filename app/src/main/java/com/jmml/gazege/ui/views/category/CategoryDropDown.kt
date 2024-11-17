package com.jmml.gazege.ui.views.category

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.recursiveFirstOrNull
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.widgets.TreeComboBox
import com.jmml.gazege.ui.widgets.treeview.Node
import com.jmml.gazege.ui.widgets.treeview.NodeId

data class CategoryWithBudgetNode(
    override val content: CategoryWithSubcategoriesAndBudgetWithCalculatedData,
    override val level: Int = 0,
    override val parentId: NodeId? = null
) : Node<CategoryWithSubcategoriesAndBudgetWithCalculatedData, CategoryWithBudgetNode> {
    override val relativeIndex: Int
        get() = content.category.id ?: -1

    override val children: List<CategoryWithBudgetNode>
        get() = content.subCategories.map { CategoryWithBudgetNode(it, level + 1, this.id()) }
}

data class CategoryNode(
    override val content: CategoryWithSubCategories,
    override val level: Int = 0,
    override val parentId: NodeId? = null
) : Node<CategoryWithSubCategories, CategoryNode> {
    override val relativeIndex: Int
        get() = content.category.id ?: -1

    override val children: List<CategoryNode>
        get() = content.subCategories.map { CategoryNode(it, level + 1, this.id()) }
}

@Composable
fun CategoryDropDown(
    modifier: Modifier = Modifier,
    categoryList: List<Category>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>?,
    selectedCategory: Category?,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onItemClick: (Category?) -> Unit
) {
    var dropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    if (budgetWithCalculatedDataAndCategory != null) {
        val categoryToBudgetWithCalculatedData =
            remember(budgetWithCalculatedDataAndCategory, categoryList) {
                categoryList.associateWith { category ->
                    budgetWithCalculatedDataAndCategory.recursiveFirstOrNull {
                        it.category.id == category.id
                    }
                }
            }
        val selectedBudgetNode = selectedCategory
            ?.let { CategoryWithBudgetNode(categoryToBudgetWithCalculatedData[selectedCategory]!!) }
        val categoryWithBudgetNodes: List<CategoryWithBudgetNode> =
            budgetWithCalculatedDataAndCategory
                .map {
                    CategoryWithBudgetNode(it)
                }
        val budgetToString = { it: CategoryWithBudgetNode? ->
            val category = it?.content?.category
            if (category == null) {
                ""
            } else {
                val leftToPayToday = it.content.leftToPayToday
                "${category.name}: ${doubleToMoneyString(leftToPayToday)}"
            }
        }
        TreeComboBox(
            modifier = modifier,
            dropDownExpanded = dropDownExpanded,
            onExpandedChange = {
                dropDownExpanded = !dropDownExpanded
            },
            options = categoryWithBudgetNodes,
            selectedItem = selectedBudgetNode,
            itemToString = budgetToString,
            label = label,
            onItemClick = { onItemClick(it.content.category.category) },
            canClearSelection = true,
            onClearSelectionClicked = {
                onItemClick(null)
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            nodeEnabled = { true }
        )
    } else {
        val categoryWithSubCategories = remember(categoryList) {
            CategoryWithSubCategories.from(categoryList)
        }
        val categoryToCategoryWithSubCategories =
            remember(categoryWithSubCategories, categoryList) {
                categoryList.associateWith { category ->
                    categoryWithSubCategories.recursiveFirstOrNull {
                        it.category.id == category.id
                    }
                }
            }
        val selectedCategoryNode = selectedCategory
            ?.let { CategoryNode(categoryToCategoryWithSubCategories[selectedCategory]!!) }
        val categoryNodes: List<CategoryNode> = categoryWithSubCategories
            .map { CategoryNode(it) }
        val categoryToString = { it: CategoryNode? ->
            val category = it?.content?.category
            category?.name ?: ""
        }
        TreeComboBox(
            modifier = modifier,
            dropDownExpanded = dropDownExpanded,
            onExpandedChange = {
                dropDownExpanded = !dropDownExpanded
            },
            options = categoryNodes,
            selectedItem = selectedCategoryNode,
            itemToString = categoryToString,
            label = label,
            onItemClick = { onItemClick(it.content.category) },
            canClearSelection = true,
            onClearSelectionClicked = {
                onItemClick(null)
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            nodeEnabled = { true }
        )
    }
}

@Preview
@Composable
fun CategoryDropDownPreview() {
    DatabaseSample {
        val budgetWithCalculatedDataAndCategory =
            this.categoryWithSubcategoriesAndBudgetWithCalculatedDataSample
        val selectedCategory = null
        Box(
            Modifier
                .navigationBarsPadding()
                .systemBarsPadding()
        ) {
            CategoryDropDown(
                categoryList = emptyList(),
                budgetWithCalculatedDataAndCategory = budgetWithCalculatedDataAndCategory,
                selectedCategory = selectedCategory,
                label = { Text(text = "Label") })
            {

            }
        }
    }
}