package com.yes.settings.data.repository

import androidx.datastore.preferences.core.intPreferencesKey
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONVALUE
import com.yes.shared.data.dataSource.SettingsDataSource
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    object PreferencesKeys {
        val RESOLUTIONVALUE = intPreferencesKey("resolution value")

    }

    private suspend fun setResolutionValue(isoValue: Int?) {
        isoValue?.let {

            settingsDataSource.set(it, RESOLUTIONVALUE)

        } ?: run {
            settingsDataSource.remove(RESOLUTIONVALUE)
        }

    }

    private suspend fun getResolutionValue(): Int? {
        return settingsDataSource.subscribe(RESOLUTIONVALUE, null).first()
    }
}