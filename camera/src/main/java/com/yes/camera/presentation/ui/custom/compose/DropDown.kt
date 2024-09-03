package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun DropDown(
    isOpen: Boolean,
    modifier: Modifier,
    initiallyOpened: Boolean = false,
    content: @Composable () -> Unit
) {
    /* var isOpen by remember {
         mutableStateOf(isOpen)
     }*/
    val alpha = animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300
        ), label = ""
    )
    val rotateX = animateFloatAsState(
        targetValue = if (isOpen) 0f else -90f,
        animationSpec = tween(
            durationMillis = 300
        ), label = ""
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
         //   .fillMaxWidth()
         //   .height(70.dp)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 0f)
                rotationX = rotateX.value
            }
            .alpha(alpha.value)
    ) {
        content()
    }

}