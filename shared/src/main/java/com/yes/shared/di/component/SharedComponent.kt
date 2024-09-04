package com.yes.shared.di.component

import android.content.Context
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.di.module.MainDispatcher
import com.yes.shared.di.module.SharedModule
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        SharedModule::class,
    ]
)
interface SharedComponent {
    @IoDispatcher
    fun providesIoDispatcher(): CoroutineDispatcher

    @MainDispatcher
    fun providesMainDispatcher(): CoroutineDispatcher
    fun providesContext(): Context
}
