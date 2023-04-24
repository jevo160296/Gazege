package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.ui.views.category.CategoryListView
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.ModalSheetContent
import com.example.gazege.ui.widgets.fab.FAB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EditarCategorias(
    categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>,
    onAddCategoryRequested: () -> Unit,
    onEditCategoryRequested: (Category) -> Unit,
    onSetBudgetRequested: (Category) -> Unit,
    onDeleteCategoryRequested: (Category) -> Unit
) {
    var categoryClicked: Category? by remember {
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
                    categoryClicked?.name
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
            },
            topBar = {
                TopAppBar(
                    title = { MediumHeadline(text = stringResource(id = R.string.Categorias)) }
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            Box(modifier = Modifier.padding(it)) {
                CategoryListView(
                    categoriesWithCalculatedData = categoriesWithCalculatedData,
                    onItemClick = onEditCategoryRequested,
                    onItemLongClick = {
                        scope.launch {
                            categoryClicked = it
                            sheetState.show()
                        }
                    },
                    onSetBudgetRequested = onSetBudgetRequested
                )
            }
        }
    }
}