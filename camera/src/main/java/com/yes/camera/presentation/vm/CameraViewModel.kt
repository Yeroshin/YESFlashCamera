package com.yes.camera.presentation.vm

import android.graphics.SurfaceTexture
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.usecase.CloseCameraUseCase
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.RecordVideoUseCase
import com.yes.camera.domain.usecase.SetInputCharacteristicsUseCase
import com.yes.camera.domain.usecase.SubscribeCameraSettingsUseCase
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Stable
class CameraViewModel(
    private val mapper: MapperUI,
    private val openCameraUseCase: OpenCameraUseCase,
    private val closeCameraUseCase: CloseCameraUseCase,
    private val setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
    private val recordVideoUseCase: RecordVideoUseCase,
    private val subscribeCameraSettingsUseCase: SubscribeCameraSettingsUseCase,
) : BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
    }

    init {
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = {
                println(it.message)
                setState {
                    copy(
                        state = CameraState.Error(
                            error = it
                        )
                    )
                }
            },
            block = {
                subscribeCameraSettingsUseCase()
                    .collect { characteristics ->
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
    fun getChanges(old: Characteristics, new: Characteristics): Map<String, Pair<Any?, Any?>> {
        val changes = mutableMapOf<String, Pair<Any?, Any?>>()

        // Используем Java Reflection (она доступна по умолчанию)
        old::class.java.declaredFields.forEach { field ->
            field.isAccessible = true // Даем доступ к приватным полям
            val oldVal = field.get(old)
            val newVal = field.get(new)

            if (oldVal != newVal) {
                // Проверка для массивов (wbModeItems), так как у них != сравнивает ссылки
                if (oldVal is IntArray && newVal is IntArray) {
                    if (!oldVal.contentEquals(newVal)) {
                        changes[field.name] = oldVal to newVal
                    }
                } else {
                    changes[field.name] = oldVal to newVal
                }
            }
        }
        return changes
    }
    var oldChar: Characteristics?=null
    private fun setCharacteristics(characteristics: CharacteristicsUI) {
       ////////////////////

        oldChar?.let {
            val changes = getChanges(it, mapper.map(characteristics))
            val t = changes
        }
        oldChar=mapper.map(characteristics)
        val t =oldChar
        /////////////////////


        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = {
                println(it.message)
                setState {
                    copy(
                        state = CameraState.Error(
                            error = it
                        )
                    )
                }
            },
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
            onError = {
                println(it.message)
            },
            block = {
                openCameraUseCase(
                    OpenCameraUseCase.Params(backCamera, surfaceTexture)
                )/*.collect { characteristics ->
                    setState {
                        copy(
                            state = CameraState.Success(
                                characteristics = mapper.map(characteristics)
                            )
                        )
                    }
                }*/
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
        private val subscribeCameraSettingsUseCase: SubscribeCameraSettingsUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                mapper,
                openCameraUseCase,
                closeCameraUseCase,
                setInputCharacteristicsUseCase,
                recordVideoUseCase,
                subscribeCameraSettingsUseCase,
            ) as T
        }
    }
}