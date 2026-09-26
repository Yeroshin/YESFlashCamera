package com.yes.camera.presentation.ui.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.yes.camera.R
import com.yes.camera.presentation.model.*
import com.yes.camera.presentation.ui.adapter.TextSelectorContent
import com.yes.camera.presentation.ui.custom.compose.*
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.shared.presentation.ui.theme.AppTheme
import com.yes.shared.presentation.ui.theme.FlashCameraTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
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
                    is RadioGroupItem.IconItem -> Icon(
                        painterResource(data.iconRes),
                        null,
                        tint = if (isSelected) AppTheme.colors.secondaryAccent else AppTheme.colors.iconPrimary
                    )
                    is RadioGroupItem.TextItem -> TextRadioContent(data.title, data.currentValue, isSelected)
                }
            }
        }.toImmutableList()
    }

    val isAutoForCategory = characteristics.isAutoForSelectedCategory
    val currentItems = characteristics.currentCategoryItems
    val currentPosition = characteristics.currentCategoryPosition

    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.transparent)) {
        ShutterBox(
            isOpen = shutterBoxIsOpen,
            onToggle = { shutterBoxIsOpen = !shutterBoxIsOpen },
            modifier = Modifier.fillMaxSize().padding(top = if (characteristics.fullScreen) AppTheme.dimens.none else AppTheme.dimens.shutterTopPadding)
        ) {}
        
        UniversalRadioGroup(
            items = paramsRadioGroupItems,
            selectedItem = characteristics.selectedCategory,
            onItemClick = { onSelectCategory(it as SettingsItem) },
            modifier = Modifier.padding(AppTheme.dimens.small).fillMaxWidth().padding(top = AppTheme.dimens.large),
        )

        characteristics.resolution?.let {
            Text(
                modifier = Modifier.padding(top = AppTheme.dimens.histogramTopPadding, end = AppTheme.dimens.large).align(Alignment.TopEnd),
                text = it,
                textAlign = TextAlign.End,
                style = TextStyle(
                    color = AppTheme.colors.textPrimary,
                    fontSize = AppTheme.dimens.textMedium,
                    shadow = Shadow(AppTheme.colors.shadow, Offset(5f, 5f), 5f)
                )
            )
        }
        
        Histogram(
            modifier = Modifier.padding(start = AppTheme.dimens.large, top = AppTheme.dimens.histogramTopPadding).align(Alignment.TopStart),
            values = characteristics.histogramData,
            widthDp = AppTheme.dimens.histogramWidth,
            heightDp = AppTheme.dimens.histogramHeight
        )
        
        Column(
            modifier = Modifier.padding(AppTheme.dimens.medium).fillMaxWidth().align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VectorShadow(
                    modifier = Modifier.size(AppTheme.dimens.iconLarge),
                    resId = R.drawable.auto,
                    vectorColor = if (isAutoForCategory) AppTheme.colors.primaryAccent else AppTheme.colors.iconPrimary,
                    shadowColor = AppTheme.colors.shadow,
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
                
                Box(modifier = Modifier.fillMaxWidth().padding(AppTheme.dimens.small).height(AppTheme.dimens.controlBarHeight)) {
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
                                    if (index != currentPosition || isAutoForCategory || characteristics.selectedCategory == SettingsItem.MAGNIFIER) {
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
            
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.radioGroupSpacing)
            ) {
                VectorShadow(Modifier.size(AppTheme.dimens.iconLarge), R.drawable.settings, AppTheme.colors.iconPrimary, AppTheme.colors.shadow, onClick = onSettingsClick)
                VectorShadow(Modifier.size(AppTheme.dimens.iconLarge), R.drawable.flip_camera_android, AppTheme.colors.iconPrimary, AppTheme.colors.shadow)
                RecordButton(modifier = Modifier.size(AppTheme.dimens.recordButtonSize), isChecked = false, onClick = { isCheck ->
                    shutterBoxIsOpen = !shutterBoxIsOpen
                    onStartVideoRecord(isCheck)
                })
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CameraScreenSuccessPreview() {
    val context = LocalContext.current
    val handler = remember { Handler(Looper.getMainLooper()) }
    val renderer = remember { GLRenderer(context, handler) {} }
    val stateFlow = remember {
        MutableStateFlow(
            CharacteristicsUI(
                resolution = "1920x1080",
                shutterValue = "1/60",
                isoValue = "100",
                wbValue = "5000K",
                characteristicsItems = persistentListOf(
                    RadioGroupItem.TextItem(SettingsItem.SHUTTER, "SHUTTER", "1/60"),
                    RadioGroupItem.TextItem(SettingsItem.ISO, "ISO", "100"),
                    RadioGroupItem.TextItem(SettingsItem.WB, "WB", "5000K"),
                    RadioGroupItem.TextItem(SettingsItem.FOCUS, "FOCUS", "A"),
                    RadioGroupItem.TextItem(SettingsItem.MAGNIFIER, "MAGNIFIER", "1")
                ),
                shutterItems = listOf(SelectorItem(0, "1/500"), SelectorItem(1, "1/250"), SelectorItem(2, "1/125"), SelectorItem(3, "1/60")),
                isoItems = listOf(SelectorItem(0, "50"), SelectorItem(1, "100"), SelectorItem(2, "200")),
                isShutterAuto = false,
                isIsoAuto = false
            )
        )
    }
    FlashCameraTheme {
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            CameraScreenSuccess(
                context = context,
                renderer = renderer,
                characteristicsFlow = stateFlow,
                onSettingsClick = {},
                onStartVideoRecord = {},
                onSetCharacteristic = {},
                onSelectCategory = {}
            )
        }
    }
}
