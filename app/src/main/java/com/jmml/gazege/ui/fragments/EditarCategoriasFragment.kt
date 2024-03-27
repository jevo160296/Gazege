package com.jmml.gazege.ui.fragments

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jmml.gazege.R
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.navigation.EmptyEditarCategoriasState
import com.jmml.gazege.ui.navigation.LoadedEditarCategoriasState
import com.jmml.gazege.ui.views.category.CategoryListView
import com.jmml.gazege.ui.widgets.DataView
import com.jmml.gazege.ui.widgets.GIndefiniteCircularProgressIndicator

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
    nestedScrollConnection: NestedScrollConnection,
    onEditCategoryRequested: (Category) -> Unit,
    onSetBudgetRequested: (Category) -> Unit,
    onExportCategoryRequested: (Category) -> Unit,
    onDeleteCategoryRequested: (Category) -> Unit,
    showType: EditarCategoriasShowType,
    onZeroElementsChanged: (Boolean) -> Unit,
    onShowTypeChanged: (newValue: EditarCategoriasShowType) -> Unit
) {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData> =
        editarCategoriasState.categoriesWithCalculatedData
    val layoutDirection = LocalLayoutDirection.current

    LaunchedEffect(categoriesWithCalculatedData.isEmpty()) {
        onZeroElementsChanged(
            categoriesWithCalculatedData.isEmpty()
        )
    }

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
            delCategory = onDeleteCategoryRequested,
            exportCategory = onExportCategoryRequested,
            onSetBudgetRequested = onSetBudgetRequested,
            paddingValues = paddingValues,
            showType = showType,
            nestedScrollConnection = nestedScrollConnection
        )
    }
}

enum class EditarCategoriasShowType {
    COMPACT, EXPANDED, GRAPHICAL
}