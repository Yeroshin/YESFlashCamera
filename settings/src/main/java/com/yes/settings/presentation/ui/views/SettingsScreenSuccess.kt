package com.yes.settings.presentation.ui.views

import androidx.compose.runtime.Composable
import com.yes.settings.presentation.model.SettingsUI

/**
 * Точка входа экрана настроек камеры (делегирует вызов новому обновленному макету [SettingsScreenSuccessNew]).
 *
 * @param settingsUI Модель MVI состояния настроек [SettingsUI].
 * @param onBackClick Колбэк возврата на главный экран.
 * @param onSettingsChanged Колбэк обновления настроек.
 */
@Composable
fun SettingsScreenSuccess(
    settingsUI: SettingsUI,
    onBackClick: () -> Unit,
    onSettingsChanged: (SettingsUI) -> Unit
) {
    SettingsScreenSuccessNew(
        settingsUI = settingsUI,
        onBackClick = onBackClick,
        onSettingsChanged = onSettingsChanged
    )
}
