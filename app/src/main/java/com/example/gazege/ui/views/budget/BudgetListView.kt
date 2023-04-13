package com.example.gazege.ui.views.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.core.entities.*
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.account.getAccountSample
import com.example.gazege.ui.views.category.getCategoriesSample
import com.example.gazege.ui.views.person.getPersonSample
import com.example.gazege.ui.views.transaction.getTransactionSample
import com.example.gazege.ui.widgets.RecyclerView
import java.time.LocalDate
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetViewHolder(
    budget: BudgetAndCategoryWithTransactions
) {
    val supportingText = "each ${budget.budgetFrequency}, period ${budget.budgetFrequencyType}\n" +
            "${budget.budgetEachClass}"
    ListItem(
        headlineText = { Text(text = budget.categoryName) },
        supportingText = { Text(text = supportingText) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun BudgetRecyclerView(
    modifier: Modifier,
    itemHolderPaddingValues: PaddingValues,
    budget: List<BudgetAndCategoryWithTransactions>,
    onItemClick: (budget: BudgetAndCategoryWithTransactions) -> Unit,
    onItemLongClick: (budget: BudgetAndCategoryWithTransactions) -> Unit
) {
    val state = rememberLazyListState()
    RecyclerView(
        elements = budget,
        modifier = modifier,
        onItemTapped = onItemClick,
        onItemLongPressed = onItemLongClick,
        itemHolderPaddingValues = itemHolderPaddingValues,
        state = state,
        viewHolder = {
            BudgetViewHolder(
                budget = it
            )
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
    val transactions = getTransactionSample()
    val categories = getCategoriesSample()
    val budgetSample = getBudgetSample(categories)
    val accounts = getAccountSample()
    val persons = getPersonSample()
    val person = persons.first()
    val accountAndOwnerWithTransactions = AccountAndOwnerWithTransactions.from(
        accounts = accounts.map { it.account },
        owners = persons,
        transactions = transactions.map { it.transaction }
    )
    val budgetAndCategoryWithTransactions = BudgetAndCategoryWithTransactions.from(
        budget = budgetSample,
        category = categories,
        person = person,
        accountAndOwnerWithTransactions = accountAndOwnerWithTransactions
    )

    GazegeTheme {
        Surface(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BudgetRecyclerView(
                modifier = Modifier,
                itemHolderPaddingValues = PaddingValues(),
                budget = budgetAndCategoryWithTransactions,
                onItemClick = {},
                onItemLongClick = {}
            )
        }
    }
}