package com.yes.camera.presentation.ui.custom.compose

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yes.camera.R
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    position: Int,
    items: List<Any>?,
    adapter: CompositeAdapter,
    onSelectedItemChanged: (Int) -> Unit
) {

    var rowWidthPx by remember { mutableIntStateOf(0) }
    var itemWidthPx by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val firstVisibleItem by rememberUpdatedState(listState.firstVisibleItemIndex)


   /* LaunchedEffect(firstVisibleItem) {
        snapshotFlow { firstVisibleItem }
            .distinctUntilChanged()
            .collect { newIndex ->
                onSelectedItemChanged(newIndex)
            }

    }*/
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(items) {
        snapshotFlow { items }
            .collect {
             //   coroutineScope.launch {
                    listState.animateScrollToItem(
                        position,
                       scrollOffset = itemWidthPx/2
                    )
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { index ->
                            onSelectedItemChanged(index)
                        }

            //    }
            }
    }
  /*  LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                onSelectedItemChanged(index)
            }
    }*/
    Column(
        modifier = Modifier
         /*   .background(
                Color.LightGray.copy(alpha = 0.5f)
            )*/
            // .height(80.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        LazyRow(
            state = listState,
            modifier = Modifier
                .wrapContentHeight()
                .onGloballyPositioned { coordinates ->
                    rowWidthPx = coordinates.size.width
                },
            contentPadding = PaddingValues(
                horizontal = pixelsToDp(rowWidthPx/2)
               // horizontal = LocalDensity.current.run { rowWidthPx.toDp() / 2 }
            ),
            flingBehavior = flingBehavior
        ) {
            val modifier=Modifier
                .width(48.dp)
                .onGloballyPositioned { coordinates ->
                    itemWidthPx = coordinates.size.width
                }
            items?.let {
                items(it.size) { index ->
                    adapter.Content(items[index],modifier)
                }
            }

        }
        VectorShadow(
            Modifier
                .size(24.dp),
            vectorColor = Color.Green,
            shadowColor = Color.DarkGray,
            resId = R.drawable.arrow_drop_up
        )
    }

}
@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }