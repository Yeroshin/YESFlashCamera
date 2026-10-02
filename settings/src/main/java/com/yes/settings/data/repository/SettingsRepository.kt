package com.yes.settings.data.repository

import android.graphics.ImageFormat
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
        val FILEPATH = stringPreferencesKey("filePath")
        val THEMEVALUE = stringPreferencesKey("themeValue")
    }

    suspend fun setSettings(settings: Settings) {
        setResolutionValue(settings.resolutionValue)
        setResolutions(settings.resolutionItems)
        setFullscreen(settings.fullScreen)
        setImageFormat(settings.imageFormat)
        setFilePath(settings.filePath)
        setThemeValue(settings.themeValue)
    }

    suspend fun subscribeSettings(): Flow<Settings> {
        return combine(
            combine(
                subscribeResolutionValue(),
                subscribeResolutions(),
                subscribeFullscreen()
            ) { resVal, resList, fs -> Triple(resVal, resList, fs) },
            combine(
                subscribeImageFormat(),
                subscribeFilePath(),
                subscribeThemeValue()
            ) { imgFmt, path, theme -> Triple(imgFmt, path, theme) }
        ) { t1, t2 ->
            Settings(
                resolutionValue = t1.first,
                resolutionItems = t1.second,
                fullScreen = t1.third,
                imageFormat = t2.first,
                filePath = t2.second,
                themeValue = t2.third
            )
        }
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
        return settingsDataSource.subscribe(RESOLUTIONS, "3000x4000,3456x4608,4224x5632,4928x6560,6000x8000,6144x8192,6936x9248,9000x12000")
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

    private suspend fun setThemeValue(themeValue: String?) {
        themeValue?.let {
            settingsDataSource.set(it, THEMEVALUE)
        } ?: run {
            settingsDataSource.remove(THEMEVALUE)
        }
    }

    private suspend fun subscribeThemeValue(): Flow<String?> {
        return settingsDataSource.subscribe(THEMEVALUE, "Dark (Default)")
    }

    fun getStorageInfo(
        customPath: String? = null,
        resolution: Dimensions? = null,
        imgFormat: ImgFormat? = null
    ): StorageInfo {
        val path = customPath ?: Environment.getExternalStorageDirectory().absolutePath
        val stat = try {
            StatFs(path)
        } catch (_: Exception) {
            StatFs(Environment.getExternalStorageDirectory().absolutePath)
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
            ImgFormat.RAW -> 2.0f
            ImgFormat.JPEGRAW -> 2.35f
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
