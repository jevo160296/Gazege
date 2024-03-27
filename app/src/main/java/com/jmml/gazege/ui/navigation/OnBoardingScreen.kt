package com.jmml.gazege.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.jmml.gazege.MainViewModel
import com.jmml.gazege.ui.fragments.LoadingBoardingFragment
import com.jmml.gazege.ui.fragments.OnBoardingFragment

const val ONBOARDINGROUTE = "onBoardingScreen"

fun NavGraphBuilder.screenOnBoardingScreen(
    viewModelOnBoarding: MainViewModel.ViewModelOnBoarding,
    onNavigateToMainScreen: () -> Unit,
    onImportData: () -> Unit
) {
    composable(ONBOARDINGROUTE) {
        val allPerson = viewModelOnBoarding.rememberAllPerson().value
        val allAccounts = viewModelOnBoarding.rememberAllAccounts().value
        val allCategories = viewModelOnBoarding.rememberAllCategories().value
        val incomeAccount = viewModelOnBoarding.rememberIncomeAccount().value
        val outcomeAccount = viewModelOnBoarding.rememberOutcomeAccount().value

        val principalPerson = viewModelOnBoarding.rememberPrincipalPerson().value

        val (mainPersonName, onMainPersonNameChanged) = viewModelOnBoarding.rememberMainPersonName(
            principalPerson?.name
        )
        val selfAccountNames =
            viewModelOnBoarding.rememberSelfAccountNames(allAccounts?.filter { it.ownerId == principalPerson?.id }
                ?.map { it.name } ?: emptyList())
        val categoryNames =
            viewModelOnBoarding.rememberCategoriesNames(allCategories?.map { it.name }
                ?: emptyList())
        val newPersonNames =
            viewModelOnBoarding.rememberNewPersonNames(allPerson?.filter { it.id != principalPerson?.id }
                ?.map { it.name } ?: emptyList())

        if (allPerson != null && allAccounts != null && allCategories != null) {
            OnBoardingFragment(
                mainPersonName = mainPersonName,
                onMainPersonNameChanged = onMainPersonNameChanged,
                onOnboardingFinished = { mainPersonName, selfAccountsNames, categoryNames, personNames ->
                    viewModelOnBoarding.onBoardingFinished(
                        allPerson = allPerson,
                        allAccounts = allAccounts,
                        allCategories = allCategories,
                        mainPersonName = mainPersonName,
                        selfAccountsNames = selfAccountsNames,
                        categoryNames = categoryNames,
                        personNames = personNames,
                        incomeAccount = incomeAccount,
                        outcomeAccount = outcomeAccount
                    )
                },
                onNavigateToMainScreen = onNavigateToMainScreen,
                onShowOnBoardingChanged = viewModelOnBoarding::onShowOnBoardingChanged,
                selfAccountsNames = selfAccountNames,
                onChangeSelfAccountName = selfAccountNames::set,
                onDeleteSelfAccountName = selfAccountNames::removeAt,
                onAddNewEmptySelfAccountName = { selfAccountNames.add("") },
                categoryNames = categoryNames,
                onAddNewEmptyCategory = { categoryNames.add("") },
                onChangeCategory = categoryNames::set,
                onDeleteCategory = categoryNames::removeAt,
                personNames = newPersonNames,
                onAddNewEmptyPerson = { newPersonNames.add("") },
                onChangePerson = newPersonNames::set,
                onDeletePerson = newPersonNames::removeAt,
                onImportData = onImportData
            )
        } else {
            LoadingBoardingFragment()
        }
    }
}

fun NavController.navigateToOnBoarding() {
    navigate(
        ONBOARDINGROUTE,
        navOptions = navOptions { popUpTo(this@navigateToOnBoarding.graph.id) }
    )
}