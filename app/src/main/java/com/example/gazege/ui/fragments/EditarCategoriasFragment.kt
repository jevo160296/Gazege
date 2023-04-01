package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.gazege.R
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.ui.views.CategoryListView
import com.example.gazege.ui.widgets.FAB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCategorias(
    categories: List<CategoryWithSubCategories>,
    onAddCategoryRequested: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FAB(onClick = onAddCategoryRequested) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "Save"
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            CategoryListView(categories = categories)
        }
    }
}