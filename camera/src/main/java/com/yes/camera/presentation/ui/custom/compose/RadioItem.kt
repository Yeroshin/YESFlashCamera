package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.presentation.model.Item

class RadioItem(
    id: Item,
    var title: String,
    private var resId: Int?,
) : RadioButton(id) {
    @Composable
    override fun item() {
        resId?.let {
            VectorShadow(
                Modifier
                    .size(24.dp),
                vectorColor = Color.White,
                shadowColor = Color.DarkGray,
                resId = it
            )
        }

        Text(
            text = title,
            style = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                shadow = Shadow(
                    color = Color.DarkGray,
                    offset = Offset(5.0f, 5.0f),
                    blurRadius = 5f
                )
            )
        )


    }

}