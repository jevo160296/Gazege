package com.example.gazege.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.ui.widgets.Card
import com.example.gazege.ui.widgets.treeview.DefaultItemHolderWithExpandIcon
import com.example.gazege.ui.widgets.treeview.RecyclerTreeView
import kotlin.random.Random

@Composable
private fun CategoryViewHolder(
    category: Category,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Box(Modifier.padding(PaddingValues(8.dp))) {
            Text(text = category.name)
        }
    }
}

@Composable
fun CategoryListView(
    categories: List<CategoryWithSubCategories>,
    onItemClick: (category: CategoryWithSubCategories) -> Unit,
    onItemLongClick: (category: CategoryWithSubCategories) -> Unit
) {
    val nodes = categories.map { CategoryNode(it) }
    RecyclerTreeView(nodes = nodes) { node, scope ->
        scope.DefaultItemHolderWithExpandIcon(
            startPadding = 8.dp,
            endPadding = 8.dp,
            node
        ) {
            CategoryViewHolder(
                category = node.content.category,
                onClick = { onItemClick(node.content) },
                onLongClick = { onItemLongClick(node.content) }
            )
        }
    }
}

fun getCategoriesSample(): List<Category> {
    val random = Random(3)
    return (0..20).map {
        val hasParent = random.nextBoolean()
        val parentId = random.nextInt(19)
            .let { parentId ->
                if (parentId < it) {
                    parentId
                } else {
                    parentId + 1
                }
            }
            .takeIf { hasParent }
        Category(it, "Cuenta $parentId.$it", parentId)
    }
}
