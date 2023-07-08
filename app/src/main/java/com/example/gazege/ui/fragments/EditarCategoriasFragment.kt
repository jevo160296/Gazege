package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.navigation.EditarCategoriasState
import com.example.gazege.ui.views.category.CategoryListView
import com.example.gazege.ui.widgets.DataViewProgressBar
import com.example.gazege.ui.widgets.DataViewWithTrailingComposable
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.ModalSheetLayout
import com.example.gazege.ui.widgets.fab.FAB
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCategorias(
    editarCategoriasState: EditarCategoriasState,
    onAddCategoryRequested: () -> Unit,
    onEditCategoryRequested: (Category) -> Unit,
    onSetBudgetRequested: (Category) -> Unit,
    onDeleteCategoryRequested: (Category) -> Unit
) {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData> =
        editarCategoriasState.categoriesWithCalculatedData
    var categoryClicked: Category? by remember {
        mutableStateOf(null)
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalSheetLayout(
        modalSheetMsg = stringResource(id = R.string.confirma_la_eliminacion_de).format(
            categoryClicked?.name
        ),
        onModalSheetMsgChanged = {},
        action = {
            val item = categoryClicked
            if (item != null) {
                onDeleteCategoryRequested(item)
            }
        },
        onActionChanged = {},
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
            Column(modifier = Modifier.padding(it)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding)),
                    modifier = Modifier
                        .padding(bottom = dimensionResource(id = R.dimen.DefaultPadding))
                        .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
                        .height(IntrinsicSize.Min)
                ) {
                    DataViewWithTrailingComposable(
                        title = stringResource(id = R.string.Ingreso),
                        value = doubleToMoneyString(editarCategoriasState.expectedTotalIncome),
                        modifier = Modifier.weight(1f),
                        trailingComposable = { DataViewProgressBar(progress = editarCategoriasState.totalIncomeProgress) }
                    )
                    DataViewWithTrailingComposable(
                        title = stringResource(id = R.string.Gasto),
                        value = doubleToMoneyString(editarCategoriasState.expectedTotalOutcome),
                        modifier = Modifier.weight(1f),
                        trailingComposable = { DataViewProgressBar(progress = editarCategoriasState.totalOutcomeProgress) }
                    )
                    DataViewWithTrailingComposable(
                        title = stringResource(id = R.string.Neto),
                        value = doubleToMoneyString(editarCategoriasState.expectedNetValue),
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        trailingComposable = {}
                    )
                }
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