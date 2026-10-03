package com.yes.settings.presentation.model

import com.yes.settings.presentation.ui.views.ImmutableCollection

data class SettingsUI(
    val resolutionValue: String = "",
    val resolutionItems: ImmutableCollection<String> = ImmutableCollection(emptyList()),
    val fullScreen: Boolean = true,
    val imgFormatValue: String,
    val imgFormatItems: ImmutableCollection<String>,
    val availableStorageText: String = "142 GB available",
    val totalStorageText: String = "256 GB total",
    val storageProgress: Float = 0.55f,
    val storagePath: String = "/DCIM/Camera/",
    val remainingShotsText: String = "Approx. 3,155 shots",
    val bufferSizeText: String = "Buffer size: ~12 MB/shot",
    val themeValue: String = "Dark (Default)",
    val themeItems: ImmutableCollection<String> = ImmutableCollection(listOf("Dark (Default)", "AMOLED Black", "System Dark", "High Contrast"))
)
