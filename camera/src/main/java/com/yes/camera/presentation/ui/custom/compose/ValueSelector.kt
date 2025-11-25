package com.yes.camera.presentation.ui.custom.compose

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
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
import com.yes.camera.presentation.model.IconItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.TextItem
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.IconSelectorItemUI
import com.yes.camera.presentation.ui.adapter.TextSelectorItemUI
import com.yes.camera.presentation.ui.views.ImmutableCollection
import com.yes.camera.presentation.ui.views.MapImmutableCollection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.lang.reflect.TypeVariable
import kotlin.math.truncate

/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    modifier: Modifier,
    position: Int,
  // items: List<SelectorItem>?,
    items:ImmutableCollection<SelectorItem>?,
    //  adapter: CompositeAdapter,ut
    onSelectedItemChanged: (index: Int, manual: Boolean) -> Unit,
    updatedPosition: Int? = null,
    onPositionUpdated: () -> Unit = {}
) {
    /* val items by remember (items){
         mutableStateOf(items)
     }*/
   /* val itemsR by remember(items) {
        mutableStateOf(ImmutableCollection(list = items ?: emptyList()))
    }*/


    val adapter = remember {

            CompositeAdapter(
                MapImmutableCollection(
                    mapOf<Class<*>, CompositeAdapter.AdapterDelegate<*>>(
                        TextItem::class.java to TextSelectorItemUI(),
                        IconItem::class.java to IconSelectorItemUI()
                    )
                ).map
            )

    }
    /*  val items by remember {
          mutableStateOf(
              ImmutableCollection(
                  listOf( TextItem(10f, "hel",false))
              )
          )
      }*/
    var rowWidthPx by remember { mutableIntStateOf(0) }
    var itemWidthPx by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val firstVisibleItem by rememberUpdatedState(listState.firstVisibleItemIndex)
    var isProgrammaticScroll by remember { mutableStateOf(false) }
    var curIndex by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(updatedPosition) {
       /* snapshotFlow { updatedPosition }
            .collect { position ->
                position?.let {
                    curIndex=position
                    isProgrammaticScroll = true
                    listState.animateScrollToItem(
                        position,
                        scrollOffset = itemWidthPx / 2
                    )
                    onSelectedItemChanged(position, false)
                    isProgrammaticScroll = false
                    onPositionUpdated()
                }

            }*/
        updatedPosition?.let { newPos ->
            if (newPos != listState.firstVisibleItemIndex) {
                listState.scrollToItem(
                    index = newPos,
                    scrollOffset = itemWidthPx / 2
                )
                onSelectedItemChanged(newPos, false)
                onPositionUpdated() // Уведомляем родителя о завершении
            }
        }
    }
    val itemsHashKey = remember(items) {
        items?.list?.joinToString {
            when (it) {
                is IconItem -> "icon:${it.icon}:${it.passed}"
                is TextItem -> "text:${it.text}:${it.passed}"
                else -> it.hashCode().toString()
            }
        }?.hashCode()
    }
   LaunchedEffect(itemsHashKey, itemWidthPx ) {
/*snapshotFlow { itemsHashKey }
            .distinctUntilChanged()
            .collect {*/
                isProgrammaticScroll = true
               // delay(10)
                listState.scrollToItem(
                    position,
                    scrollOffset = itemWidthPx / 2
                )
                curIndex=position
                onSelectedItemChanged(position, false)
                delay(100)
                isProgrammaticScroll = false
           // }
    }

   LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged() // Только при реальном изменении
            .collect { index ->
                curIndex=index
                if (!isProgrammaticScroll) {
                    onSelectedItemChanged(index, true)
                  /*  items?.let {
                         /* it.forEachIndexed { curIndex, item ->
                               item.passed = curIndex <= index

                           } */ for (i in it.indices) {
                          //  it[i].passed = i <= index
                        }
                    }*/
                }
            }
    }
   /* var itemsR: ImmutableCollection<SelectorItem>? by remember(items) {
        mutableStateOf(
            items?.let {
                ImmutableCollection(
                    it
                )
            }



        )
    }*/
  /*  LaunchedEffect(items) {
        snapshotFlow { items }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                itemsR = ImmutableCollection(
                    newValue
                )
            }
    }*/

    Box(
        modifier = modifier
            .fillMaxWidth()

            .padding(4.dp),
    ) {

        LazyRow(
            state = listState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentHeight()
                .onGloballyPositioned { coordinates ->
                    /* items?.list?.forEachIndexed{index,item->
                      /*  if (curIndex >= index){
                            item.passed=true
                        }*/
                    }*/
                    rowWidthPx = coordinates.size.width
                },
            contentPadding = PaddingValues(
                horizontal = pixelsToDp(rowWidthPx / 2)
                // horizontal = LocalDensity.current.run { rowWidthPx.toDp() / 2 }
            ),
            flingBehavior = flingBehavior
        ) {
            val modifier = Modifier
                .width(48.dp)
                .onGloballyPositioned { coordinates ->
                    itemWidthPx = coordinates.size.width
                }

            items?.let {
                items(it.list.size) { index ->
                    it.list.forEachIndexed{index,item->
                        if (curIndex >= index){
                            item.passed=true
                        }else{
                            item.passed=false
                        }
                    }
                   /* if (curIndex >= index){
                        it[index].passed=true
                    }*/
                    adapter.Content(it.list[index], modifier)
                }
            }

        }
        VectorShadow(
            Modifier
                .align(Alignment.BottomCenter)
                .size(14.dp),
            vectorColor = Color.Green,
            shadowColor = Color.DarkGray,
            resId = R.drawable.arrow_drop_up
        )
    }

}*/

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    modifier: Modifier,
    position: Int,
    items: ImmutableCollection<SelectorItem>?,
    onSelectedItemChanged: (index: Int, manual: Boolean) -> Unit,
    updatedPosition: Int? = null,
    onPositionUpdated: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Адapter с делегатами
    val adapter = remember {
        CompositeAdapter(
            MapImmutableCollection(
                mapOf<Class<*>, CompositeAdapter.AdapterDelegate<*>>(
                    TextItem::class.java to TextSelectorItemUI(),
                    IconItem::class.java to IconSelectorItemUI()
                )
            ).map
        )
    }

    // Кэшируем ширину списка и элемента
    var rowWidthPx by remember { mutableIntStateOf(0) }
    var itemWidthPx by remember { mutableIntStateOf(0) }

    // Текущее выбранное значение
    var curIndex by remember { mutableIntStateOf(position) }

    // Управление автоматической прокруткой
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Обновляем выбранную позицию при обновлении извне
    LaunchedEffect(updatedPosition) {
        updatedPosition?.let { newPos ->
            if (newPos != listState.firstVisibleItemIndex) {
                isProgrammaticScroll = true
                listState.animateScrollToItem(newPos, scrollOffset = itemWidthPx / 2)
                curIndex = newPos
                onSelectedItemChanged(newPos, false)
                onPositionUpdated()
                isProgrammaticScroll = false
            }
        }
    }

    // Обработка изменения позиции через состояние ленты
    LaunchedEffect(listState.firstVisibleItemIndex) {
        if (!isProgrammaticScroll) {
            val index = listState.firstVisibleItemIndex
            if (index != curIndex) {
                curIndex = index
                onSelectedItemChanged(index, true)
            }
        }
    }

    // Обработка изменений массива элементов
    val itemsHashKey = remember(items) {
        items?.list?.joinToString { item ->
            when (item) {
                is IconItem -> "icon:${item.icon}:${item.passed}"
                is TextItem -> "text:${item.text}:${item.passed}"
                else -> item.hashCode().toString()
            }
        }?.hashCode()
    }

    // Плавная прокрутка при изменении элементов
    LaunchedEffect(itemsHashKey, itemWidthPx) {
        items?.list?.let { itemList ->
            // Ищем текущий выбранный индекс
            if (curIndex !in itemList.indices) {
                curIndex = 0
            } else if (curIndex != listState.firstVisibleItemIndex) {
                isProgrammaticScroll = true
                listState.animateScrollToItem(curIndex, scrollOffset = itemWidthPx / 2)
                isProgrammaticScroll = false
            }
        }
    }

    // Передача данных об passing состояниях
    val preparedItems = items?.list?.mapIndexed { index, item ->
        val passed = index <= curIndex
        when (item) {
            is TextItem -> item.copy(passed = passed)
            is IconItem -> item.copy(passed = passed)
            else -> item
        }
    } ?: emptyList()

    // Основная UI
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentHeight()
                .onGloballyPositioned { coordinates ->
                    rowWidthPx = coordinates.size.width
                },
            contentPadding = PaddingValues(horizontal = pixelsToDp(rowWidthPx / 2)),
            flingBehavior = flingBehavior
        ) {
            // Устанавливаем ширину каждого элемента
            val itemModifier = Modifier
                .width(48.dp)
                .onGloballyPositioned { coordinates ->
                    itemWidthPx = coordinates.size.width
                }

            // Отрисовка элементов
            items(preparedItems.size) { index ->
                val item = preparedItems[index]
                adapter.Content(item, itemModifier)
            }
        }

        // Вставка стрелки или другого индикатора (по необходимости)
        VectorShadow(
            Modifier
                .align(Alignment.BottomCenter)
                .size(14.dp),
            vectorColor = Color.Green,
            shadowColor = Color.DarkGray,
            resId = R.drawable.arrow_drop_up
        )
    }
}


@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }