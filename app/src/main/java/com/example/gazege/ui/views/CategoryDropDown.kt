package com.example.gazege.ui.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.ui.widgets.DefaultDropDownViewHolder
import com.example.gazege.ui.widgets.DropDownTreeMenu
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.NodeId

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropDown(
    categoryList: List<Category>,
    selectedCategory: Category?,
    label: @Composable () -> Unit,
    onItemClick: (Category?) -> Unit
) {
    val selectedNode = selectedCategory
        ?.let { CategoryNode(CategoryWithSubCategories(it, listOf())) }
    val categoryNode: List<CategoryNode> = categoryList
        .let {
            CategoryWithSubCategories.from(it)
        }
        .map {
            CategoryNode(it)
        }
    var dropDownExpanded by rememberSaveable {
        mutableStateOf(false)
    }
    val itemToString = { it: CategoryNode? -> it?.content?.category?.name ?: "" }
    DropDownTreeMenu(
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = {
            dropDownExpanded = !dropDownExpanded
        },
        options = categoryNode,
        selectedItem = selectedNode,
        itemToString = itemToString,
        label = label,
        viewHolder = { node ->
            DefaultDropDownViewHolder(
                itemToString = itemToString,
                node = node,
                onExpandedChange = { dropDownExpanded = !dropDownExpanded },
                onItemClick = {
                    dropDownExpanded = false
                    onItemClick(it.content.category)
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding.let {
                    val layoutDirection = LocalLayoutDirection.current
                    PaddingValues(
                        start = 8.dp,
                        top = it.calculateTopPadding(),
                        bottom = it.calculateBottomPadding(),
                        end = it.calculateEndPadding(layoutDirection)
                    )
                },
                enabled = true
            )
        },
        canClearSelection = true,
        onClearSelectionClicked = {
            onItemClick(null)
        }
    )
}