package com.yes.shared.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class UseCase<REQUEST, RESULT>(
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(params: REQUEST?=null): RESULT {

        return withContext(dispatcher) {
            run(params)
        }
    }
    suspend operator fun invoke(): RESULT {

        return withContext(dispatcher) {
            run(null)
        }
    }
    abstract suspend fun run(params: REQUEST?): RESULT

}