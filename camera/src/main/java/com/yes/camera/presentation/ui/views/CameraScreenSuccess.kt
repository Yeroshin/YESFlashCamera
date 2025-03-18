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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.R
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.SettingsItemUI
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.ShutterValueItemAdapterDelegate
import com.yes.camera.presentation.ui.custom.compose.Histogram
import com.yes.camera.presentation.ui.custom.compose.RadioGroup

import com.yes.camera.presentation.ui.custom.compose.RadioItem
import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.compose.VectorShadow
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer


@Composable
fun CameraScreenSuccess(
    context: Context,
    renderer: GLRenderer,
    characteristicsInitial: CharacteristicsUI,
    onSettingsClick: () -> Unit,
    onStartVideoRecord: (enabled: Boolean) -> Unit,
    onCharacteristicChanged: (characteristics: CharacteristicsUI) -> Unit,
    histogram: MutableMap<Int, Int>?
) {

    var characteristics by remember(key1 = characteristicsInitial) {
        mutableStateOf(characteristicsInitial)
    }

    /*  LaunchedEffect(characteristicsInitial) {
          snapshotFlow { characteristicsInitial }
              .collect {ch->
                  characteristics=ch
                  shutter.value=characteristics.characteristics[Item.SHUTTER]?.value.toString()
              }
      }*/

    val adapter = CompositeAdapter(
        mapOf(
            SettingsItemUI::class.java to ShutterValueItemAdapterDelegate(),
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

    /////////////////////
    var selectedItem: MutableState<Item?> = remember {
        mutableStateOf(null)
    }
    var visibleSelector by remember {
        mutableStateOf(false)
    }
    var selectorItems: List<SettingsItemUI>? by remember {
        mutableStateOf(
            null
        )
    }
    var shutter = remember {
        mutableStateOf("-")
    }
    var iso = remember {
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
            RadioItem(Item.SHUTTER, shutter, null),
            RadioItem(Item.ISO, iso, R.drawable.iso),
            RadioItem(Item.FOCUS, focus, R.drawable.metering),
            RadioItem(Item.MAGNIFIER, magnifier, R.drawable.loupe)
        )
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // AndroidView(factory = {SurfacePanel(context)})
        AndroidView(
            modifier = Modifier
                .align(Alignment.Center),
            factory = {
                AutoFitSurfaceView(
                    context,
                    null
                ).apply {
                    // autoFitSurfaceView = it
                    // setFullscreen(true)
                    setFullscreen(false)
                    setAspectRatio(4096, 3072)
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
                                characteristics.copy(
                                    touchPoint= floatArrayOf(normalizedX,normalizedY)
                                )
                            }

                        }
                        //   }

                        true

                    }
                }
            }
        )
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

            var positionRadioGroup by remember {
                mutableStateOf("")
            }


            ////////////////radio group
            AnimatedVisibility(
                visible = visibleRadioGroup,
                enter = scaleIn() + expandHorizontally(),
                exit = scaleOut() + shrinkHorizontally()
            ) {
                RadioGroup(
                    modifier = Modifier,
                    items = radioGroupItems,
                    onOptionSelected = { value ->
                        selectedItem.value = value
                        // characteristics.characteristics[value]

                        positionRadioGroup = when (value) {
                            Item.SHUTTER ->{
                                selectorItems = characteristics.shutterItems
                                characteristics.shutterValue
                            }
                            Item.ISO ->{
                                selectorItems = characteristics.isoItems
                                characteristics.isoValue
                            }
                            Item.FOCUS ->{
                                selectorItems = characteristics.focusItems
                                characteristics.focusValue
                            }
                            Item.MAGNIFIER ->{
                                selectorItems = characteristics.magnifierItems
                                characteristics.magnifierValue
                            }
                            null -> ""
                        }
                        value?.let { visibleSelector = true } ?: run { visibleSelector = false }
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
            ////////////////////////histogram
            Histogram(
                Modifier
                    .align(Alignment.Start),
                histogram,
                150.dp,
                80.dp
            )
            ///////////value selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                AnimatedContent(
                    targetState = visibleSelector,
                    transitionSpec = {
                        (scaleIn() + expandHorizontally()) togetherWith
                                // Комбинируем анимации для исчезновения
                                (scaleOut() + shrinkHorizontally())
                    }
                    /* enter = scaleIn() + expandHorizontally(),
                     exit = scaleOut() + shrinkHorizontally()*/
                ) { isVisible ->
                    if (isVisible) {
                        ValueSelector(
                            position = when (selectedItem.value) {
                                Item.SHUTTER -> {
                                    characteristics.shutterPosition
                                }

                                Item.ISO -> {
                                    characteristics.isoPosition
                                }

                                Item.FOCUS -> {
                                    characteristics.focusPosition
                                }

                                Item.MAGNIFIER -> characteristics.magnifierPosition
                                null -> 0
                            },
                            items = selectorItems,
                            adapter = adapter,
                            onSelectedItemChanged = { index ->

                                characteristics = when (selectedItem.value) {
                                    Item.SHUTTER -> {
                                        selectorItems?.get(index)?.text?.let {
                                            shutter.value = it
                                        }
                                        characteristics.copy(
                                            shutterPosition = index,
                                            shutterValue = shutter.value
                                        )
                                    }

                                    Item.ISO -> {
                                        selectorItems?.get(index)?.text?.let {
                                            iso.value = it
                                        }
                                       /* characteristics.characteristics[Item.ISO]?.copy(
                                            value = index
                                        )*/
                                        characteristics.copy(
                                            isoPosition = index,
                                            isoValue = iso.value
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
                                        selectorItems?.get(index)?.text?.toFloat()?.let {
                                            renderer.configureMagnifier(
                                                it,
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
                                onCharacteristicChanged(
                                    characteristics
                                )
                                selectorItems?.let {
                                    for (i in it.indices) {
                                        it[i].passed = i <= index
                                    }
                                }
                            }
                        )
                    }
                }
            }

            //////////////////////////capture
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            ) {
                VectorShadow(
                    Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .clickable {
                            onSettingsClick()
                        },
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.settings,
                )
                Button(
                    modifier = Modifier
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
                        ButtonDefaults.buttonColors(containerColor = Color.Blue)
                    }

                ) {
                    Text(text = "Capture", fontSize = 40.sp)
                }
                VectorShadow(
                    Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .clickable {

                        },
                    vectorColor = Color.White,
                    shadowColor = Color.DarkGray,
                    resId = R.drawable.flip_camera_android,
                )
            }
        }


    }

}