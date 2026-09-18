package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
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
    private val settingsRepository: SettingsRepository
) : UseCase<OpenCameraUseCase.Params, Unit>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params) {
        // 1. Получаем настройки и открываем камеру
        val settingsCharacteristics = settingsRepository.getCharacteristics()
        val cameraCharacteristics = cameraRepository.openCamera(settingsCharacteristics.backCamera ?: true)

        // 2. Ждем получения аппаратных характеристик (поддерживаемые размеры и т.д.)
        val hardwareInfo = cameraCharacteristics.filterNotNull().first()

        // 3. Определяем рабочее разрешение
        // Если в настройках 0x0 или пусто - берем максимальное доступное
        val selectedResolution = if (settingsCharacteristics.resolution.width > 0) {
            settingsCharacteristics.resolution
        } else {
            val bestSize = hardwareInfo.resolutionItems.maxByOrNull { it.width * it.height } ?: Dimensions(640, 480)
            settingsRepository.setResolutionValue(bestSize)
            bestSize
        }

        // 4. Сохраняем список всех доступных разрешений
        settingsRepository.setResolutions(hardwareInfo.resolutionItems)

        // 5. Запускаем сессию с валидными данными
        cameraRepository.startSession(
            params.glSurfaceTexture,
            settingsCharacteristics.copy(resolution = selectedResolution)
        )
    }

    data class Params(
        val backCamera: Boolean,
        val glSurfaceTexture: SurfaceTexture
    )
}
