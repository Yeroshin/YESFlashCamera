package com.yes.shared.di.component

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.di.module.CameraDispatcher
import com.yes.shared.di.module.IoDispatcher
import com.yes.shared.di.module.MainDispatcher
import com.yes.shared.di.module.SharedModule
import com.yes.shared.utils.CameraThreadManager
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

    @CameraDispatcher
    fun providesCameraDispatcher(): CoroutineDispatcher

    fun providesCameraThreadManager(): CameraThreadManager

    @MainDispatcher
    fun providesMainDispatcher(): CoroutineDispatcher
    fun providesContext(): Context

    fun providesSettingsDataSource(): SettingsDataSource

}
