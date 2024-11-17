package com.jmml.gazege.ui.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jmml.gazege.R
import com.jmml.gazege.core.dao.CategoryDao
import com.jmml.gazege.core.entities.Category
import com.jmml.gazege.core.entities.CategoryWithSubCategories
import com.jmml.gazege.core.entities.CategoryWithSubcategoriesAndBudgetWithCalculatedData
import com.jmml.gazege.core.entities.sumOrNull
import com.jmml.gazege.ui.doubleToMoneyString
import com.jmml.gazege.ui.navigation.ICategoriesDataView
import com.jmml.gazege.ui.navigation.ICategoriesView
import com.jmml.gazege.ui.navigation.ICategoriesWithBudgetDataView
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.views.category.CategoryListView
import com.jmml.gazege.ui.views.category.CategoryWithBudgetListView
import com.jmml.gazege.ui.views.category.ahorroExcesoTexto
import com.jmml.gazege.ui.views.category.excessColor
import com.jmml.gazege.ui.views.category.faltaPagarRecibirTexto
import com.jmml.gazege.ui.widgets.DataView
import com.jmml.gazege.ui.widgets.treeview.TreeState
import com.jmml.gazege.ui.widgets.treeview.rememberTreeState
import com.jmml.zoo.ui.state.ZIndefiniteCircularProgressIndicator
import com.jmml.zoo.ui.state.ZProgressIndicator

@Composable
fun LoadingEditarCategorias(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding)),
            modifier = Modifier
                .padding(bottom = dimensionResource(id = R.dimen.DefaultPadding))
                //.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
                .height(IntrinsicSize.Min)
        ) {
            DataView(
                title = faltaPagarRecibirTexto(value = 0.0),
                value = doubleToMoneyString(0.0),
                modifier = Modifier.weight(1f)
            )
            DataView(
                title = stringResource(id = R.string.Flujo_total),
                value = doubleToMoneyString(0.0),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
            DataView(
                title = stringResource(id = R.string.Flujo_categorizado),
                value = doubleToMoneyString(0.0),
                modifier = Modifier.weight(1f),
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column {
                ZIndefiniteCircularProgressIndicator()
                Text(stringResource(id = R.string.Cargando))
            }
        }
    }
}

