package com.yes.shared.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yes.shared.domain.CoroutinesUseCaseRunner
import  com.yes.shared.presentation.vm.BaseViewModel.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseViewModel<Event : UiEvent, State : UiState, Effect : UiEffect> : ViewModel(), CoroutinesUseCaseRunner {
    interface UiEvent
    interface UiState
    interface UiEffect

    private val initialState: State by lazy { createInitialState() }
    abstract fun createInitialState(): State



    private val _uiState: MutableStateFlow<State> = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    private val event = _event.asSharedFlow()

    private val _effect: Channel<Effect> = Channel()
    val effect = _effect.receiveAsFlow()

    abstract fun handleEvent(event: Event)

    override val useCaseCoroutineScope = viewModelScope
    override fun withUseCaseScope(
        loadingUpdater: ((Boolean) -> Unit)?,
        onError: ((Throwable) -> Unit)?,
        onComplete: (() -> Unit)?,
        block: suspend () -> Unit
    ) {

        super.withUseCaseScope(
            loadingUpdater = {
                loadingUpdater?.invoke(it)
            },
            onError = {
                onError?.invoke(it)
            },
            onComplete = onComplete,
            block = block
        )
    }
}