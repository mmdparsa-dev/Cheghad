package com.mmdparsadev.cheghad.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.AlarmEntity
import com.mmdparsadev.cheghad.data.models.NewsArticle
import com.mmdparsadev.cheghad.data.repository.CurrencyRepository
import com.mmdparsadev.cheghad.data.repository.AlarmRepository
import com.mmdparsadev.cheghad.data.repository.NewsRepository
import com.mmdparsadev.cheghad.data.repository.NetworkResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CurrencyUiState(
    val IsLoading: Boolean = false,
    val Items: List<CurrencyItem> = emptyList(),
    val ErrorMessageResId: Int? = null,
    val LastUpdatedTime: String = "",
    val ShowSuccessMessage: Boolean = false,
    val SelectedCategory: String = "all",
    val Alarms: List<AlarmEntity> = emptyList(),
    val HistoryPoints: Map<String, List<Double>> = emptyMap(),
    val IsHistoryLoading: Boolean = false,
    val NewsArticles: List<NewsArticle> = emptyList(),
    val IsNewsLoading: Boolean = false
)

class CurrencyViewModel(
    private val Repository: CurrencyRepository,
    private val alarmRepository: AlarmRepository,
    private val context: Context? = null,
    private val newsRepository: NewsRepository = NewsRepository()
) : ViewModel() {
    private val prefs: SharedPreferences? = context?.getSharedPreferences("currency_cache_prefs", Context.MODE_PRIVATE)

    private val _UiState = MutableStateFlow(
        CurrencyUiState(
            Items = loadCachedItemsFromPrefs(),
            NewsArticles = newsRepository.getInitialNewsArticles()
        )
    )
    val UiState: StateFlow<CurrencyUiState> = _UiState.asStateFlow()

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    private fun loadCachedItemsFromPrefs(): List<CurrencyItem> {
        return try {
            val json = prefs?.getString("cached_items_json", null)
            if (!json.isNullOrEmpty()) {
                jsonFormat.decodeFromString<List<CurrencyItem>>(json)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCachedItemsToPrefs(items: List<CurrencyItem>) {
        try {
            if (items.isNotEmpty()) {
                val json = jsonFormat.encodeToString(items)
                prefs?.edit()?.putString("cached_items_json", json)?.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _triggeredAlarmFlow = MutableSharedFlow<Pair<AlarmEntity, Double>>()
    val triggeredAlarmFlow = _triggeredAlarmFlow.asSharedFlow()

    init {
        ObserveCurrencies()
        StartPeriodicUpdates()
        ObserveAlarms()
        FetchNews()
    }

    private fun ObserveCurrencies() {
        viewModelScope.launch {
            // Immediately load from cache once
            val initialCurrencies = Repository.getCachedCurrencies()
            if (initialCurrencies.isNotEmpty()) {
                saveCachedItemsToPrefs(initialCurrencies)
                val cachedTime = prefs?.getString("cached_time", "") ?: ""
                _UiState.update { 
                    it.copy(
                        Items = initialCurrencies.filter { item -> item.HiddenUntil < System.currentTimeMillis() },
                        LastUpdatedTime = cachedTime
                    )
                }
            }
            
            // Then observe for updates
            Repository.getVisibleCurrenciesFlow(System.currentTimeMillis()).collect { currencies ->
                if (currencies.isNotEmpty()) {
                    saveCachedItemsToPrefs(currencies)
                    val cachedTime = prefs?.getString("cached_time", "") ?: ""
                    _UiState.update { 
                        it.copy(
                            Items = currencies,
                            LastUpdatedTime = if (it.LastUpdatedTime.isEmpty()) cachedTime else it.LastUpdatedTime
                        )
                    }
                }
            }
        }
    }

    fun HideCurrencyForItem(id: String) {
        viewModelScope.launch {
            Repository.hideCurrency(id, 3600000L) // 1 hour in millis
            FetchData(false) // Trigger a refresh to update the list immediately
        }
    }

    fun FetchNews() {
        viewModelScope.launch {
            _UiState.update { it.copy(IsNewsLoading = true) }
            val news = newsRepository.fetchLiveNews()
            _UiState.update { it.copy(NewsArticles = news, IsNewsLoading = false) }
        }
    }

    fun FetchHistory(symbol: String, range: String, currentPrice: Double? = null, changePercentage: Double? = null) {
        viewModelScope.launch {
            _UiState.update { it.copy(IsHistoryLoading = true) }
            val points = Repository.FetchHistory(symbol, range, currentPrice, changePercentage)
            _UiState.update {
                it.copy(
                    IsHistoryLoading = false,
                    HistoryPoints = it.HistoryPoints + (symbol to points)
                )
            }
        }
    }

    private fun ObserveAlarms() {
        viewModelScope.launch {
            alarmRepository.allAlarmsFlow.collect { alarmsList ->
                _UiState.update { it.copy(Alarms = alarmsList) }
            }
        }
    }

    private fun StartPeriodicUpdates() {
        viewModelScope.launch {
            while (isActive) {
                FetchData(IsManualRefresh = false)
                delay(7 * 60 * 1000L) // 7 minutes
            }
        }
    }

    fun RefreshData() {
        FetchData(IsManualRefresh = true)
    }

    fun ClearSuccessMessage() {
        _UiState.update { it.copy(ShowSuccessMessage = false) }
    }

    fun ClearErrorMessage() {
        _UiState.update { it.copy(ErrorMessageResId = null) }
    }

    fun SetCategory(category: String) {
        _UiState.update { it.copy(SelectedCategory = category) }
    }

    fun AddAlarm(symbol: String, title: String, targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            alarmRepository.insertAlarm(
                AlarmEntity(
                    symbol = symbol,
                    title = title,
                    targetPrice = targetPrice,
                    isAbove = isAbove,
                    isActive = true
                )
            )
        }
    }

    fun UpdateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.updateAlarm(alarm)
        }
    }

    fun DeleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarm)
        }
    }

    fun DeleteAlarmById(id: Long) {
        viewModelScope.launch {
            alarmRepository.deleteAlarmById(id)
        }
    }

    private fun CheckTriggeredAlarms(items: List<CurrencyItem>) {
        viewModelScope.launch {
            val activeAlarms = alarmRepository.getActiveAlarms()
            for (alarm in activeAlarms) {
                val currentItem = items.find { it.Symbol == alarm.symbol } ?: continue
                val currentPrice = currentItem.CurrentPrice
                
                var isTriggered = false
                if (alarm.isAbove && currentPrice >= alarm.targetPrice) {
                    isTriggered = true
                } else if (!alarm.isAbove && currentPrice <= alarm.targetPrice) {
                    isTriggered = true
                }
                
                if (isTriggered) {
                    alarmRepository.updateAlarm(alarm.copy(isActive = false))
                    _triggeredAlarmFlow.emit(alarm to currentPrice)
                }
            }
        }
    }

    private fun FetchData(IsManualRefresh: Boolean) {
        if (_UiState.value.IsLoading) return
        _UiState.update { it.copy(IsLoading = true, ErrorMessageResId = null) }
        viewModelScope.launch {
            when (val Result = Repository.FetchLivePrices()) {
                is NetworkResult.Success -> {
                    val CurrentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    Repository.saveCurrenciesToCache(Result.Data)
                    saveCachedItemsToPrefs(Result.Data)
                    prefs?.edit()?.putString("cached_time", CurrentTime)?.apply()
                    _UiState.update {
                        it.copy(
                            IsLoading = false,
                            Items = Result.Data,
                            LastUpdatedTime = CurrentTime,
                            ShowSuccessMessage = IsManualRefresh
                        )
                    }
                    CheckTriggeredAlarms(Result.Data)
                }
                is NetworkResult.Error -> {
                    _UiState.update {
                        it.copy(
                            IsLoading = false,
                            ErrorMessageResId = Result.MessageResId
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun ProvideFactory(Repository: CurrencyRepository, alarmRepository: AlarmRepository, context: Context? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CurrencyViewModel(Repository, alarmRepository, context) as T
                }
            }
    }
}
