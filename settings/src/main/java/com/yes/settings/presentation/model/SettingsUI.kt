package com.yes.settings.presentation.model

import com.yes.settings.presentation.ui.views.ImmutableCollection

data class SettingsUI(
    val resolutionValue: String = "",
    val resolutionItems: ImmutableCollection<String> = ImmutableCollection(emptyList()),
    val fullScreen: Boolean = false,
    val imgFormatValue:String,
    val imgFormatItems:ImmutableCollection<String>
)