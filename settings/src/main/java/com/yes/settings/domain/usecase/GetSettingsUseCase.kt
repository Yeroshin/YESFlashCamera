package com.yes.settings.domain.usecase

import android.graphics.SurfaceTexture
import com.yes.settings.data.repository.SettingsRepository
import com.yes.settings.domain.model.Settings
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    dispatcher: CoroutineDispatcher,
    private val settingsRepository: SettingsRepository
) : UseCase<Unit,Settings> (dispatcher) {

    override suspend fun run():Settings {
        val   resolutionValue = settingsRepository.getResolutionValue()


        return Settings(
            resolutionValue = resolutionValue?: throw IllegalStateException("Resolutions items is null"),
           // resolutionValue = settingsRepository.getResolutionValue()?: throw IllegalStateException("Resolution value is null"),
            resolutionItems = settingsRepository.getResolutions()?: throw IllegalStateException("Resolutions items is null")
        )
    }
}