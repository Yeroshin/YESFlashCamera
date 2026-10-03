package com.yes.settings.presentation.mapper

import com.yes.settings.domain.model.Settings
import com.yes.settings.domain.model.StorageInfo
import com.yes.settings.presentation.model.SettingsUI
import com.yes.settings.presentation.ui.views.ImmutableCollection
import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat
import kotlin.math.sqrt

class MapperUI {
    fun map(settings: Settings, storageInfo: StorageInfo? = null): SettingsUI {
        val resValStr = settings.resolutionValue?.let {
            val mp = (it.width.toLong() * it.height.toLong() + 500_000L) / 1_000_000L
            if (mp > 0) "$mp MP (${it.width}x${it.height})" else "${it.width}x${it.height}"
        } ?: ""

        return SettingsUI(
            resolutionValue = resValStr,
            resolutionItems = ImmutableCollection(
                settings.resolutionItems?.map {
                    val mp = (it.width.toLong() * it.height.toLong() + 500_000L) / 1_000_000L
                    if (mp > 0) "$mp MP (${it.width}x${it.height})" else "${it.width}x${it.height}"
                } ?: run { emptyList() }
            ),
            fullScreen = settings.fullScreen ?: run { true },
            imgFormatValue = when (settings.imageFormat) {
                ImgFormat.JPEG -> "JPEG"
                ImgFormat.RAW -> "RAW"
                ImgFormat.JPEGRAW -> "JPEG+RAW"
                null -> "JPEG"
            },
            imgFormatItems = ImmutableCollection(
                list = listOf("JPEG", "RAW", "JPEG+RAW")
            ),
            availableStorageText = storageInfo?.let { "${it.availableFormatted} available" } ?: "142 GB available",
            totalStorageText = storageInfo?.let { "${it.totalFormatted} total" } ?: "256 GB total",
            storageProgress = storageInfo?.usagePercentage ?: 0.55f,
            storagePath = storageInfo?.storagePath ?: settings.filePath ?: "/DCIM/Camera/",
            remainingShotsText = storageInfo?.let {
                "Approx. ${it.remainingShots} shots (${it.avgShotSizeFormatted}/shot)"
            } ?: "Approx. 3,155 shots",
            bufferSizeText = storageInfo?.let {
                "Buffer size: ~${it.avgShotSizeFormatted}/shot"
            } ?: "Buffer size: ~12 MB/shot",
            themeValue = settings.themeValue?.ifEmpty { "Dark (Default)" } ?: "Dark (Default)",
            themeItems = ImmutableCollection(
                list = listOf("Dark (Default)", "AMOLED Black", "System Dark", "High Contrast")
            )
        )
    }

    fun map(settingsUI: SettingsUI): Settings {
        return Settings(
            resolutionValue = settingsUI.resolutionValue.toDimensions(),
            resolutionItems = settingsUI.resolutionItems.list.mapNotNull {
                it.toDimensions()
            },
            fullScreen = settingsUI.fullScreen,
            imageFormat = when (settingsUI.imgFormatValue) {
                "JPEG" -> ImgFormat.JPEG
                "RAW" -> ImgFormat.RAW
                "JPEG+RAW", "JPEG + RAW" -> ImgFormat.JPEGRAW
                else -> ImgFormat.JPEG
            },
            filePath = settingsUI.storagePath,
            themeValue = settingsUI.themeValue
        )
    }

    private fun String.toDimensions(): Dimensions? {
        val cleanStr = replace("×", "x").trim()
        val match = Regex("(\\d+)\\s*x\\s*(\\d+)").find(cleanStr)
        if (match != null) {
            val width = match.groupValues[1].toIntOrNull() ?: return null
            val height = match.groupValues[2].toIntOrNull() ?: return null
            return Dimensions(width, height)
        }
        val mpMatch = Regex("(\\d+)\\s*MP", RegexOption.IGNORE_CASE).find(cleanStr)
        if (mpMatch != null) {
            val mp = mpMatch.groupValues[1].toIntOrNull() ?: 12
            val totalPixels = mp.toLong() * 1_000_000L
            val width = sqrt(totalPixels * 4.0 / 3.0).toInt()
            val height = (width * 3 / 4)
            return Dimensions(width, height)
        }
        val parts = cleanStr.split("x")
        if (parts.size == 2) {
            val width = parts[0].trim().toIntOrNull() ?: return null
            val height = parts[1].trim().toIntOrNull() ?: return null
            return Dimensions(width, height)
        }
        return null
    }
}
