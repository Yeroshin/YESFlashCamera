package com.yes.shared.di.module

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.yes.shared.data.dataSource.SettingsDataSource
import com.yes.shared.utils.CameraThreadManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.android.asCoroutineDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton

private const val USER_PREFERENCES = "YES_preferences"
@Module
class SharedModule(
    private val context: Context
) {

     // Для shared экземпляра
     @Singleton
    @Provides
    fun providesCameraThreadManager(): CameraThreadManager = CameraThreadManager()
    @CameraDispatcher
    @Singleton
    @Provides
    fun providesCameraDispatcher(manager: CameraThreadManager): CoroutineDispatcher =
       // manager.mBackgroundHandler.asCoroutineDispatcher()
        manager.dispatcher

    @Provides
    fun providesContext(): Context {
        return context
    }


    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    fun providesDataStore():DataStore<Preferences> {
        return  PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @Provides
    @Singleton
    fun providesSettingsDataSource(
        dataStore:DataStore<Preferences>
    ): SettingsDataSource {
        return SettingsDataSource(dataStore)
    }

}
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class CameraDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainDispatcher