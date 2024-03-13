package com.example.gazege.ui.fragments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.gazege.R
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.example.gazege.ui.doubleToMoneyString
import com.example.gazege.ui.navigation.EmptyEditarCategoriasState
import com.example.gazege.ui.navigation.LoadedEditarCategoriasState
import com.example.gazege.ui.views.category.CategoryListView
import com.example.gazege.ui.widgets.DataView
import com.example.gazege.ui.widgets.GIndefiniteCircularProgressIndicator
import kotlinx.coroutines.launch

@Composable
fun EmptyEditarCategorias(
    paddingValues: PaddingValues,
    editarCategoriasState: EmptyEditarCategoriasState
) {
    Column(modifier = Modifier.padding(paddingValues)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding)),
            modifier = Modifier
                .padding(bottom = dimensionResource(id = R.dimen.DefaultPadding))
                .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
                .height(IntrinsicSize.Min)
        ) {
            DataView(
                title = stringResource(id = R.string.Falta_pagar_recibir),
                value = doubleToMoneyString(editarCategoriasState.leftToPay),
                modifier = Modifier.weight(1f)
            )
            DataView(
                title = stringResource(id = R.string.Flujo_real),
                value = doubleToMoneyString(editarCategoriasState.realTotalFlow),
                modifier = Modifier.weight(1f),
            )
            DataView(
                title = stringResource(id = R.string.Flujo_total),
                value = doubleToMoneyString(editarCategoriasState.netFlow),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                GIndefiniteCircularProgressIndicator()
                Text(stringResource(id = R.string.Cargando))
            }
        }
    }
}

@Composable
fun LoadedEditarCategorias(
    paddingValues: PaddingValues,
    editarCategoriasState: LoadedEditarCategoriasState,
    onEditCategoryRequested: (Category) -> Unit,
    onSetBudgetRequested: (Category) -> Unit,
    onExportCategoryRequested: (Category) -> Unit,
    onDeleteCategoryRequested: (Category) -> Unit,
    showType: EditarCategoriasShowType,
    onShowTypeChanged: (newValue: EditarCategoriasShowType) -> Unit
) {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData> =
        editarCategoriasState.categoriesWithCalculatedData
    val scope = rememberCoroutineScope()
    val snackbarHostState = SnackbarHostState()
    val actionLabel = stringResource(id = R.string.Si)
    val template = stringResource(id = R.string.confirma_la_eliminacion_de)
    val confirmationMessageBuilder = { categoryName: String -> template.format(categoryName) }
    val layoutDirection = LocalLayoutDirection.current

    Column(
        modifier = Modifier.padding(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding)),
            modifier = Modifier
                .padding(bottom = dimensionResource(id = R.dimen.DefaultPadding))
                .height(IntrinsicSize.Min)
        ) {
            DataView(
                title = stringResource(id = R.string.Falta_pagar_recibir),
                value = doubleToMoneyString(editarCategoriasState.leftToPay),
                modifier = Modifier.weight(1f)
            )
            DataView(
                title = stringResource(id = R.string.Flujo_real),
                value = doubleToMoneyString(editarCategoriasState.realTotalFlow),
                modifier = Modifier.weight(1f),
            )
            DataView(
                title = stringResource(id = R.string.Flujo_total),
                value = doubleToMoneyString(editarCategoriasState.netFlow),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(id = R.dimen.DefaultPadding),
                alignment = Alignment.End
            )
        ) {
            FilledTonalIconToggleButton(
                checked = showType == EditarCategoriasShowType.GRAPHICAL,
                onCheckedChange = { onShowTypeChanged(EditarCategoriasShowType.GRAPHICAL) },
                colors = IconButtonDefaults.filledTonalIconToggleButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.show_graphical),
                    contentDescription = ""
                )
            }
            FilledTonalIconToggleButton(
                checked = showType == EditarCategoriasShowType.EXPANDED,
                onCheckedChange = { onShowTypeChanged(EditarCategoriasShowType.EXPANDED) },
                colors = IconButtonDefaults.filledTonalIconToggleButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.show_expanded),
                    contentDescription = ""
                )
            }
            FilledTonalIconToggleButton(
                checked = showType == EditarCategoriasShowType.COMPACT,
                onCheckedChange = { onShowTypeChanged(EditarCategoriasShowType.COMPACT) },
                colors = IconButtonDefaults.filledTonalIconToggleButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.show_compact),
                    contentDescription = ""
                )
            }
        }
        CategoryListView(
            categoriesWithCalculatedData = categoriesWithCalculatedData,
            editCategory = onEditCategoryRequested,
            delCategory = {
                scope.launch {
                    val response = snackbarHostState.showSnackbar(
                        message = confirmationMessageBuilder(it.name),
                        actionLabel = actionLabel,
                        withDismissAction = true
                    )
                    if (response == SnackbarResult.ActionPerformed) {
                        onDeleteCategoryRequested(it)
                    }
                }
            },
            exportCategory = onExportCategoryRequested,
            onSetBudgetRequested = onSetBudgetRequested,
            paddingValues = paddingValues,
            showType = showType
        )
    }
}

enum class EditarCategoriasShowType {
    COMPACT, EXPANDED, GRAPHICAL
}