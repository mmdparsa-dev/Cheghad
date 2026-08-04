package com.mmdparsadev.cheghad.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.currentState
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.glance.LocalSize
import androidx.glance.appwidget.SizeMode
import com.mmdparsadev.cheghad.ui.theme.AppThemeColor
import com.mmdparsadev.cheghad.data.repository.dataStore
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.stringPreferencesKey as dsStringPreferencesKey
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mmdparsadev.cheghad.MainActivity
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.database.AppDatabase
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.formatPercent
import com.mmdparsadev.cheghad.formatPrice
import com.mmdparsadev.cheghad.worker.CurrencySyncWorker
import kotlin.math.min

private fun fixedColorProvider(color: Color): ColorProvider = object : ColorProvider {
    override fun getColor(context: Context): Color = color
}

/**
 * Receiver for the Minimal Currency Badge widget.
 */
class MinimalBadgeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MinimalBadgeWidget()

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

/**
 * Receiver for the Price Delta Bar widget.
 */
class PriceDeltaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PriceDeltaWidget()

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

/**
 * Minimal Currency Badge Widget (1x1)
 */
class MinimalBadgeWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val currencies = db.currencyDao().getAllCurrencies()

        val appSettings = context.dataStore.data.first()
        val appColorSeedName = appSettings[dsStringPreferencesKey("color_seed")] ?: "DEFAULT"
        val appColorSeed = AppThemeColor.entries.find { it.name == appColorSeedName } ?: AppThemeColor.DEFAULT

        // Read Lock Screen specific settings from App's main DataStore
        val selectedId = appSettings[dsStringPreferencesKey("lockscreen_widget_currency_id")] ?: "USD"
        val widgetTheme = appSettings[dsStringPreferencesKey("lockscreen_widget_theme")] ?: "glassy"

        provideContent {
            val displayItem = currencies.find { it.id == selectedId } ?: currencies.firstOrNull()
            
            GlanceTheme {
                MinimalBadgeLayout(displayItem, widgetTheme, appColorSeed)
            }
        }
    }
}

/**
 * Price Delta Bar Widget (2x1)
 */
class PriceDeltaWidget : GlanceAppWidget() {
    override val sizeMode = androidx.glance.appwidget.SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val currencies = db.currencyDao().getAllCurrencies()

        val appSettings = context.dataStore.data.first()
        val appColorSeedName = appSettings[dsStringPreferencesKey("color_seed")] ?: "DEFAULT"
        val appColorSeed = AppThemeColor.entries.find { it.name == appColorSeedName } ?: AppThemeColor.DEFAULT

        // Read Lock Screen specific settings from App's main DataStore
        val selectedId = appSettings[dsStringPreferencesKey("lockscreen_widget_currency_id")] ?: "USD"
        val widgetTheme = appSettings[dsStringPreferencesKey("lockscreen_widget_theme")] ?: "glassy"

        provideContent {
            val displayItem = currencies.find { it.id == selectedId } ?: currencies.firstOrNull()

            GlanceTheme {
                PriceDeltaLayout(displayItem, widgetTheme, appColorSeed)
            }
        }
    }
}

