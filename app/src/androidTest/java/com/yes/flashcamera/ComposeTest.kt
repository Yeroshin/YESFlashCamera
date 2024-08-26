package com.yes.flashcamera

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yes.flashcamera.presentation.model.ShutterItemUI
import com.yes.flashcamera.presentation.ui.CompositeAdapter
import com.yes.flashcamera.presentation.ui.ShutterValueItemAdapterDelegate
import com.yes.flashcamera.presentation.ui.ValueSelector

@Preview
@Composable
fun ComposeTest() {
    val adapter = CompositeAdapter(
        mapOf(
            ShutterItemUI::class.java to ShutterValueItemAdapterDelegate()
            )
    )
    val items = listOf(
        ShutterItemUI("1"),
        ShutterItemUI("World"),
        ShutterItemUI("Hello"),
        ShutterItemUI("World"),
        ShutterItemUI("Hello"),
        ShutterItemUI("World"),
        ShutterItemUI("Hello"),
        ShutterItemUI("World"),
        ShutterItemUI("Hello"),
        ShutterItemUI("World"),
        ShutterItemUI("Hello"),
        ShutterItemUI("8"),
    )
    ValueSelector(
        items = items,
        adapter =adapter
    )

}