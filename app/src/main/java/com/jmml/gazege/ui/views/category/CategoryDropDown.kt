package com.jmml.gazege.ui.views.category

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
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
    categoryList: List<Category>,
    budgetWithCalculatedDataAndCategory: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    selectedCategory: CategoryWithSubcategoriesAndBudgetWithCalculatedData?,
    label: @Composable () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onItemClick: (Category?) -> Unit
) {
    val selectedNode = selectedCategory
        ?.let { CategoryWithBudgetNode(it) }
    val categoryWithBudgetNodes: List<CategoryWithBudgetNode> = budgetWithCalculatedDataAndCategory
        .map {
            CategoryWithBudgetNode(it)
        }
    var dropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val itemToString = { it: CategoryWithBudgetNode? ->
        val category = it?.content?.category
        if (category == null) {
            ""
        } else {
            val leftToPayToday = it.content.leftToPayToday
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
        onItemClick = { onItemClick(it.content.category.category) },
        canClearSelection = true,
        onClearSelectionClicked = {
            onItemClick(null)
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        nodeEnabled = { true }
    )
}