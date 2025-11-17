package com.yes.camera.presentation.vm

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.camera.data.repository.SettingsRepository
import com.yes.camera.domain.usecase.CloseCameraUseCase
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.RecordVideoUseCase
import com.yes.camera.domain.usecase.SetInputCharacteristicsUseCase
import com.yes.camera.domain.usecase.SubscribeHistogramUseCase
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel



class CameraViewModel(
    private val mapper: MapperUI,
    private val openCameraUseCase: OpenCameraUseCase,
    private val closeCameraUseCase: CloseCameraUseCase,
    private val setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
    private val recordVideoUseCase: RecordVideoUseCase,
    private val subscribeHistogramUseCase: SubscribeHistogramUseCase,
) : BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
    }

    init {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                subscribeHistogramUseCase()
                    .collect { data ->
                        setState {
                            copy(
                                histogram = data.second,
                                        state = CameraState.Success(
                                        characteristics = mapper.map(data.first)
                                        )
                            )
                        }
                    }
            }
        )
        /*  viewModelScope.launch {
              subscribeHistogramUseCase()
                  .collect { histogramData ->
                      setState {
                          copy(
                              histogram = histogramData

                          )
                      }
                  }
          }*/
        /*viewModelScope.launch {
            subscribeCharacteristicsUseCase()
                .collect { characteristics ->
                    setState {
                        copy(
                            state = CameraState.Success(
                                characteristics = mapper.map(characteristics)
                            )
                        )
                    }
                }
        }*/
    }

    override fun createInitialState(): State {
        return State(
            CameraState.Success(
                CharacteristicsUI()
            )
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            Event.OnGetOffers -> {}
            is Event.OnOpenCamera -> {
                openCamera(event.backCamera, event.surfaceTexture)
            }

            is Event.OnSetCharacteristics -> {
                setCharacteristics(event.characteristics)
            }

            is Event.OnStartVideoRecord -> {
                startVideoRecord(event.enabled)
            }

            Event.OnCloseCamera -> {
                closeCamera()
            }
        }
    }

    private fun startVideoRecord(enabled: Boolean) {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                recordVideoUseCase(
                    RecordVideoUseCase.Params(enable = enabled)
                )
            }
        )
    }

    private fun setCharacteristics(characteristics: CharacteristicsUI) {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                val camera = setInputCharacteristicsUseCase(
                    SetInputCharacteristicsUseCase.Params(
                        mapper.map(characteristics)
                    )
                )
                /* setState {
                     copy(
                         state = CameraState.Success(
                             characteristics = mapper.map(camera)
                         )

                     )
                 }*/
            }
        )
    }

    private fun openCamera(backCamera: Boolean, surfaceTexture: SurfaceTexture) {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                openCameraUseCase(
                    OpenCameraUseCase.Params(backCamera, surfaceTexture)
                ).collect { characteristics ->
                    setState {
                        copy(
                            state = CameraState.Success(
                                characteristics = mapper.map(characteristics)
                            )
                        )
                    }
                }
            }
        )
    }
    private fun closeCamera() {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                closeCameraUseCase()
            }
        )
    }

    class Factory(
        private val mapper: MapperUI,
        private val openCameraUseCase: OpenCameraUseCase,
        private val closeCameraUseCase: CloseCameraUseCase,
        private val setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
        private val recordVideoUseCase: RecordVideoUseCase,
        private val subscribeHistogramUseCase: SubscribeHistogramUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                mapper,
                openCameraUseCase,
                closeCameraUseCase,
                setInputCharacteristicsUseCase,
                recordVideoUseCase,
                subscribeHistogramUseCase,
            ) as T
        }
    }
}