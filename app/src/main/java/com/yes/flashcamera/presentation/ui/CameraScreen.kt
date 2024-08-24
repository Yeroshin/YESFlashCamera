package com.yes.flashcamera.presentation.ui

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import androidx.activity.ComponentActivity.CAMERA_SERVICE
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yes.flashcamera.data.repository.CameraRepository

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CameraScreen(
    context: Context,
    onButtonClick:()->Unit
) {
    Column(modifier = Modifier.fillMaxSize()
        , verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Camera", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(45.dp))
        var autoFitSurfaceView by remember { mutableStateOf<AutoFitSurfaceView?>(null) }
        Button(onClick = {
          //  onButtonClick()
            autoFitSurfaceView?.setFullscreen(false)
           // navController.navigate("B")
        }) {
            Text(text = "Go to screen B", fontSize = 40.sp)
        }
        val mBackgroundThread= HandlerThread("CameraThread").apply { start() }
        val mBackgroundHandler = Handler(mBackgroundThread.looper)
        val cameraRepository =
            CameraRepository(
                context.getSystemService(CAMERA_SERVICE) as CameraManager,
                mBackgroundHandler
            )
        RadioGroup(
          /*  onOptionSelected = {
                println()
            }*/
        )
        AndroidView(
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
        )


    }
}