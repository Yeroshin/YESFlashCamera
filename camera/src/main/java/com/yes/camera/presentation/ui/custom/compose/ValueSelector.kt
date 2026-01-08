package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.yes.camera.R
import com.yes.camera.presentation.model.IconItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.TextItem
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.IconSelectorItemUI
import com.yes.camera.presentation.ui.adapter.TextSelectorItemUI





/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    modifier: Modifier,
    position: Int,
    items: ImmutableCollection< SelectorItem>?,
    onSelectedItemChanged: (index: Int, manual: Boolean) -> Unit,
  //  updatedPosition: Int? = null,
 //   onPositionUpdated: () -> Unit = {}
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
    LaunchedEffect(position) {
        position.let { newPos ->
            if (newPos != listState.firstVisibleItemIndex) {
                isProgrammaticScroll = true
                listState.animateScrollToItem(newPos, scrollOffset = itemWidthPx / 2)
                curIndex = newPos
                onSelectedItemChanged(newPos, false)
              //  onPositionUpdated()
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
*/
//////////////////////////
///////////////////////////
/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    modifier: Modifier,
    position: Int,
    itemsInit: ImmutableCollection<SelectorItem>?,
    onSelectedItemChanged: (index: Int, manual: Boolean) -> Unit,
    //  updatedPosition: Int? = null,
    //   onPositionUpdated: () -> Unit = {}
) {
    var items by remember(itemsInit) {
        mutableStateOf(itemsInit)
    }
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Adapter с делегатами
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
    LaunchedEffect(position) {
        if (position != listState.firstVisibleItemIndex && position >= 0) {
            isProgrammaticScroll = true
            try {
                listState.animateScrollToItem(position, scrollOffset = itemWidthPx / 2)
                curIndex = position
                onSelectedItemChanged(position, false)
                //  onPositionUpdated()
            } finally {
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
            val validIndex = if (curIndex in itemList.indices) curIndex else 0
            if (validIndex != listState.firstVisibleItemIndex && itemWidthPx > 0) {
                isProgrammaticScroll = true
                try {
                    listState.animateScrollToItem(validIndex, scrollOffset = itemWidthPx / 2)
                } finally {
                    isProgrammaticScroll = false
                }
            }
        }
    }

    // Передача данных об passing состояниях
    val preparedItems = remember(curIndex, items) {
        items?.list?.mapIndexed { index, item ->
            val passed = index <= curIndex
            when (item) {
                is TextItem -> item.copy(passed = passed)
                is IconItem -> item.copy(passed = passed)
                else -> item
            }
        } ?: emptyList()
    }

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
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }*/


/////////////////////////////
/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    modifier: Modifier,
    position: Int,
    itemsInit: List<SelectorItem>?, // Используем стандартный List для простоты примера
    onSelectedItemChanged: (index: Int, manual: Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val adapter = remember {
        // Предполагается ваша реализация CompositeAdapter
        mapOf(
            TextItem::class.java to TextSelectorItemUI(),
            IconItem::class.java to IconSelectorItemUI() // Должен быть обновлен аналогично
        )
    }

    var rowWidthPx by remember { mutableIntStateOf(0) }
    var itemWidthPx by remember { mutableIntStateOf(0) }
    var curIndex by remember { mutableIntStateOf(position) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Синхронизация внешней позиции
    LaunchedEffect(position) {
        if (position != listState.firstVisibleItemIndex && position >= 0) {
            isProgrammaticScroll = true
            try {
                listState.animateScrollToItem(position, scrollOffset = itemWidthPx / 2)
                curIndex = position
            } finally {
                isProgrammaticScroll = false
            }
        }
    }

    // Синхронизация при скролле пользователем
    // Используем snapshotFlow для более стабильного отслеживания в 2026 году
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            if (!isProgrammaticScroll && index != curIndex) {
                curIndex = index
                onSelectedItemChanged(index, true)
            }
        }
    }

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
                .onGloballyPositioned { rowWidthPx = it.size.width },
            contentPadding = PaddingValues(horizontal = pixelsToDp(rowWidthPx / 2)),
            flingBehavior = flingBehavior
        ) {
            val itemModifier = Modifier
                .width(48.dp)
                .onGloballyPositioned { itemWidthPx = it.size.width }

            items(
                count = itemsInit?.size ?: 0,
                key = { index ->
                    // Ключ крайне важен для предотвращения рекомпозиций всего списка
                    when (val it = itemsInit!![index]) {
                        is TextItem -> it.id
                        is IconItem -> it.id
                        else -> index
                    }
                }
            ) { index ->
                val item = itemsInit!![index]
                val isPassed = index <= curIndex

                // Вызываем напрямую из мапы адаптеров
                val delegate = adapter[item::class.java]
                @Suppress("UNCHECKED_CAST")
                (delegate as? CompositeAdapter.AdapterDelegate<SelectorItem>)?.Content(
                    item = item,
                    isPassed = isPassed,
                    modifier = itemModifier
                )
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
}

@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }

*/
//////////////////
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueSelector(
    modifier: Modifier,
    position: Int,
    items: List<SelectorItem>?,
    onSelectedItemChanged: (index: Int, manual: Boolean) -> Unit
) {
    if (items.isNullOrEmpty()) return

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    var rowWidthPx by remember { mutableIntStateOf(0) }
    var itemWidthPx by remember { mutableIntStateOf(0) }
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    val textDelegate = remember { TextSelectorItemUI() }
    val iconDelegate = remember { IconSelectorItemUI() }

    // Синхронизация скролла при изменении внешней позиции или списка
    LaunchedEffect(position, items) {
        if (position in items.indices && position != listState.firstVisibleItemIndex) {
            isProgrammaticScroll = true
            try {
                listState.animateScrollToItem(position, scrollOffset = itemWidthPx / 2)
            } finally {
                isProgrammaticScroll = false
            }
        }
    }

    // Отслеживание ручного выбора
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            if (!isProgrammaticScroll && index in items.indices) {
                onSelectedItemChanged(index, true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { rowWidthPx = it.size.width }
    ) {
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(
                horizontal = with(LocalDensity.current) { (rowWidthPx / 2).toDp() }
            )
        ) {
            items(
                count = items.size,
                key = { index -> items[index].id }
            ) { index ->
                val item = items[index]
                val itemModifier = Modifier
                    .width(48.dp)
                    .onGloballyPositioned { itemWidthPx = it.size.width }

                val isPassed = index <= position

                when (item) {
                    is TextItem -> textDelegate.Content(item, isPassed, itemModifier)
                    is IconItem -> iconDelegate.Content(item, isPassed, itemModifier)
                }
            }
        }
    }
}
