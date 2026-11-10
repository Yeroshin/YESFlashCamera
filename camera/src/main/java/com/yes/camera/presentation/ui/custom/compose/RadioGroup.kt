package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.yes.shared.presentation.ui.theme.AppTheme

@Immutable
data class RadioUiItem<T>(
    val id: T,
    val content: @Composable (isSelected: Boolean) -> Unit
)

@Composable
fun <T> UniversalRadioGroup(
    items: List<RadioUiItem<T>>,
    selectedItem: T?,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemRotation: Float = 0f
) {
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(32.dp))
            .background(color = AppTheme.colors.surface)
            //.alpha (0.5f)
            .selectableGroup()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedItem

            key(item.id) {
                Box(
                    modifier = Modifier
                        .selectable(
                            selected = isSelected,
                            onClick = { onItemClick(item.id) },
                            role = Role.RadioButton
                        )
                        .padding(AppTheme.dimens.medium)
                        .graphicsLayer { rotationZ = itemRotation }
                ) {
                    item.content(isSelected)
                }
            }
        }
    }
}
