package com.yes.settings.domain.usecase

import android.graphics.SurfaceTexture
import com.yes.settings.domain.model.Settings
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    dispatcher: CoroutineDispatcher,
) : UseCase<GetSettingsUseCase.Params,Unit> (dispatcher) {

    override suspend fun run() {

    }

    data class Params(
        val settings:Settings
    )
}