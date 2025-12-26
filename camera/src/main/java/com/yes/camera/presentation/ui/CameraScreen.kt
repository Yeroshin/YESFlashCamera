package com.yes.camera.presentation.ui

import ads_mobile_sdk.h6
import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.camera.utils.ShutterSpeedsResourcesProvider
import com.yes.camera.presentation.ui.views.CameraScreenSuccess
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.presentation.ui.PermissionsHelper

/*
@Composable
fun CameraScreen(
    context: Context,
    cameraViewModel: CameraViewModel,
    onSettingsClick: () -> Unit
) {
    val renderer = remember {
        GLRenderer(
            context
        ) { surfaceTexture ->
            surfaceTexture.setDefaultBufferSize(1920,1080/*4096,3072)//(1920,1080*/)//4096,3072//3840,2160
         //   surfaceTexture.setDefaultBufferSize(3840,2160)//(1920,1080)//4096,3072//3840,2160
        //    surfaceTexture.setDefaultBufferSize(1920,1080)
          //  surfaceTexture.setDefaultBufferSize(4096,3072)
            cameraViewModel.setEvent(
                CameraContract.Event.OnOpenCamera(true, surfaceTexture)
            )
        }
    }
    val viewState = cameraViewModel.uiState.collectAsState()
    when(val state = viewState.value.state){
        CameraContract.CameraState.Idle -> { }
        CameraContract.CameraState.Loading -> {}
        is CameraContract.CameraState.Success -> CameraScreenSuccess(
            context = context,
            renderer = renderer,
            characteristicsInit = state.characteristics ,
            onSettingsClick ={
                cameraViewModel.setEvent(
                    CameraContract.Event.OnCloseCamera
                )
                onSettingsClick()
            } ,
            onStartVideoRecord = {enabled->
                cameraViewModel.setEvent(
                    CameraContract.Event.OnStartVideoRecord(enabled)
                )
            },
            onCharacteristicChanged = {characteristics->
                cameraViewModel.setEvent(
                    CameraContract.Event.OnSetCharacteristics(characteristics)
                )
            },
            fullScreen = state.characteristics.fullScreen
        )
    }

    val shutterSpeeds = ShutterSpeedsResourcesProvider(LocalContext.current).getShutterSpeeds()
    // ResourcesProvider(LocalContext.current).getString()

   /* Column(
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

       /* AndroidView(
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

    }*/

}*/

///////////////////////////
///////////////////////////
@Composable
fun CameraScreen(
    context: Context,
    cameraViewModel: CameraViewModel,
    onSettingsClick: () -> Unit
) {
    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        // Для камеры и хранения изображений/видео данных
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,  // Для чтения изображений (или READ_MEDIA_VIDEO для видео)
            Manifest.permission.MANAGE_EXTERNAL_STORAGE // Manifest.permission.WRITE_EXTERNAL_STORAGE не нужен, используйте MANAGE_EXTERNAL_STORAGE для полного доступа если критично
        )
    } else {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var requestCounter by remember { mutableIntStateOf(0) }

    // Интеграция PermissionsHelper для проверки/запроса разрешения на камеру
    PermissionsHelper(
        permissions = permissions,
        onPermissionsResult = { granted, denied ->
            cameraPermissionGranted = Manifest.permission.CAMERA in granted

        },
        rationaleMessage = "Разрешение на камеру требуется для работы с камерой. Пожалуйста, предоставьте доступ.",
        requestCounter = requestCounter
    )

    // Обработчик для повторного запроса (опционально, если PermissionsHelper не авто-повторяет)
    val requestPermissionAgain:() -> Unit = {
        requestCounter++
    }


    // Инициализируем renderer и события только если разрешени дано
    if (cameraPermissionGranted) {
        val viewState = cameraViewModel.uiState.collectAsState()
        val renderer = remember {
            GLRenderer(context) { surfaceTexture ->
                surfaceTexture.setDefaultBufferSize(1920, 1080)
                cameraViewModel.setEvent(
                    CameraContract.Event.OnOpenCamera(true, surfaceTexture)
                )
            }
        }
        // Теперь логика состояний
        when (val state = viewState.value.state) {
            CameraContract.CameraState.Idle -> { /* Показать idle UI если нужно */
            }

            CameraContract.CameraState.Loading -> { /* Показать лоадер */
            }

            is CameraContract.CameraState.Success -> {
                CameraScreenSuccess(
                    context = context,
                    renderer = renderer,
                    characteristicsInit = state.characteristics,
                    onSettingsClick = {
                        cameraViewModel.setEvent(CameraContract.Event.OnCloseCamera)
                        onSettingsClick()
                    },
                    onStartVideoRecord = { enabled ->
                        cameraViewModel.setEvent(CameraContract.Event.OnStartVideoRecord(enabled))
                    },
                    onCharacteristicChanged = { characteristics ->
                        cameraViewModel.setEvent(
                            CameraContract.Event.OnSetCharacteristics(
                                characteristics
                            )
                        )
                    },
                    fullScreen = state.characteristics.fullScreen
                )
            }
        }
    } else {

        CameraScreenError(
            onRetryPermission = requestPermissionAgain,
            onSettingsClick = onSettingsClick
        )
    }
}

// Композит для экрана ошибки
@Composable
fun CameraScreenError(
    onRetryPermission: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Разрешение на камеру не предоставлено",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetryPermission) {
            Text("Запросить разрешение снова")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSettingsClick) {
            Text("Вернуться в настройки")
        }
    }
}