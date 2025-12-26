package com.yes.camera.presentation.ui

import ads_mobile_sdk.h6
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.presentation.ui.PermissionsHelper

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
  //  context: Context,
    cameraViewModel: CameraViewModel,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val permissions = remember {
        if (Build.VERSION.SDK_INT >= 33) {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    // Инициализируем состояние разрешений сразу на основе реального статуса (без null)
    val cameraPermissionGranted = remember {
        val granted = permissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        mutableStateOf(granted)
    }

    var requestCounter by remember { mutableIntStateOf(0) }

    // PermissionsHelper показывается только если разрешения не предоставлены
    if (!cameraPermissionGranted.value) {
        PermissionsHelper(
            permissions = permissions,
            onPermissionsResult = { granted, _ ->
                // Обновляем состояние после результата запроса
                val newGranted = Manifest.permission.CAMERA in granted
                cameraPermissionGranted.value = newGranted
            },
            rationaleMessage = "Разрешение на камеру требуется для работы с камерой. Пожалуйста, предоставьте доступ.",
            requestCounter = requestCounter
        )
    }

    // Инициализируем renderer и события только если разрешение предоставлено
    if (cameraPermissionGranted.value) {
        val renderer = remember {
            GLRenderer(context) { surfaceTexture ->
                surfaceTexture.setDefaultBufferSize(1920, 1080)
                cameraViewModel.setEvent(
                    CameraContract.Event.OnOpenCamera(true, surfaceTexture)
                )
            }
        }

        // Теперь логика состояний (рекомпозиция только здесь, если состояние VM меняется)
        val viewState = cameraViewModel.uiState.collectAsState()
        when (val state = viewState.value.state) {
            CameraContract.CameraState.Idle -> { /* Показать idle UI если нужно */ }
            CameraContract.CameraState.Loading -> { /* Показать лоадер */ }
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
        }
    } else {
        // Экран ошибки с кнопкой для повторного запроса
        CameraScreenError(
            onRetryPermission = {
                requestCounter++  // Увеличиваем счетчик для повторного запуска LaunchedEffect
            },
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


