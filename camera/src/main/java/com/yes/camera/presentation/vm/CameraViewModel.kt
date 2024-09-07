package com.yes.camera.presentation.vm

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.SetCharacteristicsUsCase
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.camera.presentation.model.SettingsItemUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel


class CameraViewModel(
    private val mapper:MapperUI,
    private val openCameraUseCase: OpenCameraUseCase,
    private val setCharacteristicsUsCase: SetCharacteristicsUsCase
): BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
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
                openCamera(event.backCamera,event.surfaceTexture)
            }

            is Event.OnSetCharacteristics -> {
                setCharacteristics(event.characteristics)
            }
        }
    }
    private fun setCharacteristics(characteristics:CharacteristicsUI){
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                val camera=setCharacteristicsUsCase(
                    SetCharacteristicsUsCase.Params(
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
    private fun openCamera(backCamera:Boolean,surfaceTexture: SurfaceTexture){
        withUseCaseScope(
            //  loadingUpdater = { isLoading -> updateUiState { copy(isLoading = isLoading) } },
            onError = { println(it.message) },
            block = {
                val camera=openCameraUseCase(
                    OpenCameraUseCase.Params(backCamera,surfaceTexture)
                )
                setState {
                    copy(
                        state = CameraState.Success(
                            characteristics = mapper.map(camera)
                        )

                    )
                }
            }
        )
    }
    class Factory(
        private val mapper:MapperUI,
        private val openCameraUseCase: OpenCameraUseCase,
        private val setCharacteristicsUsCase: SetCharacteristicsUsCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                mapper,
                openCameraUseCase,
                setCharacteristicsUsCase
            ) as T
        }
    }
}