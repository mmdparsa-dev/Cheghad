package com.mmdparsadev.cheghad.data.repository

import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.api.CheghadApi
import com.mmdparsadev.cheghad.data.database.CurrencyDao
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

class CurrencyRepository(
    private val api: CheghadApi,
    private val currencyDao: CurrencyDao
) {

    // In-memory cache for historical points per symbol and range (valid for 60 seconds)
    private val historyCache = ConcurrentHashMap<String, Pair<Long, List<Double>>>()

    val allCurrenciesFlow: Flow<List<CurrencyItem>> = currencyDao.getAllCurrenciesFlow()

    fun getVisibleCurrenciesFlow(now: Long): Flow<List<CurrencyItem>> = currencyDao.getVisibleCurrenciesFlow(now)

    suspend fun getCachedCurrencies(): List<CurrencyItem> {
        return withContext(Dispatchers.IO) {
            try {
                currencyDao.getAllCurrencies()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveCurrenciesToCache(currencies: List<CurrencyItem>) {
        withContext(Dispatchers.IO) {
            try {
                currencyDao.insertAll(currencies)
            } catch (e: Exception) {
                android.util.Log.e("CurrencyRepository", "Failed to cache currencies to DB", e)
            }
        }
    }

    suspend fun hideCurrency(id: String, durationMillis: Long) {
        withContext(Dispatchers.IO) {
            val until = System.currentTimeMillis() + durationMillis
            currencyDao.updateHiddenUntil(id, until)
        }
    }

    suspend fun fetchLivePrices(): NetworkResult<List<CurrencyItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getLivePrices()
                if (response.items.isNotEmpty()) {
                    saveCurrenciesToCache(response.items)
                }
                NetworkResult.Success(response.items, isFresh = true)
            } catch (e: Exception) {
                val cached = getCachedCurrencies()
                if (cached.isNotEmpty()) {
                    NetworkResult.Success(cached, isFresh = false)
                } else {
                    val resId = when (e) {
                        is SocketTimeoutException -> R.string.error_timeout
                        is IOException -> R.string.error_network
                        else -> R.string.error_server
                    }
                    NetworkResult.Error(resId, e)
                }
            }
        }
    }

    /**
     * Fetch real historical close prices from TGJU via [CheghadApi.getHistory].
     * Windowing and currency unit conversions are handled consistently by ApiClient.
     * Guaranteed NO random/mock data generator.
     */
    suspend fun fetchHistory(
        symbol: String,
        range: String,
        currentPrice: Double? = null,
        @Suppress("UNUSED_PARAMETER") changePercentage: Double? = null
    ): List<Double> {
        return withContext(Dispatchers.IO) {
            val cacheKey = "${symbol.uppercase()}_${range.uppercase()}"
            val cachedEntry = historyCache[cacheKey]
            val nowMs = System.currentTimeMillis()
            if (cachedEntry != null && (nowMs - cachedEntry.first < 60_000L)) {
                return@withContext cachedEntry.second
            }

            try {
                val resultPrices = api.getHistory(symbol, range)
                    .filter { it > 0.0 && !it.isNaN() && !it.isInfinite() }
                if (resultPrices.size >= 2) {
                    historyCache[cacheKey] = Pair(nowMs, resultPrices)
                    return@withContext resultPrices
                }
            } catch (e: Exception) {
                android.util.Log.e("CurrencyRepository", "fetchHistory failed for $symbol $range", e)
            }

            // Real data fallback: flat line at current price or empty list.
            // NEVER generate random noise or fake walk data.
            if (currentPrice != null && currentPrice > 0) {
                listOf(currentPrice, currentPrice)
            } else {
                emptyList()
            }
        }
    }
}
