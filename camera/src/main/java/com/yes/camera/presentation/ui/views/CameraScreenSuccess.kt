package com.yes.camera.presentation.ui.views

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.OrientationEventListener
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yes.camera.R
import com.yes.camera.presentation.model.*
import com.yes.camera.presentation.ui.adapter.TextSelectorContent
import com.yes.camera.presentation.ui.custom.compose.*
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.shared.presentation.ui.theme.AppTheme
import com.yes.shared.presentation.ui.theme.FlashCameraTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class LayoutOrientation {
    PORTRAIT, LANDSCAPE_LEFT, LANDSCAPE_RIGHT, REVERSE_PORTRAIT
}

@Composable
fun CameraScreenSuccess(
    context: Context,
    renderer: GLRenderer,
    characteristicsFlow: StateFlow<CharacteristicsUI>,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onSingleCapture: () -> Unit,
    onSetCharacteristic: (characteristics: CharacteristicsUI) -> Unit,
    onSelectCategory: (category: SettingsItem) -> Unit,
) {
    val characteristics by characteristicsFlow.collectAsState()

    var layoutOrientation by remember { mutableStateOf(LayoutOrientation.PORTRAIT) }

    DisposableEffect(context) {
        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val newOrientation = when (orientation) {
                    in 45..135 -> LayoutOrientation.LANDSCAPE_RIGHT
                    in 135..225 -> LayoutOrientation.REVERSE_PORTRAIT
                    in 225..315 -> LayoutOrientation.LANDSCAPE_LEFT
                    else -> LayoutOrientation.PORTRAIT
                }
                if (newOrientation != layoutOrientation) {
                    layoutOrientation = newOrientation
                }
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        onDispose {
            orientationEventListener.disable()
        }
    }

    val isLandscape = layoutOrientation == LayoutOrientation.LANDSCAPE_LEFT || layoutOrientation == LayoutOrientation.LANDSCAPE_RIGHT

    // Top elements follow top edge of phone
    val targetTopRotation = when (layoutOrientation) {
        LayoutOrientation.PORTRAIT -> 0f
        LayoutOrientation.LANDSCAPE_LEFT -> 90f
        LayoutOrientation.LANDSCAPE_RIGHT -> 270f
        LayoutOrientation.REVERSE_PORTRAIT -> 180f
    }

    // Bottom elements follow bottom edge of phone (opposite of top when in landscape)
    val targetBottomRotation = when (layoutOrientation) {
        LayoutOrientation.PORTRAIT -> 0f
        LayoutOrientation.LANDSCAPE_LEFT -> 270f
        LayoutOrientation.LANDSCAPE_RIGHT -> 90f
        LayoutOrientation.REVERSE_PORTRAIT -> 180f
    }

    val topRotation by animateFloatAsState(
        targetValue = targetTopRotation,
        animationSpec = tween(durationMillis = 300),
        label = "topRotation"
    )

    val bottomRotation by animateFloatAsState(
        targetValue = targetBottomRotation,
        animationSpec = tween(durationMillis = 300),
        label = "bottomRotation"
    )

    // Sync magnifier
    LaunchedEffect(characteristics.touchPoint) {
        characteristics.touchPoint?.let { point ->
            val normalizedX = point.x * 2f - 1f
            val normalizedY = -(point.y * 2f - 1f)
            renderer.handleTouchPress(normalizedX, normalizedY)
        }
    }

    LaunchedEffect(characteristics.magnifierValue, characteristics.isFocused) {
        val mag = characteristics.magnifierValue?.replace("x", "")?.toFloatOrNull() ?: 1f
        val frame = if (characteristics.isFocused) 1 else 0
        val tintColor = if (characteristics.isFocused) {
            Color.parseColor("#81c784") // Green when focused
        } else {
            Color.parseColor("#d3e2ff") // Primary accent / light blue when searching
        }
        renderer.configureMagnifier(mag, frame = frame, tintColor = tintColor)
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

    val isAutoForCategory = remember(characteristics.isAutoForSelectedCategory) { characteristics.isAutoForSelectedCategory }
    val currentPosition = remember(characteristics.currentCategoryPosition) { characteristics.currentCategoryPosition }

    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.transparent)) {
        ShutterBox(
            isOpen = shutterBoxIsOpen,
            onToggle = { shutterBoxIsOpen = !shutterBoxIsOpen },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (characteristics.fullScreen || isLandscape) AppTheme.dimens.none else AppTheme.dimens.shutterTopPadding)
        ) {}

        // Top category bar (near top of phone / camera cutout) rotates with topRotation
        UniversalRadioGroup(
            items = paramsRadioGroupItems,
            selectedItem = characteristics.selectedCategory,
            onItemClick = { onSelectCategory(it as SettingsItem) },
            modifier = Modifier
                .padding(AppTheme.dimens.small)
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.large),
            itemRotation = topRotation
        )

        // Top-end resolution & format text rotates with topRotation
        Column(
            modifier = Modifier
                .padding(top = AppTheme.dimens.histogramTopPadding, end = AppTheme.dimens.large)
                .align(Alignment.TopEnd)
                .graphicsLayer { rotationZ = topRotation },
            horizontalAlignment = Alignment.End
        ) {
            characteristics.resolution?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.End,
                    style = TextStyle(
                        color = AppTheme.colors.textPrimary,
                        fontSize = AppTheme.dimens.textMedium,
                        shadow = Shadow(AppTheme.colors.shadow, Offset(5f, 5f), 5f)
                    )
                )
            }
            Text(
                text = characteristics.imgFormat,
                textAlign = TextAlign.End,
                style = TextStyle(
                    color = AppTheme.colors.textPrimary.copy(alpha = 0.8f),
                    fontSize = AppTheme.dimens.textSmall,
                    shadow = Shadow(AppTheme.colors.shadow, Offset(3f, 3f), 3f)
                )
            )
        }

        // Top-start histogram rotates with topRotation
        Histogram(
            modifier = Modifier
                .padding(start = AppTheme.dimens.large, top = AppTheme.dimens.histogramTopPadding)
                .align(Alignment.TopStart)
                .graphicsLayer { rotationZ = topRotation },
            values = characteristics.histogramData,
            widthDp = AppTheme.dimens.histogramWidth,
            heightDp = AppTheme.dimens.histogramHeight
        )

        // Bottom control bar and shutter row rotate with bottomRotation
        Column(
            modifier = Modifier
                .padding(AppTheme.dimens.medium)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.medium)
        ) {
            CameraControlBar(
                selectedCategory = characteristics.selectedCategory,
                isAutoForCategory = isAutoForCategory,
                shutterItems = characteristics.shutterItems,
                isoItems = characteristics.isoItems,
                wbItems = characteristics.wbItems,
                focusItems = characteristics.focusItems,
                magnifierItems = characteristics.magnifierItems,
                currentPosition = currentPosition,
                topRotation = topRotation,
                wbModeItems = characteristics.wbModeItems,
                focusModeItems = characteristics.focusModeItems,
                wbMode = characteristics.wbMode,
                focusMode = characteristics.focusMode,
                renderer = renderer,
                onAutoToggle = {
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
                },
                onWbModeClick = { mode: ModeItem.WbItem ->
                    val updated = characteristics.copy(isWbAuto = true, wbValue = null, wbMode = mode)
                    onSetCharacteristic(updated)
                },
                onFocusModeClick = { mode: ModeItem.FocusItem ->
                    val updated = characteristics.copy(isFocusAuto = true, focusValue = null, focusMode = mode)
                    onSetCharacteristic(updated)
                },
                onValueIndexChanged = { index: Int, newValue: String? ->
                    if (index != currentPosition || isAutoForCategory || characteristics.selectedCategory == SettingsItem.MAGNIFIER) {
                        val updated = when (characteristics.selectedCategory) {
                            SettingsItem.SHUTTER -> characteristics.copy(isShutterAuto = false, shutterValue = newValue, shutterPosition = index)
                            SettingsItem.ISO -> characteristics.copy(isIsoAuto = false, isoValue = newValue, isoPosition = index)
                            SettingsItem.WB -> characteristics.copy(isWbAuto = false, wbValue = newValue, wbPosition = index)
                            SettingsItem.FOCUS -> characteristics.copy(isFocusAuto = false, focusValue = newValue, focusPosition = index)
                            SettingsItem.MAGNIFIER -> characteristics.copy(magnifierValue = newValue)
                        }
                        onSetCharacteristic(updated)
                    }
                }
            )

            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.radioGroupSpacing)
            ) {
                VectorShadow(
                    modifier = Modifier
                        .size(AppTheme.dimens.iconLarge)
                        .graphicsLayer { rotationZ = bottomRotation },
                    resId = R.drawable.settings,
                    vectorColor = AppTheme.colors.iconPrimary,
                    shadowColor = AppTheme.colors.shadow,
                    onClick = onSettingsClick
                )
                VectorShadow(
                    modifier = Modifier
                        .size(AppTheme.dimens.iconLarge)
                        .graphicsLayer { rotationZ = bottomRotation },
                    resId = R.drawable.flip_camera_android,
                    vectorColor = AppTheme.colors.iconPrimary,
                    shadowColor = AppTheme.colors.shadow
                )
                RecordButton(
                    modifier = Modifier
                        .size(AppTheme.dimens.recordButtonSize)
                        .graphicsLayer { rotationZ = bottomRotation },
                    onClick = { onSingleCapture() }
                )
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
                shutterItems = persistentListOf(SelectorItem(0, "1/500"), SelectorItem(1, "1/250"), SelectorItem(2, "1/125"), SelectorItem(3, "1/60")),
                isoItems = persistentListOf(SelectorItem(0, "50"), SelectorItem(1, "100"), SelectorItem(2, "200")),
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
                onSingleCapture = {},
                onSetCharacteristic = {},
                onSelectCategory = {}
            )
        }
    }
}

