package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class OpenCameraUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository,
    private val closeCameraUseCase: CloseCameraUseCase
) : UseCase<OpenCameraUseCase.Params, Unit>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params) {
        // 1. Закрываем текущую камеру и сессию перед открытием новой
        closeCameraUseCase()

        // 2. Считываем камеру и ее настройки из settingsrepository
        val savedBack = settingsRepository.getBackCamera()
        val savedResolutions = settingsRepository.getResolutions()

        // 3. Определяем целевую камеру: при смене камеры (params.backCamera != null) или по сохраненному значению
        val isCameraSwitch = params.backCamera != null && params.backCamera != savedBack
        val targetBack = params.backCamera ?: savedBack ?: true

        // 4. Получаем ориентацию сенсора и обновляем проекцию
        val targetOrientation = cameraRepository.getCameraSensorOrientation(targetBack)
        cameraRepository.updateCharacteristicsForSwitch(targetBack, targetOrientation)

        // 5. При смене камеры или первом запуске записываем камеру в settingsrepository
        if (savedBack == null || savedResolutions.isNullOrEmpty() || isCameraSwitch) {
            settingsRepository.setBackCamera(targetBack)
        }

        // 6. Открываем выбранную камеру через camerarepository
        val cameraCharacteristics = cameraRepository.openCamera(targetBack)

        // 7. Считываем актуальные настройки и характеристики из camerarepository
        val hardwareInfo = cameraCharacteristics.filterNotNull().first { it.resolutionItems.isNotEmpty() }

        // 8. Если настроек не было в settingsrepository или произошла смена камеры, записываем их туда
        if (savedBack == null || savedResolutions.isNullOrEmpty() || isCameraSwitch) {
            settingsRepository.setResolutions(hardwareInfo.resolutionItems)
            settingsRepository.setSupportsRaw(hardwareInfo.supportsRaw)
        }

        // 9. Определяем рабочее разрешение для открытой камеры
        val currentResolutionValue = settingsRepository.getResolutionValue()
        val selectedResolution = if (currentResolutionValue != null &&
            hardwareInfo.resolutionItems.contains(currentResolutionValue)) {
            currentResolutionValue
        } else {
            val bestSize = hardwareInfo.resolutionItems.maxByOrNull { it.width * it.height } ?: Dimensions(640, 480)
            settingsRepository.setResolutionValue(bestSize)
            bestSize
        }

        // 10. Запускаем сессию в camerarepository
        val settingsCharacteristics = settingsRepository.getCharacteristics()
        cameraRepository.startSession(
            params.glSurfaceTexture,
            settingsCharacteristics.copy(
                backCamera = targetBack,
                resolution = selectedResolution,
                supportsRaw = hardwareInfo.supportsRaw
            )
        )
    }

    data class Params(
        val backCamera: Boolean? = null,
        val glSurfaceTexture: SurfaceTexture
    )
}
