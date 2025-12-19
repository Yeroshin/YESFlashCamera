package com.yes.camera.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SubscribeCameraSettingsUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Flow<Characteristics>>(dispatcher) {
    private val scope = CoroutineScope(dispatcher)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(): Flow<Characteristics> {
       val histogramFlow= cameraRepository.subscribeOutputBuffer()
           .map { buffer ->

               ////////////////////
               /* Оптимизация:
                to do in future
                kotlin
                val histogram = IntArray(256) { 0 }
                for (i in buffer.indices step stepValue) {
                    histogram[buffer[i].toUByte().toInt()]++
                }*/
               /////////////////////
               val myMap: MutableMap<Int, Int> = (0..255).associateWith { 0 }.toMutableMap()
               for(i in buffer.indices step 600){
                   // for(i in buffer.indices step 250){
                   val keyValue= buffer[i].toInt() and 0xFF
                   myMap[keyValue] = myMap.getOrDefault(keyValue, 0) + 1
               }
               // filterOutliers(myMap)
               myMap
           }
           .stateIn(scope)

       val cameraCharacteristicsFlow=cameraRepository.subscribeCameraSettings().filterNotNull()
       // val settingsFlow=settingsRepository.subscribeSettings()

      //  val combinedCameraFlow: Flow<Pair<Characteristics?, MutableMap<Int, Int>?>>

       return combine(
           histogramFlow,cameraCharacteristicsFlow
        ) {histogram, cameraCharacteristics,->
           val settingsCharacteristics=settingsRepository.getCharacteristics()
            cameraCharacteristics.copy(
                fullscreen = settingsCharacteristics.fullscreen,
                resolution = settingsCharacteristics.resolution,
               histogramData = histogram
            )
        }
      //  return combinedCameraFlow
       /* return cameraRepository.subscribeOutputBuffer()
            .map { buffer ->

                ////////////////////
               /* Оптимизация:
                to do in future
                kotlin
                val histogram = IntArray(256) { 0 }
                for (i in buffer.indices step stepValue) {
                    histogram[buffer[i].toUByte().toInt()]++
                }*/
                /////////////////////
                val myMap: MutableMap<Int, Int> = (0..255).associateWith { 0 }.toMutableMap()
                for(i in buffer.indices step 600){
               // for(i in buffer.indices step 250){
                    val keyValue= buffer[i].toInt() and 0xFF
                    myMap[keyValue] = myMap.getOrDefault(keyValue, 0) + 1
                }
               // filterOutliers(myMap)
                myMap
            }
            .stateIn(scope)*/

    }
}