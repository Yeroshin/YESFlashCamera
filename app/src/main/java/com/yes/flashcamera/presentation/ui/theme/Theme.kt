package com.yes.flashcamera.presentation.ui.theme

import androidx.compose.runtime.Composable
import com.yes.shared.presentation.ui.theme.AppColors
import com.yes.shared.presentation.ui.theme.FlashCameraTheme as SharedFlashCameraTheme

@Composable
fun FlashCameraTheme(
    colors: AppColors = AppColors(),
    content: @Composable () -> Unit
) {
    SharedFlashCameraTheme(
        colors = colors,
        content = content
    )
}
