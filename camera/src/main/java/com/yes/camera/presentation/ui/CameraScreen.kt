package com.yes.camera.presentation.ui

import android.Manifest
import android.content.Intent
import android.graphics.SurfaceTexture
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.ui.custom.compose.CameraPreviewContainer
import com.yes.camera.presentation.ui.custom.gles.GLRenderer
import com.yes.camera.presentation.ui.views.CameraScreenSuccess
import com.yes.camera.presentation.ui.views.ErrorScreen
import com.yes.camera.presentation.vm.CameraViewModel
import com.yes.shared.presentation.ui.PermissionManager
import com.yes.shared.utils.CameraThreadManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first


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
    var surface: SurfaceTexture? by remember {
        mutableStateOf(null)
    }
    val cameraThreadManager = remember { CameraThreadManager() }
    var hasPermission by remember { mutableStateOf(false) }
    var isPermanentDenied by remember { mutableStateOf(false) }
   /* PermissionManager(
        permissions = arrayOf(
            Manifest.permission.CAMERA,
            //  Manifest.permission.READ_MEDIA_IMAGES
        ),
        onPermissionsGranted = {
            hasPermission = true
            // Всё содержимое из оригинальной ветки if (permissionsGranted)


        },
        onPermissionsDenied = { isPermanent, onRetry ->
            hasPermission = false


        }
    )*/
    val registryOwner = LocalActivityResultRegistryOwner.current
        ?: error("No ActivityResultRegistryOwner found")
    val permissionManager = remember {
        PermissionManager(
            context = context,
            registry = registryOwner.activityResultRegistry,
            permissions = arrayOf(Manifest.permission.CAMERA),
            onPermissionsGranted = {
                hasPermission = true // Рендерер ниже сразу увидит этот флаг
            },
            onPermissionsDenied = { isPermanent ->
                isPermanentDenied = isPermanent
            }
        )
    }

    // 2. Жизненный цикл менеджера разрешений.
    // Отрабатывает строго ОДИН РАЗ при открытии и закрытии экрана.
    DisposableEffect(permissionManager) {
        permissionManager.register()
        permissionManager.checkAndRequestPermissions()

        onDispose {
            permissionManager.unregister() // Чистим за собой реестр лаунчеров
        }
    }
    val renderer = remember(hasPermission) {
        if (hasPermission) {
            GLRenderer(
                context = context,
                cameraHandler = cameraThreadManager.handler
            ) { surfaceTexture ->

                surface = surfaceTexture
                surfaceTexture.setDefaultBufferSize(640, 480/*,4096,3072*//*1920, 1080*/)
                cameraViewModel.setEvent(
                    CameraContract.Event.OnOpenCamera(true, surfaceTexture)
                )
            }
        } else null

    }
    var surfaceViewSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(renderer) {
        // Ждем, когда размеры станут известны (не 0)
        snapshotFlow { surfaceViewSize }
            .filter { it.width > 0 && it.height > 0 }
            .first() // Берем только самое первое валидное значение
            .let { size ->
                val normalizedX =
                    (size.width.toFloat() / 2f / size.width.toFloat()) * 2f - 1f
                val normalizedY =
                    -((size.height.toFloat() / 2f / size.height.toFloat()) * 2f - 1f)
                renderer?.handleTouchPress(normalizedX, normalizedY)
                renderer?.configureMagnifier(1f)
            }
    }
    val onPreviewTouch = remember(cameraViewModel) {
        fun(offset: Offset) { // Используем ключевое слово fun для точного вывода типа
            val freshestState = cameraViewModel.uiState.value.state
            (freshestState as? CameraContract.CameraState.Success)?.let { successState ->
                cameraViewModel.setEvent(
                    CameraContract.Event.OnSetCharacteristics(
                        successState.characteristics.copy(touchPoint = offset)
                    )
                )
            }
        }
    }
    val viewState = cameraViewModel.uiState.collectAsState()
    val currentState = viewState.value.state
    if (hasPermission && renderer != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            val successCharacteristics = (currentState as? CameraContract.CameraState.Success)?.characteristics

            // 1. ОПТИМИЗАЦИЯ: Извлекаем примитивы и привязываем их к remember.
            // Теперь мы не создаем нестабильный Pair.
            val isFullScreen = remember(successCharacteristics?.fullScreen) {
                successCharacteristics?.fullScreen ?: false
            }

            // Передаем aspectRatio. ОБРАТИТЕ ВНИМАНИЕ: если ваш класс aspectRatio внутри структуры
            // часто пересоздается во ViewModel, лучше передавать отдельно ширину и высоту (Int)!
            val currentAspectRatio = remember(successCharacteristics?.aspectRatio) {
                successCharacteristics?.aspectRatio
            }

            // 2. ОПТИМИЗАЦИЯ: Фиксируем колбэк изменения размера.
            // Теперь эта лямбда имеет стабильную ссылку и не провоцирует перерисовку.
            val onSizeChangedIndexed = remember {
                { size: IntSize -> surfaceViewSize = size }
            }

            // ТЕПЕРЬ ВСЕ АРГУМЕНТЫ НА 100% СТАБИЛЬНЫ:
            // renderer — закеширован
            // fullScreen — примитив Boolean (Stable)
            // aspectRatio — закеширован через remember
            // onSizeChanged — закеширован
            // onTouchPoint — закеширован (onPreviewTouch)
            //
            // Результат: При изменении ISO этот блок кода будет СКИПАТЬСЯ (Skip)!
            CameraPreviewContainer(
                renderer = renderer,
                fullScreen = isFullScreen,
                aspectRatio = currentAspectRatio,
                onSizeChanged = onSizeChangedIndexed,
                onTouchPoint = onPreviewTouch
            )


            when (currentState) {
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
                        characteristicsInit = currentState.characteristics,
                        onSettingsClick = {
                            cameraViewModel.setEvent(CameraContract.Event.OnCloseCamera)
                            onSettingsClick()
                        },
                        onStartVideoRecord = { enabled ->
                            cameraViewModel.setEvent(CameraContract.Event.OnStartVideoRecord(enabled))
                        },
                        onSetCharacteristic = { characteristics ->
                            cameraViewModel.setEvent(
                                CameraContract.Event.OnSetCharacteristics(characteristics)
                            )
                        },
                        // fullScreen = state.characteristics.fullScreen
                    )
                }

                is CameraContract.CameraState.Error -> {
                    ErrorScreen(error = currentState.error) {
                        surface?.let {
                            cameraViewModel.setEvent(
                                CameraContract.Event.OnOpenCamera(true, it)
                            )
                        }

                    }
                }
            }
        }
    }else{
       /* Column {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPermanent) {
                    Text("Разрешение заблокировано. Пожалуйста, включите его в настройках.")
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        context.startActivity(intent)
                    }) {
                        Text("Открыть настройки")
                    }
                } else {
                    Text("Для работы приложения необходим доступ к камере")
                    Button(onClick = onRetry) {
                        Text("Дать разрешение")
                    }
                }
            }
        }*/
        CameraScreenError(
            //  onRetryPermission: () -> Unit,
            onSettingsClick= {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(intent)
            }
        )
    }


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


