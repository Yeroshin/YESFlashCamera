package com.yes.camera.presentation.ui.views


import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.FocusItem
import com.yes.camera.presentation.model.IconItem

import com.yes.camera.presentation.model.SettingsRadioGroupItem
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.SelectorRadioGroupItem

import com.yes.camera.presentation.model.TextItem
import com.yes.camera.presentation.model.WbItem
import com.yes.camera.presentation.ui.adapter.IconSelectorItemUI
import com.yes.camera.presentation.ui.adapter.TextSelectorItemUI
import com.yes.camera.presentation.ui.custom.compose.Histogram
import com.yes.camera.presentation.ui.custom.compose.RadioButton
import com.yes.camera.presentation.ui.custom.compose.RadioGroup
import com.yes.camera.presentation.ui.custom.compose.TextRadioItem

import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.compose.IconRadioItem
import com.yes.camera.presentation.ui.custom.compose.RecordButton
import com.yes.camera.presentation.ui.custom.compose.ShutterBox
import com.yes.camera.presentation.ui.custom.compose.VectorShadow
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import kotlinx.coroutines.flow.distinctUntilChanged


@Composable
@Preview
fun orew() {
    val standardShutterSpeeds = mapOf(
        31_250L to "1/32000",
        62_500L to "1/16000",
        125_000L to "1/8000",
        250_000L to "1/4000",
        500_000L to "1/2000",
        1_000_000L to "1/1000",
        2_000_000L to "1/500",
        4_000_000L to "1/250",
        8_000_000L to "1/125",
        16_000_000L to "1/60",
        33_333_333L to "1/30",
        66_666_667L to "1/15",
        125_000_000L to "1/8",
        250_000_000L to "1/4",
        500_000_000L to "1/2",
        1_000_000_000L to "1",
        2_000_000_000L to "2",
        4_000_000_000L to "4",
        8_000_000_000L to "8",
    )
    val standardIsoValues = listOf(
        50,
        100,
        200,
        400,
        800,
        1600,
        3200,
        6400,
        12800,
        25600,
        51200,
        102400,
        204800,
        409600,
        819200,
        1638400,
        3280000,
        4560000
    )
    val characteristics = CharacteristicsUI(

    )
    val context = LocalContext.current
    val renderer = remember {
        GLRenderer(
            context
        ) {}
    }
    CameraScreenSuccess(
        context = context,
        renderer = renderer,
        characteristicsInit = characteristics,
        onSettingsClick = {},
        onStartVideoRecord = {},
        onCharacteristicChanged = {},
    )
}

@Stable
data class ImmutableCollection<T>(
    val list: List<T>
)

@Immutable
data class MapImmutableCollection<T, R>(
    val map: Map<T, R>
)

