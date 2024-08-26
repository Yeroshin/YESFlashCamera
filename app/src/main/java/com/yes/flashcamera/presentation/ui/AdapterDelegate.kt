package com.yes.flashcamera.presentation.ui

import androidx.compose.runtime.Composable

interface AdapterDelegate<T> {
    @Composable
    fun Content(item: T)
}