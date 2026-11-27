package com.mmdparsadev.cheghad.data.api

import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyResponse
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.PriceDirection
import com.mmdparsadev.cheghad.data.models.TgjuSymbolMapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.round

object ApiClient {
    private const val BASE_URL = "https://api.example.com/" // Intercepted locally via LiveTgjuInterceptor
    private const val TGJU_AJAX_URL = "https://call.tgju.org/ajax.json"
    private const val TGJU_TV2_HISTORY_URL = "https://dashboard-api.tgju.org/v1/tv2/history"
    private const val USD_SLUG = "price_dollar_rl"
    private const val USOON_SYMBOL = "USOON"
    private const val USOON_ICON_URL = "https://cdn.bitpin.ir/media/market/currency/1771999775.svg"
    private const val USD_CACHE_TTL_MS = 60_000L

    private val JsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val ScraperOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var cachedUsdToman: Pair<Double, Long>? = null

    private enum class UnitMode {
        IRR_TO_TOMAN,
        USD_TO_TOMAN,
        NATIVE
    }

    private data class AssetSpec(
        val id: String,
        val symbol: String,
        val title: String,
        val category: CurrencyType,
        val unitMode: UnitMode,
        val iconUrl: String = ""
    ) {
        val tgjuSlug: String = TgjuSymbolMapper.toTgjuSymbol(symbol)
    }

