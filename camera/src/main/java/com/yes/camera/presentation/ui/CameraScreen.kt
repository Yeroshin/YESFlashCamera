package com.yes.camera.presentation.ui

import android.content.Context

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.camera.utils.ShutterSpeedsResourcesProvider
import com.yes.camera.presentation.ui.views.CameraScreenSuccess
import com.yes.camera.presentation.vm.CameraViewModel


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
            surfaceTexture.setDefaultBufferSize(3072,4096)//(3072x4096)//(1280, 720)//(1920,1080)
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
            characteristicsInitial = state.characteristics ,
            onSettingsClick = onSettingsClick,
            onStartVideoRecord = {
                cameraViewModel.setEvent(
                    CameraContract.Event.OnStartVideoRecord
                )
            },
            onCharacteristicChanged = {characteristics->
                cameraViewModel.setEvent(
                    CameraContract.Event.OnSetCharacteristics(characteristics)
                )
            }
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

}