package com.yes.camera.presentation.vm

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.yes.camera.domain.model.Characteristics
import com.yes.camera.domain.usecase.ClearCapturedBitmapUseCase
import com.yes.camera.domain.usecase.CloseCameraUseCase
import com.yes.camera.domain.usecase.OpenCameraUseCase
import com.yes.camera.domain.usecase.SaveCapturedImageUseCase
import com.yes.camera.domain.usecase.SetInputCharacteristicsUseCase
import com.yes.camera.domain.usecase.SubscribeCameraSettingsUseCase
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.mapper.MapperUI
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class CameraViewModel(
    private val mapper: MapperUI,
    private val openCameraUseCase: OpenCameraUseCase,
    private val closeCameraUseCase: CloseCameraUseCase,
    private val setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
    private val saveCapturedImageUseCase: SaveCapturedImageUseCase,
    private val subscribeCameraSettingsUseCase: SubscribeCameraSettingsUseCase,
    private val clearCapturedBitmapUseCase: ClearCapturedBitmapUseCase,
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

            is Event.OnShowCapturedPreview -> {
                setState {
                    copy(
                        state = CameraState.CapturedPreview(event.bitmap)
                    )
                }
            }

            Event.OnClearCapturedBitmap -> {
                clearCapturedBitmap()
            }

            Event.OnCloseCamera -> {
                closeCamera()
            }
        }
    }

    private fun startVideoRecord(enabled: Boolean) {
        val captureCharacteristics = _characteristicsInternal.value.copy(isCaptureRequested = enabled)
        _characteristicsInternal.update { it.copy(isCaptureRequested = enabled) }
        val domainModel = mapper.map(captureCharacteristics)
        useCaseCoroutineScope.launch {
            setInputCharacteristicsUseCase.runSync(SetInputCharacteristicsUseCase.Params(domainModel))
        }
    }

    private fun performSingleCapture() {
        val captureCharacteristics = _characteristicsInternal.value.copy(isCaptureRequested = true)
        _characteristicsInternal.update { it.copy(isCaptureRequested = false) }
        val domainModel = mapper.map(captureCharacteristics)
        useCaseCoroutineScope.launch {
            setInputCharacteristicsUseCase.runSync(SetInputCharacteristicsUseCase.Params(domainModel))
        }
    }

    private fun saveCapturedImage() {
        withUseCaseScope(
            onError = { Log.e(TAG, "Save captured image error: ${it.message}") },
            block = {
                if (true) { // subscription stub for future paid feature verification
                    saveCapturedImageUseCase()
                }
                returnToSuccessState()
            }
        )
    }

    private fun clearCapturedBitmap() {
        withUseCaseScope(
            onError = { Log.e(TAG, "Clear captured bitmap error: ${it.message}") },
            block = {
                clearCapturedBitmapUseCase()
                _characteristicsInternal.update { it.copy(capturedBitmap = null) }
            }
        )
    }

    private fun returnToSuccessState() {
        withUseCaseScope(
            onError = { Log.e(TAG, "Clear captured bitmap error: ${it.message}") },
            block = {
                clearCapturedBitmapUseCase()
                _characteristicsInternal.update { it.copy(capturedBitmap = null, isCaptureRequested = false) }
                setState {
                    copy(
                        state = CameraState.Success(
                            characteristicsFlow = _characteristicsInternal.asStateFlow()
                        )
                    )
                }
            }
        )
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

    private var previewJob: Job? = null
    private var persistenceJob: Job? = null
    private var subscriptionJob: Job? = null
    private var lastHandledBitmap: Bitmap? = null

    private fun setCharacteristics(characteristics: CharacteristicsUI) {
        _characteristicsInternal.value = characteristics
        val domainModel = mapper.map(characteristics)

        // 1. Throttled hardware preview request (50ms) to prevent flooding Camera2 setRepeatingRequest during drag
        previewJob?.cancel()
        previewJob = useCaseCoroutineScope.launch {
            delay(50)
            setInputCharacteristicsUseCase.runSync(SetInputCharacteristicsUseCase.Params(domainModel))
        }

        // 2. Immediate background disk persistence
        persistenceJob?.cancel()
        persistenceJob = useCaseCoroutineScope.launch {
            try {
                setInputCharacteristicsUseCase.run(SetInputCharacteristicsUseCase.Params(domainModel))
            } catch (e: Exception) {
                Log.e(TAG, "Persistence error: ${e.message}")
            }
        }
    }

    private fun openCamera(backCamera: Boolean?, surfaceTexture: SurfaceTexture) {
        subscriptionJob?.cancel()
        lastHandledBitmap = null
        subscriptionJob = useCaseCoroutineScope.launch {
            try {
                openCameraUseCase(
                    OpenCameraUseCase.Params(backCamera, surfaceTexture)
                )

                val firstData = subscribeCameraSettingsUseCase().first()
                _characteristicsInternal.value = mapper.map(firstData)

                subscribeCameraSettingsUseCase()
                    .collect { domainCharacteristics ->
                        val newBitmap = domainCharacteristics.capturedBitmap

                        _characteristicsInternal.update { currentUi ->
                            val merged = mapper.merge(currentUi, domainCharacteristics)
                            if (currentUi == merged) currentUi else merged
                        }

                        if (newBitmap != null && newBitmap != lastHandledBitmap) {
                            lastHandledBitmap = newBitmap
                            useCaseCoroutineScope.launch {
                                if (true) { // subscription stub for future paid feature verification
                                    try {
                                        saveCapturedImageUseCase()
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Auto save error: ${e.message}")
                                    }
                                }
                            }
                        } else if (newBitmap == null) {
                            lastHandledBitmap = null
                        }
                    }
            } catch (e: Exception) {
                if (e is java.util.concurrent.CancellationException) {
                    Log.e(TAG, "Error: ${e.message}")
                }else{
                    Log.e(TAG, "Open camera error: ${e.message}", e)
                    setState { copy(state = CameraState.Error(e)) }
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
        private val saveCapturedImageUseCase: SaveCapturedImageUseCase,
        private val subscribeCameraSettingsUseCase: SubscribeCameraSettingsUseCase,
        private val clearCapturedBitmapUseCase: ClearCapturedBitmapUseCase,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(
                mapper,
                openCameraUseCase,
                closeCameraUseCase,
                setInputCharacteristicsUseCase,
                saveCapturedImageUseCase,
                subscribeCameraSettingsUseCase,
                clearCapturedBitmapUseCase,
            ) as T
        }
    }
}
