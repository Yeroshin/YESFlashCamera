package com.yes.flashcamera.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ValueSelector (items: List<Any>, adapter: CompositeAdapter) {
    var rowWidth by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rowWidth = coordinates.size.width
            },
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                  .fillMaxWidth(),

               /* .onGloballyPositioned { coordinates ->
                    rowWidth = coordinates.size.width
                },*/
            contentPadding = PaddingValues(horizontal =(rowWidth/40).dp),
            // horizontalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            items(items.size) { index ->

                adapter.Content(items[index])
            }
        }
        LaunchedEffect(rowWidth) {
            /*  with(listState.layoutInfo) {
            val itemSize = visibleItemsInfo.first().size
            val itemScrollOffset = viewportEndOffset - itemSize
            listState.scrollToItem(5, -50)
            listState.animateScrollToItem(index = 0, scrollOffset = -50)
        }*/
            // Прокрутка к первому элементу и его центрирование при инициализации
            // listState.scrollToItem(index = 0, scrollOffset = -rowWidth / 2)
        }
       /* Button(onClick = {
            coroutineScope.launch {
                listState.animateScrollToItem(index = items.size - 1, scrollOffset = -rowWidth / 2)
            }
        }) {
            Text("Scroll to End")
        }*/
    }

}