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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
    contentCanScrollBack: () -> Boolean,
    headerScrollEnabled: () -> Boolean,
    freeScrollRange: ClosedRange<Float> = 0f..20f,
    header: @Composable BoxScope.() -> Unit,
    content: @Composable StickyHeaderScope.() -> Unit
) {
    val density = LocalDensity.current
    val (headerHeight, onHeaderHeightChange) = remember { mutableStateOf(0.dp) }
    val headerScrolling = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val scrollEnabled = headerScrollEnabled()
    val sHscope = remember(headerHeight, contentCanScrollBack, freeScrollRange, scrollEnabled) {
        StickyHeaderScope(
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val headerCanScrollDown = headerScrolling.targetValue < 0f
                    val headerCanScrollUp = headerScrolling.targetValue > -headerHeight.value
                    val scrollAmount = available.y
                    val scrollingDown = scrollAmount > 0
                    val scrollingDownFast = scrollAmount > freeScrollRange.endInclusive
                    val scrollingUpFast = scrollAmount < freeScrollRange.start
                    val scrollHeader = {
                        val verticalScroll = with(density) { available.y.toDp().value }
                        val newHeaderScroll =
                            (headerScrolling.targetValue + verticalScroll).coerceIn(-headerHeight.value..0f)
                        scope.launch { headerScrolling.snapTo(newHeaderScroll) }
                        available
                    }
                    val scrollContent = { Offset.Zero }
                    return if (
                        scrollEnabled &&
                        (headerCanScrollDown || !scrollingDown) &&
                        (scrollingUpFast && headerCanScrollUp ||
                                scrollingDownFast && headerCanScrollDown ||
                                scrollingDown && !contentCanScrollBack())
                    ) scrollHeader()
                    else scrollContent()
                }
            },
            NestedScrollDispatcher()
        )
    }
    LaunchedEffect(scrollEnabled) { if (!scrollEnabled) headerScrolling.animateTo(0f) }
    Box(
        modifier
            .pointerInput(sHscope) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (scrollEnabled) {
                        change.consume()
                        scope.launch {
                            headerScrolling.snapTo(
                                (headerScrolling.targetValue + dragAmount.toDp().value).coerceIn(
                                    -headerHeight.value..0f
                                )
                            )
                        }
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
    val (scrollEnabled, scrollEnabledChanged) = remember { mutableStateOf(true) }
    GazegeTheme {
        StickyHeaderLayout(
            Modifier
                .imePadding()
                .statusBarsPadding()
                .systemBarsPadding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            freeScrollRange = -20f..20f,
            headerScrollEnabled = { scrollEnabled },
            contentCanScrollBack = { state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 10 },
            header = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                ) {
                    Text(text = "firstVisibleItemIndex=${remember { derivedStateOf { state.firstVisibleItemIndex } }}")
                    Text(text = "offset=${remember { derivedStateOf { state.firstVisibleItemScrollOffset } }}")
                    Text(text = "T3")
                    Switch(checked = scrollEnabled, onCheckedChange = scrollEnabledChanged)
                }
            }) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .nestedScroll(nestedScrollConnection, nestedScrollDispatcher),
                state = state,
            ) {
                items(items.size) {
                    Text(modifier = Modifier, text = it.toString())
                }
            }
        }
    }
}