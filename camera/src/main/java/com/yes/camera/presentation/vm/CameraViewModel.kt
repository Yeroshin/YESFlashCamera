package com.yes.camera.presentation.vm

import android.graphics.SurfaceTexture
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Stable
class CameraViewModel(
    private val mapper: MapperUI,
    private val openCameraUseCase: OpenCameraUseCase,
    private val closeCameraUseCase: CloseCameraUseCase,
    private val setInputCharacteristicsUseCase: SetInputCharacteristicsUseCase,
    private val recordVideoUseCase: RecordVideoUseCase,
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
            onError = { Log.e(TAG, "Video record error: ${it.message}") },
            onComplete = { Log.d(TAG, "Video record command sequence completed (enabled=$enabled)") },
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

    var oldChar: Characteristics? = null
    private fun setCharacteristics(characteristics: CharacteristicsUI) {
        val domainModel = mapper.map(characteristics)
        
        oldChar?.let {
            val changes = getChanges(it, domainModel)
            val t = changes
        }
        oldChar = domainModel

        // Используем встроенный в runner метод launchHybridUseCase:
        // Передаем привязанный (bound) UseCaseAction
        launchHybridUseCase(
            onComplete = { Log.d(TAG, "Characteristics update and persistence completed") },
            block = setInputCharacteristicsUseCase.bind(
                SetInputCharacteristicsUseCase.Params(domainModel)
            )
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
                // 1. Инициируем открытие
                openCameraUseCase(
                    OpenCameraUseCase.Params(backCamera, surfaceTexture)
                )

                // 2. Ждем ПЕРВОГО реального значения характеристик (подтверждение успеха)
                val firstData = subscribeCameraSettingsUseCase().first()
                _characteristicsInternal.value = mapper.map(firstData)

                // 3. ТОЛЬКО ТЕПЕРЬ запускаем постоянную подписку в фоне
                useCaseCoroutineScope.launch {
                    subscribeCameraSettingsUseCase()
                        .collect { characteristics ->
                            _characteristicsInternal.value = mapper.map(characteristics)
                        }
                }

                // 4. И только теперь переходим в состояние Success
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
