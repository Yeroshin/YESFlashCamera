package com.yes.camera.presentation.vm

import android.graphics.SurfaceTexture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.model.CameraUI
import com.yes.camera.presentation.model.SettingsItemUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel


class CameraViewModel(
    private val mapper:MapperUI,
    private val openCameraUseCase: OpenCameraUseCase
): BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
    }





    override fun createInitialState(): State {
        return State(
            CameraState.Success(
                CameraUI(
                    true,
                    3,
                    listOf(
                        SettingsItemUI("0"),
                        SettingsItemUI("1"),
                        SettingsItemUI("2"),
                        SettingsItemUI("3"),
                        SettingsItemUI("4"),
                        SettingsItemUI("5"),
                        SettingsItemUI("6"),
                        SettingsItemUI("7"),
                        SettingsItemUI("8"),
                        SettingsItemUI("9"),
                        SettingsItemUI("10"),
                        SettingsItemUI("11"),
                        SettingsItemUI("12"),
                        SettingsItemUI("13"),
                    ),
                    listOf(
                        SettingsItemUI("000"),
                        SettingsItemUI("100"),
                        SettingsItemUI("200"),
                        SettingsItemUI("300"),
                        SettingsItemUI("400"),
                        SettingsItemUI("500"),
                        SettingsItemUI("600"),
                        SettingsItemUI("700"),
                        SettingsItemUI("800"),
                        SettingsItemUI("900"),
                        SettingsItemUI("1000"),
                        SettingsItemUI("1100"),
                        SettingsItemUI("1200"),
                        SettingsItemUI("1300"),
                    )
                )

            )
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            Event.OnGetOffers -> {}
            is Event.OnOpenCamera -> {
                openCamera(event.backCamera,event.surfaceTexture)
            }
        }
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
                            camera = mapper.map(camera)
                        )

                    )
                }
            }
        )
    }
    class Factory(
        private val mapper:MapperUI,
        private val openCameraUseCase: OpenCameraUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                mapper,
                openCameraUseCase
            ) as T
        }
    }
}