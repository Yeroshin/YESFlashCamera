package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.yes.shared.presentation.ui.theme.AppTheme

@Composable
fun TextRadioContent(
    title: String,
    value: String,
    selected: Boolean
) {
    val activeColor = AppTheme.colors.primaryAccent
    val normalColor = AppTheme.colors.textPrimary
    val shadowColor = AppTheme.colors.shadow
    val initialTextSize = AppTheme.dimens.textMedium

    Column {
        Text(
            text = title,
            style = TextStyle(
                color = if (selected) activeColor else normalColor,
                fontSize = 8.sp,
                shadow = Shadow(shadowColor, Offset(5f, 5f), 5f)
            )
        )

        val minFontSize = 8.sp
        var fontSize by remember(value) { mutableStateOf(initialTextSize) }

        Text(
            text = value,
            maxLines = 1,
            onTextLayout = { layoutResult ->
                if (layoutResult.hasVisualOverflow) {
                    val newSize = fontSize.value * 0.95f
                    if (newSize.sp >= minFontSize) fontSize = newSize.sp
                }
            },
            style = TextStyle(
                color = if (selected) activeColor else normalColor,
                fontSize = fontSize,
                shadow = Shadow(shadowColor, Offset(5f, 5f), 5f)
            )
        )
    }
}
