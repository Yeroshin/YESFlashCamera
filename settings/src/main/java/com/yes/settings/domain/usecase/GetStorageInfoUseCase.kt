package com.yes.settings.domain.usecase

import com.yes.settings.data.repository.SettingsRepository
import com.yes.settings.domain.model.StorageInfo
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat
import com.yes.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher

class GetStorageInfoUseCase(
    dispatcher: CoroutineDispatcher,
    private val settingsRepository: SettingsRepository
) : UseCase<GetStorageInfoUseCase.Params, StorageInfo>(dispatcher) {

    override suspend fun run(params: Params): StorageInfo {
        return settingsRepository.getStorageInfo(
            customPath = params.path,
            resolution = params.resolution,
            imgFormat = params.imageFormat
        )
    }

    data class Params(
        val path: String? = null,
        val resolution: Dimensions? = null,
        val imageFormat: ImgFormat? = null
    )
}
