package com.yes.flashcamera

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yes.camera.presentation.model.ShutterItemUI
import com.yes.shared.presentation.adapter.CompositeAdapter
import com.yes.camera.presentation.ui.adapter.ShutterValueItemAdapterDelegate
import com.yes.camera.presentation.ui.custom.compose.ValueSelector

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
    ){}

}