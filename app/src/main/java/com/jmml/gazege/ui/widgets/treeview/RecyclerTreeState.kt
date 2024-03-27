package com.jmml.gazege.ui.widgets.treeview

import android.os.Parcelable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.parcelize.Parcelize

@Parcelize
data class NodeId(
    val parentId: NodeId?,
    val relativeIndex: Int,
    val level: Int
) : Parcelable {
    companion object {
        fun <T : Node<*, *>> from(node: T): NodeId {
            return NodeId(node.parentId, node.relativeIndex, node.level)
        }
    }
}

@Parcelize
class ParcelableTreeState(
    val expandedItems: List<NodeId>,
    val listState: List<Int>
) : Parcelable

@Composable
fun rememberTreeState(): TreeState {
    return rememberSaveable(saver = TreeState.Saver) {
        TreeState(
            mutableStateListOf(),
            LazyListState()
        )
    }
}

data class TreeState(
    val expandedItems: SnapshotStateList<NodeId>,
    val listState: LazyListState
) {
    companion object {
        val Saver: Saver<TreeState, *> = Saver<TreeState, ParcelableTreeState>(
            save = {
                ParcelableTreeState(
                    expandedItems = it.expandedItems,
                    listState = it.listState.let { state ->
                        listOf(
                            state.firstVisibleItemIndex,
                            state.firstVisibleItemScrollOffset
                        )
                    }
                )
            },
            restore = {
                TreeState(
                    expandedItems = mutableStateListOf(*it.expandedItems.toTypedArray()),
                    listState = LazyListState(
                        it.listState[0],
                        it.listState[1]
                    )
                )
            }
        )
    }
}