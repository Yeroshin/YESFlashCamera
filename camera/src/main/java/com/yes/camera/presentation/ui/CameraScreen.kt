package com.yes.camera.presentation.ui

import ads_mobile_sdk.h6
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.camera.utils.ShutterSpeedsResourcesProvider
import com.yes.camera.presentation.ui.views.CameraScreenSuccess
import com.yes.camera.presentation.ui.views.ErrorScreen
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.presentation.ui.PermissionManager
import com.yes.shared.utils.CameraThreadManager


/*
@Composable
fun CameraScreen(
    onSettingsClick: () -> Unit
    // Убираем cameraViewModel как параметр — он будет создан внутри
) {
    val context = LocalContext.current

    // Получаем фабрику аналогично вашему коду в Activity
    val factory = remember {
        (context.applicationContext as CameraViewModel.DependencyResolver)
            .resolveCameraDependency()
            .viewModelFactory
    }

    // Создаём ViewModel с фабрикой
    val cameraViewModel: CameraViewModel = viewModel(factory = factory)
    val renderer = remember {
        GLRenderer(context) { surfaceTexture ->
            surfaceTexture.setDefaultBufferSize(1920, 1080)
            cameraViewModel.setEvent(
                CameraContract.Event.OnOpenCamera(true, surfaceTexture)
            )
        }
    }

    val viewState = cameraViewModel.uiState.collectAsState()
/////////////////
    when (val state = viewState.value.state) {
        is CameraContract.CameraState.Idle -> { /* Placeholder */ }
        is CameraContract.CameraState.Loading -> { /* Progress indicator */ }
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
                    cameraViewModel.setEvent(CameraContract.Event.OnSetCharacteristics(characteristics))
                },
                fullScreen = state.characteristics.fullScreen
            )
        }
    }

    // Дополнительные remember для оптимизации, как в вашем коде
    val shutterSpeeds = remember { ShutterSpeedsResourcesProvider(context).getShutterSpeeds() }
}
*/

///////////////////////////
///////////////////////////


/////////////////////////////


@Composable
fun CameraScreen(
    cameraViewModel: CameraViewModel,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    var surface:SurfaceTexture? by remember {
        mutableStateOf(null)
    }
    val cameraThreadManager = remember { CameraThreadManager() }
    PermissionManager(
        permissions = arrayOf(
            Manifest.permission.CAMERA,
          //  Manifest.permission.READ_MEDIA_IMAGES
        ),
        onPermissionsGranted = {
            // Всё содержимое из оригинальной ветки if (permissionsGranted)
            val renderer = remember {
                GLRenderer(
                    context = context,
                    cameraHandler = cameraThreadManager.handler
                ) { surfaceTexture ->

                    surface=surfaceTexture
                    surfaceTexture.setDefaultBufferSize(640, 480/*,4096,3072*//*1920, 1080*/)
                    cameraViewModel.setEvent(
                        CameraContract.Event.OnOpenCamera(true, surfaceTexture)
                    )
                }
            }

            val viewState = cameraViewModel.uiState.collectAsState()
            when (val state = viewState.value.state) {
                CameraContract.CameraState.Idle -> {
                    /* Показать idle UI если нужно */
                }
                CameraContract.CameraState.Loading -> {
                    /* Показать лоадер */
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
                                CameraContract.Event.OnSetCharacteristics(characteristics)
                            )
                        },
                        fullScreen = state.characteristics.fullScreen
                    )
                }

                is CameraContract.CameraState.Error -> {
                    ErrorScreen(error =state.error ){
                        surface?.let {
                            cameraViewModel.setEvent(
                                CameraContract.Event.OnOpenCamera(true, it)
                            )
                        }

                    }
                }
            }
        },
        onPermissionsDenied = {
            Text("Cannot proceed without permissions.")
        }
    )
}



// Композит для экрана ошибки
@Composable
fun CameraScreenError(
  //  onRetryPermission: () -> Unit,
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
      /*  Button(onClick = onRetryPermission) {
            Text("Запросить разрешение снова")
        }*/
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSettingsClick) {
            Text("Вернуться в настройки")
        }
    }
}