@Composable
fun LoadedEditarCategorias(
    paddingValues: PaddingValues,
    editarCategoriasState: ICategoriesView,
    nestedScrollConnection: NestedScrollConnection,
    state: TreeState = rememberTreeState(),
    onEditCategoryRequested: (Category) -> Unit,
    onSetBudgetRequested: (Category) -> Unit,
    onExportCategoryRequested: (Category) -> Unit,
    onDeleteCategoryRequested: (Category) -> Unit,
    showType: EditarCategoriasShowType,
    onZeroElementsChanged: (Boolean) -> Unit,
    onFirstElementVisibleChanged: (isVisible: Boolean) -> Unit,
    onShowTypeChanged: (newValue: EditarCategoriasShowType) -> Unit
) {
    val categoriesWithCalculatedData: List<CategoryWithSubcategoriesAndBudgetWithCalculatedData>?
    val categories: List<CategoryWithSubCategories>?
    val initialExpectation: Double
    val totalCompleition: Double
    val totalAhorroExceso: Double
    val leftToPay: Double
    val realTotalFlow: Double
    val listIsempty: Boolean

    when (editarCategoriasState) {
        is ICategoriesDataView -> {
            categories = editarCategoriasState.categoriesWithSubCategories
            categoriesWithCalculatedData = null
            initialExpectation = 0.0
            totalCompleition = 0.0
            totalAhorroExceso = 0.0
            leftToPay = 0.0
            realTotalFlow = 0.0
            listIsempty = categories.isEmpty()
        }

        is ICategoriesWithBudgetDataView -> {
            categoriesWithCalculatedData = editarCategoriasState.categoriesWithCalculatedData
            categories = null
            initialExpectation = remember(editarCategoriasState) {
                editarCategoriasState.categoriesWithCalculatedData
                    .map { it.aggregatedBudget + it.childrenAggregatedBudget }
                    .sumOrNull()
                    ?.expectedTotalFlow
                    ?: 0.0
            }
            totalCompleition = remember(editarCategoriasState) {
                CategoryDao.calculateCategoryCompleition(
                    editarCategoriasState.realTotalFlow,
                    initialExpectation
                )
            }
            totalAhorroExceso = CategoryDao.calculateAhorroExceso(
                editarCategoriasState.realTotalFlow,
                initialExpectation
            )
            leftToPay = editarCategoriasState.leftToPay
            realTotalFlow = editarCategoriasState.realTotalFlow
            listIsempty = categoriesWithCalculatedData.isEmpty()
        }
    }

    val animatedTotalCompleition by animateFloatAsState(
        totalCompleition.toFloat(),
        label = "AnimateTotalCompleition"
    )
    val animatedLeftToPay by animateFloatAsState(leftToPay.toFloat(), label = "AnimateLeftToPay")
    val animatedTotalAhorroExceso by animateFloatAsState(
        totalAhorroExceso.toFloat(),
        label = "AnimateTotalAhorroExceso"
    )
    val animatedRealTotalFlow by animateFloatAsState(
        realTotalFlow.toFloat(),
        label = "AnimateRealTotalFlow"
    )

    val hasZeroElements: Boolean by rememberSaveable(listIsempty) {
        onZeroElementsChanged(listIsempty)
        mutableStateOf(listIsempty)
    }

    LoadedEditarCategoriasUI(
        paddingValues = paddingValues,
        nestedScrollConnection = nestedScrollConnection,
        leftToPay = animatedLeftToPay.toDouble(),
        totalAhorroExceso = animatedTotalAhorroExceso.toDouble(),
        realTotalFlow = animatedRealTotalFlow.toDouble(),
        hasZeroElements = hasZeroElements
    ) {
        AnimatedVisibility(
            visible = categoriesWithCalculatedData != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                Modifier
                    .fillMaxWidth(1f)
                    .pointerInput(1) {
                        detectDragGestures { change, dragAmount ->
                            if (hasZeroElements.not()) {
                                change.consume()
                                nestedScrollConnection.onPreScroll(
                                    dragAmount,
                                    NestedScrollSource.Wheel
                                )
                            }
                        }
                    },
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
        }
        AnimatedVisibility(
            visible = categoriesWithCalculatedData != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                ZProgressIndicator(
                    compleition = animatedTotalCompleition.toDouble(),
                    labelString = stringResource(id = R.string.Progreso),
                    color = GazegeTheme.gazegeColorScheme.neutral,
                    excessColor = excessColor(value = totalAhorroExceso)
                )
            }
        }
        Crossfade(Pair(categoriesWithCalculatedData, categories), label = "CrossFade") {
            val innerCategoriesWithCalculatedData = it.first
            val innerCategories = it.second
            if (innerCategoriesWithCalculatedData != null) {
                CategoryWithBudgetListView(
                    categoriesWithCalculatedData = innerCategoriesWithCalculatedData,
                    editCategory = onEditCategoryRequested,
                    delCategory = onDeleteCategoryRequested,
                    exportCategory = onExportCategoryRequested,
                    onSetBudgetRequested = onSetBudgetRequested,
                    paddingValues = paddingValues,
                    showType = showType,
                    nestedScrollConnection = nestedScrollConnection,
                    onFirstElementsVisibleChanged = onFirstElementVisibleChanged,
                    state = state
                )
            } else if (innerCategories != null) {
                CategoryListView(
                    paddingValues = paddingValues,
                    nestedScrollConnection = nestedScrollConnection,
                    state = state,
                    onFirstElementsVisibleChanged = onFirstElementVisibleChanged,
                    categoryWithSubCategories = innerCategories,
                    editCategory = onEditCategoryRequested,
                    delCategory = onDeleteCategoryRequested,
                    exportCategory = onExportCategoryRequested
                )
            } else {
                Text("Categories and categories with budget is null.")
            }
        }
    }
}

@Composable
private fun LoadedEditarCategoriasUI(
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
    leftToPay: Double,
    totalAhorroExceso: Double,
    realTotalFlow: Double,
    hasZeroElements: Boolean,
    categoryListView: @Composable () -> Unit
) {
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
                .pointerInput(1) {
                    detectDragGestures { change, dragAmount ->
                        if (hasZeroElements.not()) {
                            change.consume()
                            nestedScrollConnection.onPreScroll(
                                dragAmount,
                                NestedScrollSource.Wheel
                            )
                        }
                    }
                }

        ) {
            DataView(
                title = faltaPagarRecibirTexto(leftToPay),
                value = doubleToMoneyString(leftToPay),
                enabled = false,
                modifier = Modifier.weight(1f)
            )
            DataView(
                title = ahorroExcesoTexto(value = totalAhorroExceso),
                value = doubleToMoneyString(totalAhorroExceso),
                enabled = false,
                modifier = Modifier.weight(1f)
            )
            DataView(
                title = stringResource(id = R.string.Flujo_categorizado),
                value = doubleToMoneyString(realTotalFlow),
                enabled = false,
                modifier = Modifier.weight(1f),
            )
        }
        categoryListView()
    }
}

enum class EditarCategoriasShowType {
    COMPACT, EXPANDED, GRAPHICAL
}