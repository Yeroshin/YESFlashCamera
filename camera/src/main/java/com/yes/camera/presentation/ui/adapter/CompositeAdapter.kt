package com.yes.camera.presentation.ui.adapter

import androidx.compose.runtime.Composable
interface AdapterDelegate<T> {
    @Composable
    fun Content(item: T)
}
class CompositeAdapter(private val delegates: Map<Class<*>, AdapterDelegate<*>>) {

    @Composable
    fun <T:Any> go(item: T) {

        val delegate = delegates[item::class.java] as AdapterDelegate<T>
        delegate.Content(item)
    }
}