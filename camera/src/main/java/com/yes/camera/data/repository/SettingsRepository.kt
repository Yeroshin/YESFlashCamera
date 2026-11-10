package com.yes.camera.data.repository

import android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
import android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO
import android.os.Environment
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.BACKCAMERA
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.FILEPATH
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.FOCUSMODE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.FOCUSVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.FULLSCREEN
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.ISOVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONS
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.SHUTTERVALUE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.TOUCHPOINT
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.WBMODE
import com.yes.camera.data.repository.SettingsRepository.PreferencesKeys.WBVALUE
import com.yes.camera.domain.model.Characteristics
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
        val BACKCAMERA = booleanPreferencesKey("backCamera")
        val RESOLUTIONS = stringPreferencesKey("resolutionItems")
        val RESOLUTIONVALUE = stringPreferencesKey("resolutionValue")
        val ISOVALUE = intPreferencesKey("isoValue")
        val SHUTTERVALUE = longPreferencesKey("shutterValue")
        val WBVALUE = intPreferencesKey("wbValue")
        val WBMODE = intPreferencesKey("wbMode")
        val FOCUSVALUE = floatPreferencesKey("focusValue")
        val FOCUSMODE = intPreferencesKey("focusMode")
        val TOUCHPOINT = stringPreferencesKey("touchPoint")
        val FULLSCREEN = booleanPreferencesKey("fullScreen")
        val FILEPATH = stringPreferencesKey("filePath")
        val IMGFORMAT = stringPreferencesKey("imgFormat")
        val SUPPORTS_RAW = booleanPreferencesKey("supportsRaw")
        val GRID_ENABLED = booleanPreferencesKey("gridEnabled")
        val SAVE_LOCATION = booleanPreferencesKey("saveLocation")
        val EXTERNAL_FLASH = booleanPreferencesKey("externalFlash")
        val EXTERNAL_FLASH_POWER = intPreferencesKey("externalFlashPower")
    }

    fun subscribeExternalFlashPower(): Flow<Int?> {
        return settingsDataSource.subscribe(PreferencesKeys.EXTERNAL_FLASH_POWER, 100)
    }

    suspend fun getExternalFlashPower(): Int? {
        return settingsDataSource.subscribe(PreferencesKeys.EXTERNAL_FLASH_POWER, 100).first()
    }

    suspend fun setExternalFlashPower(power: Int) {
        settingsDataSource.set(power, PreferencesKeys.EXTERNAL_FLASH_POWER)
    }

    fun subscribeExternalFlash(): Flow<Boolean?> {
        return settingsDataSource.subscribe(PreferencesKeys.EXTERNAL_FLASH, false)
    }

    suspend fun getExternalFlash(): Boolean? {
        return settingsDataSource.subscribe(PreferencesKeys.EXTERNAL_FLASH, false).first()
    }

    suspend fun setExternalFlash(enabled: Boolean) {
        settingsDataSource.set(enabled, PreferencesKeys.EXTERNAL_FLASH)
    }

    fun subscribeSaveLocation(): Flow<Boolean?> {
        return settingsDataSource.subscribe(PreferencesKeys.SAVE_LOCATION, false)
    }

    suspend fun getSaveLocation(): Boolean? {
        return settingsDataSource.subscribe(PreferencesKeys.SAVE_LOCATION, false).first()
    }

    suspend fun setSaveLocation(enabled: Boolean) {
        settingsDataSource.set(enabled, PreferencesKeys.SAVE_LOCATION)
    }

    fun subscribeGridEnabled(): Flow<Boolean?> {
        return settingsDataSource.subscribe(PreferencesKeys.GRID_ENABLED, true)
    }

    suspend fun getGridEnabled(): Boolean? {
        return settingsDataSource.subscribe(PreferencesKeys.GRID_ENABLED, true).first()
    }

    suspend fun getSupportsRaw(): Boolean? {
        return settingsDataSource.subscribe(PreferencesKeys.SUPPORTS_RAW, null).first()
    }

    suspend fun setSupportsRaw(supportsRaw: Boolean) {
        settingsDataSource.set(supportsRaw, PreferencesKeys.SUPPORTS_RAW)
    }

    private val defaultDcimPath by lazy {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
    }

    suspend fun setCharacteristics(characteristics: Characteristics) {
        settingsDataSource.edit { preferences ->
            if (characteristics.isoValue != null) preferences[ISOVALUE] = characteristics.isoValue else preferences.remove(ISOVALUE)
            if (characteristics.shutterValue != null) preferences[SHUTTERVALUE] = characteristics.shutterValue else preferences.remove(SHUTTERVALUE)
            if (characteristics.wbValue != null) preferences[WBVALUE] = characteristics.wbValue else preferences.remove(WBVALUE)
            if (characteristics.wbMode != null) preferences[WBMODE] = characteristics.wbMode else preferences.remove(WBMODE)
            if (characteristics.focusValue != null) preferences[FOCUSVALUE] = characteristics.focusValue else preferences.remove(FOCUSVALUE)
            if (characteristics.focusMode != null) preferences[FOCUSMODE] = characteristics.focusMode else preferences.remove(FOCUSMODE)
            if (characteristics.touchPoint != null) {
                preferences[TOUCHPOINT] = characteristics.touchPoint.joinToString(",")
            } else {
                preferences.remove(TOUCHPOINT)
            }
        }
    }

    suspend fun getImgFormat(): ImgFormat {
        val formatStr = settingsDataSource.subscribe(PreferencesKeys.IMGFORMAT, "jpeg").first()
        return when (formatStr?.lowercase()) {
            "dng", "raw" -> ImgFormat.DNG
            "jpegdng", "jpeg_dng", "jpeg+dng", "jpegraw" -> ImgFormat.JPEGDNG
            else -> ImgFormat.JPEG
        }
    }

    fun subscribeImageFormat(): Flow<ImgFormat?> {
        return settingsDataSource.subscribe(PreferencesKeys.IMGFORMAT, "jpeg")
            .map {
                when (it?.lowercase()) {
                    "dng", "raw" -> ImgFormat.DNG
                    "jpegdng", "jpeg_dng", "jpeg+dng", "jpegraw" -> ImgFormat.JPEGDNG
                    else -> ImgFormat.JPEG
                }
            }
    }

    fun subscribeSupportsRaw(): Flow<Boolean?> {
        return settingsDataSource.subscribe(PreferencesKeys.SUPPORTS_RAW, null)
    }

    suspend fun getCharacteristics(): Characteristics {
        return Characteristics(
            backCamera = getBackCamera(),
            isoValue = getIsoValue(),
            shutterValue = getShutterValue(),
            wbValue = getWbValue(),
            wbMode = getWbMode(),
            focusValue = getFocusValue(),
            focusMode = getFocusMode(),
            fullscreen = getFullScreen(),
            resolution = getResolutionValue() ?: Dimensions(0, 0),
            imgFormat = getImgFormat(),
            supportsRaw = getSupportsRaw() == true,
            gridEnabled = getGridEnabled() == true,
            saveLocation = getSaveLocation() == true,
            externalFlash = getExternalFlash() == true,
            externalFlashPower = getExternalFlashPower() ?: 100,
            filePath = getFilePath() ?: run {
                throw IllegalArgumentException("Filepath must not be null")
            }
        )
    }

    fun subscribeSettings(): Flow<Characteristics> {
        return combine(
            subscribeFullScreen(),
            subscribeResolutionValue(),
            subscribeImageFormat(),
            subscribeSupportsRaw(),
            subscribeGridEnabled(),
            subscribeSaveLocation(),
            subscribeExternalFlash(),
            subscribeExternalFlashPower(),
            subscribeIsoValue(),
            subscribeShutterValue(),
            subscribeWbValue(),
            subscribeWbMode(),
            subscribeFocusValue(),
            subscribeFocusMode(),
            subscribeBackCamera()
        ) { args: Array<Any?> ->
            val fullscreen = args[0] as? Boolean
            val resolution = args[1] as? Dimensions
            val imgFormat = args[2] as? ImgFormat
            val supportsRaw = args[3] as? Boolean
            val gridEnabled = args[4] as? Boolean
            val saveLocation = args[5] as? Boolean
            val externalFlash = args[6] as? Boolean
            val externalFlashPower = args[7] as? Int
            val isoValue = args[8] as? Int
            val shutterValue = args[9] as? Long
            val wbValue = args[10] as? Int
            val wbMode = args[11] as? Int
            val focusValue = args[12] as? Float
            val focusMode = args[13] as? Int
            val backCamera = args[14] as? Boolean

            Characteristics(
                backCamera = backCamera ?: true,
                fullscreen = fullscreen ?: true,
                resolution = resolution ?: Dimensions(0, 0),
                imgFormat = imgFormat ?: ImgFormat.JPEG,
                supportsRaw = supportsRaw == true,
                gridEnabled = gridEnabled ?: true,
                saveLocation = saveLocation == true,
                externalFlash = externalFlash == true,
                externalFlashPower = externalFlashPower ?: 100,
                isoValue = isoValue,
                shutterValue = shutterValue,
                wbValue = wbValue,
                wbMode = wbMode,
                focusValue = focusValue,
                focusMode = focusMode
            )
        }
    }

    fun subscribeIsoValue(): Flow<Int?> = settingsDataSource.subscribe(ISOVALUE, null)
    fun subscribeShutterValue(): Flow<Long?> = settingsDataSource.subscribe(SHUTTERVALUE, null)
    fun subscribeWbValue(): Flow<Int?> = settingsDataSource.subscribe(WBVALUE, null)
    fun subscribeWbMode(): Flow<Int?> = settingsDataSource.subscribe(WBMODE, CONTROL_AWB_MODE_AUTO)
    fun subscribeFocusValue(): Flow<Float?> = settingsDataSource.subscribe(FOCUSVALUE, null)
    fun subscribeFocusMode(): Flow<Int?> = settingsDataSource.subscribe(FOCUSMODE, CONTROL_AF_MODE_CONTINUOUS_PICTURE)

    suspend fun getFullScreen(): Boolean? {
        return settingsDataSource.subscribe(FULLSCREEN, null).first()
    }

    fun subscribeFullScreen(): Flow<Boolean?> {
        return settingsDataSource.subscribe(FULLSCREEN, null)
    }

    fun subscribeBackCamera(): Flow<Boolean?> {
        return settingsDataSource.subscribe(BACKCAMERA, true)
    }

    suspend fun setBackCamera(backCamera: Boolean?) {
        backCamera?.let {
            settingsDataSource.set(it, BACKCAMERA)
        } ?: run {
            settingsDataSource.remove(BACKCAMERA)
        }
    }

    suspend fun getBackCamera(): Boolean? {
        return settingsDataSource.subscribe(BACKCAMERA, null).first()
    }

    suspend fun setResolutions(dimensions: List<Dimensions>?) {
        dimensions?.let {
            settingsDataSource.set(
                it.joinToString(",") { dim -> "${dim.width}x${dim.height}" },
                RESOLUTIONS
            )
        } ?: run {
            settingsDataSource.remove(RESOLUTIONS)
        }
    }

    suspend fun getResolutions(): List<Dimensions>? {
        return settingsDataSource.subscribe(RESOLUTIONS, "").first()
            ?.split(",")?.mapNotNull { resolution ->
                val parts = resolution.trim().split("x")
                if (parts.size == 2) {
                    val width = parts[0].trim().toIntOrNull()
                    val height = parts[1].trim().toIntOrNull()
                    if (width != null && height != null) Dimensions(width, height) else null
                } else null
            }
    }

    suspend fun setResolutionValue(dimension: Dimensions?) {
        dimension?.let {
            settingsDataSource.set("${it.width}x${it.height}", RESOLUTIONVALUE)
        } ?: run {
            settingsDataSource.remove(RESOLUTIONVALUE)
        }
    }

    suspend fun getResolutionValue(): Dimensions? {
        return settingsDataSource.subscribe(RESOLUTIONVALUE, null).first()
            ?.split("x")
            ?.takeIf { it.size == 2 }
            ?.let { parts ->
                val width = parts[0].toIntOrNull()
                val height = parts[1].toIntOrNull()
                if (width != null && height != null) Dimensions(width, height) else null
            }
    }

    fun subscribeResolutionValue(): Flow<Dimensions?> {
        return settingsDataSource.subscribe(RESOLUTIONVALUE, null)
            .map { str ->
                str?.split("x")
                    ?.takeIf { it.size == 2 }
                    ?.let { parts ->
                        val width = parts[0].toIntOrNull()
                        val height = parts[1].toIntOrNull()
                        if (width != null && height != null) Dimensions(width, height) else null
                    }
            }
    }

    private suspend fun getIsoValue(): Int? {
        return settingsDataSource.subscribe(ISOVALUE, null).first()
    }

    private suspend fun getShutterValue(): Long? {
        return settingsDataSource.subscribe(SHUTTERVALUE, null).first()
    }

    private suspend fun getWbValue(): Int? {
        return settingsDataSource.subscribe(WBVALUE, null).first()
    }

    private suspend fun getWbMode(): Int? {
        return settingsDataSource.subscribe(WBMODE, CONTROL_AWB_MODE_AUTO).first()
    }

    private suspend fun getFocusValue(): Float? {
        return settingsDataSource.subscribe(FOCUSVALUE, -1f).first()
    }

    private suspend fun getFocusMode(): Int? {
        return settingsDataSource.subscribe(FOCUSMODE, CONTROL_AF_MODE_CONTINUOUS_PICTURE).first()
    }

    private suspend fun getFilePath(): String? {
        return settingsDataSource.subscribe(FILEPATH, defaultDcimPath).first()
    }
}
