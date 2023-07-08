package com.example.gazege.sample.data

import com.example.gazege.MainViewModel
import com.example.gazege.ui.DatabaseSampleScope
import com.example.gazege.ui.databaseSample

enum class SampleId {
    BigSample,
    SmallSample
}

fun sample(sampleId: SampleId, viewModel: MainViewModel) {
    when (sampleId) {
        SampleId.BigSample -> bigSample(viewModel)
        SampleId.SmallSample -> smallSample(viewModel)
    }
}

private fun bigSample(
    viewModel: MainViewModel
) {
    databaseSample { buildSample(viewModel) }
}

private fun smallSample(
    viewModel: MainViewModel
) {
    databaseSample(
        categoriesAmount = 0,
        transactionAmount = 0,
        budgetAmount = 0,
        principalPersonAccountAmount = 5
    ) { buildSample(viewModel) }
}

private fun DatabaseSampleScope.buildSample(viewModel: MainViewModel) {
    viewModel.insertPerson(*personSample.toTypedArray()) {}
    viewModel.insertAccount(
        *accountSample.toTypedArray(),
        onErrorAction = {}) {}
    viewModel.insertCategory(*categorieSample.map { it.copy(parentId = null) }
        .toTypedArray(), onErrorAction = {}, onCompleitionAction = {})
    viewModel.updateCategory(
        *categorieSample.toTypedArray(),
        onErrorAction = {},
        onCompleitionAction = {})
    viewModel.insertTransaction(*transactionSample.toTypedArray()) {}
    viewModel.insertBudget(
        *budgetSample.toTypedArray(),
        onCompleitionAction = {},
        onErrorAction = {})
}