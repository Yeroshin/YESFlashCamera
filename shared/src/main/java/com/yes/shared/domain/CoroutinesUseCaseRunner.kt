package com.yes.shared.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


interface CoroutinesUseCaseRunner {
    val useCaseCoroutineScope: CoroutineScope

    fun withUseCaseScope(
        loadingUpdater: ((Boolean) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        block: (suspend () -> Unit)
    ) {
        useCaseCoroutineScope.launch {
            loadingUpdater?.invoke(true)
            try {
                block()
            } catch (e: Exception) {
                onError?.invoke(e)
            } finally {
                loadingUpdater?.invoke(false)
                onComplete?.invoke()
            }
        }
    }

    /**
     * Executes a bound UseCaseAction in two phases:
     * 1. Immediate sync call (executeSync) on the current thread.
     * 2. Background async call (executeAsync) via withUseCaseScope.
     */
    fun <R> launchHybridUseCase(
        loadingUpdater: ((Boolean) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        block: UseCaseAction<R>
    ) {
        // Phase 1: Instant sync execution
        block.executeSync()

        // Phase 2: Standard async execution with lifecycle management
        withUseCaseScope(loadingUpdater, onError, onComplete) {
            block.executeAsync()
        }
    }
}
