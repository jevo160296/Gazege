package com.example.gazege.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.CategoryWithSubCategories
import com.example.gazege.ui.views.category.CategoryListView
import com.example.gazege.ui.widgets.ModalSheetContent
import com.example.gazege.ui.widgets.fab.FAB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EditarCategorias(
    categories: List<CategoryWithSubCategories>,
    onAddCategoryRequested: () -> Unit,
    onEditCategoryRequested: (CategoryWithSubCategories) -> Unit,
    onDeleteCategoryRequested: (CategoryWithSubCategories) -> Unit
) {
    var categoryClicked: CategoryWithSubCategories? by remember {
        mutableStateOf(null)
    }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    ModalBottomSheetLayout(
        sheetContent = {
            ModalSheetContent(
                onSiClicked = {
                    val item = categoryClicked
                    if (item != null) {
                        onDeleteCategoryRequested(item)
                        scope.launch { sheetState.hide() }
                    }
                },
                onNoClicked = { scope.launch { sheetState.hide() } },
                titleText = stringResource(id = R.string.confirmar_eliminacion),
                bodyText = stringResource(id = R.string.confirma_la_eliminacion_de).format(
                    categoryClicked?.category?.name
                )
            )
        },
        sheetState = sheetState
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
                CategoryListView(
                    categories = categories,
                    onItemClick = onEditCategoryRequested,
                    onItemLongClick = {
                        scope.launch {
                            categoryClicked = it
                            sheetState.show()
                        }
                    }
                )
            }
        }
    }
}