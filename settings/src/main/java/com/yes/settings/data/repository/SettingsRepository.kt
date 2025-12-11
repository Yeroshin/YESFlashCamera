package com.yes.settings.data.repository

import android.graphics.ImageFormat
import android.os.Environment
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.FILEPATH
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.FULLSCREEN
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.IMGFORMAT
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONS
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONVALUE
import com.yes.settings.domain.model.Settings
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource
) {
    object PreferencesKeys {
        val RESOLUTIONS = stringPreferencesKey("resolutionItems")
        val RESOLUTIONVALUE = stringPreferencesKey("resolutionValue")
        val FULLSCREEN = booleanPreferencesKey("fullScreen")
        val IMGFORMAT = stringPreferencesKey("imgFormat")
        val FILEPATH=stringPreferencesKey("filePath")
    }

    suspend fun setSettings(settings: Settings) {
        setResolutionValue(settings.resolutionValue)
        setResolutions(settings.resolutionItems)
        setFullscreen(settings.fullScreen)
        setImageFormat(settings.imageFormat)
        setFilePath(settings.filePath)
    }

    suspend fun subscribeSettings(): Flow<Settings> {
        return combine(
            subscribeResolutionValue(),
            subscribeResolutions(),
            subscribeFullscreen(),
            subscribeImageFormat(),
            subscribeFilePath()
        ) { resolutionValue, resolutions, fullscreen, imageFormat,filePath ->
            Settings(
                resolutionValue = resolutionValue,
                resolutionItems = resolutions,
                fullScreen = fullscreen,
                imageFormat = imageFormat,
                filePath=filePath
            )

        }
        /* return Settings(
             resolutionValue = getResolutionValue(),
             resolutionItems = getResolutions(),
             fullScreen = getFullscreen()
         )*/
    }

    private suspend fun setFullscreen(fullscreen: Boolean?) {
        fullscreen?.let { it ->
            settingsDataSource.set(it, FULLSCREEN)
        } ?: run {
            settingsDataSource.remove(FULLSCREEN)
        }

    }

    private suspend fun subscribeFullscreen(): Flow<Boolean?> {
        return settingsDataSource.subscribe(FULLSCREEN, null)

    }

    private suspend fun setResolutionValue(dimension: Dimensions?) {
        dimension?.let { it ->
            settingsDataSource.set(
                it.width.toString() + "x" + it.height.toString(),
                RESOLUTIONVALUE
            )
        } ?: run {
            settingsDataSource.remove(RESOLUTIONVALUE)
        }

    }

    private suspend fun subscribeResolutionValue(): Flow<Dimensions?> {
        val tmp = settingsDataSource.subscribe(RESOLUTIONVALUE, null).first()
        val t = tmp
        return settingsDataSource.subscribe(RESOLUTIONVALUE, null)
            .map {
                it?.split("x")
                    ?.takeIf { it.size == 2 }
                    ?.let { parts ->
                        Dimensions(parts[0].toInt(), parts[1].toInt())
                    }
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

    private suspend fun subscribeResolutions(): Flow<List<Dimensions>?> {
        return settingsDataSource.subscribe(RESOLUTIONS, "")
            .map {
                it?.split(",")
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

    private suspend fun setImageFormat(imageFormat: ImgFormat?) {
        imageFormat?.let {
            when (it) {
                ImgFormat.JPEG -> settingsDataSource.set(
                    "jpeg",
                    IMGFORMAT
                )

                ImgFormat.RAW -> settingsDataSource.set(
                    "raw",
                    IMGFORMAT
                )

                ImgFormat.JPEGRAW -> settingsDataSource.set(
                    "jpegRaw",
                    IMGFORMAT
                )
            }
        } ?: run {
            settingsDataSource.remove(IMGFORMAT)
        }

    }

    private suspend fun subscribeImageFormat(): Flow<ImgFormat?> {
        return settingsDataSource.subscribe(IMGFORMAT, "jpeg")
            .map {
                when (it) {
                    "jpeg" -> ImgFormat.JPEG
                    "jpegRaw" -> ImgFormat.JPEGRAW
                    "raw" -> ImgFormat.RAW
                    else -> ImgFormat.JPEG
                }
            }

    }
    private suspend fun setFilePath(filePath:String?) {
        filePath?.let {
            settingsDataSource.set(
                filePath,
                FILEPATH
            )
        } ?: run {
            settingsDataSource.remove(FILEPATH)
        }

    }
    private suspend fun subscribeFilePath(): Flow<String?> {
        val dcimDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        val path:String = dcimDir.absolutePath
        return settingsDataSource.subscribe(FILEPATH, path)
    }
}