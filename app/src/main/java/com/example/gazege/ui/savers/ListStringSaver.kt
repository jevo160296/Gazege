package com.example.gazege.ui.savers

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableStringList(
    val values: List<String>
) : Parcelable

val listStringSaver = Saver<SnapshotStateList<String>, ParcelableStringList>(
    save = { state ->
        ParcelableStringList(state.toList())
    },
    restore = { mutableStateListOf(*it.values.toTypedArray()) }
)