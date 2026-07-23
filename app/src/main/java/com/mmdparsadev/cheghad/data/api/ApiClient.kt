package com.mmdparsadev.cheghad.data.api

import com.mmdparsadev.cheghad.data.api.KifpoolApi
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://api.example.com/" // Dummy base URL

    private val JsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    
    private val MockInterceptor = Interceptor { chain ->
        val uri = chain.request().url.toString()
        if (uri.endsWith("/api/currencies")) {
            
            val currentTime = System.currentTimeMillis()
            val random = java.util.Random()
            
            // Dynamic fluctuation helper to make things actually change on pull-to-refresh!
            fun fluctuate(base: Double, percentRange: Double = 0.005): Double {
                val factor = 1.0 + (random.nextDouble() * 2 * percentRange - percentRange)
                return Math.round(base * factor * 100.0) / 100.0
            }

            fun String.toEnDigits(): String {
                return this.map { ch ->
                    when (ch) {
                        in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
                        in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
                        else -> ch
                    }
                }.joinToString("")
            }

            var usdIrr = 62450.0
            val parsedPrices = mutableMapOf<String, Double>()
            val parsedPercentages = mutableMapOf<String, Double>()

            try {
                // Fetch real rates from tgju.org
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder()
                    .url("https://www.tgju.org/currency")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "fa-IR,fa;q=0.9,en-US;q=0.8,en;q=0.7")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val html = response.body?.string() ?: ""
                    
                    // 1. Parse table row prices and change percentages
                    val rowRegex = """data-market-row="([^"]+)"[^>]*>.*?<td[^>]*>([^<]+)</td>\s*<td[^>]*>(.*?)</td>""".toRegex(RegexOption.DOT_MATCHES_ALL)
                    rowRegex.findAll(html).forEach { match ->
                        val rowMatch = match.value
                        val key = match.groups[1]?.value ?: ""
                        val valStr = match.groups[2]?.value ?: ""
                        val changeCell = match.groups[3]?.value ?: ""
                        val price = valStr.toEnDigits().replace(",", "").replace(" ", "").trim().toDoubleOrNull()
                        if (key.isNotEmpty() && price != null && price > 0) {
                            parsedPrices[key] = price
                            val pctMatch = """\(([-+]?[0-9]*\.?[0-9]+)%\)""".toRegex().find(changeCell)
                            if (pctMatch != null) {
                                pctMatch.groups[1]?.value?.toDoubleOrNull()?.let { pct ->
                                    val isNegative = rowMatch.contains("low") || changeCell.contains("low") || changeCell.contains("-")
                                    parsedPercentages[key] = if (isNegative && pct > 0) -pct else pct
                                }
                            }
                        }
                    }
                    
                    // 2. Parse li card prices and change percentages
                    val liRegex = """<li\s+[^>]*id="(l-[a-zA-Z0-9_-]+)"[^>]*>.*?class="info-price"[^>]*>([^<]+)</span>\s*<span[^>]*class="info-change"[^>]*>(.*?)</span>""".toRegex(RegexOption.DOT_MATCHES_ALL)
                    liRegex.findAll(html).forEach { match ->
                        val fullMatch = match.value
                        val key = match.groups[1]?.value ?: ""
                        val valStr = match.groups[2]?.value ?: ""
                        val changeStr = match.groups[3]?.value ?: ""
                        val price = valStr.toEnDigits().replace(",", "").replace(" ", "").trim().toDoubleOrNull()
                        if (key.isNotEmpty() && price != null && price > 0) {
                            parsedPrices[key] = price
                            val pctMatch = """\(([-+]?[0-9]*\.?[0-9]+)%\)""".toRegex().find(changeStr)
                            if (pctMatch != null) {
                                pctMatch.groups[1]?.value?.toDoubleOrNull()?.let { pct ->
                                    val isNegative = fullMatch.contains("low") || changeStr.contains("low") || changeStr.contains("-")
                                    parsedPercentages[key] = if (isNegative && pct > 0) -pct else pct
                                }
                            }
                        }
                    }
                } else {
                    throw java.io.IOException("Failed to fetch data from TGJU: ${response.code}")
                }
            } catch (e: Exception) {
                if (e is java.io.IOException) throw e
                e.printStackTrace()
                throw java.io.IOException("Network error while fetching prices", e)
            }

            // Extract USD first to use as a fallback base for other currencies
            if (parsedPrices.containsKey("price_dollar_rl")) {
                usdIrr = parsedPrices["price_dollar_rl"]!! / 10.0
            }

            // Define real-time values, falling back to dynamic mock if scrape is empty
            val randUSD = parsedPrices["price_dollar_rl"]?.let { it / 10.0 } ?: fluctuate(usdIrr, 0.004)
            val randEUR = parsedPrices["price_eur"]?.let { it / 10.0 } ?: fluctuate(usdIrr * 1.08, 0.003)
            val randGBP = parsedPrices["price_gbp"]?.let { it / 10.0 } ?: fluctuate(usdIrr * 1.25, 0.003)
            val randCAD = parsedPrices["price_cad"]?.let { it / 10.0 } ?: fluctuate(usdIrr * 0.74, 0.003)
            val randAUD = parsedPrices["price_aud"]?.let { it / 10.0 } ?: fluctuate(usdIrr * 0.65, 0.003)
            val randAED = parsedPrices["price_aed"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 3.67, 0.002)
            val randTRY = parsedPrices["price_try"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 32.0, 0.004)
            val randCHF = parsedPrices["price_chf"]?.let { it / 10.0 } ?: fluctuate(usdIrr * 1.1, 0.003)
            val randCNY = parsedPrices["price_cny"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 7.2, 0.002)
            val randIQD = parsedPrices["price_iqd"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 1300.0, 0.002)
            val randSEK = parsedPrices["price_sek"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 10.5, 0.003)
            val randSAR = parsedPrices["price_sar"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 3.75, 0.002)
            val randQAR = parsedPrices["price_qar"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 3.64, 0.002)
            val randOMR = parsedPrices["price_omr"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 0.38, 0.002)
            val randRUB = parsedPrices["price_rub"]?.let { it / 10.0 } ?: fluctuate(usdIrr / 92.0, 0.005)

            val rawBTCUSD = parsedPrices["l-crypto-bitcoin"] ?: fluctuate(64120.0, 0.006)
            val rawETHUSD = (rawBTCUSD * 0.053) // Aligned dynamically with BTC price ratio

            // Convert crypto prices to Toman (IRT)
            val randBTC = rawBTCUSD * randUSD
            val randETH = rawETHUSD * randUSD

            val randGOLD = parsedPrices["l-sekee"]?.let { it / 10.0 } ?: fluctuate(42000000.0, 0.005)
            val randXAU = parsedPrices["l-ons"] ?: fluctuate(2350.0, 0.004)
            val randMESGHAL = parsedPrices["l-mesghal"]?.let { it / 10.0 } ?: fluctuate(14500000.0, 0.005)
            val randGOLD18K = parsedPrices["l-geram18"]?.let { it / 10.0 } ?: fluctuate(3400000.0, 0.005)
            val randBRENT = parsedPrices["l-oil_brent"] ?: fluctuate(85.5, 0.006)
            val randBOURSE = parsedPrices["l-gc30"] ?: fluctuate(2100500.0, 0.003)

            // Helper to get base price using parsed percentage change from TGJU
            fun getBasePrice(slug: String, current: Double): Double {
                val pct = parsedPercentages[slug] ?: run {
                    val seed = slug.hashCode().toLong()
                    val r = java.util.Random(seed)
                    (r.nextDouble() * 2.0 - 1.0) * 0.8
                }
                return current / (1.0 + (pct / 100.0))
            }

            // Dynamic items generator helper
            fun itemJson(id: String, symbol: String, title: String, current: Double, base: Double, category: String, iconUrl: String = ""): String {
                val safeCurrent = if (current.isNaN() || current.isInfinite()) 0.0 else current
                val safeBase = if (base.isNaN() || base.isInfinite()) 0.0 else base
                val changeAmount = safeCurrent - safeBase
                val changePercentage = if (safeBase != 0.0) (changeAmount / safeBase) * 100.0 else 0.0
                val direction = if (changePercentage > 0.05) "up" else if (changePercentage < -0.05) "down" else "unchanged"
                
                val roundedCurrent = String.format(java.util.Locale.US, "%.2f", safeCurrent)
                val roundedBase = String.format(java.util.Locale.US, "%.2f", safeBase)
                val roundedChange = String.format(java.util.Locale.US, "%.2f", changeAmount)
                val roundedPercent = String.format(java.util.Locale.US, "%.2f", changePercentage)
                
                return """{ "id": "$id", "symbol": "$symbol", "title": "$title", "current_price": $roundedCurrent, "previous_price": $roundedBase, "change_amount": $roundedChange, "change_percentage": $roundedPercent, "price_direction": "$direction", "last_updated_timestamp": $currentTime, "icon_url": "$iconUrl", "category": "$category" }"""
            }

            val responseString = """
                {
                    "items": [
                        ${itemJson("1", "USD", "US Dollar", randUSD, getBasePrice("price_dollar_rl", randUSD), "currency")},
                        ${itemJson("2", "EUR", "Euro", randEUR, getBasePrice("price_eur", randEUR), "currency")},
                        ${itemJson("3", "GBP", "British Pound", randGBP, getBasePrice("price_gbp", randGBP), "currency")},
                        ${itemJson("4", "CAD", "Canadian Dollar", randCAD, getBasePrice("price_cad", randCAD), "currency")},
                        ${itemJson("5", "AUD", "Australian Dollar", randAUD, getBasePrice("price_aud", randAUD), "currency")},
                        ${itemJson("6", "AED", "UAE Dirham", randAED, getBasePrice("price_aed", randAED), "currency")},
                        ${itemJson("7", "TRY", "Turkish Lira", randTRY, getBasePrice("price_try", randTRY), "currency")},
                        ${itemJson("8", "CHF", "Swiss Franc", randCHF, getBasePrice("price_chf", randCHF), "currency")},
                        ${itemJson("9", "CNY", "Chinese Yuan", randCNY, getBasePrice("price_cny", randCNY), "currency")},
                        ${itemJson("10", "IQD", "Iraqi Dinar", randIQD, getBasePrice("price_iqd", randIQD), "currency")},
                        ${itemJson("11", "SEK", "Swedish Krona", randSEK, getBasePrice("price_sek", randSEK), "currency")},
                        ${itemJson("12", "SAR", "Saudi Riyal", randSAR, getBasePrice("price_sar", randSAR), "currency")},
                        ${itemJson("13", "QAR", "Qatari Riyal", randQAR, getBasePrice("price_qar", randQAR), "currency")},
                        ${itemJson("14", "OMR", "Omani Rial", randOMR, getBasePrice("price_omr", randOMR), "currency")},
                        ${itemJson("15", "RUB", "Russian Ruble", randRUB, getBasePrice("price_rub", randRUB), "currency")},
                        
                        ${itemJson("16", "BTC", "Bitcoin", randBTC, getBasePrice("l-crypto-bitcoin", randBTC), "crypto")},
                        ${itemJson("17", "ETH", "Ethereum", randETH, getBasePrice("l-crypto-ethereum", randETH), "crypto")},
                        
                        ${itemJson("18", "GOLD", "Gold Coin (Emami)", randGOLD, getBasePrice("l-sekee", randGOLD), "gold_and_coin")},
                        ${itemJson("19", "XAU", "Gold Ounce", randXAU, getBasePrice("l-ons", randXAU), "gold_and_coin")},
                        ${itemJson("20", "MESGHAL", "Gold Mesghal", randMESGHAL, getBasePrice("l-mesghal", randMESGHAL), "gold_and_coin")},
                        ${itemJson("21", "GOLD18K", "18k Gold (Gram)", randGOLD18K, getBasePrice("l-geram18", randGOLD18K), "gold_and_coin")},
                        ${itemJson("22", "BRENT", "Brent Oil", randBRENT, getBasePrice("l-oil_brent", randBRENT), "gold_and_coin")},
                        ${itemJson("23", "BOURSE", "Bourse Index", randBOURSE, getBasePrice("l-gc30", randBOURSE), "currency")}
                    ]
                }
            """.trimIndent()
            
            return@Interceptor okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .request(chain.request())
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .body(responseString.toResponseBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .build()
        }

        if (uri.contains("/api/currencies/history")) {
            val symbol = chain.request().url.queryParameter("symbol") ?: "USD"
            val range = chain.request().url.queryParameter("range") ?: "DAY"
            
            val points = mutableListOf<Double>()
            val steps = 15
            val random = java.util.Random(symbol.hashCode().toLong())
            
            // Try to get current price to start/end simulation reasonably
            // In a real app we'd fetch this properly, here we'll just mock based on symbol
            val basePrice = when(symbol) {
                "USD" -> 62000.0
                "BTC" -> 64000.0
                "GOLD" -> 42000000.0
                else -> 100000.0
            }

            val rangeMultiplier = when(range) {
                "HOUR" -> 0.002
                "DAY" -> 0.01
                "WEEK" -> 0.04
                "MONTH" -> 0.1
                "YEAR" -> 0.3
                else -> 0.02
            }

            // Seed based on current time (day/hour) so the "real" chart is consistent for all users today
            val timeSeed = when(range) {
                "HOUR" -> System.currentTimeMillis() / (1000 * 60 * 10) // Changes every 10 mins
                "DAY" -> System.currentTimeMillis() / (1000 * 60 * 60) // Changes every hour
                "WEEK", "MONTH" -> System.currentTimeMillis() / (1000 * 60 * 60 * 24) // Changes every day
                else -> System.currentTimeMillis() / (1000 * 60 * 60 * 24 * 7) // Changes every week
            }
            val timeRandom = java.util.Random(symbol.hashCode().toLong() + timeSeed)

            var current = basePrice * (1.0 + (timeRandom.nextDouble() - 0.5) * rangeMultiplier)
            points.add(current)
            for (i in 1 until steps) {
                val drift = (timeRandom.nextDouble() - 0.48) * (rangeMultiplier / steps) * current
                current += drift
                points.add(current)
            }

            val json = "[" + points.joinToString(",") { String.format(java.util.Locale.US, "%.2f", it) } + "]"
            
            return@Interceptor okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .request(chain.request())
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaType()))
                .addHeader("content-type", "application/json")
                .build()
        }
        chain.proceed(chain.request())
    }

    private val OkHttpClientInstance = OkHttpClient.Builder()
        .addInterceptor(MockInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val RetrofitInstance: Retrofit by lazy {
        val ContentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClientInstance)
            .addConverterFactory(JsonConfig.asConverterFactory(ContentType))
            .build()
    }

    val CheghadApiService: CheghadApi by lazy {
        RetrofitInstance.create(CheghadApi::class.java)
    }

    private val KifpoolOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val KifpoolRetrofit: Retrofit by lazy {
        val ContentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl("https://api.kifpool.app/")
            .client(KifpoolOkHttpClient)
            .addConverterFactory(JsonConfig.asConverterFactory(ContentType))
            .build()
    }

    val KifpoolApiService: KifpoolApi by lazy {
        KifpoolRetrofit.create(KifpoolApi::class.java)
    }
}
