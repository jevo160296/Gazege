package com.jmml.gazege.ui.views.promissorynote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableOpenTarget
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import com.jmml.gazege.core.entities.PromissoryNote
import com.jmml.gazege.ui.DatabaseSample
import com.jmml.gazege.ui.DateFormat
import com.jmml.gazege.ui.localDateToString
import com.jmml.gazege.ui.templates.GroupedLazyList
import com.jmml.gazege.ui.theme.GazegeTheme
import com.jmml.gazege.ui.widgets.DefaultGroupViewHolder
import com.jmml.gazege.ui.widgets.LargeBody
import com.jmml.gazege.ui.widgets.LargeEmphasis
import com.jmml.gazege.ui.widgets.PulsatingCard
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
) { DefaultPromissoryNoteViewHolder(null) }

@Composable
fun LoadedPromissoryNoteListView(
    promissoryNotes: List<PromissoryNoteViewModel>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: LazyListState = rememberLazyListState(),
    dateViewHolder: @Composable (String) -> Unit = { DefaultGroupViewHolder(it) },
    promissoryNoteViewHolder: @Composable (PromissoryNoteViewModel) -> Unit = { DefaultPromissoryNoteViewHolder(it) }
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
fun DefaultPromissoryNoteViewHolder(promissoryNote: PromissoryNoteViewModel?){
     if(promissoryNote != null){
         val sourceId = promissoryNote.promissoryNote.sourceId
         val destinationId = promissoryNote.promissoryNote.destinationId
         val principalPerson = promissoryNote.principalPersonId
         val amount = promissoryNote.promissoryNote.amount.toMoneyString()
         val isIn = when{
             sourceId == principalPerson -> true
             destinationId == principalPerson -> false
             else -> null
         }
         val color = when(isIn){
             true -> GazegeTheme.gazegeColorScheme.income
             false -> GazegeTheme.gazegeColorScheme.outcome
             else -> MaterialTheme.colorScheme.onBackground
         }
         val personName = when (isIn) {
             true -> promissoryNote.destinationPerson.name
             false -> promissoryNote.sourcePerson.name
             else -> "${promissoryNote.sourcePerson.name} --> ${promissoryNote.destinationPerson.name}"
         }
         Row(
             horizontalArrangement = Arrangement.SpaceBetween,
             verticalAlignment = Alignment.CenterVertically,
             modifier = Modifier
                 .fillMaxWidth()
                 .height(70.dp)
         ) {
             LargeEmphasis(personName, modifier = Modifier.weight(0.7f))
             LargeBody(amount,
                 modifier = Modifier.weight(0.3f),
                 textAlign = TextAlign.End,
                 color = color
             )
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
                LoadedPromissoryNoteListView(promissoryNotes = PromissoryNoteViewModel.from(
                    promissoryNoteSample,
                    personSample,
                    principalPersonId = principalPersonSample?.id
                ))
            }
        }
    }
}