package com.mmdparsadev.cheghad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mmdparsadev.cheghad.data.repository.SettingsRepository
import com.mmdparsadev.cheghad.data.repository.UserSettings
import com.mmdparsadev.cheghad.widget.MinimalBadgeWidget
import com.mmdparsadev.cheghad.widget.PriceDeltaWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings("system", "DEFAULT", "jalali", "fa", "standard", "USD", "glassy", isLoaded = false)
    )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun setColorSeed(seedName: String) {
        viewModelScope.launch {
            repository.updateColorSeed(seedName)
        }
    }

    fun setCalendarType(type: String) {
        viewModelScope.launch {
            repository.updateCalendarType(type)
        }
    }

    fun setDigitType(type: String) {
        viewModelScope.launch {
            repository.updateDigitType(type)
        }
    }

    fun setColorSchemeMode(mode: String) {
        viewModelScope.launch {
            repository.updateColorSchemeMode(mode)
        }
    }

    fun setLockscreenWidgetCurrencyId(id: String) {
        viewModelScope.launch {
            repository.updateLockscreenWidgetCurrencyId(id)
            // Trigger Glance update for all instances
            MinimalBadgeWidget().updateAll(repository.context)
            PriceDeltaWidget().updateAll(repository.context)
        }
    }

    fun setLockscreenWidgetTheme(theme: String) {
        viewModelScope.launch {
            repository.updateLockscreenWidgetTheme(theme)
            // Trigger Glance update for all instances
            MinimalBadgeWidget().updateAll(repository.context)
            PriceDeltaWidget().updateAll(repository.context)
        }
    }

    class Factory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }

    companion object {
        fun provideFactory(repository: SettingsRepository): ViewModelProvider.Factory = Factory(repository)
    }
}
