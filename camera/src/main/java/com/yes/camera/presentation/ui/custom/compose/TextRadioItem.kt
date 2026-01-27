package com.yes.camera.presentation.ui.custom.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yes.camera.presentation.model.RadioGroupItem



@Composable
fun TextRadioContent(
    title: String,
    value: String,
    selected: Boolean
) {
    Column(
       // modifier = Modifier.fillMaxWidth(),
        //  horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = if (selected) Color.Green else Color.White,
                fontSize = 8.sp,
                shadow = Shadow(Color.DarkGray, Offset(5f, 5f), 5f)
            )
        )

        val minFontSize = 8.sp
        var fontSize by remember(value) { mutableStateOf(16.sp) }

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
                color = if (selected) Color.Green else Color.White,
                fontSize = fontSize,
                shadow = Shadow(Color.DarkGray, Offset(5f, 5f), 5f)
            )
        )
    }
}