    private val ASSET_SPECS = listOf(
        AssetSpec("1", "USD", "US Dollar", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("2", "EUR", "Euro", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("3", "GBP", "British Pound", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("4", "CAD", "Canadian Dollar", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("5", "AUD", "Australian Dollar", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("6", "AED", "UAE Dirham", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("7", "TRY", "Turkish Lira", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("8", "CHF", "Swiss Franc", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("9", "CNY", "Chinese Yuan", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("10", "IQD", "Iraqi Dinar", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("11", "SEK", "Swedish Krona", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("12", "SAR", "Saudi Riyal", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("13", "QAR", "Qatari Riyal", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("14", "OMR", "Omani Rial", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("15", "RUB", "Russian Ruble", CurrencyType.Currency, UnitMode.IRR_TO_TOMAN),
        AssetSpec("16", "BTC", "Bitcoin", CurrencyType.Crypto, UnitMode.USD_TO_TOMAN),
        AssetSpec("17", "ETH", "Ethereum", CurrencyType.Crypto, UnitMode.USD_TO_TOMAN),
        AssetSpec("24", USOON_SYMBOL, "Ondo US Oil", CurrencyType.Crypto, UnitMode.USD_TO_TOMAN, USOON_ICON_URL),
        AssetSpec("18", "GOLD", "Gold Coin (Emami)", CurrencyType.GoldAndCoin, UnitMode.IRR_TO_TOMAN),
        AssetSpec("19", "XAU", "Gold Ounce", CurrencyType.GoldAndCoin, UnitMode.NATIVE),
        AssetSpec("20", "MESGHAL", "Gold Mesghal", CurrencyType.GoldAndCoin, UnitMode.IRR_TO_TOMAN),
        AssetSpec("21", "GOLD18K", "18k Gold (Gram)", CurrencyType.GoldAndCoin, UnitMode.IRR_TO_TOMAN),
        AssetSpec("22", "BRENT", "Brent Oil", CurrencyType.GoldAndCoin, UnitMode.NATIVE),
        AssetSpec("23", "BOURSE", "Bourse Index", CurrencyType.Currency, UnitMode.NATIVE)
    )

    @Serializable
    private data class TgjuTv2CloseResponse(
        @SerialName("s") val status: String? = null,
        @SerialName("t") val timestamps: List<Long>? = null,
        @SerialName("c") val closePrices: List<Double>? = null
    )

    internal data class TgjuAjaxResult(
        val prices: Map<String, Double>,
        val percentages: Map<String, Double>
    )

    private val LiveTgjuInterceptor = Interceptor { chain ->
        val request = chain.request()
        val uri = request.url.toString()

        if (uri.endsWith("/api/currencies")) {
            return@Interceptor handleCurrenciesRequest(request)
        }

        if (uri.contains("/api/currencies/history")) {
            return@Interceptor handleHistoryRequest(request)
        }

        chain.proceed(request)
    }

    private fun handleCurrenciesRequest(request: Request): Response {
        val currentTimeMs = System.currentTimeMillis()
        val toTs = currentTimeMs / 1000L
        val usoonSlug = TgjuSymbolMapper.toTgjuSymbol(USOON_SYMBOL)

        val ajaxFuture = CompletableFuture.supplyAsync { fetchTgjuAjax() }
        val usoonFuture = CompletableFuture.supplyAsync {
            fetchTv2History(usoonSlug, "60", toTs - 3L * 86400L, toTs)
        }

        val ajaxResult = runCatching { ajaxFuture.get(12, TimeUnit.SECONDS) }.getOrNull()
        val usoonHistory = runCatching { usoonFuture.get(12, TimeUnit.SECONDS) }.getOrNull()

        val liveUsdToman = ajaxResult?.prices?.get(USD_SLUG)?.div(10.0)
        if (ajaxResult == null || ajaxResult.prices.isEmpty() || liveUsdToman == null || liveUsdToman <= 0.0) {
            return errorResponse(request, 503, "Service Unavailable - Live TGJU USD rate unreachable")
        }

        cachedUsdToman = liveUsdToman to currentTimeMs

        val mergedPrices = ajaxResult.prices.toMutableMap()
        val mergedPercentages = ajaxResult.percentages.toMutableMap()

        val usoonCloses = usoonHistory?.closePrices?.filter { it > 0.0 }.orEmpty()
        if (usoonCloses.isNotEmpty()) {
            val latestClose = usoonCloses.last()
            mergedPrices[usoonSlug] = latestClose
            if (usoonCloses.size >= 2) {
                val prevClose = usoonCloses[usoonCloses.size - 2]
                if (prevClose > 0.0) {
                    mergedPercentages[usoonSlug] = ((latestClose - prevClose) / prevClose) * 100.0
                }
            }
        }

        val items = buildCurrencyItems(
            prices = mergedPrices,
            percentages = mergedPercentages,
            liveUsdToman = liveUsdToman,
            timestampMs = currentTimeMs
        )
        val payload = JsonConfig.encodeToString(CurrencyResponse(items))
        return jsonResponse(request, 200, "OK", payload)
    }

    private fun handleHistoryRequest(request: Request): Response {
        val symbol = request.url.queryParameter("symbol") ?: "USD"
        val range = request.url.queryParameter("range") ?: "DAY"

        val tgjuSlug = TgjuSymbolMapper.findTgjuSymbol(symbol)
            ?: return errorResponse(request, 503, "Unsupported symbol: $symbol")

        val (windowSeconds, primaryResolution) = mapRangeToTgjuParams(range)
        val toTs = System.currentTimeMillis() / 1000L
        val fromTs = (toTs - windowSeconds).coerceAtLeast(0L)

        var history = fetchTv2History(tgjuSlug, primaryResolution, fromTs, toTs)
        var windowedCloses = sliceHistoryCloses(
            timestamps = history?.timestamps,
            closes = history?.closePrices.orEmpty(),
            fromTs = fromTs,
            toTs = toTs,
            windowSeconds = windowSeconds
        )

        // Some TGJU assets lack 1m/5m candles or have very sparse hourly points in the immediate window;
        // try alternate intraday resolutions so 1H and 1D charts show meaningful recent structure.
        if (windowedCloses.size < 4 && primaryResolution in listOf("1", "5", "60")) {
            val fallbackRes = when (primaryResolution) {
                "1" -> listOf("5", "15", "60")
                "5" -> listOf("15", "60")
                else -> listOf("15", "5")
            }
            for (res in fallbackRes) {
                val candidate = fetchTv2History(tgjuSlug, res, (toTs - maxOf(windowSeconds, 3L * 86400L)).coerceAtLeast(0L), toTs)
                    ?: continue
                val candidateWindow = sliceHistoryCloses(
                    timestamps = candidate.timestamps,
                    closes = candidate.closePrices.orEmpty(),
                    fromTs = fromTs,
                    toTs = toTs,
                    windowSeconds = windowSeconds
                )
                if (candidateWindow.size > windowedCloses.size) {
                    history = candidate
                    windowedCloses = candidateWindow
                }
                if (windowedCloses.size >= 4) break
            }
        }

        if (history == null || history.closePrices.isNullOrEmpty()) {
            return errorResponse(request, 503, "TGJU history unavailable for $symbol ($range)")
        }
        if (windowedCloses.isEmpty()) {
            return errorResponse(request, 503, "Empty TGJU history window for $symbol ($range)")
        }

        val converted = convertHistoryCloses(symbol, windowedCloses) { getLiveUsdTomanRate() }
            ?: return errorResponse(request, 503, "Live TGJU USD rate unavailable for $symbol conversion")

        val jsonBody = JsonConfig.encodeToString(JsonArray(converted.map { JsonPrimitive(roundValue(it)) }))
        return jsonResponse(request, 200, "OK", jsonBody)
    }

    internal fun buildCurrencyItems(
        prices: Map<String, Double>,
        percentages: Map<String, Double>,
        liveUsdToman: Double,
        timestampMs: Long
    ): List<CurrencyItem> {
        return ASSET_SPECS.map { spec ->
            val rawPrice = prices[spec.tgjuSlug] ?: 0.0
            val current = when (spec.unitMode) {
                UnitMode.IRR_TO_TOMAN -> rawPrice / 10.0
                UnitMode.USD_TO_TOMAN -> if (rawPrice > 0.0 && liveUsdToman > 0.0) rawPrice * liveUsdToman else 0.0
                UnitMode.NATIVE -> rawPrice
            }
            val safeCurrent = if (current.isNaN() || current.isInfinite() || current <= 0.0) 0.0 else current
            val pct = percentages[spec.tgjuSlug] ?: 0.0
            val denom = 1.0 + (pct / 100.0)
            val rawPrevious = if (safeCurrent > 0.0 && denom > 0.0) safeCurrent / denom else safeCurrent
            val safePrevious = if (rawPrevious.isNaN() || rawPrevious.isInfinite()) safeCurrent else rawPrevious

            val changeAmount = safeCurrent - safePrevious
            val changePercentage = if (safePrevious != 0.0) (changeAmount / safePrevious) * 100.0 else 0.0
            val direction = when {
                changePercentage > 0.05 -> PriceDirection.Up
                changePercentage < -0.05 -> PriceDirection.Down
                else -> PriceDirection.Unchanged
            }

            CurrencyItem(
                id = spec.id,
                symbol = spec.symbol,
                title = spec.title,
                currentPrice = roundValue(safeCurrent),
                previousPrice = roundValue(safePrevious),
                changeAmount = roundValue(changeAmount),
                changePercentage = round2(changePercentage),
                priceDirection = direction,
                lastUpdatedTimestamp = timestampMs,
                iconUrl = spec.iconUrl,
                category = spec.category
            )
        }
    }

    internal fun mapRangeToTgjuParams(range: String): Pair<Long, String> {
        return when (range.uppercase(Locale.US)) {
            "1M", "1MIN", "MIN1" -> 350L * 60L to "1"
            "5M", "5MIN", "MIN5" -> 350L * 5L * 60L to "5"
            "HOUR" -> 3600L to "5"
            "DAY" -> 86400L to "60"
            "WEEK" -> 7L * 86400L to "240"
            "MONTH" -> 30L * 86400L to "D"
            "YEAR" -> 365L * 86400L to "W"
            else -> 86400L to "60"
        }
    }

    internal fun sliceHistoryCloses(
        timestamps: List<Long>?,
        closes: List<Double>,
        fromTs: Long,
        toTs: Long,
        windowSeconds: Long
    ): List<Double> {
        if (timestamps == null || timestamps.size < closes.size) {
            return closes.filter { it > 0.0 }.takeLast(60)
        }

        val validPairs = closes.indices
            .map { i -> timestamps[i] to closes[i] }
            .filter { (ts, price) -> ts <= toTs && price > 0.0 && !price.isNaN() && !price.isInfinite() }
        if (validPairs.isEmpty()) return emptyList()

        val inWindow = validPairs.filter { (ts, _) -> ts >= fromTs }
        val latestTs = validPairs.last().first
        val isRecentEnough = latestTs >= toTs - (windowSeconds / 4L)
        val result = if (inWindow.size >= 2 && (isRecentEnough || inWindow.size >= 6)) {
            inWindow.map { it.second }
        } else {
            // Anchor the requested window at the latest real candle when markets are closed (e.g. weekends/holidays/off-hours)
            val anchorStart = (latestTs - windowSeconds).coerceAtLeast(0L)
            validPairs.filter { (ts, _) -> ts >= anchorStart }.map { it.second }
        }
        return if (result.size >= 2 || validPairs.size < 2) {
            result
        } else {
            // Daily-only assets on short windows (e.g. 1H) still need at least 2 real points for a chart line
            validPairs.takeLast(2).map { it.second }
        }
    }

    internal fun convertHistoryCloses(
        symbol: String,
        closes: List<Double>,
        liveUsdRateProvider: () -> Double?
    ): List<Double>? {
        return when {
            TgjuSymbolMapper.isTomanPrice(symbol) -> closes.map { it / 10.0 }
            TgjuSymbolMapper.isUsdCrypto(symbol) -> {
                val usdRate = liveUsdRateProvider() ?: return null
                if (usdRate <= 0.0) return null
                closes.map { it * usdRate }
            }
            else -> closes
        }
    }

    internal fun parseTgjuAjaxJson(body: String): TgjuAjaxResult {
        val prices = LinkedHashMap<String, Double>()
        val percentages = LinkedHashMap<String, Double>()

        val currentObj = runCatching {
            JsonConfig.parseToJsonElement(body).jsonObject["current"]?.jsonObject
        }.getOrNull() ?: return TgjuAjaxResult(prices, percentages)

        for ((slug, element) in currentObj) {
            val entry = element as? JsonObject ?: continue
            val price = parseTgjuNumber(entry["p"]?.jsonPrimitive?.contentOrNull)
            if (price != null && price > 0.0) {
                prices[slug] = price
            }

            val dp = parseTgjuNumber(entry["dp"]?.jsonPrimitive?.contentOrNull)
            val dt = entry["dt"]?.jsonPrimitive?.contentOrNull.orEmpty()
            if (dp != null) {
                percentages[slug] = when (dt.lowercase(Locale.US)) {
                    "low" -> -abs(dp)
                    "high" -> abs(dp)
                    else -> dp
                }
            }
        }
        return TgjuAjaxResult(prices, percentages)
    }

    internal fun parseTgjuNumber(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        val normalized = buildString(raw.length) {
            for (ch in raw) {
                when (ch) {
                    in '۰'..'۹' -> append(('0'.code + (ch.code - '۰'.code)).toChar())
                    in '٠'..'٩' -> append(('0'.code + (ch.code - '٠'.code)).toChar())
                    ',', '٬', ' ' -> Unit
                    else -> append(ch)
                }
            }
        }.trim()
        return normalized.toDoubleOrNull()?.takeIf { !it.isNaN() && !it.isInfinite() }
    }

    private fun fetchTgjuAjax(): TgjuAjaxResult? {
        return try {
            val request = Request.Builder()
                .url(TGJU_AJAX_URL)
                .header("Referer", "https://www.tgju.org/")
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .build()

            ScraperOkHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body.string()
                if (body.isEmpty()) return null
                val parsed = parseTgjuAjaxJson(body)
                parsed.prices[USD_SLUG]?.div(10.0)?.takeIf { it > 0.0 }?.let { usdToman ->
                    cachedUsdToman = usdToman to System.currentTimeMillis()
                }
                parsed
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchTv2History(
        slug: String,
        resolution: String,
        fromTs: Long,
        toTs: Long
    ): TgjuTv2CloseResponse? {
        return try {
            val url = "$TGJU_TV2_HISTORY_URL?symbol=$slug&resolution=$resolution&from=$fromTs&to=$toTs"
            val request = Request.Builder()
                .url(url)
                .header("Referer", "https://www.tgju.org/")
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .build()

            ScraperOkHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body.string()
                if (body.isEmpty()) return null
                val decoded = JsonConfig.decodeFromString<TgjuTv2CloseResponse>(body)
                if (decoded.status == "ok" && !decoded.closePrices.isNullOrEmpty()) decoded else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getLiveUsdTomanRate(): Double? {
        val now = System.currentTimeMillis()
        val cached = cachedUsdToman
        if (cached != null && (now - cached.second) < USD_CACHE_TTL_MS && cached.first > 0.0) {
            return cached.first
        }
        return fetchTgjuAjax()?.prices?.get(USD_SLUG)?.div(10.0)?.takeIf { it > 0.0 }
    }

    private fun round2(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return round(value * 100.0) / 100.0
    }

    private fun roundValue(value: Double): Double {
        if (value.isNaN() || value.isInfinite() || value == 0.0) return 0.0
        return if (abs(value) >= 1.0) round(value * 100.0) / 100.0 else round(value * 10000.0) / 10000.0
    }

    private fun jsonResponse(request: Request, code: Int, message: String, body: String): Response {
        return Response.Builder()
            .code(code)
            .message(message)
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(body.toResponseBody("application/json".toMediaType()))
            .addHeader("content-type", "application/json")
            .build()
    }

    private fun errorResponse(request: Request, code: Int, message: String): Response {
        val errorBody = buildJsonObject { put("error", message) }.toString()
        return jsonResponse(request, code, message, errorBody)
    }

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    private val OkHttpClientInstance = OkHttpClient.Builder()
        .addInterceptor(LiveTgjuInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val RetrofitInstance: Retrofit by lazy {
        val contentType = "application/json".toMediaType()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClientInstance)
            .addConverterFactory(JsonConfig.asConverterFactory(contentType))
            .build()
    }

    val CheghadApiService: CheghadApi by lazy {
        RetrofitInstance.create(CheghadApi::class.java)
    }
}
