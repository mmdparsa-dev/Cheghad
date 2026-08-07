package com.mmdparsadev.cheghad.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.res.ResourcesCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mmdparsadev.cheghad.MainActivity
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.database.AppDatabase
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.formatPercent
import com.mmdparsadev.cheghad.formatPrice
import com.mmdparsadev.cheghad.getLocalizedTitle
import com.mmdparsadev.cheghad.worker.CurrencySyncWorker
import androidx.compose.ui.graphics.toArgb
import com.mmdparsadev.cheghad.ui.theme.AppThemeColor
import com.mmdparsadev.cheghad.data.repository.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.datastore.preferences.core.Preferences as DataStorePreferences
import androidx.datastore.preferences.core.stringPreferencesKey as dsStringPreferencesKey
import kotlin.math.min

import androidx.glance.appwidget.updateAll

suspend fun updateAllWidgets(context: Context) {
    try {
        CurrencyWidget().updateAll(context)
        MinimalBadgeWidget().updateAll(context)
        PriceDeltaWidget().updateAll(context)
    } catch (e: Exception) {
    }
}

class CurrencyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CurrencyWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val workRequest = OneTimeWorkRequestBuilder<CurrencySyncWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val workRequest = OneTimeWorkRequestBuilder<CurrencySyncWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

fun isAppEnglish(context: Context): Boolean {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val digitType = prefs.getString("digit_type", null)
    if (digitType == "en") return true
    if (digitType == "fa") return false
    return java.util.Locale.getDefault().language == "en"
}

fun getWidgetLocalizedTitle(symbol: String, rawTitle: String, isEnglish: Boolean): String {
    val s = symbol.uppercase()
    if (isEnglish) {
        return when {
            s == "USD" || s == "USDT" -> "US Dollar"
            s == "EUR" -> "Euro"
            s == "GBP" -> "British Pound"
            s == "CAD" -> "Canadian Dollar"
            s == "AUD" -> "Australian Dollar"
            s == "AED" -> "UAE Dirham"
            s == "TRY" -> "Turkish Lira"
            s == "CHF" -> "Swiss Franc"
            s == "CNY" -> "Chinese Yuan"
            s == "IQD" -> "Iraqi Dinar"
            s == "SEK" -> "Swedish Krona"
            s == "SAR" -> "Saudi Riyal"
            s == "QAR" -> "Qatari Riyal"
            s == "OMR" -> "Omani Rial"
            s == "RUB" -> "Russian Ruble"
            s == "BTC" -> "Bitcoin"
            s == "ETH" -> "Ethereum"
            s == "SOL" -> "Solana"
            s == "BNB" -> "Binance Coin"
            s == "XRP" -> "Ripple"
            s == "DOGE" -> "Dogecoin"
            s == "ADA" -> "Cardano"
            s == "TRX" -> "TRON"
            s == "AVAX" -> "Avalanche"
            s == "DOT" -> "Polkadot"
            s == "LINK" -> "Chainlink"
            s == "MATIC" || s == "POL" -> "Polygon"
            s == "LTC" -> "Litecoin"
            s == "SHIB" -> "Shiba Inu"
            s == "TON" -> "Toncoin"
            s == "NEAR" -> "NEAR Protocol"
            s == "PEPE" -> "Pepe"
            s == "SUI" -> "Sui"
            s == "UNI" -> "Uniswap"
            s == "USOON" -> "Ondo US Oil"
            s == "GOLD" || s == "PAXG" -> "Emami Gold Coin"
            s == "XAU" -> "Gold Ounce"
            s == "BAHAR" -> "Bahar Azadi Coin"
            s == "NIM" -> "Half Gold Coin"
            s == "RAB" -> "Quarter Gold Coin"
            s == "GERAMI" -> "Gerami Gold Coin"
            s == "18AYAR" || s == "GOLD18K" -> "18K Gold (Gram)"
            s == "MESGHAL" -> "Gold Mesghal"
            s == "BRENT" -> "Brent Crude Oil"
            s == "BOURSE" -> "TSE Bourse Index"
            else -> rawTitle
        }
    } else {
        return getLocalizedTitle(symbol, rawTitle)
    }
}

class CurrencyWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val currencies = db.currencyDao().getAllCurrencies()

        // Get app-wide settings for color seed
        val appSettings = context.dataStore.data.first()
        val appColorSeedName = appSettings[dsStringPreferencesKey("color_seed")] ?: "DEFAULT"
        val appColorSeed = AppThemeColor.entries.find { it.name == appColorSeedName } ?: AppThemeColor.DEFAULT

        provideContent {
            val prefs = currentState<Preferences>()
            val selectedId = prefs[stringPreferencesKey("selected_currency_id")]
            val theme = prefs[stringPreferencesKey("widget_theme")] ?: "glassy"
            
            val displayItem = currencies.find { it.id == selectedId } ?: currencies.firstOrNull()

            GlanceTheme {
                WidgetLayout(displayItem, theme, appColorSeed)
            }
        }
    }
}