@Composable
fun MinimalBadgeLayout(item: CurrencyItem?, theme: String, appColorSeed: AppThemeColor) {
    val context = LocalContext.current
    val isEnglish = isAppEnglish(context)
    val digitType = if (isEnglish) "en" else "fa"

    val isNegative = (item?.changePercentage ?: 0.0) < 0
    val isZeroChange = Math.abs(item?.changePercentage ?: 0.0) < 0.001

    val backgroundModifier = when (theme) {
        "dark" -> GlanceModifier.background(fixedColorProvider(Color(context.getColor(R.color.widget_dark_bg))))
        "light" -> GlanceModifier.background(fixedColorProvider(Color.White))
        "trend" -> {
            val colorRes = when {
                isZeroChange -> R.color.widget_trend_neutral
                isNegative -> R.color.widget_trend_negative
                else -> R.color.widget_trend_positive
            }
            GlanceModifier.background(fixedColorProvider(Color(context.getColor(colorRes))))
        }
        "app_color" -> {
            GlanceModifier.background(fixedColorProvider(appColorSeed.seedColor))
        }
        else -> GlanceModifier
    }

    val size = LocalSize.current
    val minSide = min(size.width.value, size.height.value)
    
    val contentColor = if (theme == "light") fixedColorProvider(Color.Black) else fixedColorProvider(Color.White)
    val secondaryColor = if (theme == "light") fixedColorProvider(Color(context.getColor(R.color.widget_secondary_light))) else fixedColorProvider(Color(context.getColor(R.color.widget_secondary_dark)))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(if (minSide > 60) 16.dp else 12.dp)
            .then(backgroundModifier)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        if (item == null) {
            Text(text = "...", style = TextStyle(color = GlanceTheme.colors.onSurface))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.symbol.uppercase(),
                    style = TextStyle(
                        color = if (theme == "glassy") GlanceTheme.colors.onSurface else contentColor,
                        fontSize = (minSide * 0.28f).sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = formatPrice(item.currentPrice, digitType, item.symbol),
                    style = TextStyle(
                        color = if (theme == "glassy") GlanceTheme.colors.onSurfaceVariant else secondaryColor,
                        fontSize = (minSide * 0.22f).sp
                    )
                )
            }
        }
    }
}

@Composable
fun PriceDeltaLayout(item: CurrencyItem?, theme: String, appColorSeed: AppThemeColor) {
    val context = LocalContext.current
    val isEnglish = isAppEnglish(context)
    val digitType = if (isEnglish) "en" else "fa"

    val isNegative = (item?.changePercentage ?: 0.0) < 0
    val isZeroChange = Math.abs(item?.changePercentage ?: 0.0) < 0.001

    val backgroundModifier = when (theme) {
        "dark" -> GlanceModifier.background(fixedColorProvider(Color(context.getColor(R.color.widget_dark_bg))))
        "light" -> GlanceModifier.background(fixedColorProvider(Color.White))
        "trend" -> {
            val colorRes = when {
                isZeroChange -> R.color.widget_trend_neutral
                isNegative -> R.color.widget_trend_negative
                else -> R.color.widget_trend_positive
            }
            GlanceModifier.background(fixedColorProvider(Color(context.getColor(colorRes))))
        }
        "app_color" -> {
            GlanceModifier.background(fixedColorProvider(appColorSeed.seedColor))
        }
        else -> GlanceModifier
    }

    val size = LocalSize.current
    val minSide = min(size.width.value, size.height.value)
    
    val contentColor = if (theme == "light") fixedColorProvider(Color.Black) else fixedColorProvider(Color.White)
    val secondaryColor = if (theme == "light") fixedColorProvider(Color(context.getColor(R.color.widget_secondary_light))) else fixedColorProvider(Color(context.getColor(R.color.widget_secondary_dark)))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(999.dp)
            .then(backgroundModifier)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        if (item == null) {
            Text(text = "...", style = TextStyle(color = GlanceTheme.colors.onSurface))
        } else {
            Row(
                modifier = GlanceModifier.padding(horizontal = (size.width.value * 0.05f).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = getWidgetLocalizedTitle(item.symbol, item.title, isEnglish),
                        style = TextStyle(
                            color = if (theme == "glassy") GlanceTheme.colors.onSurface else contentColor,
                            fontSize = (minSide * 0.32f).sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = formatPrice(item.currentPrice, digitType, item.symbol),
                        style = TextStyle(
                            color = if (theme == "glassy") GlanceTheme.colors.onSurfaceVariant else secondaryColor,
                            fontSize = (minSide * 0.26f).sp
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.width((size.width.value * 0.05f).dp))
                
                val percentColor = when {
                    theme == "trend" -> contentColor
                    theme == "glassy" -> if (isNegative) GlanceTheme.colors.error else GlanceTheme.colors.primary
                    isNegative -> fixedColorProvider(Color(context.getColor(R.color.widget_negative_red)))
                    else -> fixedColorProvider(Color(context.getColor(R.color.widget_positive_green)))
                }
                
                Text(
                    text = formatPercent(item.changePercentage, digitType),
                    style = TextStyle(
                        color = percentColor,
                        fontSize = (minSide * 0.26f).sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
