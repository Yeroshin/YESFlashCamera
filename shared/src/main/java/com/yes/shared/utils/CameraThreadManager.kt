package com.yes.shared.utils

import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import javax.inject.Inject

class CameraThreadManager @Inject constructor() {
     val cameraThread = HandlerThread("CameraThread").apply { start() }
    // 1. Для методов, требующих Handler (setRepeatingRequest)
    val handler = Handler(cameraThread.looper)

    // 2. Для SessionConfiguration и других API на Executor
    val executor = Executor { command ->
        if (!handler.post(command)) {
            throw RejectedExecutionException("$handler is shutting down")
        }
    }

    // 3. Для ваших UseCase и корутин (Команды управления)
    val dispatcher: CoroutineDispatcher = handler.asCoroutineDispatcher("CameraDispatcher")

    // 4. Отдельный пул для тяжелого анализа изображений (ImageReader)
    // Используем Default, чтобы не нагружать основной поток камеры расчетами
    val analysisExecutor: Executor = Dispatchers.Default.asExecutor()
    // 5. НОВОЕ: Пул для записи файлов (I/O-интенсивные задачи: сохранение на диск)
    // Использует Dispatchers.IO (оптимизирован для ожидания записи на диск/флешку)
    val ioExecutor: Executor = Dispatchers.IO.asExecutor()
    // Не забудьте остановить поток при закрытии приложения
    fun shutdown() {
        cameraThread.quitSafely()
    }
}