@Composable
fun WidgetLayout(item: CurrencyItem?, theme: String, appColorSeed: AppThemeColor) {
    val context = LocalContext.current
    val size = LocalSize.current
    val isEnglish = isAppEnglish(context)

    val density = context.resources.displayMetrics.density
    val widthPx = (size.width.value * density).toInt().coerceAtLeast(150)
    val heightPx = (size.height.value * density).toInt().coerceAtLeast(150)

    val bitmap = remember(item, isEnglish, widthPx, heightPx, theme, appColorSeed) {
        renderWidgetBitmap(context, item, isEnglish, widthPx, heightPx, theme, appColorSeed)
    }

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = "Currency Widget",
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    )
}

fun renderWidgetBitmap(
    context: Context,
    item: CurrencyItem?,
    isEnglish: Boolean,
    widthPx: Int,
    heightPx: Int,
    theme: String,
    appColorSeed: AppThemeColor = AppThemeColor.DEFAULT
): Bitmap {
    val w = widthPx.coerceAtLeast(150)
    val h = heightPx.coerceAtLeast(150)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val vazirTypeface: Typeface? = try {
        ResourcesCompat.getFont(context, R.font.vazir_variable)
    } catch (e: Exception) {
        Typeface.DEFAULT
    }

    // Proportional layout variables based on square dimensions
    val squareSide = min(w, h).toFloat()
    val padding = squareSide * 0.085f
    val cornerRadius = squareSide * 0.15f

    val isNegative = (item?.changePercentage ?: 0.0) < 0
    val isZeroChange = Math.abs(item?.changePercentage ?: 0.0) < 0.001

    // 1. Background logic based on theme
    val bgColor = when (theme) {
        "dark" -> android.graphics.Color.parseColor("#FF1E1E24")
        "light" -> android.graphics.Color.WHITE
        "trend" -> when {
            isZeroChange -> android.graphics.Color.parseColor("#FF757575") // Gray for no change
            isNegative -> android.graphics.Color.parseColor("#FFD32F2F")
            else -> android.graphics.Color.parseColor("#FF388E3C")
        }
        "app_color" -> {
            appColorSeed.seedColor.toArgb()
        }
        else -> android.graphics.Color.parseColor("#E61E1E24") // glassy (default)
    }
    
    val contentColor = if (theme == "light") android.graphics.Color.BLACK else android.graphics.Color.WHITE
    val secondaryContentColor = if (theme == "light") android.graphics.Color.parseColor("#99000000") else android.graphics.Color.parseColor("#B3FFFFFF")

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(), cornerRadius, cornerRadius, bgPaint)

    if (item == null) {
        val loadingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = contentColor
            textSize = squareSide * 0.12f
            typeface = vazirTypeface
            textAlign = Paint.Align.CENTER
        }
        val loadingText = "-------"
        canvas.drawText(loadingText, w / 2f, h / 2f, loadingPaint)
        return bitmap
    }

    // Format strings
    val digitType = if (isEnglish) "en" else "fa"
    val localizedTitle = getWidgetLocalizedTitle(item.symbol, item.title, isEnglish)
    val formattedPrice = formatPrice(item.currentPrice, digitType, item.symbol)
    val formattedPercent = formatPercent(item.changePercentage, digitType)
    val unitText = if (isEnglish) "Toman" else "تومان"

    val changeColor = when {
        isZeroChange -> secondaryContentColor
        theme == "trend" -> contentColor
        isNegative -> android.graphics.Color.parseColor("#FFFF5252")
        else -> android.graphics.Color.parseColor("#FF4CAF50")
    }

    val symbolBadgeIcon = when {
        item.symbol.contains("USD", ignoreCase = true) -> "$"
        item.symbol.contains("EUR", ignoreCase = true) -> "€"
        item.symbol.contains("GBP", ignoreCase = true) -> "£"
        item.symbol.contains("BTC", ignoreCase = true) -> "₿"
        item.symbol.contains("ETH", ignoreCase = true) -> "Ξ"
        item.symbol.contains("USDT", ignoreCase = true) -> "₮"
        item.category == CurrencyType.GoldAndCoin -> "🪙"
        else -> item.symbol.take(1).uppercase()
    }

    // Dynamic scale ratios
    val badgeRadius = squareSide * 0.11f
    val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (theme == "light") android.graphics.Color.parseColor("#22000000") else android.graphics.Color.parseColor("#33FFFFFF")
        style = Paint.Style.FILL
    }
    val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = contentColor
        textSize = badgeRadius * 0.85f
        typeface = vazirTypeface
        textAlign = Paint.Align.CENTER
    }

    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = contentColor
        textSize = squareSide * 0.082f
        typeface = Typeface.create(vazirTypeface, Typeface.BOLD)
    }

    val symbolPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryContentColor
        textSize = squareSide * 0.052f
        typeface = vazirTypeface
    }

    // 2. Top Header Row Positioning
    val topY = padding
    val badgeY = topY + badgeRadius
    val availableTitleWidth = w - (padding * 2.2f) - (badgeRadius * 2) - (padding * 0.5f)

    // Auto-scale title if needed
    while (titlePaint.measureText(localizedTitle) > availableTitleWidth && titlePaint.textSize > squareSide * 0.04f) {
        titlePaint.textSize -= 0.5f
    }

    if (isEnglish) {
        // LTR Layout: Title/Symbol on Left, Badge on Right
        val badgeX = w - padding - badgeRadius
        canvas.drawCircle(badgeX, badgeY, badgeRadius, badgeBgPaint)
        val badgeTextY = badgeY - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f)
        canvas.drawText(symbolBadgeIcon, badgeX, badgeTextY, badgeTextPaint)

        titlePaint.textAlign = Paint.Align.LEFT
        symbolPaint.textAlign = Paint.Align.LEFT

        val titleX = padding
        val titleBaselineY = topY + titlePaint.textSize
        canvas.drawText(localizedTitle, titleX, titleBaselineY, titlePaint)

        val symbolBaselineY = titleBaselineY + symbolPaint.textSize + (squareSide * 0.02f)
        canvas.drawText(item.symbol.uppercase(), titleX, symbolBaselineY, symbolPaint)
    } else {
        // RTL Layout: Badge on Left, Title/Symbol on Right
        val badgeX = padding + badgeRadius
        canvas.drawCircle(badgeX, badgeY, badgeRadius, badgeBgPaint)
        val badgeTextY = badgeY - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f)
        canvas.drawText(symbolBadgeIcon, badgeX, badgeTextY, badgeTextPaint)

        titlePaint.textAlign = Paint.Align.RIGHT
        symbolPaint.textAlign = Paint.Align.RIGHT

        val titleX = w - padding
        val titleBaselineY = topY + titlePaint.textSize
        canvas.drawText(localizedTitle, titleX, titleBaselineY, titlePaint)

        val symbolBaselineY = titleBaselineY + symbolPaint.textSize + (squareSide * 0.02f)
        canvas.drawText(item.symbol.uppercase(), titleX, symbolBaselineY, symbolPaint)
    }

    // 3. Bottom Row Typography & Auto-scaling
    val pricePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = contentColor
        textSize = squareSide * 0.125f
        typeface = Typeface.create(vazirTypeface, Typeface.BOLD)
    }

    val unitPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryContentColor
        textSize = squareSide * 0.058f
        typeface = vazirTypeface
    }

    val percentPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = changeColor
        textSize = squareSide * 0.058f
        typeface = Typeface.create(vazirTypeface, Typeface.BOLD)
    }

    val percentWidth = percentPaint.measureText(formattedPercent)
    val maxPriceWidth = w - (padding * 2.2f) - percentWidth - (squareSide * 0.08f)

    // Auto-fit price text if long
    while ((pricePaint.measureText(formattedPrice) + unitPaint.measureText(unitText)) > maxPriceWidth && pricePaint.textSize > squareSide * 0.05f) {
        pricePaint.textSize -= 0.5f
    }

    val bottomBaselineY = h - padding - (squareSide * 0.02f)

    if (isEnglish) {
        // LTR: Price & Unit on Left, Percent on Right
        pricePaint.textAlign = Paint.Align.LEFT
        unitPaint.textAlign = Paint.Align.LEFT
        percentPaint.textAlign = Paint.Align.RIGHT

        val priceX = padding
        canvas.drawText(formattedPrice, priceX, bottomBaselineY, pricePaint)

        val priceWidth = pricePaint.measureText(formattedPrice)
        val unitX = priceX + priceWidth + (squareSide * 0.025f)
        canvas.drawText(unitText, unitX, bottomBaselineY, unitPaint)

        val percentX = w - padding
        canvas.drawText(formattedPercent, percentX, bottomBaselineY, percentPaint)
    } else {
        // RTL: Percent on Left, Price & Unit on Right
        pricePaint.textAlign = Paint.Align.RIGHT
        unitPaint.textAlign = Paint.Align.RIGHT
        percentPaint.textAlign = Paint.Align.LEFT

        val percentX = padding
        canvas.drawText(formattedPercent, percentX, bottomBaselineY, percentPaint)

        val priceX = w - padding
        canvas.drawText(formattedPrice, priceX, bottomBaselineY, pricePaint)

        val priceWidth = pricePaint.measureText(formattedPrice)
        val unitX = priceX - priceWidth - (squareSide * 0.025f)
        canvas.drawText(unitText, unitX, bottomBaselineY, unitPaint)
    }

    return bitmap
}
