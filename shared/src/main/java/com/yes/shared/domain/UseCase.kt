package com.yes.shared.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class UseCase<REQUEST, RESULT>(
    private val dispatcher: CoroutineDispatcher
) {

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

    open suspend fun run(params: REQUEST): RESULT {
        throw NotImplementedError("This method is optional and not implemented")
    }

    open suspend fun run(): RESULT {
        throw NotImplementedError("This method is optional and not implemented")
    }

}