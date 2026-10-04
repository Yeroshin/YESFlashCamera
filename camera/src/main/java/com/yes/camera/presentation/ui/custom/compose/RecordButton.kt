package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.yes.shared.presentation.ui.theme.AppTheme

@Composable
fun RecordButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val activeColor = AppTheme.colors.primaryAccent
    val bgBlack = AppTheme.colors.background
    val btnBg = AppTheme.colors.textPrimary

    Button(
        modifier = modifier.size(AppTheme.dimens.recordButtonSize),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = btnBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(x = this.size.width / 2f, y = this.size.height / 2f)
            val radiusOuter = size.width / 2f
            val radiusMiddle = radiusOuter * 1.75f
            val radiusInner = radiusOuter * 1.55f

            drawCircle(color = bgBlack, radius = radiusMiddle, center = center)
            drawCircle(
                color = activeColor,
                radius = radiusInner,
                center = center
            )
        }
    }
}
