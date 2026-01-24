package com.yes.camera.presentation.ui.views

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.presentation.ui.adapter.TextSelectorContent
import com.yes.camera.presentation.ui.custom.compose.Histogram
import com.yes.camera.presentation.ui.custom.compose.RadioUiItem
import com.yes.camera.presentation.ui.custom.compose.RecordButton
import com.yes.camera.presentation.ui.custom.compose.SelectorUiItem
import com.yes.camera.presentation.ui.custom.compose.ShutterBox
import com.yes.camera.presentation.ui.custom.compose.TextRadioContent
import com.yes.camera.presentation.ui.custom.compose.UniversalRadioGroup
import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.compose.VectorShadow

import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun CameraScreenSuccess(
    context: Context,
    renderer: GLRenderer,
    characteristicsInit: CharacteristicsUI,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onSetCharacteristic: (characteristics: CharacteristicsUI) -> Unit,
) {
    var characteristics by remember() {
        mutableStateOf(characteristicsInit)
    }
    LaunchedEffect(characteristicsInit) {
        // 1. Извлекаем наше локальное "накрученное" значение лупы
        val myMagnifierValue = (characteristics.characteristicsItems
            .find { it.id == SettingsItem.MAGNIFIER } as? RadioGroupItem.TextItem)?.currentValue

        // 2. Применяем ВСЕ новые данные из ViewModel (ISO, Shutter, списки элементов)
        // Но в списке иконок/текста (characteristicsItems) сохраняем нашу лупу
        characteristics = characteristicsInit.copy(
            characteristicsItems = characteristicsInit.characteristicsItems.map { item ->
                if (item.id == SettingsItem.MAGNIFIER && item is RadioGroupItem.TextItem && myMagnifierValue != null) {
                    // Возвращаем новую структуру из ViewModel, но со старым текстом значения
                    item.copy(currentValue = myMagnifierValue)
                } else {
                    // Все остальные пункты (ISO, Shutter и т.д.) заменяются на новые из ViewModel
                    item
                }
            }
        )
    }
    var characteristicsRrequest by remember {
        mutableStateOf(characteristicsInit)
    }

    var surfaceViewSize by remember { mutableStateOf(IntSize.Zero) }
    LaunchedEffect(surfaceViewSize) {
        // Ждем, когда размеры станут известны (не 0)
        snapshotFlow { surfaceViewSize }
            .filter { it.width > 0 && it.height > 0 }
            .first() // Берем только самое первое валидное значение
            .let { size ->
                val normalizedX =
                    (size.width.toFloat() / 2f / size.width.toFloat()) * 2f - 1f
                val normalizedY =
                    -((size.height.toFloat() / 2f / size.height.toFloat()) * 2f - 1f)
                renderer.handleTouchPress(normalizedX, normalizedY)
                renderer.configureMagnifier(1f)
            }
    }
    var touchPoint by remember {
        mutableStateOf(FloatArray(0))
    }
    var shutterBoxIsOpen by remember { mutableStateOf(true) }
    var paramsRadioGroupSelectedItem by remember( /*characteristics.characteristicsItems*/) {
        mutableStateOf(
            characteristics.characteristicsItems.firstOrNull()?.id
        )
    }
    LaunchedEffect(characteristics.characteristicsItems) {
        val items = characteristics.characteristicsItems
        if (items.isNotEmpty() && items.none { it.id == paramsRadioGroupSelectedItem }) {
            paramsRadioGroupSelectedItem = items.first().id
        }
    }
    val paramsRadioGroupItems = remember(characteristics.characteristicsItems) {
        characteristics.characteristicsItems.map { data ->
            RadioUiItem(id = data.id) { isSelected ->
                when (data) {
                    is RadioGroupItem.IconItem -> {
                        Icon(
                            painter = painterResource(data.iconRes),
                            contentDescription = null,
                            tint = if (isSelected) Color.Yellow else Color.White
                        )
                    }

                    is RadioGroupItem.TextItem -> {
                        TextRadioContent(
                            title = data.title,
                            value = data.currentValue,
                            selected = isSelected
                        )
                    }
                }


            }
        }
    }

    var autoMode by remember {
        mutableStateOf(false)
    }
    var isSelectorVisible by remember {
        mutableStateOf(true)
    }
    var selectorPosition:Int by remember {
        mutableIntStateOf(0)
    }
    var selectorItems: List<SelectorUiItem>? by remember {
        mutableStateOf(
            null
        )
    }
    var magnifierPosition by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(selectorPosition) {
        if (characteristics.characteristicsItems.isNotEmpty()) {

            val updatedCharacteristics = when(paramsRadioGroupSelectedItem){
                SettingsItem.SHUTTER->{
                    characteristics.copy(
                        shutterValue = characteristics.shutterItems[selectorPosition].value
                    )
                }
                SettingsItem.ISO->{
                    characteristics.copy(
                        isoValue = characteristics.isoItems[selectorPosition].value
                    )
                }
                SettingsItem.WB->{
                    characteristics.copy(
                        wbValue = characteristics.wbItems[selectorPosition].value
                    )
                }
                SettingsItem.FOCUS->{
                    characteristics.copy(
                        focusValue = characteristics.focusItems[selectorPosition].value
                    )
                }
                SettingsItem.MAGNIFIER->{
                    magnifierPosition = characteristics.magnifierItems.indexOf(
                        characteristics.magnifierItems[selectorPosition]

                    )
                    val index = characteristics.characteristicsItems.indexOfFirst { it.id == SettingsItem.MAGNIFIER }

                    if (index != -1) {
                        val oldItem = characteristics.characteristicsItems[index]

                        if (oldItem is RadioGroupItem.TextItem) {
                            // 2. Создаем обновленный объект
                            val updatedItem = oldItem.copy(
                                currentValue = characteristics.magnifierItems[magnifierPosition].value
                            )

                            // 3. Создаем новый список, заменяя элемент по индексу
                            val updatedItems = characteristics.characteristicsItems.toMutableList().apply {
                                this[index] = updatedItem
                            }

                            // 4. Обновляем состояние целиком (иммутабельно)
                            characteristics = characteristics.copy(characteristicsItems = updatedItems)
                            renderer.configureMagnifier(
                                updatedItem.currentValue.toFloat()
                            )

                        }else{}

                    }
                    null
                }
                else -> characteristics.copy()
            }

            updatedCharacteristics?.let { onSetCharacteristic(it) }
        }


    }
   /* var selectorSelectedItemIndex by remember {
        mutableIntStateOf(0)
    }*/


    LaunchedEffect(paramsRadioGroupSelectedItem) {
        val (dataList, position) = when (paramsRadioGroupSelectedItem) {
            SettingsItem.SHUTTER -> characteristics.shutterItems to characteristics.shutterPosition
            SettingsItem.ISO -> characteristics.isoItems to characteristics.isoPosition
            SettingsItem.WB -> characteristics.wbItems to characteristics.wbPosition
            SettingsItem.FOCUS -> characteristics.focusItems to characteristics.focusPosition
            SettingsItem.MAGNIFIER -> characteristics.magnifierItems to magnifierPosition
            else -> null to 0
        }

        selectorItems = dataList?.map { data ->
            SelectorUiItem(id = data.id) { isSelected ->
                TextSelectorContent(data, isSelected)
            }
        }
        selectorPosition = position
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ///////////preview]
        Box() {
            AndroidView(
                modifier = Modifier
                    .padding(
                        top = if (characteristics.fullScreen) {
                            0.dp
                        } else {
                            84.dp
                        }
                    )
                    .onSizeChanged { size ->
                        surfaceViewSize = size
                        ///////////
                        //   surfaceViewSize = IntSize(width, height)
                        /*  val normalizedX =
                              (surfaceViewSize.width.toFloat() / 2f / surfaceViewSize.width.toFloat()) * 2f - 1f
                          val normalizedY =
                              -((surfaceViewSize.height.toFloat() / 2f / surfaceViewSize.height.toFloat()) * 2f - 1f)
                          renderer.handleTouchPress(normalizedX, normalizedY)*/
                        // renderer.configureMagnifier(1f)
                        /////////////
                    },
                factory = {
                    AutoFitSurfaceView(context, null).apply {
                        setEGLContextClientVersion(3)
                        setRenderer(renderer)
                        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                        /* viewTreeObserver.addOnGlobalLayoutListener {
                             surfaceViewSize = IntSize(width, height)
                             val normalizedX =
                                 (surfaceViewSize.width.toFloat() / 2f / surfaceViewSize.width.toFloat()) * 2f - 1f
                             val normalizedY =
                                 -((surfaceViewSize.height.toFloat() / 2f / surfaceViewSize.height.toFloat()) * 2f - 1f)
                             renderer.handleTouchPress(normalizedX, normalizedY)
                             renderer.configureMagnifier(1f)
                         }*/
                        setOnTouchListener { v, event ->
                            v.performClick()
                            val normalizedX = (event.x / v.width.toFloat()) * 2f - 1f
                            val normalizedY = -((event.y / v.height.toFloat()) * 2f - 1f)
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    renderer.handleTouchPress(normalizedX, normalizedY)
                                }

                                MotionEvent.ACTION_MOVE -> {
                                    renderer.handleTouchDrag(normalizedX, normalizedY)
                                }

                                MotionEvent.ACTION_UP -> {
                                    touchPoint = floatArrayOf(event.x / v.width, event.y / v.height)
                                }
                            }
                            true
                        }
                        renderer.glSurfaceView = this
                    }
                },
                update = { view ->
                    view.setFullscreen(characteristics.fullScreen)
                    view.setAspectRatio(
                        // 1280, 720
                        characteristics.aspectRatio?.width ?: 3,
                        characteristics.aspectRatio?.height ?: 2
                    )
                }
            )

            ShutterBox(
                isOpen = shutterBoxIsOpen,
                onToggle = { shutterBoxIsOpen = !shutterBoxIsOpen },
                modifier = Modifier.padding(
                    top = if (characteristics.fullScreen) 0.dp else 84.dp
                )
            ) {}
        }
        ////////////////radio group
        paramsRadioGroupSelectedItem?.let {
            UniversalRadioGroup(
                items = paramsRadioGroupItems,
                selectedId = it,
                onItemClick = { value ->
                    paramsRadioGroupSelectedItem = value
                },
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp
                    ),
            )
        }

        //////////resolution
        characteristics.resolution?.let {
            Text(
                modifier = Modifier
                    .padding(
                        top = 98.dp,
                        end = 18.dp
                    )
                    .align(Alignment.TopEnd),
                textAlign = TextAlign.End,
                text = it,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    shadow = Shadow(
                        color = Color.DarkGray,
                        offset = Offset(5.0f, 5.0f),
                        blurRadius = 5f
                    )
                )
            )
        }
        ////////////////////////histogram
        Histogram(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 98.dp
                )
                .align(Alignment.TopStart),
            characteristics.histogramData,
            150.dp,
            80.dp
        )
        //////////////////////////bottom panel
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //selector row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ///////////auto
                VectorShadow(
                    modifier = Modifier.size(32.dp),
                    vectorColor = if (autoMode) Color.Green else Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.auto,
                    onClick = {
                        autoMode = !autoMode
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .height(50.dp)
                ) {
                    if (isSelectorVisible) {
                        ValueSelector(
                            modifier = Modifier.height(42.dp),
                            position = selectorPosition,
                            items = selectorItems,
                            onSelectedItemChanged = { index ->

                                selectorPosition = index

                            }
                        )
                    } else {
                        /*  RadioGroup(
                              modifier = Modifier
                                  .padding(4.dp)
                                  .fillMaxWidth(),
                              items = selectorRadioGroupItems,
                              selectedOption = when (selectorRadioGroupItems?.firstOrNull()?.id) {
                                  is WbItem -> wbSelectorRadioGroupSelectedItem
                                  is FocusItem -> focusSelectorRadioGroupSelectedItem
                                  else -> null
                              },
                              onOptionSelected = { value ->
                                  selectorRadioGroupSelectedItem = value as IconRadioGroupItem
                              }
                          )*/
                    }

                }
            }
            //////////////////////////BOTTOM BUTTONS
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(56.dp)
            ) {
                ///settings
                VectorShadow(
                    Modifier
                        //  .padding(24.dp)
                        .size(32.dp)
                    /*.clickable {
                        onSettingsClick()
                    }*/,
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.settings,
                    onClick = onSettingsClick
                )

                //////////////////camera flip
                VectorShadow(
                    Modifier
                        // .padding(24.dp)
                        .size(32.dp),
                    /* .clickable {

                     }*/
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.flip_camera_android,
                )
                /////capture
                val startVideoRecord = remember {
                    onStartVideoRecord
                }
                RecordButton(
                    modifier = Modifier
                        .size(96.dp),
                    isChecked = false,
                    onClick = { isCheck ->
                        shutterBoxIsOpen = !shutterBoxIsOpen
                        startVideoRecord(isCheck)
                    }

                )
            }
        }


    }
}