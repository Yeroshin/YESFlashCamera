package com.yes.flashcamera.presentation.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.yes.flashcamera.presentation.model.ShutterItemUI

class ShutterValueItemAdapterDelegate: AdapterDelegate<ShutterItemUI> {
    @Composable
    override fun Content(item:  ShutterItemUI) {
        Text(text = item.text)
    }
}