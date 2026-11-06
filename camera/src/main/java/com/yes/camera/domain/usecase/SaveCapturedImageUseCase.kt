package com.yes.camera.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class SaveCapturedImageUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Unit>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Unit) {
        val characteristics = settingsRepository.getCharacteristics()
        val path = characteristics.filePath.ifEmpty { "/DCIM/Camera" }
        val resolution = characteristics.resolution.let {
            if (it.width > 0 && it.height > 0) it else Dimensions(4000, 3000)
        }
        val format = characteristics.imgFormat

        cameraRepository.saveCapturedImage(
            savePath = path,
            imageFormat = format,
            resolution = resolution
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run() {
        run(Unit)
    }
}
