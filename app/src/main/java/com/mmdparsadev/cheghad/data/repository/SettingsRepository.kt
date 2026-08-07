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

class SettingsRepository(val context: Context) {
    private object PreferencesKeys {
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val COLOR_SEED = stringPreferencesKey("color_seed")
        val CALENDAR_TYPE = stringPreferencesKey("calendar_type")
        val DIGIT_TYPE = stringPreferencesKey("digit_type")
        val COLOR_SCHEME_MODE = stringPreferencesKey("color_scheme_mode")
        val LOCKSCREEN_WIDGET_CURRENCY_ID = stringPreferencesKey("lockscreen_widget_currency_id")
        val LOCKSCREEN_WIDGET_THEME = stringPreferencesKey("lockscreen_widget_theme")
        val DOWNLOAD_BETA_VERSIONS = booleanPreferencesKey("download_beta_versions")
        val NEWS_ENABLED = booleanPreferencesKey("news_enabled")
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
            val lockscreenWidgetCurrencyId = preferences[PreferencesKeys.LOCKSCREEN_WIDGET_CURRENCY_ID] ?: "USD"
            val lockscreenWidgetTheme = preferences[PreferencesKeys.LOCKSCREEN_WIDGET_THEME] ?: "glassy"
            val downloadBetaVersions = preferences[PreferencesKeys.DOWNLOAD_BETA_VERSIONS] ?: false
            val newsEnabled = preferences[PreferencesKeys.NEWS_ENABLED] ?: false

            UserSettings(
                themeMode = themeMode,
                colorSeed = colorSeed,
                calendarType = calendarType,
                digitType = digitType,
                colorSchemeMode = colorSchemeMode,
                lockscreenWidgetCurrencyId = lockscreenWidgetCurrencyId,
                lockscreenWidgetTheme = lockscreenWidgetTheme,
                downloadBetaVersions = downloadBetaVersions,
                newsEnabled = newsEnabled,
                isLoaded = true,
            )
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

    suspend fun updateLockscreenWidgetCurrencyId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCKSCREEN_WIDGET_CURRENCY_ID] = id
        }
    }

    suspend fun updateLockscreenWidgetTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCKSCREEN_WIDGET_THEME] = theme
        }
    }

    suspend fun updateDownloadBetaVersions(download: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOWNLOAD_BETA_VERSIONS] = download
        }
    }

    suspend fun updateNewsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NEWS_ENABLED] = enabled
        }
    }
}

data class UserSettings(
    val themeMode: String,
    val colorSeed: String,
    val calendarType: String,
    val digitType: String,
    val colorSchemeMode: String,
    val lockscreenWidgetCurrencyId: String = "USD",
    val lockscreenWidgetTheme: String = "glassy",
    val downloadBetaVersions: Boolean = false,
    val newsEnabled: Boolean = false,
    val isLoaded: Boolean = false
)
