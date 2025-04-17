package com.yes.camera.presentation.model

interface SelectorItem {
    val value: Float
    var passed: Boolean
}

data class IconItem(
    override val value: Float,
    val icon: Int,
    override var passed: Boolean = false
) : SelectorItem {
    // hashCode уже правильно реализован в data class,
    // но можно переопределить для явного контроля
    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + icon
        result = 31 * result + passed.hashCode()
        return result
    }
}

data class TextItem(
    override val value: Float,
    val text: String,
    override var passed: Boolean = false
) : SelectorItem {
    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + passed.hashCode()
        return result
    }
}



