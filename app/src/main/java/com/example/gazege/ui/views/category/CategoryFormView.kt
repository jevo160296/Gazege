package com.example.gazege.ui.views.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.BudgetWithCalculatedDataAndCategory
import com.example.gazege.core.entities.Category
import com.example.gazege.ui.savers.PartialCategory
import com.example.gazege.ui.savers.categorySaver
import com.example.gazege.ui.views.budget.BudgetRecyclerView
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.LargeBody
import com.example.gazege.ui.widgets.MediumHeadline
import com.example.gazege.ui.widgets.TextField
import kotlinx.coroutines.launch

@Composable
fun CategoryForm(
    categoryMap: Pair<Category, List<BudgetWithCalculatedDataAndCategory>>?,
    categories: List<Category>,
    onCategorySave: (Category, SnackbarHostState) -> Unit,
    onBudgetDetailRequested: (BudgetWithCalculatedDataAndCategory) -> Unit,
    onBudgetEditRequested: (BudgetWithCalculatedDataAndCategory) -> Unit,
    onBudgetDeleteRequested: (BudgetWithCalculatedDataAndCategory) -> Unit,
    onBudgetAddRequested: ((Category) -> Unit)?
) {
    val category = categoryMap?.first
    val budgetData = categoryMap?.second?.takeIf { it.isNotEmpty() }
    var partialCategory by rememberSaveable(
        stateSaver = categorySaver
    ) {
        mutableStateOf(
            if (category == null) {
                PartialCategory.blankEntity()
            } else {
                PartialCategory.from(category)
            }
        )
    }
    val scope = rememberCoroutineScope()

    val snackbarHostState = SnackbarHostState()
    val selectedCategory = categories.firstOrNull { it.id == partialCategory.parentId }
    val filteredCategories = categories.filter { it.id != partialCategory.id }

    val focusRequester = remember { FocusRequester() }
    Form(
        onSaveClicked = { onCategorySave(partialCategory.toFull(), snackbarHostState) },
        isSavedButtonEnabled = partialCategory.isComplete(),
        title = stringResource(id = R.string.Categoria),
        itemsColumnsModifier = Modifier.padding(PaddingValues(8.dp)),
        snackbarHostState = snackbarHostState,
        itemSpacing = 8.dp
    ) {
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            value = partialCategory.name ?: "",
            onValueChange = {
                partialCategory = partialCategory.copy(name = it)
            },
            label = { Text(text = stringResource(R.string.nombre)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            singleLine = true
        )
        CategoryDropDown(
            categoryList = filteredCategories,
            selectedCategory = selectedCategory,
            label = { Text(stringResource(id = R.string.CategoriaPadre)) },
            onItemClick = {
                partialCategory = partialCategory.copy(parentId = it?.id)
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            MediumHeadline(text = stringResource(id = R.string.Presupuesto))
            onBudgetAddRequested?.let { action ->
                category?.let { category ->
                    SmallFloatingActionButton(onClick = { action(category) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_add_24),
                            contentDescription = "Add budget"
                        )
                    }
                }
            }
        }
        if (budgetData != null) {
            val mensaje = stringResource(id = R.string.confirma_la_eliminacion_de).format(
                stringResource(
                    id = R.string.Presupuesto
                )
            )
            val siText = stringResource(id = R.string.Si)
            BudgetRecyclerView(
                itemHolderPaddingValues = PaddingValues(dimensionResource(id = R.dimen.DefaultPadding)),
                budget = budgetData,
                onBudgetDetailRequested = onBudgetDetailRequested,
                onBudgetDeleteRequested = {
                    scope.launch {
                        val response = snackbarHostState.showSnackbar(
                            message = mensaje,
                            actionLabel = siText,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite
                        )
                        if (response == SnackbarResult.ActionPerformed) {
                            onBudgetDeleteRequested(it)
                        }
                    }
                },
                onBudgetEditRequested = onBudgetEditRequested,
                modifier = Modifier.heightIn(max = 1024.dp)
            )
        } else {
            LargeBody(text = stringResource(id = R.string.Presupuesto_vacio))
        }
    }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }
}