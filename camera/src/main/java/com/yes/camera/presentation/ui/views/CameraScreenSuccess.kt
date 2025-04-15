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
import com.yes.camera.presentation.model.SelectorItem
import com.yes.camera.presentation.model.TextItem
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.IconSelectorItemUI
import com.yes.camera.presentation.ui.adapter.TextSelectorItemUI
import com.yes.camera.presentation.ui.custom.compose.Histogram
import com.yes.camera.presentation.ui.custom.compose.RadioGroup
import com.yes.camera.presentation.ui.custom.compose.TextRadioItem

import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.compose.VectorRadioItem
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
@Immutable
data class ImmutableCollection <T>(
    val list: List<T>
)
@Immutable
data class MapImmutableCollection <T,R>(
    val map: Map<T,R>
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
    val immut=MapImmutableCollection(
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
    var radioGroupSelectedItem: Item? by remember {
        mutableStateOf(Item.SHUTTER)
    }
    var valueSelectorVisibility by remember {
        mutableStateOf(true)
    }
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
    val item by remember {
        mutableStateOf(listOf(VectorRadioItem(Item.WB, "WB", 2131099791)))
    }
    // val radioGroupItems= listOf(VectorRadioItem(Item.WB, "WB", wb.value))
    val radioGroupItems = remember(settings) {
        // emptyList<TextRadioItem>()
        ImmutableCollection(
            listOf(
                //  item
                TextRadioItem(Item.SHUTTER, settings.shutterValue, "SHUTTER"),
                TextRadioItem(Item.ISO, settings.isoValue, "ISO"),
                  VectorRadioItem(Item.WB, "WB", wb.value),
                  TextRadioItem(Item.FOCUS, settings.focusValue, "FOCUS"),
                  TextRadioItem(Item.MAGNIFIER, settings.magnifierValue, "MAGNIFIER")

            )

        )

    }
    var valueSelectorAcquiredItemIndex: Int? by remember {
        mutableStateOf(null)
    }
    var autoItems by remember {
        mutableStateOf(
            mutableMapOf(
                Item.SHUTTER to false,
                Item.ISO to false,
                Item.WB to false,
                Item.FOCUS to false,
                Item.MAGNIFIER to false
            )
        )
    }

    LaunchedEffect(autoItems) {
        snapshotFlow { settings }
            .collect {
                if (autoItems[radioGroupSelectedItem] == true) {
                    when (radioGroupSelectedItem) {
                        Item.SHUTTER -> {
                          //  valueSelectorAcquiredItemIndex = settings.shutterPosition

                        }

                        Item.ISO -> {
                          //  valueSelectorAcquiredItemIndex = settings.isoPosition
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
         key2 = radioGroupSelectedItem
     ) {
         snapshotFlow { items }
             .distinctUntilChanged() // Важно! Фильтрует одинаковые значения
             .collect { newValue ->
                 selectorItems = when (radioGroupSelectedItem) {
                     Item.SHUTTER -> {

                             items.shutterItems?.let {
                                 ImmutableCollection(
                                     it.list
                                 )
                             }

                         //map { it.copy() }
                     }

                     Item.ISO -> {
                         items.isoItems?.let {
                             ImmutableCollection(
                                 it.list
                             )
                         }

                     }

                     Item.WB -> {
                       //  items.wbItems?.map { it.copy() }
                         items.wbItems?.let {
                             ImmutableCollection(
                                 it.list
                             )
                         }
                     }

                     Item.FOCUS -> {
                       //  items.focusItems?.map { it.copy() }
                         items.focusItems?.let {
                             ImmutableCollection(
                                 it.list
                             )
                         }
                     }

                     Item.MAGNIFIER -> {
                       //  items.magnifierItems?.map { it.copy() }
                         items.magnifierItems?.let {
                             ImmutableCollection(
                                 it.list
                             )
                         }
                     }

                     null ->items.magnifierItems?.let {
                         ImmutableCollection(it.list)
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
     var selectorSelectedItemIndex by remember {
         mutableStateOf(0)
     }
     LaunchedEffect(selectorSelectedItemIndex) {
         radioGroupSelectedItem?.let {
             autoItems = autoItems.toMutableMap().apply {
                 compute(it) { _, value -> false }
             }
         }

         settingsRequest = when (radioGroupSelectedItem) {
             Item.SHUTTER -> {
                 if (autoItems[radioGroupSelectedItem] == false) {
                     settingsRequest.copy(
                         shutterValue = characteristics.items.shutterItems?.list?.get(
                             selectorSelectedItemIndex
                         )?.text ?: run { "" })
                 } else {
                     settingsRequest.copy(shutterValue = "")
                 }
                 // settingsRequest.copy(shutterPosition = selectorSelectedItemIndex)
                 /*text?.let {
                     settingsRequest.copy(shutterValue = it)
                 } ?: run { settingsRequest.copy() }*/
             }

             Item.ISO -> {
                 if (autoItems[radioGroupSelectedItem] == false) {
                     settingsRequest.copy(
                         isoValue = characteristics.items.isoItems?.list?.get(
                             selectorSelectedItemIndex
                         )?.text ?: run { "" })
                 } else {
                     settingsRequest.copy(isoValue = "")
                 }
                 // settingsRequest.copy(isoPosition = selectorSelectedItemIndex)
                 /*  selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                       settingsRequest.copy(isoValue = it)

                   } ?: run { settingsRequest.copy() }*/
             }

             Item.WB -> {
                 settings.copy()
                 /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                      settings.copy(wbValue = it)

                  } ?: run { settings.copy() }*/
             }

             Item.FOCUS -> {
                 settings.copy()
                 /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {

                      settings.copy(focusValue = it)

                  } ?: run { settings.copy() }*/
             }

             Item.MAGNIFIER -> {
                 /* selectorItems?.get(selectorSelectedItemIndex)?.text?.let {
                      magnifier = it
                      renderer.configureMagnifier(
                          it.toFloat(),
                          0.2f,
                          0.4f
                      )
                  } ?: run { settings.copy() }
                  magnifierPosition = selectorSelectedItemIndex*/
                 settings.copy()

             }

             null -> settings.copy()

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
        /*  Column(
              modifier = Modifier.fillMaxSize(),
          ) {*/
        RadioGroup(
            modifier = Modifier
                // .align(Alignment.CenterHorizontally)
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

        //////////////////////////selector
           var isCheck by remember { mutableStateOf(false) }
           Column(
               modifier = Modifier
                   .padding(8.dp)
                   .fillMaxWidth()
                   .align(Alignment.BottomCenter),
               verticalArrangement = Arrangement.spacedBy(8.dp)
           ) {


               Row {
                   ///////////auto
                   VectorShadow(
                       modifier = Modifier
                           .size(32.dp)
                           .clickable {
                               radioGroupSelectedItem?.let {
                                   autoItems = autoItems
                                       .toMutableMap()
                                       .apply {
                                           compute(it) { _, value -> !(value ?: false) }
                                       }
                               }
                               if (autoItems[radioGroupSelectedItem] == true) {
                                   settingsRequest = when (radioGroupSelectedItem) {
                                       Item.SHUTTER -> {
                                           settingsRequest.copy(
                                               shutterValue = ""
                                           )
                                       }

                                       Item.ISO -> {
                                           settingsRequest.copy(
                                               isoValue = ""
                                           )

                                       }

                                       Item.WB -> {

                                           settings.copy(wbValue = 0)

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

                               }

                               //car=car.copy()

                           },
                       vectorColor = if (autoItems[radioGroupSelectedItem] == true) {
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
                          // adapter = adapter,
                           onSelectedItemChanged = { index, manual ->
                               if (manual) {
                                   selectorSelectedItemIndex = index
                               }
                               selectorItems?.let {
                                   for (i in it.list.indices) {
                                       it.list[i].passed = i <= index
                                   }
                               }


                           },
                           updatedPosition = valueSelectorAcquiredItemIndex
                       )
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
                           ButtonDefaults.buttonColors(containerColor = Color.Gray)
                       }

                   ) {
                       // Text(text = "Capture", fontSize = 40.sp)
                   }
               }


           }

    }
}


