package com.yes.settings.domain.usecase

import com.yes.settings.domain.model.Settings
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class SetSettingsUseCase(
    dispatcher: CoroutineDispatcher,
) : UseCase<SetSettingsUseCase.Params, Unit>(dispatcher) {

    override suspend fun run() {

    }

    data class Params(
        val settings: Settings
    )
}