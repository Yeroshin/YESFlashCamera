package com.yes.camera.presentation.ui.views

import android.content.Context
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.Role
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
    histogram:MutableMap<Int,Int>?
) {

    var characteristics by remember(key1 = characteristicsInitial) {
        mutableStateOf(characteristicsInitial)
    }

    val adapter = CompositeAdapter(
        mapOf(
            SettingsItemUI::class.java to ShutterValueItemAdapterDelegate(),
        )
    )



    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // AndroidView(factory = {SurfacePanel(context)})
        AndroidView(
            factory = {
                AutoFitSurfaceView(
                    context,
                    null
                ).also {
                    // autoFitSurfaceView = it
                    it.setEGLContextClientVersion(2)
                    it.setRenderer(
                        renderer
                    )
                    it.setOnTouchListener { v, event ->
                        v.performClick()
                        if (event != null) {
                            val normalizedX =
                                (event.x / v.width.toFloat()) * 2 - 1
                            val normalizedY =
                                -((event.y / v.height.toFloat()) * 2 - 1)

                            if (event.action == MotionEvent.ACTION_DOWN) {
                                //    glSurfaceView!!.queueEvent {

                                //   it.setAspectRatio(3, 2)
                                renderer.handleTouchPress(
                                    normalizedX, normalizedY
                                )

                                //   }
                            } else if (event.action == MotionEvent.ACTION_MOVE) {
                                //   glSurfaceView!!.queueEvent {
                                renderer.handleTouchDrag(
                                    normalizedX, normalizedY
                                )
                                //   }
                            }

                            true
                        } else {
                            false
                        }
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
            var visibleSelector by remember {
                mutableStateOf(true)
            }
            var positionRadioGroup by remember {
                mutableIntStateOf(0)
            }
            var selectedCharacteristic: MutableState<Item?> = remember {
                mutableStateOf(null)
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
            var radioGroupItems =
                    listOf(
                        RadioItem(Item.SHUTTER, shutter, null),
                        RadioItem(Item.ISO, iso, R.drawable.iso),
                        RadioItem(Item.FOCUS, focus, R.drawable.metering)
                    )



            var valueSelectorItems: List<SettingsItemUI>? by remember {
                mutableStateOf(
                    null
                )
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
                        println(value.toString())
                        selectedCharacteristic.value = value
                        // characteristics.characteristics[value]
                        valueSelectorItems = characteristics.characteristics[value]?.items
                        positionRadioGroup = when (value) {
                            Item.SHUTTER -> characteristics.shutterPosition
                            Item.ISO -> characteristics.isoPosition
                            Item.FOCUS -> characteristics.focusPosition
                            null -> 0
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
            ///////////value selector
          /*  AnimatedVisibility(
                visible = visibleSelector,
                enter = scaleIn() + expandHorizontally(),
                exit = scaleOut() + shrinkHorizontally()
            ) {*/
                ValueSelector(
                    position = when (selectedCharacteristic.value) {
                        Item.SHUTTER -> {
                            characteristics.shutterPosition
                        }

                        Item.ISO -> {
                            characteristics.isoPosition
                        }

                        Item.FOCUS -> {
                            characteristics.focusPosition
                        }

                        null -> 0
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    items = valueSelectorItems,
                    adapter = adapter,
                    onSelectedItemChanged = { index ->

                        characteristics = when (selectedCharacteristic.value) {
                            Item.SHUTTER -> {

                                 valueSelectorItems?.get(index)?.text?.let {
                                  shutter.value=it
                                }
                                characteristics.copy(
                                    shutterPosition = index
                                )
                            }

                            Item.ISO ->{
                                valueSelectorItems?.get(index)?.text?.let {
                                    iso.value=it
                                }
                                characteristics.copy(
                                    isoPosition = index
                                )
                            }

                            Item.FOCUS -> {
                                valueSelectorItems?.get(index)?.text?.let {
                                    focus.value=it
                                }
                                characteristics.copy(
                                    focusPosition = index
                                )
                            }

                            null -> characteristics.copy()
                        }
                        onCharacteristicChanged(
                            characteristics
                        )


                        valueSelectorItems?.let {
                            for (i in it.indices) {
                                it[i].passed = i <= index
                            }
                        }
                    }
                )
         //   }
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
            var visibleSelectorMagnifier by remember {
                mutableStateOf(true)
            }
            var magnifierSelectorItems: List<SettingsItemUI> by remember {
                mutableStateOf(
                    listOf(
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
            }
            var magnifierSelectedItem by remember {
                mutableIntStateOf(0)
            }
            Row {
              /*  Column {*/
                    VectorShadow(
                        Modifier
                            .padding(8.dp)
                            .size(32.dp)
                            .clickable {
                                visibleSelectorMagnifier = !visibleSelectorMagnifier
                            },
                        vectorColor = Color.White,
                        shadowColor = Color.DarkGray,
                        resId = R.drawable.loupe,
                    )
                  /*  Text(
                        text = magnifierSelectorItems[magnifierSelectedItem].text,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            shadow = Shadow(
                                color = Color.DarkGray,
                                offset = Offset(5.0f, 5.0f),
                                blurRadius = 5f
                            )
                        )
                    )
                }*/
                AnimatedVisibility(
                    visible = visibleSelectorMagnifier,
                    enter = scaleIn() + expandHorizontally(),
                    exit = scaleOut() + shrinkHorizontally()
                ) {
                    ValueSelector(
                        position = magnifierSelectedItem,
                        modifier = Modifier
                            .fillMaxWidth(),
                        items = magnifierSelectorItems,
                        adapter = adapter,
                        onSelectedItemChanged = { index ->
                            magnifierSelectedItem = index
                            renderer.configureMagnifier(
                                magnifierSelectorItems[index].text.toFloat(),
                                0.2f,
                                0.4f
                            )
                            valueSelectorItems?.let {
                                for (i in it.indices) {
                                    it[i].passed = i <= index
                                }
                            }
                        }
                    )
                }
            }


        }
        ////////////////////////histogram
        val v= listOf(6,2,3,4,1,2,3,7)
        Histogram(
            Modifier,
            histogram,
            300.dp,
            100.dp
        )
        //////////////////////////capture

        var isCheck by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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