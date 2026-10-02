package com.yes.settings.presentation.wm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.settings.domain.model.Settings
import com.yes.settings.domain.usecase.GetSettingsUseCase
import com.yes.settings.domain.usecase.GetStorageInfoUseCase
import com.yes.settings.domain.usecase.SetSettingsUseCase
import com.yes.settings.presentation.contract.SettingsContract.*
import com.yes.settings.presentation.mapper.MapperUI
import com.yes.settings.presentation.model.SettingsUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setSettingsUseCase: SetSettingsUseCase,
    private val getStorageInfoUseCase: GetStorageInfoUseCase,
    private val mapperUI: MapperUI
) : BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveSettingsDependency(): BaseDependency
    }

    init {
        withUseCaseScope(
            onError = {
                println(it.message)
            },
            block = {
                getSettingsUseCase()
                    .collect { settings ->
                        val storageInfo = getStorageInfoUseCase(
                            GetStorageInfoUseCase.Params(
                                path = settings.filePath,
                                resolution = settings.resolutionValue,
                                imageFormat = settings.imageFormat
                            )
                        )
                        setState {
                            copy(
                                state = SettingsState.Success(
                                    mapperUI.map(settings, storageInfo)
                                )
                            )
                        }
                    }
            }
        )
    }

    override fun createInitialState(): State {
        return State(
            SettingsState.Idle
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.OnGetSettings -> {
            }

            is Event.OnSetSettings -> {
                setSettings(event.settings)
            }
        }
    }

    private fun setSettings(settingsUI: SettingsUI) {
        withUseCaseScope(
            onError = { println(it.message) },
            block = {
                val domainSettings = mapperUI.map(settingsUI)
                setSettingsUseCase(
                    SetSettingsUseCase.Params(
                        settings = domainSettings
                    )
                )
                // Immediate recalculation for instant UI feedback
                val storageInfo = getStorageInfoUseCase(
                    GetStorageInfoUseCase.Params(
                        path = domainSettings.filePath,
                        resolution = domainSettings.resolutionValue,
                        imageFormat = domainSettings.imageFormat
                    )
                )
                setState {
                    copy(
                        state = SettingsState.Success(
                            mapperUI.map(domainSettings, storageInfo)
                        )
                    )
                }
            }
        )
    }

    class Factory(
        val getSettingsUseCase: GetSettingsUseCase,
        val setSettingsUseCase: SetSettingsUseCase,
        val getStorageInfoUseCase: GetStorageInfoUseCase,
        val mapperUI: MapperUI
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                getSettingsUseCase,
                setSettingsUseCase,
                getStorageInfoUseCase,
                mapperUI
            ) as T
        }
    }
}
