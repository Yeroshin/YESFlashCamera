package com.yes.settings.presentation.contract

import android.graphics.SurfaceTexture
import com.yes.shared.presentation.vm.BaseViewModel.UiEffect
import com.yes.shared.presentation.vm.BaseViewModel.UiEvent
import com.yes.shared.presentation.vm.BaseViewModel.UiState

class SettingsContract {
    sealed class Event : UiEvent {
       /* data object OnGetOffers : Event()
        data class OnStartVideoRecord(val enabled:Boolean) : Event()*/
        data class OnGetCharacteristics(
            val backCamera:Boolean,
            val surfaceTexture: SurfaceTexture
        ): Event()
        data class OnSetCharacteristics(
            val characteristics:Int
        ): Event()

    }
    data class State(
        val state:SettingsState,
      //  val histogram:MutableMap<Int,Int>?=null
    ) : UiState

    sealed class SettingsState {
       /* data object Idle : CameraState()
        data object Loading : CameraState()*/
        data class Success(
            val characteristics:Int,
        ):  SettingsState()

    }
    sealed class Effect : UiEffect {
        data object UnknownException : Effect()
    }
}