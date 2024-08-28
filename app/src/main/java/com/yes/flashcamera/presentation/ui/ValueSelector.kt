package com.yes.flashcamera.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yes.flashcamera.R
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector (
    items: List<Any>,
    adapter: CompositeAdapter,
    onSelectedItemChanged: (Int) -> Unit
) {

    var rowWidthPx by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    val flingBehavior=rememberSnapFlingBehavior(lazyListState = listState)

    val middleItemIndex by remember {
        derivedStateOf {
            val lastItemIndex = items.size - 1
            // Get the layout info of the LazyList
            val layoutInfo = listState.layoutInfo
            // Determine if the last item is visible
            val isLastItemVisible =
                layoutInfo.visibleItemsInfo.any { it.index == lastItemIndex }
            //  if (!isLastItemVisible) {
            // Calculate the target index for snapping when scrolling stops
            /* calculateTargetIndex(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                itemWidthPx,
                items.size
            )*/
            val t =layoutInfo.visibleItemsInfo
            var index=0
             layoutInfo.visibleItemsInfo.find {
               it.offset+(rowWidthPx / 2) < ((rowWidthPx / 2)  ) && it.offset+(rowWidthPx / 2)+ it.size  > ((rowWidthPx / 2) )
           }?.index
                 //  index=it.index


          //  index
          //  listState.firstVisibleItemScrollOffset
        }
    }
    val firstVisibleItem by rememberUpdatedState(listState.firstVisibleItemIndex)

    LaunchedEffect(firstVisibleItem) {
        snapshotFlow { firstVisibleItem}
            .distinctUntilChanged()
            .collect { newIndex ->
                onSelectedItemChanged(newIndex)
            }
    }

    Text(text = "Middle item index: $middleItemIndex")
    Text(text = "Middle item index: $firstVisibleItem")
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                rowWidthPx =  coordinates.size.width
            },
        contentPadding = PaddingValues(horizontal =LocalDensity.current.run { rowWidthPx.toDp()/2 } ),
     //   horizontalArrangement = Arrangement.spacedBy(30.dp)
        flingBehavior = flingBehavior
    ) {
        items(items.size) { index ->

            adapter.Content(items[index])
        }
    }
    Icon(
        modifier = Modifier
            .height(70.dp)
            .width(70.dp),
        painter = painterResource(R.drawable.arrow_drop_up),
        contentDescription = "icon"
    )
}