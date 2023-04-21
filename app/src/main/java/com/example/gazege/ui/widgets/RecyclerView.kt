package com.example.gazege.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gazege.ui.templates.itemsGrouped
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun DefaultGroupViewHolder(group: String) = Box(
    modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
) {
    LargeEmphasis(
        group,
        color = MaterialTheme
            .colorScheme
            .onBackground
    )
}


@Preview(showBackground = true, widthDp = 120, heightDp = 240)
@Composable
private fun RecyclerViewPreview() {
    GazegeTheme {
        val elements = (0..30).map {
            Pair(it, "Element $it")
        }
        LazyColumn {
            itemsGrouped(
                elements,
                groupSelector = { it.first.mod(10).toString() },
                groupViewHolder = { DefaultGroupViewHolder(it) }
            ) {
                Text(it.second)
            }
        }
    }
}