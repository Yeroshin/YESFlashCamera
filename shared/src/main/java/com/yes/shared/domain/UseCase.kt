package com.yes.shared.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Interface representing a hybrid action that can be executed
 * both synchronously and asynchronously.
 */
interface UseCaseAction<out RESULT> {
    fun executeSync()
    suspend fun executeAsync(): RESULT
}

abstract class UseCase<REQUEST, RESULT>(
    protected val dispatcher: CoroutineDispatcher
) {

    // Standard suspend calls
    suspend operator fun invoke(params: REQUEST): RESULT {
        return withContext(dispatcher) {
            run(params)
        }
    }

    suspend operator fun invoke(): RESULT {
        return withContext(dispatcher) {
            run()
        }
    }

    /**
     * Binds parameters to the UseCase, creating a [UseCaseAction].
     */
    fun bind(params: REQUEST) = object : UseCaseAction<RESULT> {
        override fun executeSync() = this@UseCase.runSync(params)
        override suspend fun executeAsync(): RESULT = this@UseCase.run(params)
    }

    /**
     * Creates a [UseCaseAction] for UseCases that take no parameters (Unit).
     */
    @Suppress("UNCHECKED_CAST")
    fun bind() = object : UseCaseAction<RESULT> {
        override fun executeSync() = this@UseCase.runSync(Unit as REQUEST)
        override suspend fun executeAsync(): RESULT = this@UseCase.run(Unit as REQUEST)
    }

    // Methods to be overridden by subclasses
    open fun runSync(params: REQUEST) {}

    open suspend fun run(params: REQUEST): RESULT {
        throw NotImplementedError("Implementation for parameters required")
    }

    open suspend fun run(): RESULT {
        throw NotImplementedError("Implementation for no-parameters required")
    }
}
