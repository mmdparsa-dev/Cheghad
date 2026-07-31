package com.mmdparsadev.cheghad.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object PreferencesKeys {
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val COLOR_SEED = stringPreferencesKey("color_seed")
        val CALENDAR_TYPE = stringPreferencesKey("calendar_type")
        val DIGIT_TYPE = stringPreferencesKey("digit_type")
        val COLOR_SCHEME_MODE = stringPreferencesKey("color_scheme_mode")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeMode = preferences[PreferencesKeys.APP_THEME_MODE] ?: "system"
            val colorSeed = preferences[PreferencesKeys.COLOR_SEED] ?: "DEFAULT"
            val calendarType = preferences[PreferencesKeys.CALENDAR_TYPE] ?: "jalali"
            val digitType = preferences[PreferencesKeys.DIGIT_TYPE] ?: "fa"
            val colorSchemeMode = preferences[PreferencesKeys.COLOR_SCHEME_MODE] ?: "standard"
            UserSettings(themeMode, colorSeed, calendarType, digitType, colorSchemeMode, isLoaded = true)
        }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME_MODE] = mode
        }
    }

    suspend fun updateColorSeed(seedName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_SEED] = seedName
        }
    }

    suspend fun updateCalendarType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALENDAR_TYPE] = type
        }
    }

    suspend fun updateDigitType(type: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIGIT_TYPE] = type
        }
    }

    suspend fun updateColorSchemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_SCHEME_MODE] = mode
        }
    }
}

data class UserSettings(
    val themeMode: String,
    val colorSeed: String,
    val calendarType: String,
    val digitType: String,
    val colorSchemeMode: String,
    val isLoaded: Boolean = false
)
