package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
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
        // 1. Закрываем текущую камеру и сессию, освобождая ресурсы перед открытием новой камеры
        closeCameraUseCase()

        // 2. Определяем целевую камеру: явно переданную или сохраненную в настройках
        val targetBack = params.backCamera ?: settingsRepository.getCharacteristics().backCamera ?: true

        // 3. Получаем ориентацию сенсора целевой камеры и обновляем характеристики (поворот проекции на закрытой камере ДО открытия новой)
        val targetOrientation = cameraRepository.getCameraSensorOrientation(targetBack)
        settingsRepository.setBackCamera(targetBack)
        cameraRepository.updateCharacteristicsForSwitch(targetBack, targetOrientation)

        // 4. Открываем выбранную камеру (основную или фронтальную)
        val settingsCharacteristics = settingsRepository.getCharacteristics()
        val cameraCharacteristics = cameraRepository.openCamera(targetBack)

        // 5. Ждем получения аппаратных характеристик (поддерживаемые размеры и т.д.)
        val hardwareInfo = cameraCharacteristics.filterNotNull().first()

        // 6. Определяем рабочее разрешение для этой камеры
        val selectedResolution = if (settingsCharacteristics.resolution.width > 0 &&
            hardwareInfo.resolutionItems.contains(settingsCharacteristics.resolution)) {
            settingsCharacteristics.resolution
        } else {
            val bestSize = hardwareInfo.resolutionItems.maxByOrNull { it.width * it.height } ?: Dimensions(640, 480)
            settingsRepository.setResolutionValue(bestSize)
            bestSize
        }

        // 7. Сохраняем список всех доступных разрешений
        settingsRepository.setResolutions(hardwareInfo.resolutionItems)

        // 7.1. Записываем актуальную поддержку RAW/DNG в настройки
        settingsRepository.setSupportsRaw(hardwareInfo.supportsRaw)

        // 8. Запускаем сессию с открытой камерой
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
