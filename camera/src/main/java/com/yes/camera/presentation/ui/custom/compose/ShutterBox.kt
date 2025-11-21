package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

/**
 * ShutterBox: Анимированный контейнер, имитирующий открытие/закрытие затвора (две половины сходятся/расходятся в центр).
 * При закрытии (isOpen = false) две чёрные панели закрывают контент; при открытии (isOpen = true) они расходятся, открывая контент.
 * Нажмите на Box для переключения состояния.
 */
@Composable
fun ShutterBox(
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    shutterColor: Color = Color.Black.copy(alpha = 0.9f), // Цвет затворов
    smallWidthDp: androidx.compose.ui.unit.Dp = 0.dp, // Минимальная ширина створок при открытии
    content: @Composable () -> Unit
) {
    val parentSize = remember { mutableStateOf(Size.Zero) } // Размер родителя

    Box(
        modifier = modifier
            .fillMaxSize() // Растягиваем на весь доступный размер; измените на нужный (например, .size(300.dp))
            .onGloballyPositioned { coordinates ->
                parentSize.value = coordinates.size.toSize() // Получаем размер в px
            }
            .clickable { onToggle() } // Клик для переключения
    ) {
        // Контент (за затворами)
        content()

        val widthPx = parentSize.value.width
        if (widthPx > 0f) { // Избегаем анимации до получения размера
            val density = LocalDensity.current
            val widthDp = with(density) { widthPx.toDp() } // Преобразом px -> dp для анимации

            // Анимируемая шири innumerable на створки
            val panelWidth by animateDpAsState(
                targetValue = if (isOpen) smallWidthDp else widthDp / 2f, // При закрытии ширина половинная, при открытии минимальная
                animationSpec = tween(durationMillis = 300),
                label = "panel width"
            )

            // Анимируемая позиция слева
            val leftOffset by animateDpAsState(
                targetValue = if (isOpen) (widthDp / 2f) - smallWidthDp / 2 else 0.dp, // При открытии центрируется левый край, при закрытии к левому краю
                animationSpec = tween(durationMillis = 300),
                label = "left shutter"
            )

            // Анимируемая позиция справа
            val rightOffset by animateDpAsState(
                targetValue = if (isOpen) widthDp  else widthDp / 2f, // При открытии центрируется правый край, при закрытии к правому краю
                animationSpec = tween(durationMillis = 300),
                label = "right shutter"
            )

            // Левый затвор
            Box(
                modifier = Modifier
                    .fillMaxHeight() // Занимает всю высоту
                    .width(panelWidth) // Анимируемая ширина
                   // .offset(x = leftOffset) // Анимируемая позиция
                    .align(Alignment.TopStart)
                    .background(shutterColor)
            )

            // Правый затвор
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(panelWidth)
                    //.offset(x=widthDp / 2f)
                    .offset(x = rightOffset)
                    .align(Alignment.TopStart)
                    .background(shutterColor)
            )
        }
    }
}

// Пример использования и Preview
@Preview(name = "Shutter Open")
@Composable
private fun ShutterBoxPreviewOpen() {
    var isOpen by remember { mutableStateOf(true) }
    ShutterBox(
        isOpen = isOpen,
        onToggle = { isOpen = !isOpen },
        modifier = Modifier.size(300.dp) // Фиксированный размер для Preview
    ) {
        // Пример контента
        Text(
            text = "Контент внутри затвора\nНажмите для переключения!",
            modifier = Modifier,//.align(Alignment.Center),
            color = Color.White
        )
    }
}

@Preview(name = "Shutter Closed")
@Composable
private fun ShutterBoxPreviewClosed() {
    var isOpen by remember { mutableStateOf(false) }
    ShutterBox(
        isOpen = isOpen,
        onToggle = { isOpen = !isOpen },
        modifier = Modifier.size(300.dp)
    ) {
        Text(
            text = "Контент скрыт!",
            modifier = Modifier,//.align(Alignment.Center),
            color = Color.White
        )
    }
}