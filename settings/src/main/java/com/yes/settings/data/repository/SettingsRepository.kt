package com.yes.settings.data.repository

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONS
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONVALUE
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.domain.Dimensions
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    object PreferencesKeys {
        val RESOLUTIONS = stringPreferencesKey("resolutionItems")
        val RESOLUTIONVALUE = stringPreferencesKey("resolutionValue")
    }

    suspend fun setResolutionValue(dimension: Dimensions?) {
        dimension?.let { it ->
            settingsDataSource.set(it.width.toString() + "x" + it.height.toString(), RESOLUTIONS)
        } ?: run {
            settingsDataSource.remove(RESOLUTIONVALUE)
        }

    }

    suspend fun getResolutionValue(): Dimensions? {
        return settingsDataSource.subscribe(RESOLUTIONVALUE, null).first()
            ?.split("x")
            ?.takeIf { it.size == 2 }
            ?.let { parts ->
                Dimensions(parts[0].toInt(), parts[1].toInt())
            }
    }

    suspend fun setResolutions(dimensions: List<Dimensions>?) {
        dimensions?.let { it ->
            settingsDataSource.set(
                it.joinToString(",") { it.height.toString() + "x" + it.width.toString() },
                RESOLUTIONS
            )
        } ?: run {
            settingsDataSource.remove(RESOLUTIONS)
        }

    }

    suspend fun getResolutions(): List<Dimensions>? {
        return settingsDataSource.subscribe(RESOLUTIONS, "").first()
            ?.split(",")
            ?.map { resolution ->
                val parts = resolution.trim().split("x")
                if (parts.size == 2) {
                    val width = parts[1].trim().toInt()
                    val height = parts[0].trim().toInt()
                    Dimensions(width, height)
                } else {
                    null // или обработать ошибку по-другому
                }
            }
            ?.filterNotNull()
    }
}