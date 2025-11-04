package com.yes.settings.presentation.contract

import com.yes.settings.domain.model.Settings
import com.yes.settings.presentation.model.SettingsUI
import com.yes.shared.presentation.vm.BaseViewModel.UiEffect
import com.yes.shared.presentation.vm.BaseViewModel.UiEvent
import com.yes.shared.presentation.vm.BaseViewModel.UiState

class SettingsContract {
    sealed class Event : UiEvent {
        data class OnGetCharacteristics(
            val settings: Settings
        ): Event()
        data class OnSetCharacteristics(
            val settings: Settings
        ): Event()

    }
    data class State(
        val state:SettingsState,
    ) : UiState

    sealed class SettingsState {
        data object Idle : SettingsState()
        data object Loading : SettingsState()
        data class Success(
            val settings: SettingsUI,
        ):  SettingsState()

    }
    sealed class Effect : UiEffect {
        data object UnknownException : Effect()
    }
}