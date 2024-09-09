package com.yes.camera.domain.usecase

import android.graphics.SurfaceTexture
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class RecordVideoUseCase (dispatcher: CoroutineDispatcher,
                          private val cameraRepository: CameraRepository
) : UseCase<Unit, Unit>(dispatcher) {
    override suspend fun run(): Unit {

            cameraRepository.recordVideo()

    }
}