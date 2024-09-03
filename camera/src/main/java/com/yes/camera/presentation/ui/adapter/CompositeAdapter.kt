package com.yes.camera.presentation.ui.adapter

import androidx.compose.runtime.Composable

class CompositeAdapter(private val delegates: Map<Class<*>, AdapterDelegate<*>>) {
    interface AdapterDelegate<T> {
        @Composable
        fun Content(item: T)
    }
    @Composable
    fun <T:Any> Content(item: T) {

        val delegate = delegates[item::class.java] as AdapterDelegate<T>
        delegate.Content(item)
    }
}