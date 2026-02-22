package com.seung.sensormaster.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class SensorSpeed { NORMAL, FAST, FASTEST }

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ADVANCED_MODE = booleanPreferencesKey("advanced_mode")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SENSOR_SPEED = stringPreferencesKey("sensor_speed")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        try { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: "SYSTEM") }
        catch (_: Exception) { ThemeMode.SYSTEM }
    }

    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DYNAMIC_COLOR] ?: true
    }

    val advancedMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ADVANCED_MODE] ?: false
    }

    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAPTIC_FEEDBACK] ?: true
    }

    val sensorSpeed: Flow<SensorSpeed> = context.dataStore.data.map { prefs ->
        try { SensorSpeed.valueOf(prefs[Keys.SENSOR_SPEED] ?: "NORMAL") }
        catch (_: Exception) { SensorSpeed.NORMAL }
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.KEEP_SCREEN_ON] ?: false
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAdvancedMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ADVANCED_MODE] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setSensorSpeed(speed: SensorSpeed) {
        context.dataStore.edit { it[Keys.SENSOR_SPEED] = speed.name }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }
}
