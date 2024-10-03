package com.jmml.gazege.ui.views.promissorynote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jmml.gazege.R
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.DateFormat
import com.jmml.gazege.ui.localDateToString
import com.jmml.gazege.ui.templates.ClickableListItemViewHolder
import com.jmml.gazege.ui.templates.GroupedLazyList
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.DefaultGroupViewHolder
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.LargeEmphasis
import com.jmml.gazege.ui.widgets.PulsatingCard
import com.jmml.gazege.ui.widgets.SmallEmphasis
import com.jmml.zoo.extensions.numerical.toMoneyString

@Composable
fun LoadingPromissoryNoteListView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) = GroupedLazyList(
    modifier = modifier,
    state = LazyListState(),
    contentPadding = contentPadding,
    items = (0..10).toList(),
    groupSelector = {""},
    groupViewHolder = {
        PulsatingCard(
            modifier = Modifier
                .width(90.dp)
                .height(18.dp)
                .clip(shape = MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.scrim,
            minAlpha = 0.0F,
            maxAlpha = 0.2F
        )
    }
) {
    DefaultPromissoryNoteViewHolder(
        promissoryNote = null,
        editPromissoryNote = {},
        delPromissoryNote = {}
    )
}

@Composable
fun LoadedPromissoryNoteListView(
    promissoryNotes: List<PromissoryNoteViewModel>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: LazyListState = rememberLazyListState(),
    editPromissoryNote: (PromissoryNoteViewModel) -> Unit,
    delPromissoryNote: (PromissoryNoteViewModel) -> Unit,
    dateViewHolder: @Composable (String) -> Unit = { DefaultGroupViewHolder(it) },
    promissoryNoteViewHolder: @Composable (PromissoryNoteViewModel) -> Unit = {
        DefaultPromissoryNoteViewHolder(
            promissoryNote = it,
            editPromissoryNote = editPromissoryNote,
            delPromissoryNote = delPromissoryNote
        )
    }
) = GroupedLazyList(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
    items = promissoryNotes,
    groupSelector = { localDateToString(it.promissoryNote.date, DateFormat.DAYMONTHYEAR) },
    groupViewHolder = dateViewHolder,
    itemViewHolder = promissoryNoteViewHolder
)

@Composable
fun DefaultPromissoryNoteViewHolder(
    promissoryNote: PromissoryNoteViewModel?,
    editPromissoryNote: (PromissoryNoteViewModel) -> Unit,
    delPromissoryNote: (PromissoryNoteViewModel) -> Unit
) {
     if(promissoryNote != null){
         val sourceId = promissoryNote.promissoryNote.sourceId
         val destinationId = promissoryNote.promissoryNote.destinationId
         val principalPerson = promissoryNote.principalPersonId
         val amount = promissoryNote.promissoryNote.amount.toMoneyString()
         val isIn = when{
             sourceId == principalPerson && destinationId != principalPerson -> false
             sourceId != principalPerson && destinationId == principalPerson -> true
             else -> null
         }
         val color = when(isIn){
             true -> GazegeTheme.gazegeColorScheme.income
             false -> GazegeTheme.gazegeColorScheme.outcome
             else -> MaterialTheme.colorScheme.onBackground
         }
         val personName = when (isIn) {
             true -> promissoryNote.sourcePerson.name
             false -> promissoryNote.destinationPerson.name
             else -> "${promissoryNote.sourcePerson.name} --> ${promissoryNote.destinationPerson.name}"
         }
         val icon = @Composable {
             Row(
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.DefaultPadding))
             ) {
                 androidx.compose.material3.Icon(
                     painter = painterResource(R.drawable.promissory_note),
                     contentDescription = "Promissory note",
                     tint = color
                 )
                 SmallEmphasis(text = stringResource(R.string.promissory_note), color = color)
             }
         }

         var menuIdExpanded: Int? by remember {
             mutableStateOf(null)
         }
         ClickableListItemViewHolder(
             onItemTapped = { editPromissoryNote(promissoryNote) },
             onItemLongPressed = { menuIdExpanded = promissoryNote.promissoryNote.id }
         ) {
             Row(
                 horizontalArrangement = Arrangement.SpaceBetween,
                 verticalAlignment = Alignment.CenterVertically,
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(70.dp)
             ) {
                 Column(
                     modifier = Modifier.weight(0.7f)
                 ) {
                     icon()
                     LargeEmphasis(personName)
                 }
                 LargeBody(
                     amount,
                     modifier = Modifier.weight(0.3f),
                     textAlign = TextAlign.End
                 )
             }
             DropdownMenu(
                 expanded = menuIdExpanded == promissoryNote.promissoryNote.id,
                 onDismissRequest = { menuIdExpanded = null }
             ) {
                 DropdownMenuItem(
                     text = { Text(text = stringResource(id = R.string.Editar)) },
                     onClick = {
                         menuIdExpanded = null
                         editPromissoryNote(promissoryNote)
                     }
                 )
                 DropdownMenuItem(
                     text = { Text(text = stringResource(id = R.string.Eliminar)) },
                     onClick = {
                         menuIdExpanded = null
                         delPromissoryNote(promissoryNote)
                     }
                 )
             }
         }
     } else {
         PulsatingCard(
             modifier = Modifier
                 .fillMaxWidth()
                 .height(70.dp)
                 .clip(shape = MaterialTheme.shapes.small),
             color = MaterialTheme.colorScheme.scrim,
             minAlpha = 0.0f,
             maxAlpha = 0.2f
         )
     }
}

@Preview
@Composable
private fun LoadingPromissoryNoteListViewPreview(){
    GazegeTheme {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .systemBarsPadding()
        ){ LoadingPromissoryNoteListView() }
    }
}

@Preview
@Composable
private fun LoadedPromissoryNoteListViewPreview(){
    GazegeTheme {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .systemBarsPadding()
        ){
            DatabaseSample {
                LoadedPromissoryNoteListView(
                    promissoryNotes = PromissoryNoteViewModel.from(
                        promissoryNoteSample,
                        personSample,
                        principalPersonId = principalPersonSample?.id
                    ),
                    editPromissoryNote = {},
                    delPromissoryNote = {}
                )
            }
        }
    }
}