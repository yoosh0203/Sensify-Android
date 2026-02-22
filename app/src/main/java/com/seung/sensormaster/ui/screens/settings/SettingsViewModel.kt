package com.seung.sensormaster.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seung.sensormaster.data.settings.SettingsRepository
import com.seung.sensormaster.data.settings.SensorSpeed
import com.seung.sensormaster.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repo.themeMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM
    )

    val dynamicColor: StateFlow<Boolean> = repo.dynamicColor.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val advancedMode: StateFlow<Boolean> = repo.advancedMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    val hapticFeedback: StateFlow<Boolean> = repo.hapticFeedback.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )

    val sensorSpeed: StateFlow<SensorSpeed> = repo.sensorSpeed.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SensorSpeed.NORMAL
    )

    val keepScreenOn: StateFlow<Boolean> = repo.keepScreenOn.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { repo.setDynamicColor(enabled) }
    fun setAdvancedMode(enabled: Boolean) = viewModelScope.launch { repo.setAdvancedMode(enabled) }
    fun setHapticFeedback(enabled: Boolean) = viewModelScope.launch { repo.setHapticFeedback(enabled) }
    fun setSensorSpeed(speed: SensorSpeed) = viewModelScope.launch { repo.setSensorSpeed(speed) }
    fun setKeepScreenOn(enabled: Boolean) = viewModelScope.launch { repo.setKeepScreenOn(enabled) }
}
