package com.yes.settings.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.FULLSCREEN
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONS
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONVALUE
import com.yes.settings.domain.model.Settings
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.domain.Dimensions
import kotlinx.coroutines.flow.first

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    object PreferencesKeys {
        val RESOLUTIONS = stringPreferencesKey("resolutionItems")
        val RESOLUTIONVALUE = stringPreferencesKey("resolutionValue")
        val FULLSCREEN = booleanPreferencesKey("fullScreen")
    }
    suspend fun setSettings(settings: Settings) {
        setResolutionValue(settings.resolutionValue)
        setResolutions(settings.resolutionItems)
        setFullscreen(settings.fullScreen)
    }
    suspend fun getSettings(): Settings {
        return Settings(
            resolutionValue = getResolutionValue(),
            resolutionItems = getResolutions(),
            fullScreen = getFullscreen()
        )
    }
    private suspend fun setFullscreen(fullscreen:Boolean?) {
        fullscreen?.let { it ->
            settingsDataSource.set(it, FULLSCREEN)
        } ?: run {
            settingsDataSource.remove(FULLSCREEN)
        }

    }
    private suspend fun getFullscreen():Boolean? {
        return settingsDataSource.subscribe(FULLSCREEN,null).first()

    }
    private suspend fun setResolutionValue(dimension: Dimensions?) {
        dimension?.let { it ->
            settingsDataSource.set(it.width.toString() + "x" + it.height.toString(), RESOLUTIONS)
        } ?: run {
            settingsDataSource.remove(RESOLUTIONVALUE)
        }

    }

    private suspend fun getResolutionValue(): Dimensions? {
        return settingsDataSource.subscribe(RESOLUTIONVALUE, null).first()
            ?.split("x")
            ?.takeIf { it.size == 2 }
            ?.let { parts ->
                Dimensions(parts[0].toInt(), parts[1].toInt())
            }
    }

    private suspend fun setResolutions(dimensions: List<Dimensions>?) {
        dimensions?.let { it ->
            settingsDataSource.set(
                it.joinToString(",") { it.height.toString() + "x" + it.width.toString() },
                RESOLUTIONS
            )
        } ?: run {
            settingsDataSource.remove(RESOLUTIONS)
        }

    }

    private suspend fun getResolutions(): List<Dimensions>? {
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