package com.yes.settings.domain.model

import com.yes.shared.domain.Dimensions
import com.yes.shared.domain.ImgFormat

data class Settings(
    val resolutionValue: Dimensions?,
    val resolutionItems: List<Dimensions>? = null,
    val fullScreen: Boolean? = true,
    val imageFormat: ImgFormat? = null,
    val filePath: String? = null
)