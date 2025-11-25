package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ShutterBox(
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
   content: @Composable () -> Unit
) {
    val parentSize = remember { mutableStateOf(Size.Zero) } // Размер родителя

    val coroutineScope = rememberCoroutineScope()
    var isShutterSnapping by remember { mutableStateOf(false) }
    var isShutterOpen by remember { mutableStateOf(false) }
    LaunchedEffect(isOpen){
        if (isOpen){
            if (!isShutterSnapping) {
                coroutineScope.launch {
                    isShutterSnapping = true
                    isShutterOpen = false // Шаг 1: Закрытие
                    delay(150) // Задержка чуть больше анимации для полной смены
                    isShutterOpen = true // Шаг 2: Открытие
                    delay(150) // Задержка
                    isShutterSnapping = false
                    // После полной анимации можно вызвать onToggle, если нужно уведомить пользователя
                    onToggle()
                }
            }
        }


    }
    Box(
        modifier = modifier
            .wrapContentSize()
           // .fillMaxSize() // Растягиваем на весь доступный размер; измените на нужный (например, .size(300.dp))
            .onGloballyPositioned { coordinates ->
                parentSize.value = coordinates.size.toSize() // Получаем размер в px
            }
    ) {

        content()

        val heightPx = parentSize.value.height
        if (heightPx > 0f) { // Избегаем анимации до получения размера
            val density = LocalDensity.current
            val heightDp = with(density) { heightPx.toDp() }

            val panelHeight by animateDpAsState(
                targetValue = if (isShutterOpen) 0.dp else heightDp / 2f,
                animationSpec = tween(durationMillis = 100),
            )

            val bottomOffset by animateDpAsState(
                targetValue = if (isShutterOpen) heightDp else heightDp/ 2f,
                animationSpec = tween(durationMillis =100),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelHeight)
                    .align(Alignment.TopStart)
                    .background(Color.Black)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelHeight)
                    .offset(y = bottomOffset)
                    .align(Alignment.TopStart)
                    .background(Color.Black)
            )
        }
    }
}