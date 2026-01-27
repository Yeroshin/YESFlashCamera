package com.yes.camera.presentation.ui.views

import android.content.Context
import android.media.audiofx.EnvironmentalReverb.Settings
import android.os.StrictMode
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.ModeItem
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.presentation.ui.adapter.TextSelectorContent
import com.yes.camera.presentation.ui.custom.compose.CameraPreviewContainer
import com.yes.camera.presentation.ui.custom.compose.Histogram
import com.yes.camera.presentation.ui.custom.compose.IconRadioContent
import com.yes.camera.presentation.ui.custom.compose.RadioUiItem
import com.yes.camera.presentation.ui.custom.compose.RecordButton
import com.yes.camera.presentation.ui.custom.compose.SelectorUiItem
import com.yes.camera.presentation.ui.custom.compose.ShutterBox
import com.yes.camera.presentation.ui.custom.compose.TextRadioContent
import com.yes.camera.presentation.ui.custom.compose.UniversalRadioGroup
import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.compose.VectorShadow

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
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
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
    val autoModes = remember {
        mutableStateMapOf<SettingsItem, Boolean>()
    }
    LaunchedEffect(characteristics.characteristicsItems) {
        val items = characteristics.characteristicsItems
        if (items.isNotEmpty() && items.none { it.id == paramsRadioGroupSelectedItem }) {
            paramsRadioGroupSelectedItem = items.first().id

            items.forEach { item ->
                (item.id as? SettingsItem)?.let { category ->
                    // Кладем false только если там еще ничего нет (чтобы не затирать выбор пользователя)
                    if (!autoModes.containsKey(category)) {
                        autoModes[category] = false
                    }
                }
            }
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


    var selectorPosition: Int by remember {
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
        if (characteristics.characteristicsItems.isEmpty()) return@LaunchedEffect

        val updatedCharacteristics = when (paramsRadioGroupSelectedItem) {
            SettingsItem.MAGNIFIER -> {
                // 1. Находим индекс лупы в списке доступных увеличений
                val newMagValue = characteristics.magnifierItems[selectorPosition].value
                magnifierPosition = selectorPosition

                // 2. Обновляем текст в основном меню (RadioGroup)
                val indexInMenu = characteristics.characteristicsItems.indexOfFirst { it.id == SettingsItem.MAGNIFIER }
                if (indexInMenu != -1) {
                    val oldItem = characteristics.characteristicsItems[indexInMenu]
                    if (oldItem is RadioGroupItem.TextItem) {
                        val updatedItem = oldItem.copy(currentValue = newMagValue)
                        val newList = characteristics.characteristicsItems.toMutableList().apply {
                            this[indexInMenu] = updatedItem
                        }
                        characteristics = characteristics.copy(characteristicsItems = newList)

                        // 3. СРАЗУ настраиваем рендерер
                        renderer.configureMagnifier(newMagValue.replace("x", "").toFloatOrNull() ?: 1f)
                    }
                }
                null // Возвращаем null, чтобы не отправлять характеристики в камеру (лупа - это софт)
            }
            SettingsItem.SHUTTER -> characteristics.copy(shutterValue = characteristics.shutterItems[selectorPosition].value)
            SettingsItem.ISO -> characteristics.copy(isoValue = characteristics.isoItems[selectorPosition].value)
            SettingsItem.WB -> characteristics.copy(wbValue = characteristics.wbItems[selectorPosition].value)
            SettingsItem.FOCUS -> characteristics.copy(focusValue = characteristics.focusItems[selectorPosition].value)
            else -> null
        }

        updatedCharacteristics?.let { onSetCharacteristic(it) }
    }
    /* var selectorSelectedItemIndex by remember {
         mutableIntStateOf(0)
     }*/
    var selectorRadioGroupItems by remember {
        mutableStateOf<List<RadioUiItem<ModeItem>>?>(null)
    }
    var selectorRadioGroupSelectedItem by remember {
        mutableStateOf<Any?>(null)
    }

    LaunchedEffect(paramsRadioGroupSelectedItem, autoModes[paramsRadioGroupSelectedItem]/*, characteristics*/) {
        val currentCategory = paramsRadioGroupSelectedItem as? SettingsItem ?: return@LaunchedEffect
        val isAuto = autoModes[currentCategory] ?: false

        if (isAuto) {
            // Режим AUTO (выбор режимов: WB Mode, Focus Mode)
            val (dataList, currentMode) = when (currentCategory) {
                SettingsItem.WB -> characteristics.wbModeItems to characteristics.wbMode
                SettingsItem.FOCUS -> characteristics.focusModeItems to characteristics.focusMode
                else -> null to null
            }
            selectorRadioGroupItems = dataList?.map { data ->
                RadioUiItem(id = data.id as ModeItem) { isSelected ->
                    when (data) {
                        is RadioGroupItem.IconItem -> IconRadioContent(data.iconRes, isSelected)
                        is RadioGroupItem.TextItem -> TextRadioContent(data.title, data.currentValue, isSelected)
                    }
                }
            }
            selectorRadioGroupSelectedItem = currentMode
            selectorItems = null
        } else {
            // Режим MANUAL (ISO, Shutter, Magnifier, ручные WB/Focus)
            val (dataList, position) = when (currentCategory) {
                SettingsItem.SHUTTER -> characteristics.shutterItems to characteristics.shutterPosition
                SettingsItem.ISO -> characteristics.isoItems to characteristics.isoPosition
                SettingsItem.WB -> characteristics.wbItems to characteristics.wbPosition
                SettingsItem.FOCUS -> characteristics.focusItems to characteristics.focusPosition
                SettingsItem.MAGNIFIER -> characteristics.magnifierItems to magnifierPosition
                else -> null to 0
            }
            selectorItems = dataList?.map { data ->
                SelectorUiItem(id = data.id) { isSelected -> TextSelectorContent(data, isSelected) }
            }
            selectorPosition = position
            selectorRadioGroupItems = null
        }
    }
    ////////////////////////////



// Синхронизируем начальные значения (только для новых категорий)
  /*  LaunchedEffect(characteristics.characteristicsItems) {
        characteristics.characteristicsItems.forEach { item ->
            (item.id as? SettingsItem)?.let { category ->
                // Кладем false только если там еще ничего нет (чтобы не затирать выбор пользователя)
                if (!autoModes.containsKey(category)) {
                    autoModes[category] = false
                }
            }
        }
    }*/



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ///////////preview]
        Box() {
            /* AndroidView(
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
             )*/
            CameraPreviewContainer(
                renderer,
                characteristics.fullScreen,
                characteristics, // Предположим, это ваш класс с width и height
                { size ->
                    surfaceViewSize = size
                },
                { array ->
                    touchPoint = array
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
                selectedItem = it,
                onItemClick = { value ->
                   // autoModes[value as SettingsItem] = !(autoModes[value])!!
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
                    vectorColor = if (autoModes[paramsRadioGroupSelectedItem] == true) Color.Green else Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.auto,
                    onClick = {
                        autoModes[paramsRadioGroupSelectedItem as SettingsItem] =
                            !(autoModes[paramsRadioGroupSelectedItem] ?: false)
                        //  autoMode = !autoMode
                    }
                )
                /////////value selector
                val currentIsAuto = autoModes[paramsRadioGroupSelectedItem] ?: false
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .height(50.dp)
                ) {
                    ////tmp fast selector
                    /*  val items by remember {
                          mutableStateOf(
                              listOf(
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              0, "1"
                                          ), isSelected
                                      )
                                  },
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              2, "2"
                                          ), isSelected
                                      )
                                  },
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              3, "3"
                                          ), isSelected
                                      )
                                  },
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              4, "4"
                                          ), isSelected
                                      )
                                  },
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              5, "5"
                                          ), isSelected
                                      )
                                  },
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              6, "6"
                                          ), isSelected
                                      )
                                  },
                                  SelectorUiItem(id = 0) { isSelected ->
                                      TextSelectorContent(
                                          SelectorItem(
                                              7, "7"
                                          ), isSelected
                                      )
                                  },
                              )
                          )
                      }
                      ValueSelector(
                          modifier = Modifier.height(42.dp),
                          position = 0,
                          items = items,
                          onSelectedItemChanged = { index ->

                          }
                      )
                      ////end of tmp fast selector
                      */


                    if (currentIsAuto && (
                                paramsRadioGroupSelectedItem == SettingsItem.WB
                                        || paramsRadioGroupSelectedItem == SettingsItem.FOCUS
                            )) {
                        // Показываем выбор режимов (Auto, Cloudy, Macro...)
                        selectorRadioGroupItems?.let { items ->
                            UniversalRadioGroup(
                                items = items,
                                selectedItem = selectorRadioGroupSelectedItem as? ModeItem,
                                onItemClick = { mode ->
                                    // Здесь вызываем обновление через ViewModel
                                    val updated = when(mode) {
                                        is ModeItem.WbItem -> characteristics.copy(wbMode = mode)
                                        is ModeItem.FocusItem -> characteristics.copy(focusMode = mode)
                                        else -> characteristics
                                    }
                                    onSetCharacteristic(updated)
                                }
                            )
                        }
                    } else {
                        // Показываем обычный скролл значений (ISO, Shutter, Magnifier)
                        ValueSelector(
                            modifier = Modifier.fillMaxSize(),
                            position = selectorPosition,
                            items = selectorItems,
                            onSelectedItemChanged = { index ->
                                selectorPosition = index
                            }
                        )
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