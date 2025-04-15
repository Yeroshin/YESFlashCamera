package com.yes.camera.presentation.model

import androidx.compose.runtime.Immutable


abstract class SelectorItem(
    open val value: Float,
    open var passed: Boolean
)

data class IconItem(
    override val value: Float,
    val icon: Int,
    override var passed: Boolean = false
) : SelectorItem(value, passed)

data class TextItem(
    override val value: Float,
    val text: String,
    override var passed: Boolean = false,
) : SelectorItem(value, passed)



