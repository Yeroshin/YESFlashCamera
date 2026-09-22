package com.yes.settings.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.yes.settings.presentation.contract.SettingsContract
import com.yes.settings.presentation.ui.views.SettingsScreenSuccess
import com.yes.settings.presentation.wm.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    val viewState = settingsViewModel.uiState.collectAsState()
    when(val state = viewState.value.state){
        is SettingsContract.SettingsState.Success -> SettingsScreenSuccess(
            settingsUI = state.settings,
            onBackClick = onBackClick,
            onSettingsChanged = {settings->
                settingsViewModel.setEvent(
                    SettingsContract.Event.OnSetSettings(settings)
                )
            }
        )

        SettingsContract.SettingsState.Idle -> {}
        SettingsContract.SettingsState.Loading -> TODO()
    }

}