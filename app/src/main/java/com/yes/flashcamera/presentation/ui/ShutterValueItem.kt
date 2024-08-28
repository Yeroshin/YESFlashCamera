package com.yes.flashcamera.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yes.flashcamera.presentation.model.ShutterItemUI

class ShutterValueItemAdapterDelegate: AdapterDelegate<ShutterItemUI> {
    @Composable
    override fun Content(

        item:  ShutterItemUI
    ) {
        Text(
            modifier = Modifier
                .background(
                    if (item.text.toInt() % 2 == 0) {
                        Color(0xFFFF00FF)
                    } else {
                        Color(0xFF0000FF)
                    }

                )
                .width(50.dp)
                .height(50.dp),
            text = item.text,

        )

    }
}