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
import com.mmdparsadev.cheghad.utils.ConnectivityStatus
import com.mmdparsadev.cheghad.utils.NetworkConnectivityObserver
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.mmdparsadev.cheghad.widget.updateAllWidgets

data class CurrencyUiState(
    val isLoading: Boolean = false,
    val items: List<CurrencyItem> = emptyList(),
    val errorMessageResId: Int? = null,
    val lastUpdatedTime: String = "",
    val showSuccessMessage: Boolean = false,
    val selectedCategory: String = "all",
    val alarms: List<AlarmEntity> = emptyList(),
    val historyPoints: Map<String, List<Double>> = emptyMap(),
    val isHistoryLoading: Boolean = false,
    val newsArticles: List<NewsArticle> = emptyList(),
    val isNewsLoading: Boolean = false,
    val isOffline: Boolean = false
)

class CurrencyViewModel(
    private val repository: CurrencyRepository,
    private val alarmRepository: AlarmRepository,
    private val context: Context? = null,
    private val newsRepository: NewsRepository = NewsRepository()
) : ViewModel() {
    private val prefs: SharedPreferences? = context?.getSharedPreferences("currency_cache_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        CurrencyUiState(
            items = loadCachedItemsFromPrefs(),
            newsArticles = newsRepository.getInitialNewsArticles()
        )
    )
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        _uiState.update { it.copy(errorMessageResId = R.string.error_server) }
    }

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
        observeCurrencies()
        startPeriodicUpdates()
        observeAlarms()
        fetchNews()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        context?.let { ctx ->
            viewModelScope.launch(exceptionHandler) {
                val observer = NetworkConnectivityObserver(ctx)
                observer.observe()
                    .catch { e -> e.printStackTrace() }
                    .collect { status ->
                        val isOffline = status != ConnectivityStatus.Available
                        _uiState.update { it.copy(isOffline = isOffline) }
                    }
            }
        }
    }

    private fun observeCurrencies() {
        viewModelScope.launch(exceptionHandler) {
            // Immediately load from cache once
            val initialCurrencies = repository.getCachedCurrencies()
            if (initialCurrencies.isNotEmpty()) {
                saveCachedItemsToPrefs(initialCurrencies)
                val cachedTime = prefs?.getString("cached_time", "") ?: ""
                _uiState.update { 
                    it.copy(
                        items = initialCurrencies.filter { item -> item.hiddenUntil < System.currentTimeMillis() },
                        lastUpdatedTime = cachedTime
                    )
                }
            }
            
            // Then observe for updates
            repository.getVisibleCurrenciesFlow(System.currentTimeMillis())
                .catch { e -> e.printStackTrace() }
                .collect { currencies ->
                    if (currencies.isNotEmpty()) {
                        saveCachedItemsToPrefs(currencies)
                        val cachedTime = prefs?.getString("cached_time", "") ?: ""
                        _uiState.update { 
                            it.copy(
                                items = currencies,
                                lastUpdatedTime = if (it.lastUpdatedTime.isEmpty()) cachedTime else it.lastUpdatedTime
                            )
                        }
                    }
                }
        }
    }

    fun hideCurrencyForItem(id: String) {
        viewModelScope.launch(exceptionHandler) {
            repository.hideCurrency(id, 3600000L) // 1 hour in millis
            fetchData(false) // Trigger a refresh to update the list immediately
        }
    }

    fun fetchNews() {
        viewModelScope.launch(exceptionHandler) {
            _uiState.update { it.copy(isNewsLoading = true) }
            val news = newsRepository.fetchLiveNews()
            _uiState.update { it.copy(newsArticles = news, isNewsLoading = false) }
        }
    }

    fun fetchHistory(symbol: String, range: String, currentPrice: Double? = null, changePercentage: Double? = null) {
        viewModelScope.launch(exceptionHandler) {
            _uiState.update { it.copy(isHistoryLoading = true) }
            val points = repository.fetchHistory(symbol, range, currentPrice, changePercentage)
            _uiState.update {
                it.copy(
                    isHistoryLoading = false,
                    historyPoints = it.historyPoints + (symbol to points)
                )
            }
        }
    }

    private fun observeAlarms() {
        viewModelScope.launch(exceptionHandler) {
            alarmRepository.allAlarmsFlow
                .catch { e -> e.printStackTrace() }
                .collect { alarmsList ->
                    _uiState.update { it.copy(alarms = alarmsList) }
                }
        }
    }

    private fun startPeriodicUpdates() {
        viewModelScope.launch(exceptionHandler) {
            while (isActive) {
                fetchData(isManualRefresh = false)
                delay(3 * 60 * 1000L) // 3 minutes
            }
        }
    }

    fun refreshData() {
        fetchData(isManualRefresh = true)
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(showSuccessMessage = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessageResId = null) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun addAlarm(symbol: String, title: String, targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch(exceptionHandler) {
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

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(exceptionHandler) {
            alarmRepository.updateAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch(exceptionHandler) {
            alarmRepository.deleteAlarm(alarm)
        }
    }

    fun deleteAlarmById(id: Long) {
        viewModelScope.launch(exceptionHandler) {
            alarmRepository.deleteAlarmById(id)
        }
    }

    private fun checkTriggeredAlarms(items: List<CurrencyItem>) {
        viewModelScope.launch(exceptionHandler) {
            val activeAlarms = alarmRepository.getActiveAlarms()
            for (alarm in activeAlarms) {
                val currentItem = items.find { it.symbol == alarm.symbol } ?: continue
                val currentPrice = currentItem.currentPrice
                
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

    private fun fetchData(isManualRefresh: Boolean) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessageResId = null) }
        viewModelScope.launch(exceptionHandler) {
            when (val result = repository.fetchLivePrices()) {
                is NetworkResult.Success -> {
                    val currentTime = if (result.isFresh) {
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    } else {
                        prefs?.getString("cached_time", "") ?: ""
                    }
                    
                    if (result.isFresh) {
                        saveCachedItemsToPrefs(result.data)
                        prefs?.edit()?.putString("cached_time", currentTime)?.apply()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = result.data,
                            lastUpdatedTime = currentTime,
                            showSuccessMessage = isManualRefresh && result.isFresh,
                            errorMessageResId = if (!result.isFresh && isManualRefresh) R.string.error_showing_cache else null
                        )
                    }
                    if (result.isFresh) {
                        context?.let { updateAllWidgets(it) }
                    }
                    checkTriggeredAlarms(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageResId = result.messageResId
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: CurrencyRepository, alarmRepository: AlarmRepository, context: Context? = null): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CurrencyViewModel(repository, alarmRepository, context) as T
                }
            }
    }
}
