package com.suvojeet.clock.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val IS_24_HOUR_FORMAT = booleanPreferencesKey("is_24_hour_format")

    val is24HourFormat: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_24_HOUR_FORMAT] ?: false // Default to 12-hour format
        }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_24_HOUR_FORMAT] = is24Hour
        }
    }
}
