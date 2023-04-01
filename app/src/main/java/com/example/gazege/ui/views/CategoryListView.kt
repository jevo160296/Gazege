package com.example.gazege.ui.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.ui.widgets.treeview.RecyclerTreeView

@Composable
private fun CategoryViewHolder(category: Category) {
    Text(text = category.name)
}

@Composable
fun CategoryListView(
    categories: List<CategoryWithSubCategories>
) {
    val nodes = categories.map { CategoryNode(it) }
    RecyclerTreeView(nodes = nodes) { node, _ ->
        CategoryViewHolder(category = node.content.category)
    }
}