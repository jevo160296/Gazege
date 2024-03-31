package com.jmml.gazege.ui.widgets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
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
import com.jmml.gazege.R
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.treeview.ColumnTreeView
import com.jmml.gazege.ui.widgets.treeview.DefaultTreeLeadingIcon
import com.jmml.gazege.ui.widgets.treeview.Node
import com.jmml.gazege.ui.widgets.treeview.NodeId
import com.jmml.gazege.ui.widgets.treeview.TreeState
import com.jmml.gazege.ui.widgets.treeview.rememberTreeState
import java.text.Normalizer

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

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

private fun partialStringMatch(originalString: String, stringToMatch: String) =
    originalString
        .unaccent()
        .matches(
            Regex(
                ".*${stringToMatch.unaccent()}.*",
                setOf(
                    RegexOption.DOT_MATCHES_ALL,
                    RegexOption.IGNORE_CASE
                )
            )
        )

private fun <N, C : Node<N, C>> recursiveMatch(
    node: C,
    itemMatch: (item: C) -> Boolean
): List<NodeId> = itemMatch(node)
    .let { nodeMatches ->
        node
            .children
            .flatMap { child -> recursiveMatch(child) { nodeMatches || itemMatch(it) } }
            .let { childrenFinds ->
                if (childrenFinds.isNotEmpty() || nodeMatches) {
                    childrenFinds.plusElement(node.id())
                } else {
                    emptyList()
                }
            }
    }

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
    currentTextFilter: (item: T, text: String) -> Boolean = { item: T, text: String ->
        partialStringMatch(
            itemToString(item),
            text
        )
    },
    groupByKeySelector: ((T) -> String)? = null
) {
    val groupedOptions = options
        .filter { currentTextFilter(it, currentText) || filteringNotStarted }
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
private fun <N, C : Node<N, C>> OptionsGroupTreeView(
    groupedOptions: Map<String?, List<C>>,
    treeState: TreeState = rememberTreeState(),
    onNodeClick: (C) -> Unit,
    nodeToString: (C?) -> String,
    nodeEnabled: (C) -> Boolean,
    nodeVisible: (C) -> Boolean = { true }
) {
    val nodes: List<C> = groupedOptions.flatMap { it.value }
    val inverseMap: Map<NodeId, String?> = groupedOptions.flatMap { (key, value) ->
        value.map {
            it.id() to key
        }
    }.toMap()
    val contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
    ColumnTreeView(
        nodes = nodes,
        treeState = treeState,
        groupSelector = { inverseMap[it.id()] },
        nodeVisible = nodeVisible,
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

    val treeState = rememberTreeState()
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
    val queryResults: Set<NodeId> = remember(currentText) {
        options.flatMap {
            recursiveMatch(it) { item ->
                partialStringMatch(itemToString(item), currentText)
            }
        }
            .toSet()
            .onEach { if (dropDownExpanded && !filteringNotStarted) treeState.expandToItem(it) }
    }
    val optionsViewHolder: @Composable (Map<String?, List<C>>) -> Unit = {
        OptionsGroupTreeView(
            groupedOptions = it,
            treeState = treeState,
            onNodeClick = { item ->
                onExpandedChange(false)
                onItemClick(item)
                currentText = itemToString(item)
                filteringNotStarted = true
            },
            nodeToString = itemToString,
            nodeEnabled = nodeEnabled,
            nodeVisible = { node -> filteringNotStarted || node.id() in queryResults }
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
        filteringNotStarted = filteringNotStarted,
        currentTextFilter = { _, _ -> true }
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


@Preview(group = "treecombobox")
@Composable
fun TreeComboBoxPreview() {
    val data = remember {
        listOf(
            "Ataud" to null,
            "Bola" to "Ataud",
            "Canasta" to "Ataud",
            "Alberca" to null,
            "Bebe" to "Alberca",
            "Café" to "Alberca",
            "Canasta" to null,
            "Zapato" to "Canasta",
            "Puerta" to "Canasta",
            "Boliche" to "Puerta",
            "Mancana" to "Puerta"
        ).toMap()
    }
    val transformedData = StringTree.from(data)
    val (dropDownExpanded, onDropDownExpandedChange) = remember { mutableStateOf(false) }
    val (selectedItem, onSelectedItemChange) = remember { mutableStateOf<StringTree?>(null) }
    TreeComboBox(
        Modifier.padding(top = 24.dp),
        dropDownExpanded = dropDownExpanded,
        onExpandedChange = onDropDownExpandedChange,
        options = transformedData,
        selectedItem = selectedItem,
        itemToString = { item -> item?.let { "${item.content} ${item.id()}" } ?: "" },
        label = { Text(text = "Label") },
        onItemClick = onSelectedItemChange,
        nodeEnabled = { true }
    )
}

class StringTree(
    override val content: String,
    override val children: List<StringTree>,
    override val parentId: NodeId?,
    override val relativeIndex: Int,
    override val level: Int
) : Node<String, StringTree> {
    override fun toString(): String {
        return this.content
    }

    companion object {
        fun from(values: Map<String, String?>): List<StringTree> {
            val parents = values
                .filter { it.value == null || it.value == it.key }
                .toList()
            return parents.mapIndexed { index, pair ->
                StringTree(
                    content = pair.first,
                    children = from(
                        values,
                        values.filterValues { it == pair.first },
                        NodeId(null, index, 0),
                        1
                    ),
                    parentId = null,
                    relativeIndex = index,
                    level = 0
                )
            }
        }

        private fun from(
            values: Map<String, String?>,
            chlidren: Map<String, String?>,
            id: NodeId,
            level: Int
        ): List<StringTree> {
            val childrenList = chlidren.toList()
            return childrenList.mapIndexed { index, pair ->
                StringTree(
                    content = pair.first,
                    children = from(
                        values,
                        values.filterValues { it == pair.first },
                        NodeId(id, index, level),
                        level + 1
                    ),
                    parentId = id,
                    relativeIndex = index,
                    level = level
                )
            }
        }
    }
}
