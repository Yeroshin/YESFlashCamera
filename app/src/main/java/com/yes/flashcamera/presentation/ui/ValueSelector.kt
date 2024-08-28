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



    var rowWidthPx by remember { mutableStateOf(0) }
    var center by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val itemWidthPx = with(LocalDensity.current) { (50.dp + 0.dp).toPx() }
    val flingBehavior=rememberSnapFlingBehavior(lazyListState = listState)

   /* val middleItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val viewportCenter = layoutInfo.viewportEndOffset / 2

            visibleItems.minByOrNull {
                abs(it.offset + it.size  - viewportCenter)
            }?.index
        }
    }*/
   fun calculateTargetIndex(
       firstVisibleItemIndex: Int,
       firstVisibleItemScrollOffset: Int,
       itemWidthPx: Float,
       itemCount: Int // Pass the total number of items in the list
   ): Int {
     //  var targetIndex=
       // Calculate the total scroll offset in pixels
   /*    val totalScrollOffset = firstVisibleItemIndex * itemWidthPx + firstVisibleItemScrollOffset
       // Calculate the index based on the scroll offset
       var targetIndex = (totalScrollOffset / itemWidthPx).toInt()

       // Determine the fraction of the item that is visible
       val visibleItemFraction = totalScrollOffset % itemWidthPx
       // If more than half of the item is shown, snap to the next item
       if (visibleItemFraction > itemWidthPx / 2) {
           targetIndex++
       }

       // Special case: when the user has scrolled to the end, snap to the last item
       if (targetIndex >= itemCount - 1) {
           targetIndex = itemCount - 1
       }*/

       return 0
   }
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
  /*  val firstVisibleItem by rememberUpdatedState {
        listState.firstVisibleItemIndex
       /* derivedStateOf {
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
            listState.firstVisibleItemIndex
        }*/
    }*/
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
        contentPadding = PaddingValues(horizontal =density.run { rowWidthPx.toDp()/2 } ),
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

   /* LaunchedEffect(middleItemIndex) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    // Calculate the last item index
                    val lastItemIndex = items.size - 1
                    // Get the layout info of the LazyList
                    val layoutInfo = listState.layoutInfo
                    // Determine if the last item is visible
                    val isLastItemVisible =
                        layoutInfo.visibleItemsInfo.any { it.index == lastItemIndex }
                  //  if (!isLastItemVisible) {
                        // Calculate the target index for snapping when scrolling stops
                        val targetIndex = calculateTargetIndex(
                            listState.firstVisibleItemIndex,
                            listState.firstVisibleItemScrollOffset,
                            itemWidthPx,
                            items.size
                        )
                        // Animate scrolling to the target index to achieve snapping effect
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = targetIndex)
                        }
                  //  }
                }
            }
       /* snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                coroutineScope.launch {
                    listState.animateScrollToItem(index)
                }
            }*/
      /*  snapshotFlow { middleItemIndex}
            .collect { index ->
                index?.let {
                    coroutineScope.launch {

                        listState.animateScrollToItem(index)
                    }
                }

            }*/


       /* snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                val viewportCenter = layoutInfo.viewportEndOffset / 2
                val closestItem = visibleItems.minByOrNull {
                    kotlin.math.abs(it.offset + it.size / 2 - viewportCenter)
                }
                closestItem?.let {
                    coroutineScope.launch {
                        listState.animateScrollToItem(it.index)
                    }
                }
            }*/
      /*  with(listState.layoutInfo) {
            val itemSize = visibleItemsInfo.first().size
            val itemScrollOffset = viewportEndOffset - itemSize
            listState.scrollToItem(5, -50)
            listState.animateScrollToItem(index = 0, scrollOffset = -50)
        }*/
        // Прокрутка к первому элементу и его центрирование при инициализации
      // listState.scrollToItem(index = 0, scrollOffset = -rowWidth / 2)
    }*/
   /* Button(onClick = {
        coroutineScope.launch {
            listState.animateScrollToItem(index = items.size - 1, scrollOffset = -rowWidth / 2)
        }
    }) {
        Text("Scroll to End")
    }*/


}