package com.example.gazege.ui.views.category

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.core.entities.Category
import com.example.gazege.ui.savers.PartialCategory
import com.example.gazege.ui.savers.categorySaver
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.TextField

@Composable
fun CategoryForm(
    category: Category?,
    categories: List<Category>,
    onCategorySave: (Category, SnackbarHostState) -> Unit
) {
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

    val snackbarHostState = SnackbarHostState()
    val selectedCategory = categories.firstOrNull { it.id == partialCategory.parentId }
    val filteredCategories = categories.filter { it.id != partialCategory.id }
    Form(
        onSaveClicked = { onCategorySave(partialCategory.toFull(), snackbarHostState) },
        isSavedButtonEnabled = partialCategory.isComplete(),
        title = stringResource(id = R.string.Categoria),
        itemsColumnsModifier = Modifier.padding(PaddingValues(8.dp)),
        snackbarHostState = snackbarHostState,
        itemSpacing = 8.dp
    ) {
        TextField(
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
            label = { Text(stringResource(id = R.string.cuentaPadre)) },
            onItemClick = {
                partialCategory = partialCategory.copy(parentId = it?.id)
            }
        )
    }
}