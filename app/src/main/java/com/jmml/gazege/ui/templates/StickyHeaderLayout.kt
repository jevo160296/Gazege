package com.jmml.gazege.ui.templates

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jmml.gazege.ui.theme.GazegeTheme
import kotlinx.coroutines.launch

@Composable
fun StickyHeaderLayout(
    modifier: Modifier = Modifier,
    canScrollBack: () -> Boolean,
    freeScrollRange: ClosedRange<Float> = 0f..20f,
    header: @Composable BoxScope.() -> Unit,
    content: @Composable StickyHeaderScope.() -> Unit
) {
    val density = LocalDensity.current
    val (headerHeight, onHeaderHeightChange) = remember { mutableStateOf(0.dp) }
    val headerScrolling = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val sHscope = remember(headerHeight, canScrollBack, freeScrollRange) {
        StickyHeaderScope(
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val headerCanScrollDown = headerScrolling.targetValue < 0f
                    val headerCanScrollUp = headerScrolling.targetValue > -headerHeight.value
                    val scrollHeader = { verticalScroll: Float ->
                        val newHeaderScroll =
                            (headerScrolling.targetValue + verticalScroll).coerceIn(-headerHeight.value..0f)
                        scope.launch { headerScrolling.snapTo(newHeaderScroll) }
                        available
                    }
                    val scrollContent = { Offset.Zero }
                    return if (
                        available.y < freeScrollRange.start && headerCanScrollUp ||
                        available.y > freeScrollRange.endInclusive && headerCanScrollDown ||
                        available.y > 0 && !canScrollBack()
                    ) scrollHeader(with(density) { available.y.toDp().value })
                    else scrollContent()
                }
            },
            NestedScrollDispatcher()
        )
    }
    Box(modifier
        .pointerInput(sHscope) {
            detectVerticalDragGestures { change, dragAmount ->
                change.consume()
                scope.launch {
                    headerScrolling.snapTo(
                        (headerScrolling.targetValue + dragAmount.toDp().value).coerceIn(
                            -headerHeight.value..0f
                        )
                    )
                }
            }
        }) {
        Box(Modifier
            .onSizeChanged {
                onHeaderHeightChange(with(density) { it.height.toDp() })
            }
            .offset {
                IntOffset(
                    0,
                    headerScrolling.value.dp
                        .toPx()
                        .toInt()
                )
            }
        ) { this.header() }
        Box(Modifier
            .offset {
                IntOffset(
                    0,
                    headerHeight.toPx().toInt() + headerScrolling.value.dp.toPx().toInt()
                )
            }
        ) { sHscope.content() }
    }
}

class StickyHeaderScope(
    val nestedScrollConnection: NestedScrollConnection,
    val nestedScrollDispatcher: NestedScrollDispatcher
)

@Preview
@Composable
private fun StickyHeaderLayoutPreview() {
    val items = (0..100).toList()
    val state = rememberLazyListState()
    GazegeTheme {
        StickyHeaderLayout(
            Modifier
                .imePadding()
                .statusBarsPadding()
                .systemBarsPadding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            canScrollBack = { state.firstVisibleItemIndex > 0 },
            header = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                ) {
                    Text(text = "T1")
                    Text(text = "T2")
                    Text(text = "T3")
                }
            }) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .nestedScroll(nestedScrollConnection, nestedScrollDispatcher),
                state = state,
            ) {
                items(items.size) {
                    Text(it.toString())
                }
            }
        }
    }
}