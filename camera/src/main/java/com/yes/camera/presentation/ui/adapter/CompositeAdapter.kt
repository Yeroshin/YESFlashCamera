package com.yes.camera.presentation.ui.adapter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class CompositeAdapter(private val delegates: Map<Class<*>, AdapterDelegate<*>>) {
    interface AdapterDelegate<T> {
        @Composable
        fun Content(item: T,modifier: Modifier)
    }
    @Composable
    fun <T:Any> Content(item: T,modifier: Modifier) {

        val delegate = delegates[item::class.java] as AdapterDelegate<T>
        delegate.Content(item,modifier)
    }
}