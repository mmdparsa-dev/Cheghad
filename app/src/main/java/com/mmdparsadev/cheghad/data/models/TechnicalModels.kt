package com.mmdparsadev.cheghad.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TgjuHistoryResponse(
    @SerialName("s") val status: String? = null,
    @SerialName("t") val timestamps: List<Long>? = null,
    @SerialName("o") val openPrices: List<Double>? = null,
    @SerialName("h") val highPrices: List<Double>? = null,
    @SerialName("l") val lowPrices: List<Double>? = null,
    @SerialName("c") val closePrices: List<Double>? = null,
    @SerialName("v") val volumes: List<Double>? = null
)

data class CandleData(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val ema200: Double? = null,
    val rsi: Double? = null
)

enum class ChartTimeframe(
    val titleFa: String,
    val titleEn: String,
    val resolution: String,
    val secondsPerCandle: Long
) {
    M15("۱۵ دقیقه", "15m", "15", 15 * 60L),
    H1("۱ ساعت", "1h", "60", 60 * 60L),
    H3("۳ ساعت", "3h", "180", 3 * 60 * 60L),
    H4("۴ ساعت", "4h", "240", 4 * 60 * 60L),
    H6("۶ ساعت", "6h", "360", 6 * 60 * 60L),
    H12("۱۲ ساعت", "12h", "720", 12 * 60 * 60L),
    D1("۱ روز", "1D", "1D", 24 * 60 * 60L),
    D5("۵ روز", "5D", "5D", 5 * 24 * 60 * 60L),
    W1("۱ هفته", "1W", "1W", 7 * 24 * 60 * 60L),
    W2("۲ هفته", "2W", "2W", 14 * 24 * 60 * 60L),
    M1("۱ ماه", "1M", "1M", 30 * 24 * 60 * 60L),
    M3("۳ ماه", "3M", "3M", 90 * 24 * 60 * 60L)
}

enum class EmaTouchStatus {
    TOUCHED_FROM_ABOVE, // Reached EMA 200 from above (drop/pullback from top) -> Light red
    TOUCHED_FROM_BELOW, // Reached EMA 200 from below (rally/bounce from bottom) -> Light green
    ABOVE_EMA,
    BELOW_EMA,
    NONE
}

data class EmaAssetAnalysis(
    val currencyItem: CurrencyItem,
    val status: EmaTouchStatus,
    val currentPrice: Double,
    val ema200: Double,
    val distancePercentage: Double
)

object TgjuSymbolMapper {
    private val mapping = mapOf(
        "USD" to "price_dollar_rl",
        "EUR" to "price_eur",
        "GBP" to "price_gbp",
        "AED" to "price_aed",
        "CAD" to "price_cad",
        "AUD" to "price_aud",
        "TRY" to "price_try",
        "CHF" to "price_chf",
        "CNY" to "price_cny",
        "IQD" to "price_iqd",
        "SEK" to "price_sek",
        "SAR" to "price_sar",
        "QAR" to "price_qar",
        "OMR" to "price_omr",
        "RUB" to "price_rub",
        "GOLD" to "sekee",
        "GOLD18K" to "geram18",
        "MESGHAL" to "mesghal",
        "XAU" to "ons",
        "BTC" to "crypto-bitcoin",
        "ETH" to "crypto-ethereum",
        "USOON" to "crypto-tether",
        "BRENT" to "oil_brent",
        "BOURSE" to "gc30"
    )

    fun toTgjuSymbol(symbol: String): String {
        return mapping[symbol.uppercase()] ?: mapping[symbol] ?: "price_dollar_rl"
    }

    fun isTomanPrice(symbol: String): Boolean {
        // These are quoted in IRR by TGJU, so dividing by 10 converts to Toman
        return when (symbol.uppercase()) {
            "USD", "EUR", "GBP", "AED", "CAD", "AUD", "TRY", "CHF", "CNY", "IQD", "SEK", "SAR", "QAR", "OMR", "RUB",
            "GOLD", "GOLD18K", "MESGHAL" -> true
            else -> false
        }
    }
}

/**
 * Technical Chart Drawing Tool Types
 */
enum class DrawingToolType(val titleFa: String, val titleEn: String) {
    NONE("پیمایش و زوم", "Pan & Zoom"),
    TREND_LINE("خط روند", "Trend Line"),
    HORIZONTAL_LINE("خط افقی (حمایت/مقاومت)", "Horizontal Line"),
    RAY("پیکان و نیم‌خط", "Ray / Arrow"),
    RECTANGLE("مستطیل و زون", "Rectangle / Zone"),
    FIBONACCI("فیبوناچی ریتریسمنت", "Fibonacci Retracement"),
    BRUSH("قلم آزاد", "Freehand Brush"),
    TEXT("یادداشت متنی", "Text Annotation")
}

/**
 * Coordinate anchored to time and price so drawings stay locked to candles during pan & zoom
 */
data class ChartPoint(
    val time: Long,
    val price: Double
)

/**
 * Base sealed class for all technical drawings on chart
 */
sealed class ChartDrawing {
    abstract val id: String
    abstract val color: Long
    abstract val strokeWidth: Float

    data class TrendLine(
        override val id: String,
        val start: ChartPoint,
        val end: ChartPoint,
        override val color: Long = 0xFFFFEB3B, // Yellow
        override val strokeWidth: Float = 2.5f
    ) : ChartDrawing()

    data class HorizontalLine(
        override val id: String,
        val price: Double,
        override val color: Long = 0xFF00E5FF, // Cyan
        override val strokeWidth: Float = 2f
    ) : ChartDrawing()

    data class Ray(
        override val id: String,
        val start: ChartPoint,
        val end: ChartPoint,
        override val color: Long = 0xFFFF9100, // Orange
        override val strokeWidth: Float = 2.5f
    ) : ChartDrawing()

    data class Rectangle(
        override val id: String,
        val start: ChartPoint,
        val end: ChartPoint,
        override val color: Long = 0xFF00E676, // Green
        override val strokeWidth: Float = 2f,
        val filled: Boolean = true
    ) : ChartDrawing()

    data class Fibonacci(
        override val id: String,
        val start: ChartPoint,
        val end: ChartPoint,
        override val color: Long = 0xFFE040FB, // Magenta / Purple
        override val strokeWidth: Float = 1.5f
    ) : ChartDrawing()

    data class Brush(
        override val id: String,
        val points: List<ChartPoint>,
        override val color: Long = 0xFFFF5252, // Coral Red
        override val strokeWidth: Float = 3f
    ) : ChartDrawing()

    data class Text(
        override val id: String,
        val position: ChartPoint,
        val text: String,
        override val color: Long = 0xFFFFFFFF, // White
        override val strokeWidth: Float = 1f,
        val fontSizeSp: Float = 12f
    ) : ChartDrawing()
}

