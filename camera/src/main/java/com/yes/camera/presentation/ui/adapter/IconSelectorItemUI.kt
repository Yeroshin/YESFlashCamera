package com.yes.camera.presentation.ui.adapter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.camera.presentation.ui.custom.compose.VectorShadow
import com.yes.shared.presentation.ui.theme.AppTheme

class IconSelectorItemUI : CompositeAdapter.AdapterDelegate<RadioGroupItem.IconItem> {

    @Composable
    override fun Content(
        item: RadioGroupItem.IconItem,
        isPassed: Boolean,
        modifier: Modifier,
    ) {
        val activeColor = AppTheme.colors.primaryAccent
        val normalColor = AppTheme.colors.textPrimary
        val shadowColor = AppTheme.colors.shadow

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.wrapContentHeight()
        ) {
            val commonColor = if (isPassed) activeColor else normalColor
            val commonShadow = remember(shadowColor) {
                Shadow(
                    color = shadowColor,
                    offset = Offset(5.0f, 5.0f),
                    blurRadius = 5f
                )
            }

            VectorShadow(
                vectorColor = commonColor,
                modifier = Modifier
                    .height(AppTheme.dimens.selectorItemHeight)
                    .width(AppTheme.dimens.iconDefault),
                resId = item.iconRes
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
}
