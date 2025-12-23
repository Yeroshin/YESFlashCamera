package com.yes.shared.utils

import android.os.Handler
import android.os.HandlerThread
import javax.inject.Inject

class CameraThreadManager @Inject constructor() {
    private val mBackgroundThread = HandlerThread("CameraThread").apply { start() }
    val mBackgroundHandler: Handler = Handler(mBackgroundThread.looper)

    // Не забудьте остановить поток при закрытии приложения
    fun shutdown() {
        mBackgroundThread.quitSafely()
    }
}