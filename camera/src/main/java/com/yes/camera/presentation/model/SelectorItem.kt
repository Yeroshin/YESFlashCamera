package com.yes.camera.presentation.model

import androidx.compose.runtime.Immutable

/*
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

*/

//////////////////////
@Immutable
sealed interface SelectorItem {
    val id: Int // Теперь ID типа Int
}

@Immutable
data class IconItem(
    val icon: Int,
    override val id: Int = icon // Можно использовать ресурс иконки как ID, если они не повторяются
) : SelectorItem

@Immutable
data class TextItem(
    val text: String,
    override val id: Int // Передаем уникальное число
) : SelectorItem

