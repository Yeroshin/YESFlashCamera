package com.yes.camera.domain.usecase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.yes.camera.data.repository.CameraRepository
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.model.Characteristics
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import kotlin.math.exp
import kotlin.time.Duration.Companion.milliseconds

class SubscribeCameraSettingsUseCase(
    dispatcher: CoroutineDispatcher,
    private val cameraRepository: CameraRepository,
    private val settingsRepository: SettingsRepository
) : UseCase<Unit, Flow<Characteristics>>(dispatcher) {

    private val TAG = "SubscribeSettingsUC"

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override suspend fun run(): Flow<Characteristics> {
        // 1. Поток гистограммы. Начинаем с пустой карты, чтобы не блокировать UI.
        val histogramFlow = cameraRepository.subscribeHistogramBuffer()
            .map { buffer ->
                val histogram = IntArray(256)
                for (i in buffer.indices step 20) {
                    val value = buffer[i].toInt() and 0xFF
                    histogram[value]++
                }
                val smoothed = applyGaussianSmoothing(histogram, kernelSize = 5, sigma = 1.0f)
                val result = mutableMapOf<Int, Int>()
                for (i in 0..255) { result[i] = smoothed[i] }
                result
            }
            .onStart { emit(mutableMapOf()) } 
            .sample(100.milliseconds)

        // 2. Поток характеристик из камеры. 
        // Начинаем с текущего значения (даже если оно null), чтобы не блокировать combine.
        val cameraCharacteristicsFlow = cameraRepository.subscribeCameraSettings()
            .onStart { emit(cameraRepository.subscribeCameraSettings().value) }

        // 3. Поток настроек.
        val settingsFlow = settingsRepository.subscribeSettings()
            .onStart { emit(settingsRepository.getCharacteristics()) }

        // 4. Объединение данных.
        return combine(
            cameraCharacteristicsFlow,
            histogramFlow,
            settingsFlow
        ) { cameraData, histogram, settings ->
            // Если данных от камеры еще нет, берем данные из настроек как базу
            val base = cameraData ?: settings
            base.copy(
                fullscreen = settings.fullscreen,
                resolution = settings.resolution,
                histogramData = histogram
            )
        }.catch { e ->
            Log.e(TAG, "Subscription error: ${e.message}")
            throw e
        }
    }

    private fun applyGaussianSmoothing(histogram: IntArray, kernelSize: Int, sigma: Float): IntArray {
        if (kernelSize % 2 == 0) return histogram
        val radius = kernelSize / 2
        val kernel = FloatArray(kernelSize)
        var sum = 0.0f
        for (i in -radius..radius) {
            kernel[i + radius] = exp(-(i * i).toFloat() / (2 * sigma * sigma))
            sum += kernel[i + radius]
        }
        kernel.forEachIndexed { index, _ -> kernel[index] /= sum }
        val smoothed = IntArray(256)
        for (i in 0 until 256) {
            var weightedSum = 0.0f
            for (j in -radius..radius) {
                val neighborIndex = (i + j).coerceIn(0, 255)
                weightedSum += histogram[neighborIndex] * kernel[j + radius]
            }
            smoothed[i] = weightedSum.toInt()
        }
        return smoothed
    }
}
