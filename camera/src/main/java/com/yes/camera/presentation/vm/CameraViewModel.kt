package com.yes.camera.presentation.vm

import android.graphics.SurfaceTexture
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.usecase.CloseCameraUseCase
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.SaveCapturedImageUseCase
import com.yes.camera.domain.usecase.SingleCaptureUseCase
import com.yes.camera.domain.usecase.SetInputCharacteristicsUseCase
import com.yes.camera.domain.usecase.SubscribeCameraSettingsUseCase
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
class CameraViewModel(
    private val mapper: MapperUI,
    private val openCameraUseCase: OpenCameraUseCase,
    private val closeCameraUseCase: CloseCameraUseCase,
    private val setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
    private val singleCaptureUseCase: SingleCaptureUseCase,
    private val saveCapturedImageUseCase: SaveCapturedImageUseCase,
    private val subscribeCameraSettingsUseCase: SubscribeCameraSettingsUseCase,
) : BaseViewModel<Event, State, Effect>() {
    private val TAG = "CameraViewModel"

    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
    }

    private val _characteristicsInternal = MutableStateFlow(CharacteristicsUI())

    override fun createInitialState(): State {
        return State(
            CameraState.Idle
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

            is Event.OnSelectCategory -> {
                _characteristicsInternal.update { it.copy(selectedCategory = event.category) }
            }

            is Event.OnStartVideoRecord -> {
                startVideoRecord(event.enabled)
            }

            Event.OnSingleCapture -> {
                performSingleCapture()
            }

            Event.OnSaveCapturedImage -> {
                saveCapturedImage()
            }

            Event.OnCancelCapturedImage -> {
                returnToSuccessState()
            }

            Event.OnCloseCamera -> {
                closeCamera()
            }
        }
    }

    private fun startVideoRecord(enabled: Boolean) {
        withUseCaseScope(
            onError = { Log.e(TAG, "Video record error: ${it.message}") },
            onComplete = {
                Log.d(
                    TAG,
                    "Video record command sequence completed (enabled=$enabled)"
                )
            },
            block = {
                singleCaptureUseCase(
                    SingleCaptureUseCase.Params(enable = enabled)
                )
            }
        )
    }

    private fun performSingleCapture() {
        withUseCaseScope(
            onError = { Log.e(TAG, "Single capture error: ${it.message}") },
            block = {
                singleCaptureUseCase().collect { bitmap ->
                    if (bitmap != null) {
                        setState {
                            copy(
                                state = CameraState.CapturedPreview(bitmap)
                            )
                        }
                    }
                }
            }
        )
    }

    private fun saveCapturedImage() {
        withUseCaseScope(
            onError = { Log.e(TAG, "Save captured image error: ${it.message}") },
            block = {
                saveCapturedImageUseCase()
                returnToSuccessState()
            }
        )
    }

    private fun returnToSuccessState() {
        _characteristicsInternal.update { it.copy(capturedBitmap = null) }
        setState {
            copy(
                state = CameraState.Success(
                    characteristicsFlow = _characteristicsInternal.asStateFlow()
                )
            )
        }
    }

    fun getChanges(old: Characteristics, new: Characteristics): Map<String, Pair<Any?, Any?>> {
        val changes = mutableMapOf<String, Pair<Any?, Any?>>()

        old::class.java.declaredFields.forEach { field ->
            field.isAccessible = true
            val oldVal = field.get(old)
            val newVal = field.get(new)

            if (oldVal != newVal) {
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

    private fun setCharacteristics(characteristics: CharacteristicsUI) {
        _characteristicsInternal.value = characteristics
        val domainModel = mapper.map(characteristics)

        launchHybridUseCase(
            block = setInputCharacteristicsUseCase.bind(
                SetInputCharacteristicsUseCase.Params(domainModel)
            ),
            onComplete = { Log.d(TAG, "Characteristics update and persistence completed") }
        )
    }

    private fun openCamera(backCamera: Boolean, surfaceTexture: SurfaceTexture) {
        withUseCaseScope(
            loadingUpdater = { isLoading ->
                if (isLoading) {
                    setState { copy(state = CameraState.Loading) }
                }
            },
            onError = {
                setState { copy(state = CameraState.Error(it)) }
            },
            block = {
                openCameraUseCase(
                    OpenCameraUseCase.Params(backCamera, surfaceTexture)
                )

                val firstData = subscribeCameraSettingsUseCase().first()
                _characteristicsInternal.value = mapper.map(firstData)

                useCaseCoroutineScope.launch {
                    subscribeCameraSettingsUseCase()
                        .collect { domainCharacteristics ->
                            _characteristicsInternal.update { currentUi ->
                                mapper.merge(currentUi, domainCharacteristics)
                            }
                            if (domainCharacteristics.capturedBitmap != null) {
                                setState {
                                    copy(
                                        state = CameraState.CapturedPreview(domainCharacteristics.capturedBitmap)
                                    )
                                }
                            }
                        }
                }

                setState {
                    copy(
                        state = CameraState.Success(
                            characteristicsFlow = _characteristicsInternal.asStateFlow()
                        )
                    )
                }
            },
            onComplete = { Log.d(TAG, "Camera opening sequence finished") }
        )
    }

    private fun closeCamera() {
        withUseCaseScope(
            onError = { Log.e(TAG, "Close camera error: ${it.message}") },
            onComplete = { Log.d(TAG, "Camera closing sequence completed") },
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
        private val singleCaptureUseCase: SingleCaptureUseCase,
        private val saveCapturedImageUseCase: SaveCapturedImageUseCase,
        private val subscribeCameraSettingsUseCase: SubscribeCameraSettingsUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                mapper,
                openCameraUseCase,
                closeCameraUseCase,
                setInputCharacteristicsUseCase,
                singleCaptureUseCase,
                saveCapturedImageUseCase,
                subscribeCameraSettingsUseCase,
            ) as T
        }
    }
}
