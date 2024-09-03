package com.yes.camera.presentation.contract

import com.yes.shared.presentation.vm.BaseViewModel
import com.yes.shared.presentation.vm.BaseViewModel.*

class CameraContract {
    sealed class Event : UiEvent {
        data object OnGetOffers : Event()
        data class OnDepartureEntered(val departure:String): Event()

    }
    data class State(
        val state:MainState,
    ) : UiState

    sealed class MainState {
        data object Idle : MainState()
        data object Loading : MainState()
        data object Success: MainState()

    }
    sealed class Effect : UiEffect {
        data object UnknownException : Effect()
    }
}