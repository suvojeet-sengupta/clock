package com.suvojeet.clock.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.suvojeet.clock.ui.clock.ClockStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val IS_24_HOUR_FORMAT = booleanPreferencesKey("is_24_hour_format")
    private val CLOCK_STYLE = stringPreferencesKey("clock_style")
    private val GRADUAL_VOLUME = booleanPreferencesKey("gradual_volume")
    private val DISMISS_METHOD = stringPreferencesKey("dismiss_method")
    private val MATH_DIFFICULTY = stringPreferencesKey("math_difficulty")
    private val SNOOZE_DURATION = intPreferencesKey("snooze_duration")
    private val MAX_SNOOZE_COUNT = intPreferencesKey("max_snooze_count")
    private val APP_THEME = stringPreferencesKey("app_theme")
    private val HIGH_CONTRAST_MODE = booleanPreferencesKey("high_contrast_mode")
    private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")

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

    val gradualVolume: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[GRADUAL_VOLUME] ?: false
        }

    val dismissMethod: Flow<DismissMethod> = context.dataStore.data
        .map { preferences ->
            val methodName = preferences[DISMISS_METHOD] ?: DismissMethod.STANDARD.name
            try {
                DismissMethod.valueOf(methodName)
            } catch (e: IllegalArgumentException) {
                DismissMethod.STANDARD
            }
        }

    val mathDifficulty: Flow<MathDifficulty> = context.dataStore.data
        .map { preferences ->
            val difficultyName = preferences[MATH_DIFFICULTY] ?: MathDifficulty.EASY.name
            try {
                MathDifficulty.valueOf(difficultyName)
            } catch (e: IllegalArgumentException) {
                MathDifficulty.EASY
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

    suspend fun setGradualVolume(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GRADUAL_VOLUME] = enabled
        }
    }

    suspend fun setDismissMethod(method: DismissMethod) {
        context.dataStore.edit { preferences ->
            preferences[DISMISS_METHOD] = method.name
        }
    }

    suspend fun setMathDifficulty(difficulty: MathDifficulty) {
        context.dataStore.edit { preferences ->
            preferences[MATH_DIFFICULTY] = difficulty.name
        }
    }

    val snoozeDuration: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[SNOOZE_DURATION] ?: 10 // Default to 10 minutes
        }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[SNOOZE_DURATION] = minutes
        }
    }

    /**
     * Maximum number of times an alarm can be snoozed.
     * 0 means unlimited snoozing.
     */
    val maxSnoozeCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[MAX_SNOOZE_COUNT] ?: 3 // Default to 3 snoozes
        }

    suspend fun setMaxSnoozeCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_SNOOZE_COUNT] = count
        }
    }

    private val SELECTED_WORLD_CLOCK_ZONES = androidx.datastore.preferences.core.stringSetPreferencesKey("selected_world_clock_zones")

    val selectedWorldClockZones: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_WORLD_CLOCK_ZONES] ?: emptySet()
        }

    suspend fun addWorldClockZone(zoneId: String) {
        context.dataStore.edit { preferences ->
            val currentZones = preferences[SELECTED_WORLD_CLOCK_ZONES] ?: emptySet()
            preferences[SELECTED_WORLD_CLOCK_ZONES] = currentZones + zoneId
        }
    }

    suspend fun removeWorldClockZone(zoneId: String) {
        context.dataStore.edit { preferences ->
            val currentZones = preferences[SELECTED_WORLD_CLOCK_ZONES] ?: emptySet()
            preferences[SELECTED_WORLD_CLOCK_ZONES] = currentZones - zoneId
        }
    }

    // App Theme
    val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[APP_THEME] ?: AppTheme.COSMIC.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.COSMIC
            }
        }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME] = theme.name
        }
    }

    // High Contrast Mode for accessibility
    val highContrastMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HIGH_CONTRAST_MODE] ?: false
        }

    suspend fun setHighContrastMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_MODE] = enabled
        }
    }

    // Haptic Feedback
    val hapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] ?: true // Enabled by default
        }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }
}
