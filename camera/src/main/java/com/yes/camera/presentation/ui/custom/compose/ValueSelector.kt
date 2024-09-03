package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.yes.camera.R
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import kotlinx.coroutines.flow.distinctUntilChanged


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    items: List<Any>,
    adapter: CompositeAdapter,
    onSelectedItemChanged: (Int) -> Unit
) {

    var rowWidthPx by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val firstVisibleItem by rememberUpdatedState(listState.firstVisibleItemIndex)

    LaunchedEffect(firstVisibleItem) {
        snapshotFlow { firstVisibleItem }
            .distinctUntilChanged()
            .collect { newIndex ->
                onSelectedItemChanged(newIndex)
            }
    }
    Column(
        modifier = Modifier
            .background(
                Color.LightGray
            )
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
                horizontal = LocalDensity.current.run { rowWidthPx.toDp() / 2 }
            ),
            flingBehavior = flingBehavior
        ) {

            items(items.size) { index ->
                adapter.go(items[index])
            }
        }
        VectorShadow(
            Modifier
                .size(24.dp),
            vectorColor = Color.Green,
            shadowColor = Color.DarkGray,
            resId = R.drawable.arrow_drop_up
        )
        /*  Image(
              modifier = Modifier
                  .size(24.dp),

              painter = painterResource(R.drawable.arrow_drop_up),
              contentDescription = "icon",
               colorFilter = ColorFilter.tint(color = Color.Green)
          )*/

    }

}