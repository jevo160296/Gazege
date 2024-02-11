package com.example.gazege.ui.views.category

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.widgets.TreeComboBox
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.NodeId
import kotlin.math.absoluteValue

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
    categoryList: List<Category>,
    budgetWithCalculatedDataAndCategory: Map<Category, BudgetWithCalculatedDataAndCategory>,
    selectedCategory: Category?,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onItemClick: (Category?) -> Unit
) {
    val selectedNode = selectedCategory
        ?.let { CategoryNode(CategoryWithSubCategories(it, listOf())) }
    val categoryWithBudgetNodes: List<CategoryNode> = categoryList
        .let {
            CategoryWithSubCategories.from(it)
        }
        .map {
            CategoryNode(it)
        }
    var dropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val itemToString = { it: CategoryNode? ->
        val category = it?.content?.category
        if (category == null) {
            ""
        } else {
            val leftToPayToday = budgetWithCalculatedDataAndCategory.getOrDefault(
                category,
                null
            )?.budgetLeftToPayToday?.absoluteValue ?: 0.0
            "${category.name}: ${doubleToMoneyString(leftToPayToday)}"
        }
    }
    TreeComboBox(
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
        },
        options = categoryWithBudgetNodes,
        selectedItem = selectedNode,
        itemToString = itemToString,
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