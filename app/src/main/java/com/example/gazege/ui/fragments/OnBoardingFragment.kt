package com.example.gazege.ui.fragments

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.gazege.R
import com.example.gazege.ui.widgets.ButtonField
import com.example.gazege.ui.widgets.Form
import com.example.gazege.ui.widgets.SmallBody
import com.example.gazege.ui.widgets.TextField

@Composable
fun OnBoardingFragment(
    mainPersonName: String?,
    selfAccountsNames: List<String>?,
    categoryNames: List<String>?,
    personNames: List<String>?,
    onMainPersonNameChanged: (String?) -> Unit,
    onOnboardingFinished: (
        mainPersonName: String,
        selfAccountsNames: List<String>,
        categoryNames: List<String>,
        personNames: List<String>
    ) -> Unit,
    onAddNewEmptySelfAccountName: () -> Unit,
    onChangeSelfAccountName: (index: Int, newName: String) -> Unit,
    onDeleteSelfAccountName: (index: Int) -> Unit,
    onAddNewEmptyCategory: () -> Unit,
    onChangeCategory: (index: Int, newName: String) -> Unit,
    onDeleteCategory: (index: Int) -> Unit,
    onAddNewEmptyPerson: () -> Unit,
    onChangePerson: (index: Int, newName: String) -> Unit,
    onDeletePerson: (index: Int) -> Unit,
    onNavigateToMainScreen: () -> Unit,
    onShowOnBoardingChanged: (Boolean) -> Unit,
    onImportData: () -> Unit
) {
    val canBeSaved = !mainPersonName.isNullOrBlank() && !selfAccountsNames.isNullOrEmpty()
    Form(
        onSaveClicked = {
            if (!mainPersonName.isNullOrBlank() && !selfAccountsNames.isNullOrEmpty()) {
                onOnboardingFinished(
                    mainPersonName,
                    selfAccountsNames,
                    categoryNames ?: emptyList(),
                    personNames ?: emptyList()
                )
                onShowOnBoardingChanged(false)
                onNavigateToMainScreen()
            }
        },
        isSavedButtonEnabled = canBeSaved,
        itemSpacing = dimensionResource(id = R.dimen.DefaultPadding),
        title = stringResource(id = R.string.OnBoarding)
    ) {
        ButtonField(
            onClick = onImportData,
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
        ) {
            Text(text = stringResource(id = R.string.Import_data))
        }
        SmallBody(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            text = stringResource(id = R.string.onboardingMessage1)
        )
        TextField(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            value = mainPersonName ?: "",
            onValueChange = {
                if (it.isBlank()) {
                    onMainPersonNameChanged(null)
                } else {
                    onMainPersonNameChanged(it)
                }
            },
            label = { Text(text = stringResource(id = R.string.whats_your_name)) },
            trailingIcon = {
                IconButton(
                    onClick = { onMainPersonNameChanged(null) },
                    enabled = !mainPersonName.isNullOrBlank()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.clear_selection),
                        contentDescription = "Clear"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        SmallBody(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            text = stringResource(id = R.string.on_boarding_add_account_message)
        )
        ListStringEditor(
            modifier = Modifier.fillMaxWidth(1f),
            title = stringResource(id = R.string.cuentas),
            label = stringResource(id = R.string.cuenta),
            values = selfAccountsNames,
            onAddValue = { onAddNewEmptySelfAccountName() },
            onRemoveValue = onDeleteSelfAccountName,
            onEditValue = onChangeSelfAccountName
        )
        SmallBody(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            text = stringResource(id = R.string.on_boarding_add_category_message)
        )
        ListStringEditor(
            modifier = Modifier.fillMaxWidth(1f),
            title = stringResource(id = R.string.Categorias),
            label = stringResource(id = R.string.Categoria),
            values = categoryNames,
            onAddValue = { onAddNewEmptyCategory() },
            onRemoveValue = onDeleteCategory,
            onEditValue = onChangeCategory
        )
        SmallBody(
            modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
            text = stringResource(id = R.string.on_boarding_add_person_message)
        )
        ListStringEditor(
            modifier = Modifier.fillMaxWidth(1f),
            title = stringResource(id = R.string.personas),
            label = stringResource(id = R.string.persona),
            values = personNames,
            onAddValue = { onAddNewEmptyPerson() },
            onRemoveValue = onDeletePerson,
            onEditValue = onChangePerson
        )
    }
}

@Composable
fun ListStringEditor(
    modifier: Modifier = Modifier,
    title: String,
    label: String,
    values: List<String>?,
    onAddValue: (newValue: String) -> Unit,
    onRemoveValue: (index: Int) -> Unit,
    onEditValue: (index: Int, newValue: String) -> Unit
) {
    if (values != null) {
        Column(modifier = modifier) {
            Text(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
                text = title
            )
            Column(
                Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding))
                    .animateContentSize()
                    .sizeIn(maxHeight = dimensionResource(id = R.dimen.MaxListSize))
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.DefaultPadding))
            ) {
                values.forEachIndexed { index, currentName ->
                    TextField(
                        value = currentName,
                        onValueChange = { newName ->
                            onEditValue(index, newName)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemoveValue(index) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.clear_selection),
                                    contentDescription = "Clear"
                                )
                            }
                        },
                        label = { Text(label) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                }
            }
            IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onAddValue("") }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "add"
                )
            }
        }
    }
}

@Composable
fun LoadingBoardingFragment() {
    Text("Loading...")
}