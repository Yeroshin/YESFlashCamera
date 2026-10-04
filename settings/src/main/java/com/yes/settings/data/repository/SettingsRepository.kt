package com.yes.settings.data.repository

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Environment
import android.os.StatFs
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.FILEPATH
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.FULLSCREEN
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.IMGFORMAT
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONS
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.RESOLUTIONVALUE
import com.yes.settings.data.repository.SettingsRepository.PreferencesKeys.THEMEVALUE
import com.yes.settings.domain.model.Settings
import com.yes.settings.domain.model.StorageInfo
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val settingsDataSource: SettingsDataSource,
    private val context: Context
) {
    private fun isDeviceSupportsRaw(): Boolean {
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager.cameraIdList.any { id ->
                val chars = manager.getCameraCharacteristics(id)
                val caps = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                caps?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true
            }
        } catch (e: Exception) {
            false
        }
    }
    object PreferencesKeys {
        val RESOLUTIONS = stringPreferencesKey("resolutionItems")
        val RESOLUTIONVALUE = stringPreferencesKey("resolutionValue")
        val FULLSCREEN = booleanPreferencesKey("fullScreen")
        val IMGFORMAT = stringPreferencesKey("imgFormat")
        val FILEPATH = stringPreferencesKey("filePath")
        val THEMEVALUE = stringPreferencesKey("themeValue")
        val SUPPORTS_RAW = booleanPreferencesKey("supportsRaw")
    }

    private val defaultDcimPath by lazy {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
    }

    private val defaultExternalStoragePath by lazy {
        Environment.getExternalStorageDirectory().absolutePath
    }

    suspend fun setSettings(settings: Settings) {
        settingsDataSource.edit { preferences ->
            // FULLSCREEN
            if (settings.fullScreen != null) {
                preferences[FULLSCREEN] = settings.fullScreen
            } else {
                preferences.remove(FULLSCREEN)
            }

            // RESOLUTIONVALUE
            if (settings.resolutionValue != null) {
                preferences[RESOLUTIONVALUE] = "${settings.resolutionValue.width}x${settings.resolutionValue.height}"
            } else {
                preferences.remove(RESOLUTIONVALUE)
            }

            // RESOLUTIONS
            if (settings.resolutionItems != null) {
                preferences[RESOLUTIONS] = settings.resolutionItems.joinToString(",") { "${it.height}x${it.width}" }
            } else {
                preferences.remove(RESOLUTIONS)
            }

            // IMGFORMAT
            if (settings.imageFormat != null) {
                preferences[IMGFORMAT] = when (settings.imageFormat) {
                    ImgFormat.JPEG -> "jpeg"
                    ImgFormat.DNG -> "dng"
                    ImgFormat.JPEGDNG -> "jpegDng"
                }
            } else {
                preferences.remove(IMGFORMAT)
            }

            // FILEPATH
            if (settings.filePath != null) {
                preferences[FILEPATH] = settings.filePath
            } else {
                preferences.remove(FILEPATH)
            }

            // THEMEVALUE
            if (settings.themeValue != null) {
                preferences[THEMEVALUE] = settings.themeValue
            } else {
                preferences.remove(THEMEVALUE)
            }
        }
    }

    fun subscribeSettings(): Flow<Settings> {
        return combine(
            subscribeResolutionValue(),
            subscribeResolutions(),
            subscribeFullscreen(),
            subscribeImageFormat(),
            subscribeFilePath(),
            subscribeThemeValue(),
            subscribeSupportsRaw()
        ) { array ->
            Settings(
                resolutionValue = array[0] as Dimensions?,
                resolutionItems = @Suppress("UNCHECKED_CAST") (array[1] as List<Dimensions>?),
                fullScreen = array[2] as Boolean?,
                imageFormat = array[3] as ImgFormat?,
                filePath = array[4] as String?,
                themeValue = array[5] as String?,
                supportsRaw = array[6] as Boolean?
            )
        }
    }

    fun subscribeSupportsRaw(): Flow<Boolean?> {
        return settingsDataSource.subscribe(PreferencesKeys.SUPPORTS_RAW, isDeviceSupportsRaw())
    }

    suspend fun setFullscreen(fullscreen: Boolean?) {
        fullscreen?.let {
            settingsDataSource.set(it, FULLSCREEN)
        } ?: run {
            settingsDataSource.remove(FULLSCREEN)
        }
    }

    fun subscribeFullscreen(): Flow<Boolean?> {
        return settingsDataSource.subscribe(FULLSCREEN, null)
    }

    suspend fun setResolutionValue(dimension: Dimensions?) {
        dimension?.let {
            settingsDataSource.set("${it.width}x${it.height}", RESOLUTIONVALUE)
        } ?: run {
            settingsDataSource.remove(RESOLUTIONVALUE)
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

    suspend fun setResolutions(dimensions: List<Dimensions>?) {
        dimensions?.let { list ->
            settingsDataSource.set(
                list.joinToString(",") { "${it.height}x${it.width}" },
                RESOLUTIONS
            )
        } ?: run {
            settingsDataSource.remove(RESOLUTIONS)
        }
    }

    fun subscribeResolutions(): Flow<List<Dimensions>?> {
        return settingsDataSource.subscribe(
            RESOLUTIONS,
            "3000x4000,3456x4608,4224x5632,4928x6560,6000x8000,6144x8192,6936x9248,9000x12000"
        ).map { str ->
            str?.split(",")
                ?.mapNotNull { resolution ->
                    val parts = resolution.trim().split("x")
                    if (parts.size == 2) {
                        val height = parts[0].trim().toIntOrNull()
                        val width = parts[1].trim().toIntOrNull()
                        if (width != null && height != null) Dimensions(width, height) else null
                    } else null
                }
        }
    }

    suspend fun setImageFormat(imageFormat: ImgFormat?) {
        imageFormat?.let {
            val strVal = when (it) {
                ImgFormat.JPEG -> "jpeg"
                ImgFormat.DNG -> "dng"
                ImgFormat.JPEGDNG -> "jpegDng"
            }
            settingsDataSource.set(strVal, IMGFORMAT)
        } ?: run {
            settingsDataSource.remove(IMGFORMAT)
        }
    }

    fun subscribeImageFormat(): Flow<ImgFormat?> {
        return settingsDataSource.subscribe(IMGFORMAT, "jpeg")
            .map {
                when (it?.lowercase()) {
                    "dng", "raw" -> ImgFormat.DNG
                    "jpegdng", "jpeg_dng", "jpeg+dng", "jpegraw" -> ImgFormat.JPEGDNG
                    else -> ImgFormat.JPEG
                }
            }
    }

    suspend fun setFilePath(filePath: String?) {
        filePath?.let {
            settingsDataSource.set(it, FILEPATH)
        } ?: run {
            settingsDataSource.remove(FILEPATH)
        }
    }

    fun subscribeFilePath(): Flow<String?> {
        return settingsDataSource.subscribe(FILEPATH, defaultDcimPath)
    }

    suspend fun setThemeValue(themeValue: String?) {
        themeValue?.let {
            settingsDataSource.set(it, THEMEVALUE)
        } ?: run {
            settingsDataSource.remove(THEMEVALUE)
        }
    }

    fun subscribeThemeValue(): Flow<String?> {
        return settingsDataSource.subscribe(THEMEVALUE, "Dark (Default)")
    }

    fun getStorageInfo(
        customPath: String? = null,
        resolution: Dimensions? = null,
        imgFormat: ImgFormat? = null
    ): StorageInfo {
        val path = customPath ?: defaultExternalStoragePath
        val stat = try {
            StatFs(path)
        } catch (_: Exception) {
            StatFs(defaultExternalStoragePath)
        }
        val blockSize = stat.blockSizeLong
        val totalBytes = stat.blockCountLong * blockSize
        val availableBytes = stat.availableBlocksLong * blockSize
        val usedBytes = totalBytes - availableBytes

        val usagePercentage = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) else 0f

        val width = resolution?.width ?: 4000
        val height = resolution?.height ?: 3000
        val megapixels = (width.toFloat() * height.toFloat()) / 1_000_000f

        val mbPerMp = when (imgFormat) {
            ImgFormat.DNG -> 2.0f
            ImgFormat.JPEGDNG -> 2.35f
            ImgFormat.JPEG, null -> 0.35f
        }

        val avgBytesPerShot = (megapixels * mbPerMp * 1024 * 1024).toLong().coerceAtLeast(1_000_000L)
        val remainingShots = if (avgBytesPerShot > 0) availableBytes / avgBytesPerShot else 0L
        val avgShotSizeFormatted = if (avgBytesPerShot >= 1024 * 1024) {
            "${(avgBytesPerShot.toDouble() / (1024 * 1024)).toInt()} MB"
        } else {
            "${avgBytesPerShot / 1024} KB"
        }

        return StorageInfo(
            totalBytes = totalBytes,
            availableBytes = availableBytes,
            totalFormatted = formatBytes(totalBytes),
            availableFormatted = formatBytes(availableBytes),
            usagePercentage = usagePercentage,
            storagePath = path,
            avgBytesPerShot = avgBytesPerShot,
            avgShotSizeFormatted = avgShotSizeFormatted,
            remainingShots = remainingShots
        )
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes.toDouble() / (1024 * 1024 * 1024)
        return if (gb >= 1.0) {
            String.format("%.1f GB", gb)
        } else {
            val mb = bytes.toDouble() / (1024 * 1024)
            String.format("%.0f MB", mb)
        }
    }
}
