package com.yes.camera.presentation.ui.views

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.camera.R
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.model.SettingsItemUI
import com.yes.camera.presentation.ui.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.ShutterValueItemAdapterDelegate
import com.yes.camera.presentation.ui.custom.compose.DropDown
import com.yes.camera.presentation.ui.custom.compose.RadioGroup
import com.yes.camera.presentation.ui.custom.compose.RadioItem
import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import kotlinx.coroutines.delay

@Composable
fun CameraScreenSuccess(
    context: Context,
    state: CameraContract.CameraState.Success,
    onSettingsClick: () -> Unit,
    onGetSurface:(surface: SurfaceTexture) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Camera", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(45.dp))
        var autoFitSurfaceView by remember { mutableStateOf<AutoFitSurfaceView?>(null) }
        Button(onClick = {
            onSettingsClick()
            autoFitSurfaceView?.setFullscreen(false)
            // navController.navigate("B")
        }) {
            Text(text = "Go to screen B", fontSize = 40.sp)
        }

      /*  val mBackgroundThread = HandlerThread("CameraThread").apply { start() }
        val mBackgroundHandler = Handler(mBackgroundThread.looper)
        val cameraRepository =
            CameraRepository(
                context.getSystemService(CAMERA_SERVICE) as CameraManager,
                mBackgroundHandler
            )*/

        val adapter = CompositeAdapter(
            mapOf(
                SettingsItemUI::class.java to ShutterValueItemAdapterDelegate(),
            )
        )
        val radioGroupItems = listOf(
            RadioItem(1, "SHUTTER", R.drawable.camera),
            RadioItem(2, "ISO", R.drawable.iso),
            RadioItem(3, "FOCUS", R.drawable.metering)
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
            mutableStateOf(false)
        }
        LaunchedEffect(key1 = Unit, block = {
            delay(600L)
            visible = true
        })
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(),
            exit = fadeOut() + shrinkVertically()
        ) {
            RadioGroup(
                modifier = Modifier,
                items = radioGroupItems,
                onOptionSelected = { value ->
                    println(value.toString())

                    value?.let {
                        isOpen = true
                        when(it){
                            1->{
                                valueSelectorItems=state.camera.shutterValues
                            }
                            2->{
                                valueSelectorItems=state.camera.isoValues
                            }
                            3->{}
                        }
                    } ?: run {
                        isOpen = false
                    }
                  //  radioGroupItems[0].resId = R.drawable.iso
                }
            )
        }

        DropDown(
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
        }
        val renderer=GLRenderer(
            context
        ) { surfaceTexture ->
            onGetSurface(surfaceTexture)
            /* cameraRepository.getBackCameraId()?.let {
                 cameraRepository.openCamera(
                     it
                 ) { camera ->
                     val cam = camera
                     cameraRepository.createCaptureSession(surfaceTexture)
                 }
             }*/
        }
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
                            // Convert touch coordinates into normalized device
                            // coordinates, keeping in mind that Android's Y
                            // coordinates are inverted.
                            val normalizedX =
                                (event.x / v.width.toFloat()) * 2 - 1
                            val normalizedY =
                                -((event.y / v.height.toFloat()) * 2 - 1)

                            if (event.action == MotionEvent.ACTION_DOWN) {
                                //    glSurfaceView!!.queueEvent {

                                it.setAspectRatio(3, 2)
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
    }
}