package com.yes.shared.data.dataSource


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class SettingsDataSource(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    suspend fun <T> set(value: T, key: Preferences.Key<T>) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun edit(transform: (MutablePreferences) -> Unit) {
        dataStore.edit { preferences ->
            transform(preferences)
        }
    }

    fun <T> subscribe(key: Preferences.Key<T>, defaultValue: T?): Flow<T?> {
        return dataStore.data
            .map { preferences ->
                preferences[key] ?: defaultValue
            }.distinctUntilChanged()
    }
}