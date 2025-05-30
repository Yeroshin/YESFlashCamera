package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class OpenCameraUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<OpenCameraUseCase.Params, Flow<Characteristics>>(dispatcher) {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(params: Params):Flow< Characteristics> {
      /*  return if (params.backCamera) {
            cameraRepository.openBackCamera(params.glSurfaceTexture).filterNotNull()
        } else {
            cameraRepository.openFrontCamera(params.glSurfaceTexture).filterNotNull()
        }*/
val t =settingsRepository.getCharacteristics()
        val v=t
        return cameraRepository.openCamera(
            params.glSurfaceTexture,
            settingsRepository.getCharacteristics()
        ).filterNotNull()
    }

    data class Params(
        val backCamera: Boolean,
        val glSurfaceTexture: SurfaceTexture
    )
}