package com.yes.camera.presentation.model

interface  SelectorItem {
    var passed: Boolean
}

data class IconItem(
    val icon: Int,
    override var passed: Boolean = false
) : SelectorItem

data class TextItem(
    val text: String,
    override var passed: Boolean = false
) : SelectorItem



