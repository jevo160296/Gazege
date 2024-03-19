package com.jmml.gazege.ui.previews.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.Form
import com.jmml.gazege.ui.widgets.TextField

@Preview(showBackground = true, heightDp = 400)
@Composable
fun FormPreview() {
    val texts = remember {
        mutableStateMapOf<Int, String>()
    }
    GazegeTheme {
        Box(Modifier.fillMaxSize()) {
            Form(onSaveClicked = { }, isSavedButtonEnabled = true, title = "Form preview") {
                (0..50).forEach {
                    val value = texts.getOrElse(it) { "Text$it" }
                    TextField(value = value, onValueChange = { newVal -> texts[it] = newVal })
                }
            }
        }
    }
}