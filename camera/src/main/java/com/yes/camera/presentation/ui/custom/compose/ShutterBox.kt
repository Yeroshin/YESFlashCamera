package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import com.yes.shared.presentation.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShutterBox(
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val parentSize = remember { mutableStateOf(Size.Zero) }

    val coroutineScope = rememberCoroutineScope()
    var isShutterSnapping by remember { mutableStateOf(false) }
    var isShutterOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            if (!isShutterSnapping) {
                coroutineScope.launch {
                    isShutterSnapping = true
                    isShutterOpen = false
                    delay(150)
                    isShutterOpen = true
                    delay(150)
                    isShutterSnapping = false
                    onToggle()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                parentSize.value = coordinates.size.toSize()
            }
    ) {
        val heightPx = parentSize.value.height
        if (heightPx > 0f) {
            val density = LocalDensity.current
            val heightDp = with(density) { heightPx.toDp() }

            val panelHeight by animateDpAsState(
                targetValue = if (isShutterOpen) AppTheme.dimens.none else heightDp / 2f,
                animationSpec = tween(durationMillis = 100),
            )

            val bottomOffset by animateDpAsState(
                targetValue = if (isShutterOpen) heightDp else heightDp / 2f,
                animationSpec = tween(durationMillis = 100),
            )

            val bgColor = AppTheme.colors.background

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelHeight)
                    .align(Alignment.TopStart)
                    .background(bgColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelHeight)
                    .offset(y = bottomOffset)
                    .align(Alignment.TopStart)
                    .background(bgColor)
            )
        }
    }
}
