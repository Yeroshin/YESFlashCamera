package com.yes.flashcamera.presentation.ui

import androidx.compose.runtime.Composable

class CompositeAdapter(private val delegates: Map<Class<*>, AdapterDelegate<*>>) {
    @Composable
    fun <T> Content(item: T) {
        val delegate = delegates[item!!::class.java] as AdapterDelegate<T>
        delegate.Content(item)
    }
}