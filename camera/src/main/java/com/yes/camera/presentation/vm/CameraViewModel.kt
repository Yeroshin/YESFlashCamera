package com.yes.camera.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.shared.presentation.vm.BaseViewModel


class CameraViewModel: BaseViewModel<Event, State, Effect>() {
    class Factory(


    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(

            ) as T
        }
    }

    override fun createInitialState(): State {
        TODO("Not yet implemented")
    }

    override fun handleEvent(event: Event) {
        TODO("Not yet implemented")
    }
}