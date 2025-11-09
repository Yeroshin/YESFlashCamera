package com.yes.settings.domain.usecase

import com.yes.settings.data.repository.SettingsRepository
import com.yes.settings.domain.model.Settings
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class SetSettingsUseCase(
    dispatcher: CoroutineDispatcher,
    private val settingsRepository: SettingsRepository
) : UseCase<SetSettingsUseCase.Params, Unit>(dispatcher) {

    override suspend fun run(params:Params) {
        settingsRepository.setSettings(params.settings)
    }

    data class Params(
        val settings: Settings
    )
}