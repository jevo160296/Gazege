package com.jmml.gazege.ui.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.jmml.gazege.R
import com.jmml.gazege.ui.widgets.ButtonField
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.MediumHeadline
import com.jmml.gazege.ui.widgets.SmallBody
import com.jmml.gazege.ui.widgets.TextField
import com.jmml.gazege.ui.widgets.input.TextButton

@Composable
fun OnBoardingFragment(
    onBoardingStep: Int,
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
    onboardingStepChange: (Int) -> Unit,
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
    val nameOk = !mainPersonName.isNullOrBlank()
    val selfAccountsOk = !selfAccountsNames?.filter { it.isNotBlank() }.isNullOrEmpty()
    val canBeSaved = nameOk && selfAccountsOk
    val lastStep = 3
    Box(
        Modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
    ) {
        MediumHeadline(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier.align(Alignment.TopStart)
        )
        Column(Modifier.align(Alignment.Center)) {
            when (onBoardingStep) {
                -1 -> {
                    LargeBody(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
                        text = stringResource(id = R.string.onboardingMessage1)
                    )
                    SmallBody(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
                        text = stringResource(id = R.string.Requirements_to_import_data)
                    )
                    ButtonField(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
                        onClick = { onboardingStepChange(0) }
                    ) {
                        Text(text = stringResource(id = if (canBeSaved) R.string.Additional_setup else R.string.Fresh_start))
                    }
                    ButtonField(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.DefaultPadding)),
                        onClick = onImportData
                    ) {
                        Text(text = stringResource(id = R.string.Import_data))
                    }
                }

                0 -> {
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }

                1 -> {
                    LargeBody(
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
                }

                2 -> {
                    LargeBody(
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
                }

                3 -> {
                    LargeBody(
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
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.End),
                visible = canBeSaved || onBoardingStep == lastStep
            ) {
                TextButton(
                    onClick = {
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
                    enabled = canBeSaved
                ) {
                    Text(stringResource(id = R.string.Finish))
                }
            }
            AnimatedVisibility(visible = onBoardingStep >= 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { onboardingStepChange(onBoardingStep - 1) },
                        enabled = onBoardingStep > -1
                    ) {
                        Text(text = stringResource(id = R.string.Previous_step))
                    }
                    TextButton(
                        onClick = { onboardingStepChange(onBoardingStep + 1) },
                        enabled = onBoardingStep < lastStep &&
                                (onBoardingStep != 0 || nameOk) &&
                                (onBoardingStep != 1 || selfAccountsOk)
                    ) {
                        Text(text = stringResource(id = R.string.Next_step))
                    }
                }
            }
        }
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
                    .height(dimensionResource(id = R.dimen.MaxListSize))
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                }
            }
            IconButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onAddValue("") }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "add",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LoadingBoardingFragment() {
    Text("Loading...")
}