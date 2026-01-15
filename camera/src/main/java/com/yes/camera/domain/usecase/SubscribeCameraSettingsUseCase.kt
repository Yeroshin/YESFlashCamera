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
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlin.math.exp
import kotlin.time.Duration.Companion.milliseconds

class SubscribeCameraSettingsUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Flow<Characteristics>>(dispatcher) {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(): Flow<Characteristics> {
      /* val histogramFlow= cameraRepository.subscribeOutputBuffer()
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
           }*/
         //  .stateIn(scope)
        val histogramFlow = cameraRepository.subscribeOutputBuffer()
            .map { buffer ->
                // Шаг 1: Построим гистограмму из полного буфера (без пропусков для большей точности).
                // Оптимизация: Если буфер большой, можно использовать шаг (например, step 10), но для идеальной гистограммы - полный проход.
                val stepValue = 100 // Измените на 10, 50 или другое для скорости, если буфер большой
                val histogram = IntArray(256) { 0 }
                for (i in buffer.indices step stepValue) {
                    val value = buffer[i].toInt() and 0xFF
                    histogram[value]++
                }

                // Шаг 2: Применим гауссово сглаживание к гистограмме, чтобы сгладить пики и пробелы от пропусков.
                // Гауссово сглаживание использует convolution с гауссовым ядром.
                // Параметры: kernelSize (нечетное число, например 5 для скорости), sigma (например 1.0 для умеренного сглаживания).
                val smoothedHistogram = applyGaussianSmoothing(histogram, kernelSize = 5, sigma = 1.0f)

                // Преобразуем обратно в MutableMap (если ваш код ожидает Map)
                val smoothedMap: MutableMap<Int, Int> = (0..255).associateWith { 0 }.toMutableMap()
                smoothedHistogram.forEachIndexed { index, value ->
                    smoothedMap[index] = value
                }
                smoothedMap
            }.sample(17.milliseconds)
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
    private fun applyGaussianSmoothing(histogram: IntArray, kernelSize: Int, sigma: Float): IntArray {
        require(kernelSize % 2 == 1) { "Kernel size must be odd" }
        val radius = kernelSize / 2
        val kernel = FloatArray(kernelSize)

        // Генерация гауссова ядра
        var sum = 0.0f
        for (i in -radius..radius) {
            kernel[i + radius] = exp(-(i * i).toFloat() / (2 * sigma * sigma))
            sum += kernel[i + radius]
        }
        // Нормализация ядра
        kernel.forEachIndexed { index, _ -> kernel[index] /= sum }

        // Convolution: применяем ядро к гистограмме
        val smoothed = IntArray(256)
        for (i in 0 until 256) {
            var weightedSum = 0.0f
            for (j in -radius..radius) {
                val neighborIndex = (i + j).coerceIn(0, 255)  // Обработка границ (clamp)
                weightedSum += histogram[neighborIndex] * kernel[j + radius]
            }
            smoothed[i] = weightedSum.toInt()  // Округляем до int (поскольку это подсчеты)
        }
        return smoothed
    }
}