package com.yes.camera.presentation.ui

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import androidx.activity.ComponentActivity.CAMERA_SERVICE
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.R
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.presentation.model.ShutterItemUI
import com.yes.camera.presentation.ui.adapter.ShutterValueItemAdapterDelegate
import com.yes.camera.presentation.ui.custom.compose.DropDown
import com.yes.camera.presentation.ui.custom.compose.RadioGroup
import com.yes.camera.presentation.ui.custom.compose.RadioItem
import com.yes.camera.presentation.ui.custom.compose.ValueSelector
import com.yes.camera.presentation.ui.custom.gles.AutoFitSurfaceView
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.camera.utils.ShutterSpeedsResourcesProvider
import com.yes.shared.presentation.adapter.CompositeAdapter
import kotlinx.coroutines.delay





@Composable
fun CameraScreen(
    context: Context,
    onButtonClick: () -> Unit,
  //  cameraViewModel: CameraViewModel
) {
    val shutterSpeeds =ShutterSpeedsResourcesProvider(LocalContext.current).getShutterSpeeds()
   // ResourcesProvider(LocalContext.current).getString()

    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Camera", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(45.dp))
        var autoFitSurfaceView by remember { mutableStateOf<AutoFitSurfaceView?>(null) }
        Button(onClick = {
              onButtonClick()
            autoFitSurfaceView?.setFullscreen(false)
            // navController.navigate("B")
        }) {
            Text(text = "Go to screen B", fontSize = 40.sp)
        }

        val mBackgroundThread = HandlerThread("CameraThread").apply { start() }
        val mBackgroundHandler = Handler(mBackgroundThread.looper)
        val cameraRepository =
            CameraRepository(
                context.getSystemService(CAMERA_SERVICE) as CameraManager,
                mBackgroundHandler
            )

        val adapter = CompositeAdapter(
            mapOf(
                ShutterItemUI::class.java to ShutterValueItemAdapterDelegate(),
                )
        )
        val radioGroupItems = listOf(
            RadioItem(1, "SHUTTER", R.drawable.camera),
            RadioItem(2, "ISO", R.drawable.iso),
            RadioItem(3, "FOCUS", R.drawable.metering)
        )
        val valueSelectorItems by remember {
            mutableStateOf(
                listOf(
                    ShutterItemUI("0"),
                    ShutterItemUI("1"),
                    ShutterItemUI("2"),
                    ShutterItemUI("3"),
                    ShutterItemUI("4"),
                    ShutterItemUI("5"),
                    ShutterItemUI("6"),
                    ShutterItemUI("7"),
                    ShutterItemUI("8"),
                    ShutterItemUI("9"),
                    ShutterItemUI("10"),
                    ShutterItemUI("11"),
                    ShutterItemUI("12"),
                    ShutterItemUI("13"),
                )
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
            visible = visible ,
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
                    } ?: run {
                        isOpen = false
                    }
                    radioGroupItems[0].resId = R.drawable.iso
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

                    for (i in valueSelectorItems.indices) {
                        valueSelectorItems[i].passed = i <= index
                    }


                }
            )
        }


        /* ValueSelector(
             items = valueSelectorItems,
             adapter =adapter,
             onSelectedItemChanged ={item->

                 println(item)
             }
         )*/

        /*  AndroidView(
              factory = {
                  AutoFitSurfaceView(
                      context,
                      null
                      ).also {
                          autoFitSurfaceView = it
                              it.setEGLContextClientVersion(2)
                      it.setRenderer(
                          GLRenderer(
                              context
                          ) { surfaceTexture ->

                              cameraRepository.getBackCameraId()?.let {
                                  cameraRepository.openCamera(
                                      it
                                  ) { camera ->
                                      val cam = camera
                                      cameraRepository.createCaptureSession(surfaceTexture)
                                  }
                              }


                          }
                      )
                      }
              }
          )*/

    }

}