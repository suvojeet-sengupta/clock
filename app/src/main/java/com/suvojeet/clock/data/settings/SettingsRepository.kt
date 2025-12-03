package com.suvojeet.clock.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suvojeet.clock.ui.clock.ClockStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val IS_24_HOUR_FORMAT = booleanPreferencesKey("is_24_hour_format")
    private val CLOCK_STYLE = stringPreferencesKey("clock_style")

    val is24HourFormat: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_24_HOUR_FORMAT] ?: false // Default to 12-hour format
        }

    val clockStyle: Flow<ClockStyle> = context.dataStore.data
        .map { preferences ->
            val styleName = preferences[CLOCK_STYLE] ?: ClockStyle.CLASSIC.name
            try {
                ClockStyle.valueOf(styleName)
            } catch (e: IllegalArgumentException) {
                ClockStyle.CLASSIC
            }
        }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_24_HOUR_FORMAT] = is24Hour
        }
    }

    suspend fun setClockStyle(style: ClockStyle) {
        context.dataStore.edit { preferences ->
            preferences[CLOCK_STYLE] = style.name
        }
    }
}
