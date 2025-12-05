package com.suvojeet.clock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.suvojeet.clock.data.settings.AppTheme
import com.suvojeet.clock.data.settings.DismissMethod
import com.suvojeet.clock.data.settings.MathDifficulty
import com.suvojeet.clock.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {

    val is24HourFormat: StateFlow<Boolean> = repository.is24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun set24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            repository.set24HourFormat(is24Hour)
        }
    }

    val gradualVolume: StateFlow<Boolean> = repository.gradualVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dismissMethod: StateFlow<DismissMethod> = repository.dismissMethod
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DismissMethod.STANDARD)

    val mathDifficulty: StateFlow<MathDifficulty> = repository.mathDifficulty
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MathDifficulty.EASY)

    fun setGradualVolume(enabled: Boolean) {
        viewModelScope.launch {
            repository.setGradualVolume(enabled)
        }
    }

    fun setDismissMethod(method: DismissMethod) {
        viewModelScope.launch {
            repository.setDismissMethod(method)
        }
    }

    fun setMathDifficulty(difficulty: MathDifficulty) {
        viewModelScope.launch {
            repository.setMathDifficulty(difficulty)
        }
    }

    val snoozeDuration: StateFlow<Int> = repository.snoozeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            repository.setSnoozeDuration(minutes)
        }
    }

    val maxSnoozeCount: StateFlow<Int> = repository.maxSnoozeCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    fun setMaxSnoozeCount(count: Int) {
        viewModelScope.launch {
            repository.setMaxSnoozeCount(count)
        }
    }

    private val _isAlexaLinked = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isAlexaLinked: StateFlow<Boolean> = _isAlexaLinked

    fun checkAlexaLinkStatus(context: android.content.Context) {
        _isAlexaLinked.value = com.suvojeet.clock.data.alexa.AlexaAuthManager.isLinked(context)
    }

    fun updateAlexaLinkStatus(linked: Boolean) {
        _isAlexaLinked.value = linked
    }

    // Theme selection
    val appTheme: StateFlow<AppTheme> = repository.appTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.COSMIC)

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            repository.setAppTheme(theme)
        }
    }

    // High Contrast Mode
    val highContrastMode: StateFlow<Boolean> = repository.highContrastMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setHighContrastMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHighContrastMode(enabled)
        }
    }

    // Haptic Feedback
    val hapticFeedbackEnabled: StateFlow<Boolean> = repository.hapticFeedbackEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticFeedbackEnabled(enabled)
        }
    }
}