@Composable
private fun CameraControlBar(
    selectedCategory: SettingsItem,
    isAutoForCategory: Boolean,
    shutterItems: ImmutableList<SelectorItem>,
    isoItems: ImmutableList<SelectorItem>,
    wbItems: ImmutableList<SelectorItem>,
    focusItems: ImmutableList<SelectorItem>,
    magnifierItems: ImmutableList<SelectorItem>,
    currentPosition: Int,
    topRotation: Float,
    wbModeItems: List<RadioGroupItem>,
    focusModeItems: List<RadioGroupItem>,
    wbMode: ModeItem.WbItem?,
    focusMode: ModeItem.FocusItem?,
    renderer: GLRenderer,
    onAutoToggle: () -> Unit,
    onWbModeClick: (ModeItem.WbItem) -> Unit,
    onFocusModeClick: (ModeItem.FocusItem) -> Unit,
    onValueIndexChanged: (Int, String?) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        VectorShadow(
            modifier = Modifier
                .size(AppTheme.dimens.iconLarge)
                .graphicsLayer { rotationZ = topRotation },
            resId = R.drawable.auto,
            vectorColor = if (isAutoForCategory) AppTheme.colors.primaryAccent else AppTheme.colors.iconPrimary,
            shadowColor = AppTheme.colors.shadow,
            onClick = onAutoToggle
        )

        Box(modifier = Modifier.fillMaxWidth().padding(AppTheme.dimens.small).height(AppTheme.dimens.controlBarHeight)) {
            if (isAutoForCategory && (selectedCategory == SettingsItem.WB || selectedCategory == SettingsItem.FOCUS)) {
                val modeItems = if (selectedCategory == SettingsItem.FOCUS) focusModeItems else wbModeItems
                val uiModeItems = remember(selectedCategory, modeItems) {
                    modeItems.map { data ->
                        RadioUiItem(id = data.id as ModeItem) { isSelected ->
                            when (data) {
                                is RadioGroupItem.IconItem -> IconRadioContent(data.iconRes, isSelected)
                                is RadioGroupItem.TextItem -> TextRadioContent(data.title, data.currentValue, isSelected)
                            }
                        }
                    }.toImmutableList()
                }
                val selectedMode = if (selectedCategory == SettingsItem.FOCUS) focusMode else wbMode

                UniversalRadioGroup(
                    items = uiModeItems,
                    selectedItem = selectedMode,
                    onItemClick = { mode ->
                        when (mode) {
                            is ModeItem.WbItem -> onWbModeClick(mode)
                            is ModeItem.FocusItem -> onFocusModeClick(mode)
                        }
                    },
                    itemRotation = topRotation
                )
            } else {
                key(selectedCategory) {
                    val categoryItems = when (selectedCategory) {
                        SettingsItem.SHUTTER -> shutterItems
                        SettingsItem.ISO -> isoItems
                        SettingsItem.WB -> wbItems
                        SettingsItem.FOCUS -> focusItems
                        SettingsItem.MAGNIFIER -> magnifierItems
                    }
                    val uiItems = remember(selectedCategory, categoryItems) {
                        categoryItems.map { data ->
                            SelectorUiItem(id = data.id) { isSelected -> TextSelectorContent(data, isSelected) }
                        }
                    }

                    ValueSelector(
                        modifier = Modifier.fillMaxSize(),
                        position = currentPosition,
                        items = uiItems,
                        onSelectedItemChanged = { index ->
                            val newValue = categoryItems.getOrNull(index)?.value
                            if (selectedCategory == SettingsItem.MAGNIFIER) {
                                renderer.configureMagnifier(newValue?.replace("x", "")?.toFloatOrNull() ?: 1f)
                            }
                            onValueIndexChanged(index, newValue)
                        },
                        itemRotation = topRotation
                    )
                }
            }
        }
    }
}
