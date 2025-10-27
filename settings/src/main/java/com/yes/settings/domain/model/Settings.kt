package com.yes.settings.domain.model

import com.yes.shared.domain.Dimensions

data class Settings (
    val resolutionValue:Dimensions,
    val resolutionItems:List<Dimensions>
)