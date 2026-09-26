package com.yes.flashcamera.presentation.ui.theme

import androidx.compose.runtime.Composable
import com.yes.shared.presentation.ui.theme.FlashCameraTheme as SharedFlashCameraTheme

@Composable
fun FlashCameraTheme(
    content: @Composable () -> Unit
) {
    SharedFlashCameraTheme(
        content = content
    )
}
