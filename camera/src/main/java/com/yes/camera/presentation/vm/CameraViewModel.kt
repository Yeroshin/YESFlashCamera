package com.yes.camera.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.camera.presentation.contract.CameraContract
import com.yes.camera.presentation.contract.CameraContract.*
import com.yes.camera.presentation.model.ShutterItemUI
import com.yes.shared.presentation.vm.BaseDependency
import com.yes.shared.presentation.vm.BaseViewModel


class CameraViewModel: BaseViewModel<Event, State, Effect>() {
    interface DependencyResolver {
        fun resolveCameraDependency(): BaseDependency
    }
    class Factory(


    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(

            ) as T
        }
    }

    override fun createInitialState(): State {
        return State(
            MainState.Success(
                listOf(
                    ShutterItemUI("0"),
                    ShutterItemUI("1"),
                    ShutterItemUI("2"),
                    ShutterItemUI("3"),
                    ShutterItemUI("4"),
                    ShutterItemUI("5"),
                    ShutterItemUI("6"),
                    ShutterItemUI("7"),
                    ShutterItemUI("8"),
                    ShutterItemUI("9"),
                    ShutterItemUI("10"),
                    ShutterItemUI("11"),
                    ShutterItemUI("12"),
                    ShutterItemUI("13"),
                )
            )
        )
    }

    override fun handleEvent(event: Event) {
        TODO("Not yet implemented")
    }
}