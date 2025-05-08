package com.yes.camera.presentation.ui.views


import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.IconItem
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.SettingsItem
import com.yes.camera.presentation.model.SelectorItem
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
import com.yes.camera.presentation.ui.custom.compose.VectorShadow
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import kotlinx.coroutines.flow.distinctUntilChanged


/*@Composable
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
        shutterItems = standardShutterSpeeds
            .toSortedMap(compareByDescending { it })
            .map {
                SettingsItemUI(it.value)
            },
        isoItems = standardIsoValues.map {
            SettingsItemUI(it.toString())
        },
        focusItems = listOf(
            SettingsItemUI("0.2"),
            SettingsItemUI("1"),
            SettingsItemUI("2"),
            SettingsItemUI("3"),
            SettingsItemUI("4"),
            SettingsItemUI("5"),
            SettingsItemUI("6"),
            SettingsItemUI("7"),
            SettingsItemUI("8"),
            SettingsItemUI("9"),
            SettingsItemUI("9.5"),
            SettingsItemUI("10"),
            SettingsItemUI("11"),
            SettingsItemUI("12"),
            SettingsItemUI("13"),
            SettingsItemUI("14"),
            SettingsItemUI("15"),

            ),
        magnifierItems = listOf(
            SettingsItemUI("1"),
            SettingsItemUI("2"),
            SettingsItemUI("3"),
            SettingsItemUI("4"),
            SettingsItemUI("5"),
            SettingsItemUI("6"),
            SettingsItemUI("7"),
            SettingsItemUI("8"),
            SettingsItemUI("9"),
            SettingsItemUI("10"),
        )
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
        characteristics = characteristics,
        onSettingsClick = {},
        onStartVideoRecord = {},
        onCharacteristicChanged = {},
        histogram = mutableMapOf(),
        fullscreen = false
    )
}*/
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
    characteristics: CharacteristicsUI,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onCharacteristicChanged: (characteristics: CharacteristicsUI) -> Unit,
    histogram: MutableMap<Int, Int>?,
    fullscreen: Boolean
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

    var magnifier by remember {
        mutableStateOf("")
    }
    var magnifierPosition by remember {
        mutableStateOf(0)
    }
    var settingsRadioGroupSelectedSettingsItem: SettingsItem? by remember {
        mutableStateOf(SettingsItem.SHUTTER)
    }
    /*var valueSelectorVisibility by remember {
        mutableStateOf(true)
    }*/
    var settings = characteristics.settings
    LaunchedEffect(characteristics.settings) {
        snapshotFlow { characteristics.settings }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                settings = newValue
            }
    }
    var wb = remember {
        mutableStateOf(2131099791)
    }
    val settingsItem by remember {
        mutableStateOf(listOf(IconRadioItem(SettingsItem.WB, "WB", 2131099791)))
    }
    // val radioGroupItems= listOf(VectorRadioItem(Item.WB, "WB", wb.value))
    var wbRadioGroupSelectedSettingsItem: WbItem? by remember {
        mutableStateOf(WbItem.AUTO)
    }
    val wbRadioGroupItems: ImmutableCollection<RadioButton> =
        remember(characteristics.items.wbAutoItems) {
            ImmutableCollection(
                characteristics.items.wbAutoItems as List<RadioButton>
               /* listOf(
                    IconRadioItem(WbItem.AUTO, "Auto", R.drawable.wb_auto),
                    IconRadioItem(WbItem.INCANDESCENT, "Auto", R.drawable.wb_cloudy),
                    IconRadioItem(WbItem.FLUORESCENT, "Auto", R.drawable.wb_incandescent),
                    IconRadioItem(WbItem.WARM_FLUORESCENT, "Auto", R.drawable.wb_iridescent),
                    IconRadioItem(WbItem.DAYLIGHT, "Auto", R.drawable.wb_shade),
                    IconRadioItem(WbItem.CLOUDY_DAYLIGHT, "Auto", R.drawable.wb_sunny),
                    IconRadioItem(WbItem.TWILIGHT, "Auto", R.drawable.wb_twilight)
                )*/
            )
        }
    val settingsRadioGroupItems: ImmutableCollection<RadioButton> = remember(settings) {
        // emptyList<TextRadioItem>()
        ImmutableCollection(
            listOf(
                //  item
                TextRadioItem(SettingsItem.SHUTTER, settings.shutterValue, "SHUTTER"),
                TextRadioItem(SettingsItem.ISO, settings.isoValue, "ISO"),
                TextRadioItem(SettingsItem.WB, settings.wbValue, "WB"),
                // VectorRadioItem(Item.WB, "WB", wb.value),
                TextRadioItem(SettingsItem.FOCUS, settings.focusValue, "FOCUS"),
                TextRadioItem(SettingsItem.MAGNIFIER, settings.magnifierValue, "MAGNIFIER")

            )
        )
    }
    var valueSelectorAcquiredItemIndex: Int? by remember {
        mutableStateOf(null)
    }
    var autoItems by remember {
        mutableStateOf(
            mutableMapOf(
                SettingsItem.SHUTTER to false,
                SettingsItem.ISO to false,
                SettingsItem.WB to false,
                SettingsItem.FOCUS to false,
                SettingsItem.MAGNIFIER to false
            )
        )
    }
    val isSelectorVisible = remember { mutableStateOf(true) }
    val isWbSelectorVisible = remember { mutableStateOf(false) }
    LaunchedEffect(autoItems) {
        snapshotFlow { settings }
            .collect {
                if (autoItems[settingsRadioGroupSelectedSettingsItem] == true) {
                    when (settingsRadioGroupSelectedSettingsItem) {
                        SettingsItem.SHUTTER -> {
                            //  valueSelectorAcquiredItemIndex = settings.shutterPosition

                        }

                        SettingsItem.ISO -> {
                            //  valueSelectorAcquiredItemIndex = settings.isoPosition
                        }

                        SettingsItem.WB -> {
                        }

                        SettingsItem.FOCUS -> {

                        }

                        SettingsItem.MAGNIFIER -> {

                        }

                        null -> {}
                    }
                }
            }
    }
    var items by remember {
        mutableStateOf(characteristics.items)
    }
    LaunchedEffect(characteristics.items) {
        snapshotFlow { characteristics.items }
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
    LaunchedEffect(
        key1 = items,
        key2 = settingsRadioGroupSelectedSettingsItem
    ) {
        snapshotFlow { items }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                isWbSelectorVisible.value = false
                isSelectorVisible.value = true
                selectorItems = when (settingsRadioGroupSelectedSettingsItem) {
                    SettingsItem.SHUTTER -> {

                        //    items.shutterItems


                        ImmutableCollection(items.shutterItems?.list?.map { it as SelectorItem }
                            ?: emptyList())

                        //map { it.copy() }
                    }

                    SettingsItem.ISO -> {
                        /* items.isoItems?.let {

                             it?.list

                         }*/
                        ImmutableCollection(items.isoItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    SettingsItem.WB -> {
                        //  items.wbItems?.map { it.copy() }
                        /* items.wbItems?.let {
                             it?.list
                         }*/
                        if (autoItems[SettingsItem.WB] == true) {
                            isWbSelectorVisible.value = true
                            isSelectorVisible.value = false
                        }
                        ImmutableCollection(items.wbManualItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    SettingsItem.FOCUS -> {
                        //  items.focusItems?.map { it.copy() }
                        /* items.focusItems?.let {
                             it?.list
                         }*/
                        ImmutableCollection(items.focusItems?.list?.map { it as SelectorItem }
                            ?: emptyList())
                    }

                    SettingsItem.MAGNIFIER -> {
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
            }
    }


    var settingsRequest by remember {
        mutableStateOf(settings)
    }

    LaunchedEffect(settingsRequest) {
        onCharacteristicChanged(
            characteristics.copy(
                settings = settingsRequest
            )

        )
    }
    var touchPoint by remember {
        mutableStateOf(FloatArray(0))
    }
    LaunchedEffect(touchPoint) {
        settingsRequest=settings.copy(
            touchPoint = touchPoint
        )
    }
    var selectorSelectedItemIndex by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(selectorSelectedItemIndex) {
        settingsRadioGroupSelectedSettingsItem?.let {
            autoItems = autoItems.toMutableMap().apply {
                compute(it) { _, value -> false }
            }
        }

        settingsRequest = when (settingsRadioGroupSelectedSettingsItem) {
            SettingsItem.SHUTTER -> {
                if (autoItems[settingsRadioGroupSelectedSettingsItem] == false) {
                    characteristics.settings.copy(
                        shutterValue = characteristics.items.shutterItems?.list?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "" })
                } else {
                    characteristics.settings.copy(shutterValue = "")
                }
                // settingsRequest.copy(shutterPosition = selectorSelectedItemIndex)
                /*text?.let {
                    settingsRequest.copy(shutterValue = it)
                } ?: run { settingsRequest.copy() }*/
            }

            SettingsItem.ISO -> {
                if (autoItems[settingsRadioGroupSelectedSettingsItem] == false) {
                    characteristics.settings.copy(
                        isoValue = characteristics.items.isoItems?.list?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "" })
                } else {
                    characteristics.settings.copy(isoValue = "")
                }
                // settingsRequest.copy(isoPosition = selectorSelectedItemIndex)
                /*  selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                      settingsRequest.copy(isoValue = it)

                  } ?: run { settingsRequest.copy() }*/
            }

            SettingsItem.WB -> {
                if (autoItems[settingsRadioGroupSelectedSettingsItem] == false) {
                    /* val tmp=characteristics.items.wbItems?.list?.get(
                         selectorSelectedItemIndex
                     )?.value?.toInt()*/
                    characteristics.settings.copy(
                        wbValue = characteristics.items.wbManualItems?.list?.get(
                            selectorSelectedItemIndex
                        )?.text ?: run { "0" })
                } else {
                    characteristics.settings.copy(wbValue = "0")
                }
                /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                     settings.copy(wbValue = it)

                 } ?: run { settings.copy() }*/
            }

            SettingsItem.FOCUS -> {
                characteristics.settings.copy()
                /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                     settings.copy(focusValue = it)

                 } ?: run { settings.copy() }*/
            }

            SettingsItem.MAGNIFIER -> {
                /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {
                     magnifier = it
                     renderer.configureMagnifier(
                         it.toFloat(),
                         0.2f,
                         0.4f
                     )
                 } ?: run { settings.copy() }
                 magnifierPosition = selectorSelectedItemIndex*/
                characteristics.settings.copy()

            }

            null -> characteristics.settings.copy()

        }

    }

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
                        top = if (fullscreen) {
                            0.dp
                        } else {
                            84.dp
                        }
                    ),
                //  .align(Alignment.Center),
                factory = {
                    AutoFitSurfaceView(
                        context,
                        null
                    ).apply {
                        // autoFitSurfaceView = it
                        // setFullscreen(true)
                        setFullscreen(fullscreen)
                        setAspectRatio(1280, 960)
                        setEGLContextClientVersion(3)
                        setRenderer(
                            renderer
                        )
                        setOnTouchListener { v, event ->
                            v.performClick()

                            val normalizedX =
                                (event.x / v.width.toFloat()) * 2 - 1
                            val normalizedY =
                                -((event.y / v.height.toFloat()) * 2 - 1)

                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    // it.queueEvent {

                                    //   it.setAspectRatio(3, 2)
                                    renderer.handleTouchPress(
                                        normalizedX, normalizedY
                                    )

                                }

                                MotionEvent.ACTION_MOVE -> {
                                    //   it.queueEvent {
                                    renderer.handleTouchDrag(
                                        normalizedX, normalizedY
                                    )
                                }

                                MotionEvent.ACTION_UP -> {
                                    val t = floatArrayOf(event.x / v.width, event.y / v.height)
                                    val x = event.x
                                    val y = event.y
                                    val w = v.width
                                    val h = v.height
                                    touchPoint=floatArrayOf(
                                        event.x / v.width,
                                        event.y / v.height
                                    )
                                   /* settingsRequest=settingsRequest.copy(
                                        touchPoint = floatArrayOf(
                                            event.x / v.width,
                                            event.y / v.height
                                        )
                                    )*/
                                  /*  onCharacteristicChanged(
                                        characteristics.copy(
                                            settings = settings.copy(
                                                touchPoint = floatArrayOf(
                                                    event.x / v.width,
                                                    event.y / v.height
                                                )
                                            )
                                        )
                                    )*/

                                }

                            }
                            //   }

                            true

                        }
                    }
                }
            )
            ////////////////////////histogram
            Histogram(
                Modifier

                    .padding(
                        start = 16.dp,
                        bottom = if (fullscreen) {
                            200.dp
                        } else {
                            16.dp
                        }
                    )
                    .align(Alignment.BottomStart),
                histogram,
                150.dp,
                80.dp
            )
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
                settingsRadioGroupSelectedSettingsItem = value as SettingsItem?

                /*  value?.let { valueSelectorVisibility = true }
                      ?: run { valueSelectorVisibility = false }*/
                /* value?.let {
                     isOpen = true


                     when(it){
                         Item.SHUTTER->{
                             valueSelectorItems=characteristics.shutterValues
                             position.value=1
                         }

                         Item.ISO->{
                             valueSelectorItems=characteristics.isoValues
                             position.value=3
                         }

                         Item.FOCUS->{}
                     }
                 } ?: run {
                     isOpen = false
                 }*/
                //  radioGroupItems[0].resId = R.drawable.iso
            }
        )


        /*   DropDown(
               isOpen,
               modifier = Modifier
                   .fillMaxWidth()
                   .wrapContentHeight()
               //   .wrapContentHeight()
           ) {
               ValueSelector(
                   items = valueSelectorItems,
                   adapter = adapter,
                   onSelectedItemChanged = { index ->
                       valueSelectorItems?.let {
                           for (i in it.indices) {
                               it[i].passed = i <= index
                           }
                       }



                   }
               )
           }*/
        /////////////////////magnifier


        //   }

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
                VectorShadow(
                    modifier = Modifier

                        .size(32.dp)
                        .clickable {
                            settingsRadioGroupSelectedSettingsItem?.let {
                                autoItems = autoItems
                                    .toMutableMap()
                                    .apply {
                                        compute(it) { _, value -> !(value ?: false) }
                                    }
                            }
                            isWbSelectorVisible.value = false
                            isSelectorVisible.value = true
                            if (autoItems[settingsRadioGroupSelectedSettingsItem] == true) {


                                settingsRequest = when (settingsRadioGroupSelectedSettingsItem) {
                                    SettingsItem.SHUTTER -> {

                                        settingsRequest.copy(
                                            shutterValue = ""
                                        )
                                    }

                                    SettingsItem.ISO -> {
                                        settingsRequest.copy(
                                            isoValue = ""
                                        )

                                    }

                                    SettingsItem.WB -> {
                                        isWbSelectorVisible.value = true
                                        isSelectorVisible.value = false
                                        settings.copy(wbValue = "")

                                    }

                                    SettingsItem.FOCUS -> {

                                        settings.copy(
                                            focusValue = ""
                                        )

                                    }

                                    SettingsItem.MAGNIFIER -> {

                                        settings.copy(
                                            magnifierPosition = 0
                                        )

                                    }

                                    null -> settings.copy()

                                }

                            }

                            //car=car.copy()

                        },
                    vectorColor = if (autoItems[settingsRadioGroupSelectedSettingsItem] == true) {
                        Color.Green
                    } else {
                        Color.White
                    },
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.auto,
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
                            position = when (settingsRadioGroupSelectedSettingsItem) {
                                SettingsItem.SHUTTER -> {
                                    settings.shutterPosition
                                }

                                SettingsItem.ISO -> {
                                    settings.isoPosition
                                }

                                SettingsItem.WB -> {
                                    settings.wbPosition
                                }

                                SettingsItem.FOCUS -> {
                                    settings.focusPosition
                                }

                                SettingsItem.MAGNIFIER -> {
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
                            updatedPosition = valueSelectorAcquiredItemIndex
                        )
                    }
                    //wb radio group
                    if (isWbSelectorVisible.value) {
                        RadioGroup(
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                            //  .fillMaxHeight()
                            // .height(42.dp)
                            // .align(Alignment.CenterHorizontally)
                            /* .padding(
                                 top = 4.dp
                             )*/,
                            items = wbRadioGroupItems,
                            selectedOption = wbRadioGroupSelectedSettingsItem as Item,
                            onOptionSelected = { value ->
                                 wbRadioGroupSelectedSettingsItem = value as WbItem?
                                settingsRequest=characteristics.settings.copy(
                                    wbValue = "",
                                    wbAutoMode = value?.ordinal
                                    )
                                /*  value?.let { valueSelectorVisibility = true }
                                      ?: run { valueSelectorVisibility = false }*/
                                /* value?.let {
                                     isOpen = true


                                     when(it){
                                         Item.SHUTTER->{
                                             valueSelectorItems=characteristics.shutterValues
                                             position.value=1
                                         }

                                         Item.ISO->{
                                             valueSelectorItems=characteristics.isoValues
                                             position.value=3
                                         }

                                         Item.FOCUS->{}
                                     }
                                 } ?: run {
                                     isOpen = false
                                 }*/
                                //  radioGroupItems[0].resId = R.drawable.iso
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
                        .clickable {
                            onSettingsClick()
                        },
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.settings,
                )

                //////////////////camera flip
                VectorShadow(
                    Modifier
                        // .padding(24.dp)
                        .size(32.dp)
                        .clickable {

                        },
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.flip_camera_android,
                )
                /////capture
                Button(

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
                }
            }


        }

    }
}


