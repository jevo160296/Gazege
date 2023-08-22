package com.example.gazege.ui.widgets

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gazege.R
import com.example.gazege.ui.theme.GazegeTheme
import com.example.gazege.ui.widgets.treeview.ColumnTreeView
import com.example.gazege.ui.widgets.treeview.DefaultTreeLeadingIcon
import com.example.gazege.ui.widgets.treeview.Node
import com.example.gazege.ui.widgets.treeview.NodeId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ComboBox(
    modifier: Modifier = Modifier,
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<T>,
    selectedItem: T?,
    itemToString: (T?) -> String,
    onItemClick: (T) -> Unit,
    label: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    groupByKeySelector: ((T) -> String)? = null
) {
    var filteringNotStarted by remember(dropDownExpanded) { mutableStateOf(dropDownExpanded) }
    var currentText by remember(selectedItem) { mutableStateOf(itemToString(selectedItem)) }

    val trailingIcon: @Composable () -> Unit =
        { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded) }
    val optionsViewHolder: @Composable (groupedOptions: Map<String?, List<T>>) -> Unit = {
        OptionsGroupView(
            groupedOptions = it,
            onItemClick = { item ->
                onExpandedChange(false)
                onItemClick(item)
                currentText = itemToString(item)
                filteringNotStarted = true
            },
            itemToString = itemToString
        )
    }
    CoreComboBox(
        modifier = modifier,
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = onExpandedChange,
        currentText = currentText,
        onCurrentTextChanged = {
            filteringNotStarted = false
            currentText = it
        },
        label = label,
        keyboardActions = keyboardActions,
        keyboardOptions = keyboardOptions,
        trailingIcon = { trailingIcon() },
        options = options,
        filteringNotStarted = filteringNotStarted,
        groupByKeySelector = groupByKeySelector,
        itemToString = itemToString,
        optionsViewHolder = { optionsViewHolder(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <N, C : Node<N, C>> TreeComboBox(
    modifier: Modifier = Modifier,
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<C>,
    selectedItem: C?,
    itemToString: (C?) -> String,
    label: @Composable () -> Unit,
    onItemClick: (C) -> Unit,
    canClearSelection: Boolean = false,
    onClearSelectionClicked: () -> Unit = {},
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    nodeEnabled: (C) -> Boolean,
    groupByKeySelector: ((C) -> String)? = null
) {
    var filteringNotStarted by remember(dropDownExpanded) { mutableStateOf(dropDownExpanded) }
    var currentText by remember(selectedItem) { mutableStateOf(itemToString(selectedItem)) }

    val trailingIcon: @Composable () -> Unit = {
        val showClearButton = canClearSelection && selectedItem != null
        AnimatedContent(
            targetState = showClearButton,
            transitionSpec = {
                scaleIn() togetherWith scaleOut()
            },
            contentAlignment = Alignment.Center, label = ""
        ) {
            if (it) {
                IconButton(onClick = onClearSelectionClicked) {
                    Icon(
                        painter = painterResource(id = R.drawable.clear_selection),
                        contentDescription = "Clear"
                    )
                }
            } else {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
            }
        }
    }
    val optionsViewHolder: @Composable (Map<String?, List<C>>) -> Unit = {
        OptionsGroupTreeView(
            groupedOptions = it,
            onNodeClick = { item ->
                onExpandedChange(false)
                onItemClick(item)
                currentText = itemToString(item)
                filteringNotStarted = true
            },
            nodeToString = itemToString,
            nodeEnabled = nodeEnabled
        )
    }

    CoreComboBox(
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = onExpandedChange,
        currentText = currentText,
        onCurrentTextChanged = {
            filteringNotStarted = false
            currentText = it
        },
        label = { label() },
        trailingIcon = { trailingIcon() },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier,
        options = options,
        optionsViewHolder = { optionsViewHolder(it) },
        itemToString = itemToString,
        groupByKeySelector = groupByKeySelector,
        filteringNotStarted = filteringNotStarted
    )
}

@Composable
fun <N, C : Node<N, C>> MutableTreeComboBox(
    modifier: Modifier = Modifier,
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<C>,
    selectedItem: C?,
    itemToString: (C?) -> String,
    label: @Composable () -> Unit,
    onItemClick: (C) -> Unit,
    canClearSelection: Boolean = false,
    onClearSelectionClicked: () -> Unit = {},
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    nodeEnabled: (C) -> Boolean,
    enabled: Boolean,
    contentWhenDisabled: (@Composable () -> Unit)?,
    groupByKeySelector: ((C) -> String)? = null
) {
    if (enabled) {
        TreeComboBox(
            dropDownExpanded = dropDownExpanded,
            onExpandedChange = onExpandedChange,
            options = options,
            selectedItem = selectedItem,
            itemToString = itemToString,
            label = { label() },
            onItemClick = onItemClick,
            nodeEnabled = nodeEnabled,
            canClearSelection = canClearSelection,
            onClearSelectionClicked = onClearSelectionClicked,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            modifier = modifier,
            groupByKeySelector = groupByKeySelector
        )
    } else {
        if (contentWhenDisabled != null) {
            Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
                contentWhenDisabled()
            }
        }
    }
}

@Composable
fun <N, C : Node<N, C>> DefaultComboBoxViewHolder(
    itemToString: (C?) -> String,
    node: C,
    onItemClick: (C) -> Unit,
    contentPadding: PaddingValues,
    enabled: Boolean
) {
    val layoutDirection = LocalLayoutDirection.current
    DropdownMenuItem(
        text = { Text(itemToString(node)) },
        onClick = { onItemClick(node) },
        contentPadding = contentPadding.let {
            PaddingValues(
                start = 8.dp,
                top = it.calculateTopPadding(),
                bottom = it.calculateBottomPadding(),
                end = it.calculateEndPadding(layoutDirection)
            )
        },
        enabled = enabled
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionsGroupView(
    groupedOptions: Map<String?, List<T>>,
    onItemClick: (T) -> Unit,
    itemToString: (T?) -> String
) {
    groupedOptions.map {
        val group = it.key
        val values = it.value
        if (group != null) {
            Text(group, modifier = Modifier.padding(4.dp))
        }
        values.map {
            DropdownMenuItem(
                text = { Text(itemToString(it)) },
                onClick = { onItemClick(it) },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
        }
    }
}

private fun partialStringMatch(originalString: String, stringToMatch: String) =
    originalString.matches(
        Regex(
            ".*$stringToMatch.*",
            setOf(
                RegexOption.DOT_MATCHES_ALL,
                RegexOption.IGNORE_CASE
            )
        )
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> CoreComboBox(
    modifier: Modifier = Modifier,
    currentText: String,
    onCurrentTextChanged: (String) -> Unit,
    dropDownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: (@Composable () -> Unit)?,
    keyboardActions: KeyboardActions,
    keyboardOptions: KeyboardOptions,
    trailingIcon: @Composable () -> Unit,
    options: List<T>,
    optionsViewHolder: @Composable (Map<String?, List<T>>) -> Unit,
    itemToString: (T?) -> String,
    filteringNotStarted: Boolean,
    groupByKeySelector: ((T) -> String)? = null
) {
    val groupedOptions = options
        .filter { partialStringMatch(itemToString(it), currentText) || filteringNotStarted }
        .groupBy { groupByKeySelector?.invoke(it) }
    var isFocused by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier.onFocusChanged {
            isFocused = it.hasFocus
        }
    ) {
        TextField(
            modifier = Modifier.menuAnchor(),
            value = currentText,
            onValueChange = {
                if (dropDownExpanded.not()) {
                    onExpandedChange(true)
                }
                onCurrentTextChanged(it)
            },
            readOnly = false,
            trailingIcon = { trailingIcon() },
            label = label,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            maxLines = 1,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions
        )
        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            optionsViewHolder(groupedOptions)
        }
    }

    LaunchedEffect(key1 = isFocused) {
        if (isFocused != dropDownExpanded) {
            onExpandedChange(isFocused)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <N, C : Node<N, C>> OptionsGroupTreeView(
    groupedOptions: Map<String?, List<C>>,
    onNodeClick: (C) -> Unit,
    nodeToString: (C?) -> String,
    nodeEnabled: (C) -> Boolean
) {
    val nodes: List<C> = groupedOptions.flatMap {
        it.value
    }
    val inverseMap: Map<NodeId, String?> = groupedOptions.flatMap { (key, value) ->
        value.map {
            it.id() to key
        }
    }.toMap()
    val contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
    ColumnTreeView(
        nodes = nodes,
        groupSelector = { inverseMap[it.id()] },
        itemHolderPaddingValues = contentPadding
    ) { node, treeScope ->
        Row {
            Spacer(modifier = Modifier.width(node.level.dp * 8))
            val isExpanded = treeScope.isExpanded(node)
            if (node.children.isNotEmpty()) {
                IconToggleButton(
                    modifier = Modifier.width(32.dp),
                    checked = isExpanded,
                    onCheckedChange = { treeScope.toggleExpanded(node) }
                ) {
                    DefaultTreeLeadingIcon(isExpanded = isExpanded)
                }
            } else {
                Spacer(Modifier.width(32.dp))
            }
            DefaultComboBoxViewHolder(
                itemToString = nodeToString,
                node = node,
                onItemClick = onNodeClick,
                contentPadding = contentPadding,
                enabled = nodeEnabled(node)
            )
        }
    }
}

@Preview
@Composable
fun ComboBoxPreview() {
    val (dropDownExpanded, onDropDownExpandedChange) = remember { mutableStateOf(false) }
    val (selectedItem, onSelectedItemChanged) = remember { mutableStateOf<String?>(null) }

    val options = (1..10).map { "Item$it" }
    val itemToString = { it: String? -> it ?: "NULL" }
    GazegeTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ComboBox(
                dropDownExpanded = dropDownExpanded,
                onExpandedChange = onDropDownExpandedChange,
                options = options,
                selectedItem = selectedItem,
                itemToString = itemToString,
                onItemClick = onSelectedItemChanged
            )
        }
    }
}