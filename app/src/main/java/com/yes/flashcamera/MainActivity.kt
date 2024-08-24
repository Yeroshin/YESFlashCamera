package com.yes.flashcamera

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api

class MainActivity : ComponentActivity() {
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in CAMERAX_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {

            }
        }
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
       /* setContent {
            FlashCameraTheme {

                val scaffoldState = rememberBottomSheetScaffoldState()
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_CAPTURE or
                                    CameraController.VIDEO_CAPTURE
                        )
                    }
                }
                val info=controller.cameraInfo
                val index=info?.exposureState?.exposureCompensationRange
                val control=controller.cameraControl
                index?.let {
                    control?.setExposureCompensationIndex(index.upper)
                }

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                       /* PhotoBottomSheetContent(
                            bitmaps = bitmaps,
                            modifier = Modifier
                                .fillMaxWidth()
                        )*/
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        CameraPreview(
                            controller = controller,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                controller.cameraSelector =
                                    if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else CameraSelector.DEFAULT_BACK_CAMERA
                            },
                            modifier = Modifier
                                .offset(16.dp, 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Switch camera"
                            )
                        }
                    }
                }
            }
        }*/
    }
    private fun requestPermissions() {
        activityResultLauncher.launch(CAMERAX_PERMISSIONS)
    }
    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    }
}


