package com.yes.settings.presentation.wm

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.settings.presentation.contract.SettingsContract
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel
import com.yes.settings.presentation.contract.SettingsContract.*
class SettingsViewModel (

 ) : BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
    }

    init {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
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
               0
            )
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {


            is SettingsContract.Event.OnSetCharacteristics -> {
                setCharacteristics()
            }

            is SettingsContract.Event.OnGetCharacteristics -> {
               // startVideoRecord(event.enabled)
            }

        }
    }



    private fun setCharacteristics() {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {

            }
        )
    }



    class Factory(

    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(

            ) as T
        }
    }
}