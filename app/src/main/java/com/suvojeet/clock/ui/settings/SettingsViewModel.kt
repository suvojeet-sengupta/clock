package com.suvojeet.clock.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
}


