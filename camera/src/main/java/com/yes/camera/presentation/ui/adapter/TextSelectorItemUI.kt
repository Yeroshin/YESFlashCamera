package com.yes.camera.presentation.ui.adapter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.yes.camera.presentation.model.SelectorItem
import com.yes.shared.presentation.ui.theme.AppTheme

@Composable
fun TextSelectorContent(
    item: SelectorItem,
    isPassed: Boolean
) {
    val activeColor = AppTheme.colors.primaryAccent
    val normalColor = AppTheme.colors.textPrimary
    val shadowColor = AppTheme.colors.shadow
    val initialTextSize = AppTheme.dimens.textMedium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.wrapContentHeight()
    ) {
        val commonColor = if (isPassed) activeColor else normalColor

        val commonShadow = remember(shadowColor) {
            Shadow(
                color = shadowColor,
                offset = Offset(5.0f, 5.0f),
                blurRadius = 5f
            )
        }

        Text(
            modifier = Modifier.padding(AppTheme.dimens.extraSmall),
            maxLines = 1,
            textAlign = TextAlign.Center,
            text = item.value,
            style = TextStyle(
                fontSize = initialTextSize,
                color = commonColor,
                shadow = commonShadow
            ),
            softWrap = false
        )

        Text(
            text = "|",
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = AppTheme.dimens.textBody,
                color = commonColor,
                shadow = commonShadow
            )
        )
    }
}
