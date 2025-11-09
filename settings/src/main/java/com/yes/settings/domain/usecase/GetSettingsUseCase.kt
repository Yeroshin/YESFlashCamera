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
      /*  val   resolutionValue = settingsRepository.getResolutionValue()
        val items=settingsRepository.getResolutions()
val t=items*/
        return settingsRepository.getSettings()
      /*  return Settings(
            resolutionValue = settingsRepository.getResolutionValue()?: throw IllegalStateException("ResolutionValue item is null"),
           // resolutionValue = settingsRepository.getResolutionValue()?: throw IllegalStateException("Resolution value is null"),
            resolutionItems = settingsRepository.getResolutions()?: throw IllegalStateException("Resolutions items is null")
        )*/
    }
}