@Composable
fun CameraScreenSuccess(
    context: Context,
    renderer: GLRenderer,
    // characteristicsInitial: CharacteristicsUI,
    characteristicsInit: CharacteristicsUI,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onCharacteristicChanged: (characteristics: CharacteristicsUI) -> Unit,
) {
    val immut = MapImmutableCollection(
        mapOf(
            TextItem::class.java to TextSelectorItemUI(),
            IconItem::class.java to IconSelectorItemUI()
        )
    )
    /* val adapter by remember {
         mutableStateOf(
             CompositeAdapter(
                 MapImmutableCollection(
                 mapOf<Class<*>, CompositeAdapter.AdapterDelegate<*>>((
                     TextItem::class.java to TextSelectorItemUI(),
                     IconItem::class.java to IconSelectorItemUI()
                 )
             )
             )
         )
     }*/
    /* val adapter by remember {
         mutableStateOf(
             CompositeAdapter(
                 MapImmutableCollection(
                     mapOf<Class<*>, CompositeAdapter.AdapterDelegate<*>>(
                         TextItem::class.java to TextSelectorItemUI(),
                         IconItem::class.java to IconSelectorItemUI()
                     )
                 ).map
             )
         )
     }*/
    val context = LocalContext.current
    /////////////////////
    var characteristics by remember(characteristicsInit) {
        mutableStateOf(characteristicsInit)
    }

    var settingsRequest by remember {
        mutableStateOf(characteristics)
    }


    var magnifierValue by remember {
        mutableStateOf("0")
    }
    var magnifierPosition by remember {
        mutableStateOf(0)
    }
    var settingsRadioGroupSelectedSettingsRadioGroupItem: SettingsRadioGroupItem? by remember {
        mutableStateOf(SettingsRadioGroupItem.SHUTTER)
    }
    /*var valueSelectorVisibility by remember {
        mutableStateOf(true)
    }*/

    LaunchedEffect(characteristics) {
        snapshotFlow { characteristics}
            // .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                characteristics = newValue
            }
    }
    var wb = remember {
        mutableStateOf(2131099791)
    }
    val settingsItem by remember {
        mutableStateOf(listOf(IconRadioItem(SettingsRadioGroupItem.WB, "WB", 2131099791)))
    }
    // val radioGroupItems= listOf(VectorRadioItem(Item.WB, "WB", wb.value))
    var wbRadioGroupSelectedSettingsItem: WbItem? by remember {
        mutableStateOf(WbItem.AUTO)
    }
    var selectorRadioGroupSelectedItem: SelectorRadioGroupItem? by remember {
        mutableStateOf(null)
    }
    var wbSelectorRadioGroupSelectedItem: SelectorRadioGroupItem? by remember {
        mutableStateOf(null)
    }
    var focusSelectorRadioGroupSelectedItem: SelectorRadioGroupItem? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(selectorRadioGroupSelectedItem) {
        settingsRequest = when (selectorRadioGroupSelectedItem) {

            WbItem.AUTO -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.AUTO
                )
            }

            WbItem.INCANDESCENT -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.INCANDESCENT
                )
            }

            WbItem.FLUORESCENT -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.FLUORESCENT
                )
            }

            WbItem.WARM_FLUORESCENT -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.WARM_FLUORESCENT
                )
            }

            WbItem.DAYLIGHT -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.DAYLIGHT
                )
            }

            WbItem.CLOUDY_DAYLIGHT -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.CLOUDY_DAYLIGHT
                )
            }

            WbItem.TWILIGHT -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.TWILIGHT
                )
            }

            WbItem.SHADE -> {
                wbSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    wbValue = null,
                    wbMode = WbItem.SHADE
                )
            }

            FocusItem.MACRO -> {
                focusSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    focusValue = null,
                    focusMode = FocusItem.MACRO
                )
            }

            FocusItem.CONTINUOUS -> {
                focusSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    focusValue = null,
                    focusMode = FocusItem.CONTINUOUS
                )
            }

            FocusItem.TOUCH -> {
                focusSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    focusValue = null,
                    focusMode = FocusItem.TOUCH
                )
            }

            FocusItem.INFINITE -> {
                focusSelectorRadioGroupSelectedItem = selectorRadioGroupSelectedItem
                characteristics.copy(
                    focusValue = null,
                    focusMode = FocusItem.INFINITE
                )
            }

            else -> {
                characteristics.copy()
            }
        }
    }
    val wbRadioGroupItems: ImmutableCollection<RadioButton>? =
        remember(characteristics.wbModeItems) {
            characteristics.wbModeItems?.let {
                ImmutableCollection(
                    characteristics.wbModeItems!!
                    /* listOf(
                         IconRadioItem(WbItem.AUTO, "Auto", R.drawable.wb_auto),
                         IconRadioItem(WbItem.INCANDESCENT, "Auto", R.drawable.wb_cloudy),
                         IconRadioItem(WbItem.FLUORESCENT, "Auto", R.drawable.wb_incandescent),
                         IconRadioItem(WbItem.WARM_FLUORESCENT, "Auto", R.drawable.wb_iridescent),
                         IconRadioItem(WbItem.DAYLIGHT, "day", R.drawable.wb_shade),
                         IconRadioItem(WbItem.CLOUDY_DAYLIGHT, "cloudy", R.drawable.wb_sunny),
                         IconRadioItem(WbItem.TWILIGHT, "twighlight", R.drawable.wb_twilight)
                     )*/
                )
            }

        }

    val focusRadioGroupItems: ImmutableCollection<RadioButton> =
        remember {

            //  characteristics.items.wbAutoItems as List<RadioButton>
            ImmutableCollection(
                listOf(
                    IconRadioItem(FocusItem.MACRO, "Auto", R.drawable.macro_auto),
                    IconRadioItem(FocusItem.CONTINUOUS, "Auto", R.drawable.continuous),
                    IconRadioItem(FocusItem.TOUCH, "Auto", R.drawable.touch),
                    IconRadioItem(FocusItem.INFINITE, "Auto", R.drawable.infinity),
                )
            )

        }
    val settingsRadioGroupItems: ImmutableCollection<RadioButton> =
        remember(characteristics, magnifierValue) {
            ImmutableCollection(
                listOf(
                    TextRadioItem(SettingsRadioGroupItem.SHUTTER, characteristics.shutterValue, "SHUTTER"),
                    TextRadioItem(SettingsRadioGroupItem.ISO, characteristics.isoValue, "ISO"),
                    TextRadioItem(SettingsRadioGroupItem.WB, characteristics.wbValue, "WB"),
                    TextRadioItem(SettingsRadioGroupItem.FOCUS, characteristics.focusValue, "FOCUS"),
                    TextRadioItem(SettingsRadioGroupItem.MAGNIFIER, magnifierValue, "MAGNIFIER")

                )
            )
        }
    var valueSelectorAcquiredItemIndex: Int? by remember {
        mutableStateOf(null)
    }
    var autoItems by remember {
        mutableStateOf(
            mutableMapOf(
                SettingsRadioGroupItem.SHUTTER to false,
                SettingsRadioGroupItem.ISO to false,
                SettingsRadioGroupItem.WB to false,
                SettingsRadioGroupItem.FOCUS to false,
                SettingsRadioGroupItem.MAGNIFIER to false
            )
        )
    }
    val isSelectorVisible = remember { mutableStateOf(true) }
    var isRadioGroupSelectorVisible by remember { mutableStateOf(false) }
    LaunchedEffect(autoItems, characteristics) {
        snapshotFlow {characteristics }
            .collect {
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == true) {
                    when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
                        SettingsRadioGroupItem.SHUTTER -> {
                            valueSelectorAcquiredItemIndex = characteristics.shutterPosition

                        }

                        SettingsRadioGroupItem.ISO -> {
                            valueSelectorAcquiredItemIndex = characteristics.isoPosition
                        }

                        SettingsRadioGroupItem.WB -> {
                        }

                        SettingsRadioGroupItem.FOCUS -> {

                        }

                        SettingsRadioGroupItem.MAGNIFIER -> {

                        }

                        null -> {}
                    }
                }
            }
    }
    var items by remember {
        mutableStateOf(characteristics)
    }
    LaunchedEffect(characteristics) {
        snapshotFlow { characteristics }
            .distinctUntilChanged()
            .collect { newValue ->
                items = newValue
            }
    }

    var selectorItems: ImmutableCollection<SelectorItem>? by remember {
        mutableStateOf(
            null//CharacteristicsUI().items.shutterItems
        )
    }
    var selectorRadioGroupItems: ImmutableCollection<RadioButton>? by remember {
        mutableStateOf(
            null//CharacteristicsUI().items.shutterItems
        )
    }

    LaunchedEffect(
        key1 = items,
        key2 = settingsRadioGroupSelectedSettingsRadioGroupItem
    ) {
        snapshotFlow { items }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                isRadioGroupSelectorVisible = false
                isSelectorVisible.value = true
                selectorItems = when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
                    SettingsRadioGroupItem.SHUTTER -> {
                        ImmutableCollection(items.shutterItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    SettingsRadioGroupItem.ISO -> {
                        /* items.isoItems?.let {

                             it?.list

                         }*/
                        ImmutableCollection(items.isoItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    SettingsRadioGroupItem.WB -> {
                        if (autoItems[SettingsRadioGroupItem.WB] == true) {
                            isRadioGroupSelectorVisible = true
                            isSelectorVisible.value = false
                        }
                        ImmutableCollection(items.wbManualItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    SettingsRadioGroupItem.FOCUS -> {
                        if (autoItems[SettingsRadioGroupItem.FOCUS] == true) {
                            isRadioGroupSelectorVisible = true
                            isSelectorVisible.value = false
                        }
                        ImmutableCollection(items.focusItems ?: emptyList())
                    }

                    SettingsRadioGroupItem.MAGNIFIER -> {
                        //  items.magnifierItems?.map { it.copy() }
                        /* items.magnifierItems?.let {
                             it?.list
                         }*/
                        ImmutableCollection(items.magnifierItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    null -> items.magnifierItems?.let {
                        //it?.list
                        ImmutableCollection(items.shutterItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                }
                selectorRadioGroupItems = when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
                    SettingsRadioGroupItem.SHUTTER -> {
                        null
                    }

                    SettingsRadioGroupItem.ISO -> {
                        null
                    }

                    SettingsRadioGroupItem.WB -> {
                        wbRadioGroupItems
                    }

                    SettingsRadioGroupItem.FOCUS -> {
                        focusRadioGroupItems
                    }

                    SettingsRadioGroupItem.MAGNIFIER -> {
                        null
                    }

                    null -> null

                }
            }
    }


    var touchPoint by remember {
        mutableStateOf(FloatArray(0))
    }
    LaunchedEffect(touchPoint) {
        settingsRequest = settingsRequest.copy(
            focusValue = if (autoItems[SettingsRadioGroupItem.FOCUS] == true) {
                "A"
            } else {
                characteristics.focusValue
            },
            touchPoint = touchPoint,
            //    focusMode = (focusSelectorRadioGroupSelectedItem as FocusItem)
        )
    }
    var selectorSelectedItemIndex by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(selectorSelectedItemIndex) {
        settingsRadioGroupSelectedSettingsRadioGroupItem?.let {
            autoItems = autoItems.toMutableMap().apply {
                compute(it) { _, value -> false }
            }
        }

        settingsRequest = when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
            SettingsRadioGroupItem.SHUTTER -> {
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == false) {
                    characteristics.copy(
                        shutterValue = characteristics.shutterItems?.list?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "" })
                } else {
                    characteristics.copy(shutterValue = "")
                }
                // settingsRequest.copy(shutterPosition = selectorSelectedItemIndex)
                /*text?.let {
                    settingsRequest.copy(shutterValue = it)
                } ?: run { settingsRequest.copy() }*/
            }

            SettingsRadioGroupItem.ISO -> {
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == false) {
                    characteristics.copy(
                        isoValue = characteristics.isoItems?.list?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "" })
                } else {
                    characteristics.copy(isoValue = "")
                }
                // settingsRequest.copy(isoPosition = selectorSelectedItemIndex)
                /*  selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                      settingsRequest.copy(isoValue = it)

                  } ?: run { settingsRequest.copy() }*/
            }

            SettingsRadioGroupItem.WB -> {
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == false) {
                    /* val tmp=characteristics.items.wbItems?.list?.get(
                         selectorSelectedItemIndex
                     )?.value?.toInt()*/
                    characteristics.copy(
                        wbValue = characteristics.wbManualItems?.list?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "0" })
                } else {
                    characteristics.copy(wbValue = "0")
                }
                /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                     settings.copy(wbValue = it)

                 } ?: run { settings.copy() }*/
            }

            SettingsRadioGroupItem.FOCUS -> {
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == false) {
                    characteristics.copy(
                        focusValue = characteristics.focusItems?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "0" })
                } else {
                    characteristics.copy(focusValue = "")
                }
                //  characteristics.settings.copy()
                /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                     settings.copy(focusValue = it)

                 } ?: run { settings.copy() }*/
            }

            SettingsRadioGroupItem.MAGNIFIER -> {
                /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {
                     magnifier = it
                     renderer.configureMagnifier(
                         it.toFloat(),
                         0.2f,
                         0.4f
                     )
                 } ?: run { settings.copy() }
                 magnifierPosition = selectorSelectedItemIndex*/
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == false) {
                    magnifierPosition = selectorSelectedItemIndex
                    magnifierValue =
                        characteristics.magnifierItems?.list?.get(selectorSelectedItemIndex)
                            ?.text ?: run { "" }
                    renderer.configureMagnifier(
                        characteristics.magnifierItems?.list?.get(selectorSelectedItemIndex)
                            ?.text?.toFloat() ?: run { 0f }
                    )
                } else {
                    valueSelectorAcquiredItemIndex = 0
                    magnifierValue = "1"
                    magnifierPosition = 0
                    renderer.configureMagnifier(
                        0f
                    )
                }

                characteristics.copy()

            }

            null -> characteristics.copy()

        }

    }
    var settingsRequestSkipCounter by remember { mutableIntStateOf(0) }
    LaunchedEffect(settingsRequest) {
        if (settingsRequestSkipCounter < 2) {
            settingsRequestSkipCounter++
            return@LaunchedEffect
        }
        onCharacteristicChanged(
             settingsRequest
        )
    }
    var surfaceViewSize by remember { mutableStateOf(IntSize.Zero) }
    val autoClick by remember(characteristics) {

        mutableStateOf(
            {
                settingsRadioGroupSelectedSettingsRadioGroupItem?.let {
                    autoItems = autoItems
                        .toMutableMap()
                        .apply {
                            compute(it) { _, value -> !(value ?: false) }
                        }
                }
                isRadioGroupSelectorVisible = false
                isSelectorVisible.value = true
                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == true) {
                    settingsRequest =
                        when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
                            SettingsRadioGroupItem.SHUTTER -> {

                                characteristics.copy(
                                    shutterValue = ""
                                )
                            }

                            SettingsRadioGroupItem.ISO -> {
                                characteristics.copy(
                                    isoValue = ""
                                )

                            }

                            SettingsRadioGroupItem.WB -> {
                                isRadioGroupSelectorVisible = true
                                isSelectorVisible.value = false
                                characteristics.copy(wbValue = "")

                            }

                            SettingsRadioGroupItem.FOCUS -> {
                                isRadioGroupSelectorVisible = true
                                isSelectorVisible.value = false
                                characteristics.copy(
                                    focusValue = ""
                                )

                            }

                            SettingsRadioGroupItem.MAGNIFIER -> {
                                val normalizedX = 0.0f
                                //  ((surfaceViewSize.width.toFloat()/2f / surfaceViewSize.width.toFloat()) * 2f - 1f)
                                val normalizedY = 0.0f
                                // -((surfaceViewSize.height.toFloat()/2f / surfaceViewSize.height.toFloat()) * 2f - 1f).toFloat()
                                renderer.handleTouchPress(
                                    normalizedX, normalizedY
                                )
                                valueSelectorAcquiredItemIndex = 0
                                magnifierValue = "1"
                                magnifierPosition = 0
                                renderer.configureMagnifier(
                                    1f
                                )
                                characteristics.copy(
                                    magnifierPosition = 0
                                )

                            }

                            null -> characteristics.copy()

                        }

                }
            }
        )


    }
    var isOpen by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ///////////preview
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
                    },
                //  .align(Alignment.Center),
                factory = {
                    AutoFitSurfaceView(
                        context,
                        null
                    ).apply {
                        // autoFitSurfaceView = it
                        // setFullscreen(true)
                        setFullscreen(characteristics.fullScreen)
                        // setAspectRatio(1280, 960)
                        setAspectRatio(
                            characteristics.aspectRatio?.width ?: 3,
                            characteristics.aspectRatio?.height ?: 2
                        )
                        setEGLContextClientVersion(3)
                        setRenderer(
                            renderer
                        )
                        viewTreeObserver.addOnGlobalLayoutListener {
                            surfaceViewSize = IntSize(width, height)
                            val normalizedX =
                                (surfaceViewSize.width.toFloat() / 2f / surfaceViewSize.width.toFloat()) * 2f - 1f
                            val normalizedY =
                                -((surfaceViewSize.height.toFloat() / 2f / surfaceViewSize.height.toFloat()) * 2f - 1f)
                            renderer.handleTouchPress(
                                normalizedX, normalizedY
                            )
                            renderer.configureMagnifier(
                                1f
                            )
                        }
                        setOnTouchListener { v, event ->
                            v.performClick()

                            val normalizedX =
                                (event.x / v.width.toFloat()) * 2f - 1f
                            val normalizedY =
                                -((event.y / v.height.toFloat()) * 2f - 1f)

                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    renderer.handleTouchPress(
                                        normalizedX, normalizedY
                                    )

                                }

                                MotionEvent.ACTION_MOVE -> {
                                    renderer.handleTouchDrag(
                                        normalizedX, normalizedY
                                    )
                                }

                                MotionEvent.ACTION_UP -> {
                                    touchPoint = floatArrayOf(
                                        event.x / v.width,
                                        event.y / v.height
                                    )
                                }
                            }
                            true
                        }
                    }
                }
            )
          /*  ShutterBox(
                isOpen = isOpen,
                onToggle = {
                    isOpen = !isOpen
                },
                modifier = Modifier
                    .padding(
                        top = if (settings.fullScreen) {
                            0.dp
                        } else {
                            84.dp
                        }
                    )
            ) {}*/

            /////////////

            //////////////


        }
        ////////////////radio group
        /*  Column(
              modifier = Modifier.fillMaxSize(),
          ) {*/
        RadioGroup(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                // .align(Alignment.CenterHorizontally)
                .padding(
                    top = 16.dp
                ),
            items = settingsRadioGroupItems,
            onOptionSelected = { value ->
                settingsRadioGroupSelectedSettingsRadioGroupItem = value as SettingsRadioGroupItem?
            }
        )
        /////////////////////////
        ////////////////////////resolution
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
            Modifier
                .padding(
                    start = 16.dp,
                    top = 98.dp
                )
                .align(Alignment.TopStart),
            characteristics.histogramData,
            150.dp,
            80.dp
        )
        //////////////////////////bottom buttons
        var isCheck by remember { mutableStateOf(false) }


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

                val mod by remember {
                    mutableStateOf(
                        Modifier
                            .size(32.dp)
                            .clickable {
                                settingsRadioGroupSelectedSettingsRadioGroupItem?.let {
                                    autoItems = autoItems
                                        .toMutableMap()
                                        .apply {
                                            compute(it) { _, value -> !(value ?: false) }
                                        }
                                }
                                isRadioGroupSelectorVisible = false
                                isSelectorVisible.value = true
                                if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == true) {


                                    settingsRequest =
                                        when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
                                            SettingsRadioGroupItem.SHUTTER -> {

                                                settingsRequest.copy(
                                                    shutterValue = ""
                                                )
                                            }

                                            SettingsRadioGroupItem.ISO -> {
                                                settingsRequest.copy(
                                                    isoValue = ""
                                                )

                                            }

                                            SettingsRadioGroupItem.WB -> {
                                                isRadioGroupSelectorVisible = true
                                                isSelectorVisible.value = false
                                                characteristics.copy(wbValue = "")

                                            }

                                            SettingsRadioGroupItem.FOCUS -> {
                                                isRadioGroupSelectorVisible = true
                                                isSelectorVisible.value = false
                                                characteristics.copy(
                                                    focusValue = ""
                                                )

                                            }

                                            SettingsRadioGroupItem.MAGNIFIER -> {

                                                characteristics.copy(
                                                    magnifierPosition = 0
                                                )

                                            }

                                            null -> characteristics.copy()

                                        }

                                }

                                //car=car.copy()

                            }
                    )
                }
                VectorShadow(
                    modifier = Modifier
                        .size(32.dp),
                    vectorColor = if (autoItems[settingsRadioGroupSelectedSettingsRadioGroupItem] == true) {
                        Color.Green
                    } else {
                        Color.White
                    },
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.auto,
                    onClick = autoClick

                )



                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .height(50.dp)
                ) {
                    /* AnimatedContent(
                         targetState = autoChecked,
                         transitionSpec = {
                             (scaleIn() + expandHorizontally()) togetherWith
                                     // Комбинируем анимации для исчезновения
                                     (scaleOut() + shrinkHorizontally())
                         }
                         /* enter = scaleIn() + expandHorizontally(),
                      exit = scaleOut() + shrinkHorizontally()*/
                     ) { isVisible ->
                         if (isVisible) {*/
                    ///////////value selector
                    if (isSelectorVisible.value) {
                        ValueSelector(
                            modifier = Modifier
                                .height(42.dp),
                            position = when (settingsRadioGroupSelectedSettingsRadioGroupItem) {
                                SettingsRadioGroupItem.SHUTTER -> {
                                    characteristics.shutterPosition
                                }

                                SettingsRadioGroupItem.ISO -> {
                                    characteristics.isoPosition
                                }

                                SettingsRadioGroupItem.WB -> {
                                    characteristics.wbPosition
                                }

                                SettingsRadioGroupItem.FOCUS -> {
                                    characteristics.focusPosition
                                }

                                SettingsRadioGroupItem.MAGNIFIER -> {
                                    magnifierPosition
                                }

                                null -> 0

                            },
                            items = selectorItems,
                            // adapter = adapter,
                            onSelectedItemChanged = { index, manual ->
                                if (manual) {
                                    selectorSelectedItemIndex = index
                                }
                                /*  selectorItems?.let {
                                      for (i in it.list.indices) {
                                          it.list[i].passed = i <= index
                                      }
                                  }*/


                            },
                            updatedPosition = valueSelectorAcquiredItemIndex,
                            onPositionUpdated = { valueSelectorAcquiredItemIndex = null }
                        )
                    }
                    //selector radio group
                    if (isRadioGroupSelectorVisible) {
                        RadioGroup(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth(),
                            items = selectorRadioGroupItems, //wbRadioGroupItems,
                            selectedOption = when (selectorRadioGroupItems?.list?.firstOrNull()?.id) {
                                is WbItem -> wbSelectorRadioGroupSelectedItem
                                is FocusItem -> focusSelectorRadioGroupSelectedItem
                                else -> null // Не присваиваем значение, если тип неизвестен
                            }, //selectorRadioGroupSelectedItem,// wbRadioGroupSelectedSettingsItem as Item,
                            onOptionSelected = { value ->
                                /*  if (value?.javaClass == WbItem::class.java) {
                                      wbSelectorRadioGroupSelectedItem = value
                                  }
                                  if (value?.javaClass == FocusItem::class.java) {
                                      focusSelectorRadioGroupSelectedItem = value
                                  }*/
                                selectorRadioGroupSelectedItem = value as SelectorRadioGroupItem
                                /*  settingsRequest=characteristics.settings.copy(
                                      wbValue = "",
                                      wbAutoMode = value?.ordinal
                                      )*/

                            }
                        )
                    }

                    //  }
                    // }
                }
            }

            //////////////////////////capture
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
                        isOpen = !isOpen
                        startVideoRecord(isCheck)
                    }

                )
                /* Button(

                     border = BorderStroke(5.dp, Color.Green),
                     shape = CircleShape,
                     modifier = Modifier
                         .size(96.dp)
                         // .align(Alignment.BottomCenter)
                         .toggleable(
                             value = isCheck,
                             onValueChange = {
                                 isCheck = it
                                 onStartVideoRecord(isCheck)
                             },
                             role = Role.Checkbox,
                         ),
                     onClick = {
                         isCheck = !isCheck
                         onStartVideoRecord(isCheck)
                     },
                     colors = if (isCheck) {
                         ButtonDefaults.buttonColors(containerColor = Color.Red)
                     } else {
                         ButtonDefaults.buttonColors(containerColor = Color.White)
                     }

                 ) {
                     // Text(text = "Capture", fontSize = 40.sp)
                 }*/
            }


        }

    }
}


