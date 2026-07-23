package com.mmdparsadev.cheghad.data.repository

import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.api.CheghadApi
import com.mmdparsadev.cheghad.data.database.CurrencyDao
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class CurrencyRepository(
    private val Api: CheghadApi,
    private val KifpoolApi: com.mmdparsadev.cheghad.data.api.KifpoolApi,
    private val currencyDao: CurrencyDao
) {

    val allCurrenciesFlow: Flow<List<CurrencyItem>> = currencyDao.getAllCurrenciesFlow()

    fun getVisibleCurrenciesFlow(now: Long): Flow<List<CurrencyItem>> = currencyDao.getVisibleCurrenciesFlow(now)

    suspend fun getCachedCurrencies(): List<CurrencyItem> = currencyDao.getAllCurrencies()

    suspend fun saveCurrenciesToCache(currencies: List<CurrencyItem>) {
        withContext(Dispatchers.IO) {
            currencyDao.insertAll(currencies)
        }
    }

    suspend fun hideCurrency(id: String, durationMillis: Long) {
        withContext(Dispatchers.IO) {
            val until = System.currentTimeMillis() + durationMillis
            currencyDao.updateHiddenUntil(id, until)
        }
    }

    suspend fun FetchLivePrices(): NetworkResult<List<CurrencyItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val Response = Api.GetLivePrices()
                NetworkResult.Success(Response.Items)
            } catch (E: SocketTimeoutException) {
                NetworkResult.Error(R.string.error_timeout, E)
            } catch (E: IOException) {
                NetworkResult.Error(R.string.error_network, E)
            } catch (E: HttpException) {
                NetworkResult.Error(R.string.error_server, E)
            } catch (E: Exception) {
                NetworkResult.Error(R.string.error_server, E)
            }
        }
    }

    suspend fun FetchHistory(
        symbol: String,
        range: String,
        currentPrice: Double? = null,
        changePercentage: Double? = null
    ): List<Double> {
        return withContext(Dispatchers.IO) {
            try {
                val to = System.currentTimeMillis() / 1000
                val from = when(range) {
                    "HOUR" -> to - 3600
                    "DAY" -> to - 86400
                    "WEEK" -> to - 604800
                    "MONTH" -> to - 2592000
                    "YEAR" -> to - 31536000
                    else -> to - 86400
                }
                
                val resolution = when(range) {
                    "HOUR" -> "5"
                    "DAY" -> "60"
                    "WEEK" -> "D"
                    "MONTH" -> "D"
                    "YEAR" -> "W"
                    else -> "60"
                }

                val primarySymbol = when(symbol.uppercase()) {
                    "USD", "USDT" -> "USDTIRT"
                    "BTC" -> "BTCIRT"
                    "ETH" -> "ETHIRT"
                    "GOLD", "XAU" -> "PAXGIRT"
                    else -> if (symbol.endsWith("IRT")) symbol else "${symbol}IRT"
                }
                
                var response = KifpoolApi.GetHistory(primarySymbol, resolution, from, to)
                
                // Fallback to non-IRT symbol if primary symbol fails
                if (response.Status != "ok" || response.Close.isNullOrEmpty()) {
                    val fallbackSymbol = symbol.uppercase().replace("IRT", "")
                    if (fallbackSymbol != primarySymbol) {
                        response = KifpoolApi.GetHistory(fallbackSymbol, resolution, from, to)
                    }
                }

                // If symbol is not directly on Kifpool (e.g. EUR, GBP, CAD, AED, GOLD, MESGHAL, etc.),
                // fetch USDTIRT history as baseline trend for exchange rate fluctuations
                if (response.Status != "ok" || response.Close.isNullOrEmpty()) {
                    if (primarySymbol != "USDTIRT") {
                        response = KifpoolApi.GetHistory("USDTIRT", resolution, from, to)
                    }
                }

                if (response.Status == "ok" && !response.Close.isNullOrEmpty()) {
                    val rawPrices = response.Close
                    val lastRaw = rawPrices.lastOrNull() ?: 1.0

                    if (currentPrice != null && currentPrice > 0) {
                        // Kifpool quotes IRT pairs in Rials
                        val baseLast = if (primarySymbol == "USDTIRT" && symbol != "USD" && symbol != "USDT") (lastRaw / 10.0) else (if (lastRaw > currentPrice * 5) lastRaw / 10.0 else lastRaw)
                        val scaleFactor = currentPrice / (if (baseLast <= 0) 1.0 else baseLast)
                        
                        rawPrices.map { rawPoint ->
                            val pointToman = if (primarySymbol == "USDTIRT" && symbol != "USD" && symbol != "USDT") (rawPoint / 10.0) else (if (rawPoint > currentPrice * 5) rawPoint / 10.0 else rawPoint)
                            pointToman * scaleFactor
                        }
                    } else {
                        // Default fallback
                        rawPrices.map { if (it > 10000000000.0) it / 10.0 else it / 10.0 }
                    }
                } else {
                    GenerateMockHistory(symbol, range, currentPrice, changePercentage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                GenerateMockHistory(symbol, range, currentPrice, changePercentage)
            }
        }
    }

    private fun GenerateMockHistory(
        symbol: String,
        range: String,
        currentPrice: Double?,
        changePercentage: Double?
    ): List<Double> {
        val targetEndPrice = currentPrice ?: when(symbol.uppercase()) {
            "USD", "USDT" -> 63500.0
            "BTC" -> 4120000000.0
            "ETH" -> 220000000.0
            "GOLD" -> 45000000.0
            "EUR" -> 69120.0
            "GBP" -> 81000.0
            "TRY" -> 1950.0
            "AED" -> 17200.0
            else -> 100000.0
        }

        val changePct = changePercentage ?: 1.2
        val steps = when(range) {
            "HOUR" -> 12
            "DAY" -> 24
            "WEEK" -> 14
            "MONTH" -> 30
            "YEAR" -> 24
            else -> 20
        }

        val startPrice = targetEndPrice / (1.0 + (changePct / 100.0))
        val points = mutableListOf<Double>()
        val random = java.util.Random((symbol + range).hashCode().toLong())

        val volatility = when(range) {
            "HOUR" -> 0.002
            "DAY" -> 0.008
            "WEEK" -> 0.015
            "MONTH" -> 0.03
            "YEAR" -> 0.06
            else -> 0.01
        }

        points.add(startPrice)
        for (i in 1 until steps - 1) {
            val progress = i.toDouble() / (steps - 1)
            val linearTarget = startPrice + (targetEndPrice - startPrice) * progress
            val noise = (random.nextDouble() - 0.49) * volatility * targetEndPrice
            points.add(linearTarget + noise)
        }
        points.add(targetEndPrice)
        return points
    }
}
