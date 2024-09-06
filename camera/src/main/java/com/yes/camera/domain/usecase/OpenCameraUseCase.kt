package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class OpenCameraUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository
) : UseCase<OpenCameraUseCase.Params, Characteristics>(dispatcher) {
    override suspend fun run(params: Params): Characteristics {
        return if (params.backCamera) {
            cameraRepository.openBackCamera(params.glSurfaceTexture).filterNotNull().first()
        } else {
            cameraRepository.openFrontCamera(params.glSurfaceTexture).filterNotNull().first()
        }
    }

    data class Params(
        val backCamera: Boolean,
        val glSurfaceTexture: SurfaceTexture
    )
}