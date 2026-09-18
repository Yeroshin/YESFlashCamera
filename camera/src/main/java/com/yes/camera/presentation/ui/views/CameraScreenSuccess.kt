package com.yes.camera.presentation.ui.views

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.ModeItem
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.presentation.ui.adapter.TextSelectorContent
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CameraScreenSuccess(
    context: Context,
    renderer: GLRenderer,
    characteristicsFlow: StateFlow<CharacteristicsUI>,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onSetCharacteristic: (characteristics: CharacteristicsUI) -> Unit,
) {
    val characteristics by characteristicsFlow.collectAsState()

    // Синхронизация лупы и фокуса с рендерером
    LaunchedEffect(characteristics.touchPoint) {
        characteristics.touchPoint?.let { point ->
            // Пересчитываем координаты Compose (0..1) в нормализованные OpenGL (-1..1)
            val normalizedX = point.x * 2f - 1f
            val normalizedY = -(point.y * 2f - 1f)

            // Двигаем лупу в рендерере к точке фокуса
            renderer.handleTouchPress(normalizedX, normalizedY)
        }
    }

    var characteristicsRrequest by remember(characteristics) {
        mutableStateOf(characteristics)
    }

    var shutterBoxIsOpen by remember { mutableStateOf(true) }

    val autoModes = remember {
        mutableStateMapOf<SettingsItem, Boolean>()
    }
    var paramsRadioGroupSelectedItem by remember {
        mutableStateOf(
            characteristics.characteristicsItems.firstOrNull()?.id
        )
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
        }.toImmutableList()
    }

    var selectorValue: Int by remember {
        mutableIntStateOf(0)
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
    var selectorRadioGroupItems by remember {
        mutableStateOf<List<RadioUiItem<ModeItem>>?>(null)
    }
    var selectorRadioGroupSelectedItem by remember {
        mutableStateOf<Any?>(null)
    }

    var previousCategory by remember { mutableStateOf<SettingsItem?>(null) }
    var isTechnicalScroll by remember { mutableStateOf(false) }

    LaunchedEffect(paramsRadioGroupItems) {
        if (paramsRadioGroupItems.isNotEmpty() && paramsRadioGroupItems.none { it.id == paramsRadioGroupSelectedItem }) {
            paramsRadioGroupSelectedItem = paramsRadioGroupItems.first().id

            paramsRadioGroupItems.forEach { item ->
                (item.id as? SettingsItem)?.let { category ->
                    if (!autoModes.containsKey(category)) {
                        autoModes[category] = false
                    }
                }
            }
        }
    }

    var prevSelectedItem by remember { mutableStateOf(paramsRadioGroupSelectedItem) }
    var prevAutoMode by remember { mutableStateOf(autoModes[paramsRadioGroupSelectedItem]) }
    var prevItems by remember { mutableStateOf(paramsRadioGroupItems) }

    LaunchedEffect(
        paramsRadioGroupSelectedItem,
        autoModes[paramsRadioGroupSelectedItem],
    ) {
        val currentAutoMode = autoModes[paramsRadioGroupSelectedItem]

        prevSelectedItem = paramsRadioGroupSelectedItem
        prevAutoMode = currentAutoMode
        prevItems = paramsRadioGroupItems

        val currentCategory = paramsRadioGroupSelectedItem as? SettingsItem ?: return@LaunchedEffect
        val isAuto = autoModes[currentCategory] ?: false

        val isCategoryChanged = currentCategory != previousCategory
        previousCategory = currentCategory

        if (isCategoryChanged && isAuto) {
            isTechnicalScroll = true
        }

        if (!isCategoryChanged && isAuto) {
            val alreadyAuto = when (currentCategory) {
                SettingsItem.SHUTTER -> characteristics.shutterValue == null
                SettingsItem.ISO -> characteristics.isoValue == null
                SettingsItem.WB -> characteristics.wbValue == null
                SettingsItem.FOCUS -> characteristics.focusValue == null
                else -> false
            }
            if (!alreadyAuto) {
                val resetCharacteristics = when (currentCategory) {
                    SettingsItem.SHUTTER -> characteristics.copy(shutterValue = null)
                    SettingsItem.ISO -> characteristics.copy(isoValue = null)
                    SettingsItem.WB -> characteristics.copy(wbValue = null)
                    SettingsItem.FOCUS -> characteristics.copy(focusValue = null)
                    else -> null
                }
                resetCharacteristics?.let { onSetCharacteristic(it) }
            }
        }

        if (isAuto) {
            val (dataList, currentMode) = when (currentCategory) {
                SettingsItem.WB -> characteristics.wbModeItems to characteristics.wbMode
                SettingsItem.FOCUS -> characteristics.focusModeItems to characteristics.focusMode
                else -> null to null
            }
            selectorRadioGroupItems = dataList?.map { data ->
                RadioUiItem(id = data.id as ModeItem) { isSelected ->
                    when (data) {
                        is RadioGroupItem.IconItem -> IconRadioContent(data.iconRes, isSelected)
                        is RadioGroupItem.TextItem -> TextRadioContent(
                            data.title,
                            data.currentValue,
                            isSelected
                        )
                    }
                }
            }
            selectorRadioGroupSelectedItem = currentMode
        } else {
            selectorRadioGroupItems = null
        }

        val (dataList, positionFromChars) = when (currentCategory) {
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
        if (isAuto || isCategoryChanged) {
            selectorPosition = positionFromChars
        } else {
            if (selectorValue == positionFromChars) {
                isTechnicalScroll = false
            }
        }
    }

    LaunchedEffect(selectorValue) {
        if (characteristics.characteristicsItems.isEmpty()) return@LaunchedEffect

        if (isTechnicalScroll) {
            isTechnicalScroll = false
            return@LaunchedEffect
        }

        val currentCategory = paramsRadioGroupSelectedItem as? SettingsItem ?: return@LaunchedEffect
        val isAuto = autoModes[currentCategory] ?: false

        if (currentCategory == SettingsItem.MAGNIFIER) {
            val newMagValue = characteristics.magnifierItems.getOrNull(selectorValue)?.value
                ?: return@LaunchedEffect
            magnifierPosition = selectorValue

            renderer.configureMagnifier(newMagValue.replace("x", "").toFloatOrNull() ?: 1f)
            onSetCharacteristic(characteristics.copy(magnifierValue = newMagValue))
            return@LaunchedEffect
        }

        if (!isAuto) {
            val updated = when (currentCategory) {
                SettingsItem.SHUTTER -> characteristics.copy(
                    shutterValue = characteristics.shutterItems.getOrNull(
                        selectorValue
                    )?.value
                )

                SettingsItem.ISO -> characteristics.copy(
                    isoValue = characteristics.isoItems.getOrNull(
                        selectorValue
                    )?.value
                )

                SettingsItem.WB -> characteristics.copy(
                    wbValue = characteristics.wbItems.getOrNull(
                        selectorValue
                    )?.value

                )

                SettingsItem.FOCUS -> characteristics.copy(
                    focusValue = characteristics.focusItems.getOrNull(
                        selectorValue
                    )?.value,
                    focusMode = null
                )

                else -> null
            }
            updated?.let { onSetCharacteristic(it) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Box() {
            ShutterBox(
                isOpen = shutterBoxIsOpen,
                onToggle = { shutterBoxIsOpen = !shutterBoxIsOpen },
                modifier = Modifier.padding(
                    top = if (characteristics.fullScreen) 0.dp else 84.dp
                )
            ) {}
        }
        
        paramsRadioGroupSelectedItem?.let {
            UniversalRadioGroup(
                items = paramsRadioGroupItems,
                selectedItem = it,
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
        
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                VectorShadow(
                    modifier = Modifier.size(32.dp),
                    vectorColor = if (autoModes[paramsRadioGroupSelectedItem] == true) Color.Green else Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.auto,
                    onClick = {
                        autoModes[paramsRadioGroupSelectedItem as SettingsItem] =
                            !(autoModes[paramsRadioGroupSelectedItem] ?: false)
                    }
                )
                
                val currentIsAuto = autoModes[paramsRadioGroupSelectedItem] ?: false
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .height(50.dp)
                ) {
                    if (currentIsAuto && (
                                paramsRadioGroupSelectedItem == SettingsItem.WB
                                        || paramsRadioGroupSelectedItem == SettingsItem.FOCUS
                                )
                    ) {
                        selectorRadioGroupItems?.let { items ->
                            UniversalRadioGroup(
                                items = items,
                                selectedItem = selectorRadioGroupSelectedItem as? ModeItem,
                                onItemClick = { mode ->
                                    selectorRadioGroupSelectedItem = mode
                                    val characteristicsCopy = when (mode) {
                                        is ModeItem.WbItem -> characteristics.copy(
                                            wbValue = null,
                                            wbMode = mode
                                        )

                                        is ModeItem.FocusItem -> characteristics.copy(
                                            focusValue = null,
                                            focusMode = mode
                                        )
                                    }
                                    onSetCharacteristic(characteristicsCopy)
                                }
                            )
                        }
                    } else {
                        ValueSelector(
                            modifier = Modifier.fillMaxSize(),
                            position = selectorPosition,
                            items = selectorItems,
                            onSelectedItemChanged = { index ->
                                selectorValue = index
                                autoModes[paramsRadioGroupSelectedItem as SettingsItem] = false
                            }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(56.dp)
            ) {
                VectorShadow(
                    Modifier.size(32.dp),
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.settings,
                    onClick = onSettingsClick
                )

                VectorShadow(
                    Modifier.size(32.dp),
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.flip_camera_android,
                )

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
