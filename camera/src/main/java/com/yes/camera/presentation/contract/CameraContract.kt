package com.yes.camera.presentation.contract

import android.graphics.SurfaceTexture
import com.yes.camera.presentation.model.CharacteristicsUI
import com.yes.shared.presentation.vm.BaseViewModel.*

class CameraContract {
    sealed class Event : UiEvent {
        data object OnGetOffers : Event()
        data class OnOpenCamera(
            val backCamera:Boolean,
            val surfaceTexture: SurfaceTexture
        ): Event()
        data class OnSetCharacteristics(
            val characteristics:CharacteristicsUI
        ): Event()

    }
    data class State(
        val state:CameraState,
    ) : UiState

    sealed class CameraState {
        data object Idle : CameraState()
        data object Loading : CameraState()
        data class Success(
            val characteristics:CharacteristicsUI,
       ): CameraState()

    }
    sealed class Effect : UiEffect {
        data object UnknownException : Effect()
    }
}