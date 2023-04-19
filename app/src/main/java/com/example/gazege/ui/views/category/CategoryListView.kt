package com.example.gazege.ui.views.category

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.ui.templates.ClickableTreeListItemViewHolder
import com.example.gazege.ui.templates.SimpleTreeList
import com.example.gazege.ui.widgets.treeview.rememberTreeState

@Composable
private fun CategoryViewHolder(
    category: Category
) = Box(Modifier.padding(PaddingValues(8.dp))) {
    Text(text = category.name)
}

@Composable
fun CategoryListView(
    categories: List<CategoryWithSubCategories>,
    onItemClick: (category: CategoryWithSubCategories) -> Unit,
    onItemLongClick: (category: CategoryWithSubCategories) -> Unit
) {
    val nodes = categories.map { CategoryNode(it) }
    SimpleTreeList(
        contentPadding = PaddingValues(
            bottom = dimensionResource(id = R.dimen.FABDefaultSpace),
            start = dimensionResource(id = R.dimen.DefaultPadding),
            end = dimensionResource(id = R.dimen.DefaultPadding)
        ),
        nodes = nodes,
        state = rememberTreeState()
    ) { node, scope ->
        ClickableTreeListItemViewHolder(
            level = node.level,
            showExpandIcon = node.children.isNotEmpty(),
            isExpanded = scope.isExpanded(node),
            onIsExpandedChanged = { scope.toggleExpanded(node) },
            onItemTapped = { onItemClick(node.content) },
            onItemLongPressed = { onItemLongClick(node.content) }) {
            CategoryViewHolder(category = node.content.category)
        }
    }
}

