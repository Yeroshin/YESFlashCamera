package com.yes.camera.presentation.model

interface  SelectorItem {
    val value: Float
    var passed: Boolean
}

data class IconItem(
    override val value: Float,
    val icon: Int,
    override var passed: Boolean = false
) : SelectorItem

data class TextItem(
    override val value: Float,
    val text: String,
    override var passed: Boolean = false
) : SelectorItem



