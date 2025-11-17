package com.yes.settings.presentation.wm

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.settings.domain.model.Settings
import com.yes.settings.domain.usecase.GetSettingsUseCase
import com.yes.settings.domain.usecase.SetSettingsUseCase
import com.yes.settings.presentation.contract.SettingsContract
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel
import com.yes.settings.presentation.contract.SettingsContract.*
import com.yes.settings.presentation.mapper.MapperUI
import com.yes.settings.presentation.model.SettingsUI

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setSettingsUseCase: SetSettingsUseCase,
    private val mapperUI: MapperUI
) : BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveSettingsDependency(): BaseDependency
    }

    init {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = {
                println(it.message)
                      },
            block = {
                getSettingsUseCase()
                    .collect{settings->
                        setState {
                            copy(
                                state = SettingsState.Success(
                                    mapperUI.map(settings)
                                )
                            )
                        }
                    }



                /* subscribeHistogramUseCase()
                     .collect { histogramData ->
                         setState {
                             copy(
                                 histogram = histogramData

                             )
                         }
                     }*/
            }
        )

    }

    override fun createInitialState(): State {
        return State(
            SettingsState.Success(
                SettingsUI()
            )
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.OnGetSettings -> {

            }
            is Event.OnSetSettings ->{
                setSettings(event.settings)
            }
        }
    }


    private fun setSettings(settings:SettingsUI) {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                setSettingsUseCase(
                    SetSettingsUseCase.Params(
                        settings =mapperUI.map(
                            settings
                        )
                    )
                )
            }
        )
    }


    class Factory(
        val getSettingsUseCase: GetSettingsUseCase,
        val setSettingsUseCase: SetSettingsUseCase,
        val mapperUI: MapperUI
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(
                getSettingsUseCase,
                setSettingsUseCase,
                mapperUI
            ) as T
        }
    }
}