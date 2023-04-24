package com.example.gazege.ui.widgets

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.ui.theme.GazegeTheme

@Composable
fun ButtonField(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick,
            Modifier
                .fillMaxWidth()
                .then(modifier),
            enabled,
            shape,
            colors,
            elevation,
            border,
            contentPadding,
            interactionSource,
            content
        )
    }
}

@Composable
fun SegmentedButton(
    modifier: Modifier = Modifier,
    buttonModifier: RowScope.() -> Modifier = { Modifier.weight(1f) },
    selectedIndex: Int?,
    items: List<SegmentedButtonItem>,
    onItemClicked: (index: Int) -> Unit
) {
    val cantItems = items.size
    fun Int.isFirst() = this == 0
    fun Int.isLast() = this == cantItems - 1
    Row(modifier = modifier) {
        items.forEachIndexed { index, segmentedButtonItem ->
            val isFirst = index.isFirst()
            val isLast = index.isLast()
            OutlinedButton(
                onClick = { onItemClicked(index) },
                shape = RoundedCornerShape(
                    topStartPercent = 50.takeIf { isFirst } ?: 0,
                    bottomStartPercent = 50.takeIf { isFirst } ?: 0,
                    topEndPercent = 50.takeIf { isLast } ?: 0,
                    bottomEndPercent = 50.takeIf { isLast } ?: 0
                ),
                modifier = buttonModifier(),
                colors = if (index == selectedIndex) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.outlinedButtonColors()
                }
            ) {
                segmentedButtonItem.leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
                segmentedButtonItem.text()
            }
        }
    }
}

data class SegmentedButtonItem(
    val text: @Composable () -> Unit,
    val leadingIcon: @Composable () -> Unit
) {
    companion object {
        fun from(text: String, leadingIcon: Painter) = SegmentedButtonItem(
            text = { Text(text, maxLines = 1) },
            leadingIcon = {
                Icon(
                    painter = leadingIcon,
                    contentDescription = "SegmentedButtonItem"
                )
            }
        )
    }
}

@Preview(showBackground = true, heightDp = 620, widthDp = 420)
@Composable
private fun Preview() {
    GazegeTheme {
        var selectedItem: Int? by remember { mutableStateOf(null) }
        val onItemClicked: (Int) -> Unit = { it: Int ->
            Log.println(Log.INFO, "Clicked", "Item $it clicked")
            selectedItem = it
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("ButtonField")
            ButtonField(onClick = {}) {
                Text("New person")
            }
            Text("SegmentedButton")
            SegmentedButton(
                items = listOf(
                    SegmentedButtonItem(
                        text = { Text("Opción") },
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Face),
                                contentDescription = "Opcion"
                            )
                        }
                    )
                ),
                onItemClicked = onItemClicked,
                selectedIndex = selectedItem
            )
            SegmentedButton(
                items = listOf(
                    SegmentedButtonItem(
                        text = { Text("Opción1") },
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Face),
                                contentDescription = "Opcion1"
                            )
                        }
                    ),
                    SegmentedButtonItem(
                        text = { Text("Opción2") },
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Home),
                                contentDescription = "Opcion2"
                            )
                        }
                    )
                ),
                onItemClicked = onItemClicked,
                selectedIndex = selectedItem
            )
            SegmentedButton(
                items = listOf(
                    SegmentedButtonItem(
                        text = { Text("Opción1", maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Face),
                                contentDescription = "Opcion1"
                            )
                        }
                    ),
                    SegmentedButtonItem(
                        text = { Text("Opción2", maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Rounded.Home),
                                contentDescription = "Opcion2"
                            )
                        }
                    ),
                    SegmentedButtonItem.from(
                        text = "Opción3",
                        leadingIcon = rememberVectorPainter(image = Icons.Rounded.Favorite)
                    )
                ),
                onItemClicked = onItemClicked,
                selectedIndex = selectedItem
            )
        }
    }
}