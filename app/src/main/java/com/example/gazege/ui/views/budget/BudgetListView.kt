package com.example.gazege.ui.views.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.core.entities.Budget
import com.example.gazege.core.entities.Category
import com.example.gazege.core.entities.FrequencyType
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.views.category.getCategoriesSample
import com.example.gazege.ui.widgets.Card
import com.example.gazege.ui.widgets.RecyclerView
import java.time.LocalDate
import kotlin.random.Random

@Composable
private fun BudgetViewHolder(
    budget: Budget
) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Box(Modifier.padding(PaddingValues(8.dp))) {
            Text(text = budget.id.toString())
        }
    }
}

@Composable
fun BudgetRecyclerView(
    modifier: Modifier,
    itemHolderPaddingValues: PaddingValues,
    budget: List<Budget>,
    onItemClick: (budget: Budget) -> Unit,
    onItemLongClick: (budget: Budget) -> Unit
) {
    val state = rememberLazyListState()
    RecyclerView(
        elements = budget,
        modifier = modifier,
        onItemTapped = {},
        onItemLongPressed = {},
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
    return (0..20).map {
        val categoryIndex = random.nextInt(categorySize)
        val frequencyTypeOrdinal = random.nextInt(frequencyTypeSize)
        Budget(
            id = it,
            categoryId = categorySample[categoryIndex].id!!,
            value = random.nextDouble(100.0, 1000.0),
            frequency = random.nextInt(1, 5),
            frequencyType = FrequencyType.values()[frequencyTypeOrdinal],
            startDate = LocalDate.of(2023, 1, 1)
        )
    }
}

@Preview
@Composable
private fun BudgetPreview() {
    val budgetSample = getBudgetSample(getCategoriesSample())
    GazegeTheme {
        Surface(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BudgetRecyclerView(
                modifier = Modifier,
                itemHolderPaddingValues = PaddingValues(),
                budget = budgetSample,
                onItemClick = {},
                onItemLongClick = {}
            )
        }
    }
}