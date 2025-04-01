package com.jmml.gazege.ui.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jmml.gazege.MainViewModel
import com.jmml.zoo.clases.Result

fun NavGraphBuilder.screenShareTransaction(
    viewModelShareTransaction: MainViewModel.ViewModelShareTransaction,
) {
    composable(
        "shareTransaction?transactionId={transactionId}",
        arguments = listOf(
            navArgument("transactionId") {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { navBackStackEntry ->
        val transactionId =
            navBackStackEntry.arguments?.getInt("transactionId") ?: -1
        val transactionResult = viewModelShareTransaction.rememberTransaction(transactionId).value
        when (transactionResult) {
            is Result.Error -> Text(text = "Error loading transaction $transactionId")
            Result.Loading -> Text(text = "Loading transaction $transactionId")
            is Result.Success -> {
                val transaction = transactionResult.data
                Text(text = "Transaction $transactionId: ${transaction.descriptionString}")
            }
        }
    }
}

fun NavController.navigateToShareTransaction(transactionId: Int) =
    navigate("shareTransaction?transactionId=$transactionId")