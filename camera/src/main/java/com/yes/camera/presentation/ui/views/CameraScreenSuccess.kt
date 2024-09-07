package com.yes.camera.presentation.ui.views

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import androidx.collection.mutableObjectIntMapOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.R
import com.yes.camera.presentation.model.Characteristic
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.Item
import com.yes.camera.presentation.model.SettingsItemUI
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.ShutterValueItemAdapterDelegate
import com.yes.camera.presentation.ui.custom.compose.RadioGroup
import com.yes.camera.presentation.ui.custom.compose.RadioItem
import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import kotlinx.coroutines.delay

@Composable
fun CameraScreenSuccess(
    context: Context,
    characteristicsInitial: CharacteristicsUI,
    onSettingsClick: () -> Unit,
    onGetSurface:(surface: SurfaceTexture) -> Unit,
    onCharacteristicChanged:(characteristics:CharacteristicsUI)->Unit
) {
    var characteristics =characteristicsInitial
    var autoFitSurfaceView by remember { mutableStateOf<AutoFitSurfaceView?>(null) }
    val renderer=GLRenderer(
        context
    ) { surfaceTexture ->
        onGetSurface(surfaceTexture)
    }
    val adapter = CompositeAdapter(
        mapOf(
            SettingsItemUI::class.java to ShutterValueItemAdapterDelegate(),
        )
    )
    val radioGroupItems = listOf(
        RadioItem(Item.SHUTTER, "SHUTTER", R.drawable.camera),
        RadioItem(Item.ISO, "ISO", R.drawable.iso),
        RadioItem(Item.FOCUS, "FOCUS", R.drawable.metering)
    )

    var valueSelectorItems:List<SettingsItemUI>? by remember {
        mutableStateOf(
            null
        )
    }

    var isOpen by remember {
        mutableStateOf(true)
    }
    var visible by remember {
        mutableStateOf(true)
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ){
        AndroidView(
            factory = {
                AutoFitSurfaceView(
                    context,
                    null
                ).also {
                    autoFitSurfaceView = it
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
             Button(onClick = {
               // onSettingsClick()
                 visible=!visible
              //  autoFitSurfaceView?.setFullscreen(false)
            }) {
                Text(text = "Go to screen B", fontSize = 40.sp)
            }

            LaunchedEffect(key1 = Unit, block = {
                delay(1000L)
                //visible = true
            })
            val position= remember {
                mutableIntStateOf(0)
            }
            var selectedCharacteristic:MutableState<Item?> = remember {
                mutableStateOf(null)
            }
               AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn() + expandHorizontally(),
                    exit = scaleOut() + shrinkHorizontally()
                ) {
                    RadioGroup(
                        modifier = Modifier,
                        items = radioGroupItems,
                        onOptionSelected = { value ->
                            println(value.toString())
                            selectedCharacteristic.value=value
                            characteristics.characteristics[value]
                            valueSelectorItems= characteristics.characteristics[value]?.items
                            position.intValue= when(value){
                                Item.SHUTTER -> characteristics.shutterValue
                                Item.ISO -> characteristics.isoValue
                                Item.FOCUS -> characteristics.focusValue
                                null -> 0
                            }
                            value?.let {isOpen=true}?:run{isOpen=false}
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

            AnimatedVisibility(
                visible = isOpen,
                enter = scaleIn() + expandHorizontally(),
                exit = scaleOut() + shrinkHorizontally()
            ) {
                ValueSelector(
                    position = position,
                    modifier = Modifier
                        .fillMaxWidth(),
                    items = valueSelectorItems,
                    adapter = adapter,
                    onSelectedItemChanged = { index ->

                        characteristics = when(selectedCharacteristic.value){
                            Item.SHUTTER -> characteristics.copy(
                                shutterValue= index
                            )
                            Item.ISO -> characteristics.copy(
                                isoValue = index
                            )
                            Item.FOCUS -> characteristics.copy(
                                focusValue =  index
                            )
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
            }




    }

}