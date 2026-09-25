package com.yes.camera.presentation.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.yes.camera.presentation.model.*
import com.yes.camera.presentation.ui.adapter.TextSelectorContent
import com.yes.camera.presentation.ui.custom.compose.*
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
    onSelectCategory: (category: SettingsItem) -> Unit,
) {
    val characteristics by characteristicsFlow.collectAsState()

    // Sync magnifier
    LaunchedEffect(characteristics.touchPoint) {
        characteristics.touchPoint?.let { point ->
            val normalizedX = point.x * 2f - 1f
            val normalizedY = -(point.y * 2f - 1f)
            renderer.handleTouchPress(normalizedX, normalizedY)
        }
    }

    var shutterBoxIsOpen by remember { mutableStateOf(true) }

    val paramsRadioGroupItems = remember(characteristics.characteristicsItems) {
        characteristics.characteristicsItems.map { data ->
            RadioUiItem(id = data.id) { isSelected ->
                when (data) {
                    is RadioGroupItem.IconItem -> Icon(painterResource(data.iconRes), null, tint = if (isSelected) Color.Yellow else Color.White)
                    is RadioGroupItem.TextItem -> TextRadioContent(data.title, data.currentValue, isSelected)
                }
            }
        }.toImmutableList()
    }

    val isAutoForCategory = characteristics.isAutoForSelectedCategory
    val currentItems = characteristics.currentCategoryItems
    val currentPosition = characteristics.currentCategoryPosition

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        ShutterBox(
            isOpen = shutterBoxIsOpen,
            onToggle = { shutterBoxIsOpen = !shutterBoxIsOpen },
            modifier = Modifier.padding(top = if (characteristics.fullScreen) 0.dp else 84.dp)
        ) {}
        
        UniversalRadioGroup(
            items = paramsRadioGroupItems,
            selectedItem = characteristics.selectedCategory,
            onItemClick = { onSelectCategory(it as SettingsItem) },
            modifier = Modifier.padding(4.dp).fillMaxWidth().padding(top = 16.dp),
        )

        characteristics.resolution?.let {
            Text(
                modifier = Modifier.padding(top = 98.dp, end = 18.dp).align(Alignment.TopEnd),
                text = it, textAlign = TextAlign.End,
                style = TextStyle(color = Color.White, fontSize = 16.sp, shadow = Shadow(Color.DarkGray, Offset(5f, 5f), 5f))
            )
        }
        
        Histogram(
            modifier = Modifier.padding(start = 16.dp, top = 98.dp).align(Alignment.TopStart),
            characteristics.histogramData, 150.dp, 80.dp
        )
        
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth().align(Alignment.BottomCenter), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VectorShadow(
                    modifier = Modifier.size(32.dp),
                    resId = R.drawable.auto,
                    vectorColor = if (isAutoForCategory) Color.Green else Color.White,
                    shadowColor = Color.DarkGray,
                    onClick = {
                        val updated = when (characteristics.selectedCategory) {
                            SettingsItem.SHUTTER -> characteristics.copy(isShutterAuto = !characteristics.isShutterAuto)
                            SettingsItem.ISO -> characteristics.copy(isIsoAuto = !characteristics.isIsoAuto)
                            SettingsItem.WB -> characteristics.copy(
                                isWbAuto = !characteristics.isWbAuto,
                                wbMode = characteristics.wbMode ?: ModeItem.WbItem.AUTO
                            )
                            SettingsItem.FOCUS -> characteristics.copy(
                                isFocusAuto = !characteristics.isFocusAuto,
                                focusMode = characteristics.focusMode ?: ModeItem.FocusItem.CONTINUOUS
                            )
                            else -> null
                        }
                        updated?.let(onSetCharacteristic)
                    }
                )
                
                Box(modifier = Modifier.fillMaxWidth().padding(4.dp).height(50.dp)) {
                    if (isAutoForCategory && (characteristics.selectedCategory == SettingsItem.WB || characteristics.selectedCategory == SettingsItem.FOCUS)) {
                        val modeItems = if (characteristics.selectedCategory == SettingsItem.WB) characteristics.wbModeItems else characteristics.focusModeItems
                        val uiModeItems = remember(modeItems) {
                            modeItems.map { data ->
                                RadioUiItem(id = data.id as ModeItem) { isSelected ->
                                    when (data) {
                                        is RadioGroupItem.IconItem -> IconRadioContent(data.iconRes, isSelected)
                                        is RadioGroupItem.TextItem -> TextRadioContent(data.title, data.currentValue, isSelected)
                                    }
                                }
                            }.toImmutableList()
                        }
                        val selectedMode = if (characteristics.selectedCategory == SettingsItem.WB) characteristics.wbMode else characteristics.focusMode
                        
                        UniversalRadioGroup(
                            items = uiModeItems,
                            selectedItem = selectedMode,
                            onItemClick = { mode ->
                                val updated = when (mode) {
                                    is ModeItem.WbItem -> characteristics.copy(isWbAuto = true, wbValue = null, wbMode = mode)
                                    is ModeItem.FocusItem -> characteristics.copy(isFocusAuto = true, focusValue = null, focusMode = mode)
                                    else -> characteristics
                                }
                                onSetCharacteristic(updated)
                            }
                        )
                    } else {
                        // KEY гарантирует, что ValueSelector пересоздастся (и сбросит скролл) при смене категории
                        key(characteristics.selectedCategory) {
                            val uiItems = remember(currentItems) {
                                currentItems.map { data -> 
                                    SelectorUiItem(id = data.id) { isSelected -> TextSelectorContent(data, isSelected) } 
                                }
                            }
                            
                            ValueSelector(
                                modifier = Modifier.fillMaxSize(),
                                position = currentPosition,
                                items = uiItems,
                                onSelectedItemChanged = { index ->
                                    // Сбрасываем AUTO только если реально сдвинули или уже в AUTO
                                    if (index != currentPosition || isAutoForCategory) {
                                        val newValue = currentItems.getOrNull(index)?.value
                                        val updated = when (characteristics.selectedCategory) {
                                            SettingsItem.SHUTTER -> characteristics.copy(isShutterAuto = false, shutterValue = newValue, shutterPosition = index)
                                            SettingsItem.ISO -> characteristics.copy(isIsoAuto = false, isoValue = newValue, isoPosition = index)
                                            SettingsItem.WB -> characteristics.copy(isWbAuto = false, wbValue = newValue, wbPosition = index)
                                            SettingsItem.FOCUS -> characteristics.copy(isFocusAuto = false, focusValue = newValue, focusPosition = index)
                                            SettingsItem.MAGNIFIER -> {
                                                renderer.configureMagnifier(newValue?.replace("x", "")?.toFloatOrNull() ?: 1f)
                                                characteristics.copy(magnifierValue = newValue)
                                            }
                                        }
                                        updated.let(onSetCharacteristic)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(56.dp)) {
                VectorShadow(Modifier.size(32.dp), R.drawable.settings, Color.White, Color.DarkGray, onClick = onSettingsClick)
                VectorShadow(Modifier.size(32.dp), R.drawable.flip_camera_android, Color.White, Color.DarkGray)
                RecordButton(modifier = Modifier.size(96.dp), isChecked = false, onClick = { isCheck ->
                    shutterBoxIsOpen = !shutterBoxIsOpen
                    onStartVideoRecord(isCheck)
                })
            }
        }
    }
}
