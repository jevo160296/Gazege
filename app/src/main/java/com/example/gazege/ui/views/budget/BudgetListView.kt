package com.example.gazege.ui.views.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.R
import com.example.gazege.core.entities.*
import com.example.gazege.ui.doubleToPercentageString
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.account.getAccountAndOwnerWithTransactionsSample
import com.example.gazege.ui.views.account.getAccountSample
import com.example.gazege.ui.views.category.getCategoriesSample
import com.example.gazege.ui.views.person.getPersonSample
import com.example.gazege.ui.views.transaction.getTransactionAndAccountsAndCategorySample
import com.example.gazege.ui.views.transaction.getTransactionSample
import com.example.gazege.ui.widgets.RecyclerView
import java.time.LocalDate
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetViewHolder(
    budget: BudgetAndCategoryWithCalculatedData
) {
    val overlineText = "each ${budget.budgetFrequency}, period ${budget.budgetFrequencyType}\n"
    val headlineText = "${stringResource(id = R.string.Presupuesto)}: ${budget.categoryName}"
    val supportingView = @Composable {
        Column {
            Text(text = "${stringResource(R.string.Progreso)}: ${doubleToPercentageString(budget.budgetCompleition)}")
            LinearProgressIndicator(progress = budget.budgetCompleition.toFloat())
            Text(text = "expectedTotalFlow ${budget.budgetExpectedTotalFlow}")
            Text(text = "expectedRemeiningFlow ${budget.budgetExpectedRemainingFlow}")
            Text(text = "realTotalFlow ${budget.budgetRealTotalFlow}")
            Text(text = "expectedFlowUntilNow ${budget.budgetExpectedFlowUntilNow}")
        }
    }

    ListItem(
        overlineText = { Text(text = overlineText) },
        headlineText = { Text(text = headlineText) },
        supportingText = { supportingView() },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun BudgetRecyclerView(
    modifier: Modifier,
    itemHolderPaddingValues: PaddingValues,
    budget: List<BudgetAndCategoryWithCalculatedData>,
    onBudgetDetailRequested: (budget: BudgetAndCategoryWithCalculatedData) -> Unit,
    onBudgetDeleteRequested: (budget: BudgetAndCategoryWithCalculatedData) -> Unit,
    onBudgetEditRequested: (budget: BudgetAndCategoryWithCalculatedData) -> Unit
) {
    val state = rememberLazyListState()
    var menuIdExpanded: Int? by remember {
        mutableStateOf(null)
    }
    RecyclerView(
        elements = budget,
        modifier = modifier,
        onItemTapped = onBudgetDetailRequested,
        onItemLongPressed = { menuIdExpanded = it.budgetId },
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        viewHolder = {
            Box {
                BudgetViewHolder(
                    budget = it
                )
                DropdownMenu(
                    expanded = menuIdExpanded == it.budgetId,
                    onDismissRequest = { menuIdExpanded = null }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.Editar)) },
                        onClick = {
                            menuIdExpanded = null
                            onBudgetEditRequested(it)
                        })
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.Eliminar)) },
                        onClick = {
                            menuIdExpanded = null
                            onBudgetDeleteRequested(it)
                        }
                    )
                }
            }
        }
    )
}

fun getBudgetSample(
    categorySample: List<Category>
): List<Budget> {
    val random = Random(3)
    val categorySize = categorySample.size
    val frequencyTypeSize = FrequencyType.values().size
    val startDate = LocalDate.of(2023, 1, 1)
    return (0..20).map {
        val categoryIndex = random.nextInt(categorySize)
        val frequencyTypeOrdinal = random.nextInt(frequencyTypeSize)
        val frequencyType = FrequencyType.values()[frequencyTypeOrdinal]
        val frequency = random.nextInt(1, 5)
        val value = random.nextDouble(100.0, 1000.0)
        val categoryId = categorySample[categoryIndex].id!!
        when (frequencyType) {
            FrequencyType.DAILY -> Budget.fromDaily(
                id = it,
                categoryId = categoryId,
                value = value,
                frequency = frequency,
                startDate = startDate
            )
            FrequencyType.WEEKLY -> Budget.fromWeekly(
                id = it,
                categoryId = categoryId,
                value = value,
                frequency = frequency,
                startDate = startDate,
                each = WeekDays.from(random.nextInt(until = (0b1111111 + 1)))
            )
            FrequencyType.MONTHLY -> Budget.fromMonthly(
                id = it,
                categoryId = categoryId,
                value = value
            )
        }
    }
}

@Preview
@Composable
private fun BudgetPreview() {
    val persons = getPersonSample()
    val accountSample = getAccountSample(persons)
    val categories = getCategoriesSample()
    val transactionSample = getTransactionSample(accountSample, categories)
    val accountsAndOwnerWithTransaction =
        getAccountAndOwnerWithTransactionsSample(accountSample, persons)
    val transactions =
        getTransactionAndAccountsAndCategorySample(transactionSample, accountSample, categories)
    val budgetSample = getBudgetSample(categories)
    val person = persons.first()
    val accountAndOwnerWithTransactions = AccountAndOwnerWithTransactions.from(
        accounts = accountsAndOwnerWithTransaction.map { it.account },
        owners = persons,
        transactions = transactions.map { it.transaction }
    )
    val budgetAndCategoryWithTransactions = BudgetAndCategoryWithTransactions.from(
        budget = budgetSample,
        category = categories,
        person = person,
        accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
    )
    val budgetAndCategoryWithCalculatedData = BudgetAndCategoryWithCalculatedData.from(
        budgetAndCategoryWithTransactions,
        LocalDate.of(2023, 1, 14),
        LocalDate.of(2023, 1, 1),
        LocalDate.of(2023, 1, 31)
    )

    GazegeTheme {
        Surface(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
        ) {
            BudgetRecyclerView(
                modifier = Modifier,
                itemHolderPaddingValues = PaddingValues(),
                budget = budgetAndCategoryWithCalculatedData,
                onBudgetDetailRequested = {},
                onBudgetDeleteRequested = {},
                onBudgetEditRequested = {}
            )
        }
    }
}