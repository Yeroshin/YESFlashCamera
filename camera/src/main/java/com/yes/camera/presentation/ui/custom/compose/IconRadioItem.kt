package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yes.camera.presentation.model.RadioGroupItem
import com.yes.shared.presentation.ui.theme.AppTheme

@Composable
fun createIconRadioItem(
    id: RadioGroupItem,
    resId: Int?,
): RadioUiItem<RadioGroupItem> {
    return RadioUiItem(id = id) { selected ->
        resId?.let {
            VectorShadow(
                Modifier.size(AppTheme.dimens.iconDefault),
                vectorColor = if (selected) AppTheme.colors.primaryAccent else AppTheme.colors.iconPrimary,
                shadowColor = AppTheme.colors.shadow,
                resId = it
            )
        }
    }
}

@Composable
fun IconRadioContent(
    resId: Int,
    selected: Boolean
) {
    VectorShadow(
        Modifier.size(AppTheme.dimens.iconDefault),
        vectorColor = if (selected) AppTheme.colors.primaryAccent else AppTheme.colors.iconPrimary,
        shadowColor = AppTheme.colors.shadow,
        resId = resId
    )
}
