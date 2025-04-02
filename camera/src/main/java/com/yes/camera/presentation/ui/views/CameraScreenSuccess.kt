package com.yes.camera.presentation.ui.views

import android.content.Context
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.Settings
import com.yes.camera.presentation.model.SettingsItemUI
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.SelectorItemAdapterDelegate
import com.yes.camera.presentation.ui.custom.compose.Histogram
import com.yes.camera.presentation.ui.custom.compose.RadioGroup
import com.yes.camera.presentation.ui.custom.compose.TextRadioItem

import com.yes.camera.presentation.ui.custom.compose.ValueSelector
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
    val adapter = CompositeAdapter(
        mapOf(
            SettingsItemUI::class.java to SelectorItemAdapterDelegate(),
        )
    )
    val context = LocalContext.current

    // Получаем WindowManager
    val rWidth = 4096;
    val rHeight = 3072
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    val screenWidth1 = displayMetrics.heightPixels.dp//(920*100/3072)
    val screenHeight1 = displayMetrics.heightPixels.dp//(1230*100/4096)

    val configuration = LocalConfiguration.current
    val screenWidth: Dp = configuration.screenWidthDp.dp
    val screenHeight: Dp = configuration.screenHeightDp.dp

    val screenWidth2 = configuration.screenWidthDp
    val screenHeight2 = configuration.screenHeightDp
    /////////////////////

    var magnifier by remember {
        mutableStateOf("")
    }
    var magnifierPosition by remember {
        mutableStateOf(0)
    }
    var settings by remember {
        mutableStateOf(characteristics.settings)
    }
    LaunchedEffect(characteristics.settings) {
        snapshotFlow { characteristics.settings }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                settings = newValue
            }
    }

    var radioGroupSelectedItem: Item? by remember {
        mutableStateOf(Item.SHUTTER)
    }
    var valueSelectorVisibility by remember {
        mutableStateOf(true)
    }


    var radioGroupItems =
        listOf(
            TextRadioItem(Item.SHUTTER, settings.shutterValue, "SHUTTER"),
            TextRadioItem(Item.ISO, settings.isoValue, "ISO"),
            TextRadioItem(Item.WB, settings.wbValue, "WB"),
            TextRadioItem(Item.FOCUS, settings.focusValue, "FOCUS"),
            TextRadioItem(Item.MAGNIFIER, settings.magnifierValue, "MAGNIFIER")
        )
    var autoChecked by remember { mutableStateOf(false) }

    var valueSelectorAquiredItemIndex: Int? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(autoChecked) {
        if (autoChecked) {
            when (radioGroupSelectedItem) {
                Item.SHUTTER -> {
                    valueSelectorAquiredItemIndex = settings.shutterPosition

                }

                Item.ISO -> {
                    valueSelectorAquiredItemIndex = settings.isoPosition
                }

                Item.WB -> {

                }

                Item.FOCUS -> {

                }

                Item.MAGNIFIER -> {

                }

                null -> {}

            }
        }
    }
    var items by remember {
        mutableStateOf(characteristics.items)
    }
    LaunchedEffect(characteristics.items) {
        snapshotFlow { characteristics.items }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                val t =items
                items = newValue
            }
    }

    var selectorItems by remember{
        mutableStateOf(
            CharacteristicsUI().items.shutterItems
        )
    }
    LaunchedEffect(
        key1 = items,
        key2 = radioGroupSelectedItem) {
        snapshotFlow { items }
            .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
            .collect { newValue ->
                selectorItems=    when (radioGroupSelectedItem) {
                    Item.SHUTTER -> {
                        val t=items.shutterItems
                        items.shutterItems?.map { it.copy() }
                    }

                    Item.ISO -> {
                        items.isoItems

                    }

                    Item.WB -> {
                        items.wbItems

                    }

                    Item.FOCUS -> {
                        items.focusItems

                    }

                    Item.MAGNIFIER -> {
                        items.magnifierItems

                    }

                    null -> items.magnifierItems

                }
            }
    }
    var valueSelectorSelectedItemIndex by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(valueSelectorSelectedItemIndex) {
        autoChecked = false
        val params = when (radioGroupSelectedItem) {
            Item.SHUTTER -> {
                selectorItems?.get(valueSelectorSelectedItemIndex)?.text?.let {

                    settings.copy(shutterValue = it)

                } ?: run { settings.copy() }

            }

            Item.ISO -> {
                selectorItems?.get(valueSelectorSelectedItemIndex)?.text?.let {

                    settings.copy(isoValue = it)

                } ?: run { settings.copy() }
            }

            Item.WB -> {
                selectorItems?.get(valueSelectorSelectedItemIndex)?.text?.let {

                    settings.copy(wbValue = it)

                } ?: run { settings.copy() }
            }

            Item.FOCUS -> {
                selectorItems?.get(valueSelectorSelectedItemIndex)?.text?.let {

                    settings.copy(focusValue = it)

                } ?: run { settings.copy() }
            }

            Item.MAGNIFIER -> {
                selectorItems?.get(valueSelectorSelectedItemIndex)?.text?.let {
                    magnifier = it
                    renderer.configureMagnifier(
                        it.toFloat(),
                        0.2f,
                        0.4f
                    )
                } ?: run { settings.copy() }
                magnifierPosition = valueSelectorSelectedItemIndex
                settings.copy()

            }

            null -> settings.copy()

        }
        onCharacteristicChanged(
            characteristics.copy(
                settings = params
            )

        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
        /*   modifier = Modifier
               .height(960.dp)
               .width(1280.dp)*/

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
                                    onCharacteristicChanged(
                                        characteristics.copy(
                                            settings = settings.copy(
                                                touchPoint = floatArrayOf(
                                                    event.x / v.width,
                                                    event.y / v.height
                                                )
                                            )
                                        )
                                    )

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
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {

            var visibleRadioGroup by remember {
                mutableStateOf(true)
            }

            AnimatedVisibility(
                visible = visibleRadioGroup,
                enter = scaleIn() + expandHorizontally(),
                exit = scaleOut() + shrinkHorizontally()
            ) {
                RadioGroup(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(
                            top = 16.dp
                        ),
                    items = radioGroupItems,
                    onOptionSelected = { value ->
                        radioGroupSelectedItem = value

                        value?.let { valueSelectorVisibility = true }
                            ?: run { valueSelectorVisibility = false }
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


        }

        //////////////////////////selector
        var isCheck by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            AnimatedContent(
                targetState = valueSelectorVisibility,
                transitionSpec = {
                    (scaleIn() + expandHorizontally()) togetherWith
                            // Комбинируем анимации для исчезновения
                            (scaleOut() + shrinkHorizontally())
                }
                /* enter = scaleIn() + expandHorizontally(),
                 exit = scaleOut() + shrinkHorizontally()*/
            ) { isVisible ->
                if (isVisible) {
                    Row {
                        ///////////auto
                        VectorShadow(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    autoChecked = !autoChecked
                                    if (autoChecked) {
                                        val params = when (radioGroupSelectedItem) {
                                            Item.SHUTTER -> {

                                                settings.copy(
                                                    shutterValue = ""
                                                )

                                            }

                                            Item.ISO -> {

                                                settings.copy(
                                                    isoValue = ""
                                                )

                                            }

                                            Item.WB -> {

                                                settings.copy(wbValue = "")

                                            }

                                            Item.FOCUS -> {

                                                settings.copy(
                                                    focusValue = ""
                                                )

                                            }

                                            Item.MAGNIFIER -> {

                                                settings.copy(
                                                    magnifierPosition = 0
                                                )

                                            }

                                            null -> settings.copy()

                                        }
                                        onCharacteristicChanged(
                                            CharacteristicsUI(settings = params)
                                        )
                                    }

                                    //car=car.copy()

                                },
                            vectorColor = if (autoChecked) {
                                Color.Green
                            } else {
                                Color.White
                            },
                            shadowColor = Color.DarkGray,
                            resId = R.drawable.auto,
                        )

                        ///////////value selector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
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
                            ValueSelector(
                                position = when (radioGroupSelectedItem) {
                                    Item.SHUTTER -> {
                                        settings.shutterPosition
                                    }

                                    Item.ISO -> {
                                        settings.isoPosition
                                    }

                                    Item.WB -> {
                                        settings.wbPosition
                                    }

                                    Item.FOCUS -> {
                                        settings.focusPosition
                                    }

                                    Item.MAGNIFIER -> {
                                        magnifierPosition
                                    }

                                    null -> 0

                                },
                                items = selectorItems,
                                adapter = adapter,
                                onSelectedItemChanged = { index ,manual->
                                    if (manual){
                                        valueSelectorSelectedItemIndex = index
                                    }
                                    selectorItems?.let {
                                        for (i in it.indices) {
                                            val a=items.shutterItems
                                            val c=selectorItems
                                            it[i].passed = i <= index
                                            val b=items.shutterItems
                                        }
                                    }


                                },
                                updatedPosition = valueSelectorAquiredItemIndex
                            )
                            //  }
                            // }
                        }
                    }
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
                        ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    }

                ) {
                    // Text(text = "Capture", fontSize = 40.sp)
                }
            }


        }

    }
}