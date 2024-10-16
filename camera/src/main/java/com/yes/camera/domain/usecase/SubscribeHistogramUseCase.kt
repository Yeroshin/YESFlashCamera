package com.yes.camera.domain.usecase

import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.MediaEncoder
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow

class SubscribeHistogramUseCase (
    dispatcher: CoroutineDispatcher,
    private val mediaEncoder: MediaEncoder
) : UseCase<Unit, StateFlow<ByteArray?>>(dispatcher) {
    override suspend fun run(): StateFlow<ByteArray?> {
        return mediaEncoder.subscribeOutputBuffer()
    }
}