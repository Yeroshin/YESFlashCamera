package com.yes.camera.domain.usecase

import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.MediaEncoder
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubscribeHistogramUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository
) : UseCase<Unit, StateFlow<MutableMap<Int, Int>?>>(dispatcher) {
    private val scope = CoroutineScope(dispatcher)
    override suspend fun run(): StateFlow<MutableMap<Int, Int>?> {
        var index = 0
        return cameraRepository.subscribeOutputBuffer()
            .map { buffer ->
                val myMap: MutableMap<Int, Int> = (0..255).associateWith { 0 }.toMutableMap()
                for(i in buffer.indices step 600){
                    val keyValue= buffer[i].toInt() and 0xFF
                    myMap[keyValue] = myMap.getOrDefault(keyValue, 0) + 1
                }
               /* for (byte in buffer) {
                    if (index % 150 == 0) {
                        val keyValue =
                            byte.toInt() and 0xFF // convert byte to int and mask with 0xFF to get a value between 0 and 255
                        myMap[keyValue] = myMap.getOrDefault(keyValue, 0) + 1
                    }
                    index++
                }*/
                myMap
            }
            .stateIn(scope)

    }
}