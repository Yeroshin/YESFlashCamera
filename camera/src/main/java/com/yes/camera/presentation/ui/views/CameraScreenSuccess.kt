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
import androidx.compose.runtime.getValue
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
}

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

    /*var characteristics by remember(key1 = characteristicsInitial) {
        mutableStateOf(characteristicsInitial)
    }*/

    /*  LaunchedEffect(characteristicsInitial) {
          snapshotFlow { characteristicsInitial }
              .collect {ch->
                  characteristics=ch
                  shutter.value=characteristics.characteristics[Item.SHUTTER]?.value.toString()
              }
      }*/

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
////////////////tmp
    /* var characteristicsInitial by remember( characteristics) {
         mutableStateOf(characteristics)
     }*/
    /////////////////////
    var radioGroupSelectedItem: Item? by remember {
        mutableStateOf(Item.SHUTTER)
    }
    var valueSelectorVisibility by remember {
        mutableStateOf(true)
    }
    var selectorItems: List<SettingsItemUI>? by remember {
        mutableStateOf(
            null
        )
    }
  /* LaunchedEffect(radioGroupSelectedItem) {
        snapshotFlow { characteristicsInitial }
            .collect {ch->
                characteristics=ch
                shutter.value=characteristics.characteristics[Item.SHUTTER]?.value.toString()
            }
    }*/
    var car by remember(characteristics) {
        mutableStateOf(characteristics)
    }
    var shutter by remember(car) {
        mutableStateOf(car.shutterValue)
    }

    var iso by remember(characteristics) {
        mutableStateOf(characteristics.isoValue)
    }
    var wb = remember {
        mutableStateOf("-")
    }
    var focus = remember {
        mutableStateOf("-")
    }
    var magnifier = remember {
        mutableStateOf("-")
    }
    var radioGroupItems =
        listOf(
            TextRadioItem(Item.SHUTTER, shutter, "SHUTTER"),
            TextRadioItem(Item.ISO, iso, "ISO"),
            TextRadioItem(Item.WB, wb.value, "WB"),
            TextRadioItem(Item.FOCUS, focus.value, "FOCUS"),
            TextRadioItem(Item.MAGNIFIER, magnifier.value, "MAGNIFIER")
        )

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
                                            touchPoint = floatArrayOf(
                                                event.x / v.width,
                                                event.y / v.height
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
            /*  LaunchedEffect(key1 = Unit, block = {
                 delay(1000L)
                 //visible = true
             })*/

            var visibleRadioGroup by remember {
                mutableStateOf(true)
            }

            var valueRadioGroup by remember {
                mutableStateOf("")
            }

            /* LaunchedEffect( key1 = selectedItem.value) {
                 /* positionRadioGroup = characteristics.shutterValue
                  selectorItems = characteristics.shutterItems
                  selectedItem.value =Item.SHUTTER*/

                 selectorItems = when (selectedItem.value) {
                     Item.SHUTTER -> {
                         car.shutterItems
                     }

                     Item.ISO -> {
                         characteristics.isoItems
                     }

                     Item.WB -> {
                         characteristics.wbItems
                     }

                     Item.FOCUS -> {
                         characteristics.focusItems
                     }

                     Item.MAGNIFIER -> {
                         characteristics.magnifierItems
                     }

                     null -> emptyList()

                 }
             }*/


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
                        // characteristics.characteristics[value]

                        when (value) {
                            Item.SHUTTER -> {
                                selectorItems = characteristics.shutterItems
                                characteristics.shutterValue
                            }

                            Item.ISO -> {
                                selectorItems = characteristics.isoItems
                                characteristics.isoValue
                            }

                            Item.WB -> {
                                selectorItems = characteristics.wbItems
                                characteristics.wbValue
                            }

                            Item.FOCUS -> {
                                selectorItems = characteristics.focusItems
                                characteristics.focusValue
                            }

                            Item.MAGNIFIER -> {
                                selectorItems = characteristics.magnifierItems
                                characteristics.magnifierValue
                            }

                            null -> ""

                        }
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


        var isCheck by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //////////////////////////selector
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
                        var autoChecked by remember { mutableStateOf(false) }

                        VectorShadow(
                            Modifier
                                .size(32.dp)
                                .clickable {
                                    autoChecked = !autoChecked
                                    if (autoChecked){
                                        val params = when (radioGroupSelectedItem) {
                                            Item.SHUTTER -> {
                                                car.copy(
                                                    shutterValue = ""
                                                )
                                            }

                                            Item.ISO -> {
                                                car.copy(
                                                    isoValue = ""
                                                )
                                            }

                                            Item.WB -> {
                                                characteristics.copy(
                                                    wbValue = wb.value
                                                )
                                            }

                                            Item.FOCUS -> {
                                                characteristics.copy(
                                                    focusValue = focus.value
                                                )
                                            }

                                            Item.MAGNIFIER -> {
                                                characteristics.copy(
                                                    magnifierPosition = 0,
                                                )
                                            }

                                            null -> characteristics.copy()

                                        }
                                        onCharacteristicChanged(
                                            params
                                        )
                                    }

                                    //car=car.copy()

                                },
                            vectorColor = if (autoChecked) {
                                Color.White
                            } else {
                                Color.Green
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
                                        car.shutterPosition
                                    }

                                    Item.ISO -> {
                                        car.isoPosition
                                    }

                                    Item.WB -> {
                                        characteristics.wbPosition
                                    }

                                    Item.FOCUS -> {
                                        characteristics.focusPosition
                                    }

                                    Item.MAGNIFIER -> {
                                        characteristics.magnifierPosition
                                    }

                                    null -> 0

                                },
                                items = selectorItems,
                                adapter = adapter,
                                onSelectedItemChanged = { index ->

                                    val params = when (radioGroupSelectedItem) {
                                        Item.SHUTTER -> {
                                            selectorItems?.get(index)?.text?.let {
                                                car.copy(
                                                   // shutterPosition = index,
                                                    shutterValue = it
                                                )
                                            } ?: run { car.copy() }

                                        }

                                        Item.ISO -> {
                                            selectorItems?.get(index)?.text?.let {
                                                car.copy(
                                                   // isoPosition = index,
                                                    isoValue = it
                                                )
                                            } ?: run { car.copy() }
                                            /* characteristics.characteristics[Item.ISO]?.copy(
                                             value = index
                                         )*/

                                        }

                                        Item.WB -> {
                                            selectorItems?.get(index)?.text?.let {
                                                wb.value = it
                                            }
                                            characteristics.copy(
                                                wbPosition = index,
                                                wbValue = wb.value
                                            )
                                        }

                                        Item.FOCUS -> {
                                            selectorItems?.get(index)?.text?.let {
                                                focus.value = it
                                            }
                                            characteristics.copy(
                                                focusPosition = index,
                                                focusValue = focus.value
                                            )
                                        }

                                        Item.MAGNIFIER -> {
                                            selectorItems?.get(index)?.text?.let {
                                                magnifier.value = it
                                                renderer.configureMagnifier(
                                                    it.toFloat(),
                                                    0.2f,
                                                    0.4f
                                                )
                                            }

                                            characteristics.copy(
                                                magnifierPosition = index,
                                            )
                                        }

                                        null -> characteristics.copy()

                                    }
                                    //car=car.copy()
                                    onCharacteristicChanged(
                                        params
                                    )
                                    selectorItems?.let {
                                        for (i in it.indices) {
                                            it[i].passed = i <= index
                                        }
                                    }

                                }
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