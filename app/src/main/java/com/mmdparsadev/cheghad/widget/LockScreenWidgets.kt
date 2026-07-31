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
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val currencies = db.currencyDao().getAllCurrencies()

        val appSettings = context.dataStore.data.first()
        val appColorSeedName = appSettings[dsStringPreferencesKey("color_seed")] ?: "DEFAULT"
        val appColorSeed = AppThemeColor.entries.find { it.name == appColorSeedName } ?: AppThemeColor.DEFAULT

        provideContent {
            val prefs = currentState<Preferences>()
            val selectedId = prefs[stringPreferencesKey("selected_currency_id")]
            val theme = prefs[stringPreferencesKey("widget_theme")] ?: "glassy"
            val displayItem = currencies.find { it.Id == selectedId } ?: currencies.firstOrNull()
            
            GlanceTheme {
                MinimalBadgeLayout(displayItem, theme, appColorSeed)
            }
        }
    }
}

/**
 * Price Delta Bar Widget (2x1)
 */
class PriceDeltaWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val currencies = db.currencyDao().getAllCurrencies()

        val appSettings = context.dataStore.data.first()
        val appColorSeedName = appSettings[dsStringPreferencesKey("color_seed")] ?: "DEFAULT"
        val appColorSeed = AppThemeColor.entries.find { it.name == appColorSeedName } ?: AppThemeColor.DEFAULT

        provideContent {
            val prefs = currentState<Preferences>()
            val selectedId = prefs[stringPreferencesKey("selected_currency_id")]
            val theme = prefs[stringPreferencesKey("widget_theme")] ?: "glassy"
            val displayItem = currencies.find { it.Id == selectedId } ?: currencies.firstOrNull()

            GlanceTheme {
                PriceDeltaLayout(displayItem, theme, appColorSeed)
            }
        }
    }
}

@Composable
fun MinimalBadgeLayout(item: CurrencyItem?, theme: String, appColorSeed: AppThemeColor) {
    val context = LocalContext.current
    val isEnglish = isAppEnglish(context)
    val digitType = if (isEnglish) "en" else "fa"

    val isNegative = (item?.ChangePercentage ?: 0.0) < 0
    val isZeroChange = Math.abs(item?.ChangePercentage ?: 0.0) < 0.001

    val backgroundModifier = when (theme) {
        "dark" -> GlanceModifier.background(ColorProvider(R.color.widget_dark_bg))
        "light" -> GlanceModifier.background(ColorProvider(R.color.white))
        "trend" -> {
            val colorRes = when {
                isZeroChange -> R.color.widget_trend_neutral
                isNegative -> R.color.widget_trend_negative
                else -> R.color.widget_trend_positive
            }
            GlanceModifier.background(ColorProvider(colorRes))
        }
        "app_color" -> {
            GlanceModifier.background(ColorProvider(appColorSeed.seedColor))
        }
        else -> GlanceModifier
    }

    val contentColor = if (theme == "light") ColorProvider(R.color.black) else ColorProvider(R.color.white)
    val secondaryColor = if (theme == "light") ColorProvider(R.color.widget_secondary_light) else ColorProvider(R.color.widget_secondary_dark)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .then(backgroundModifier)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
        contentAlignment = Alignment.Center
    ) {
        if (item == null) {
            Text(text = "...", style = TextStyle(color = GlanceTheme.colors.onSurface))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.Symbol.uppercase(),
                    style = TextStyle(
                        color = if (theme == "glassy") GlanceTheme.colors.onSurface else contentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = formatPrice(item.CurrentPrice, digitType, item.Symbol),
                    style = TextStyle(
                        color = if (theme == "glassy") GlanceTheme.colors.onSurfaceVariant else secondaryColor,
                        fontSize = 10.sp
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

    val isNegative = (item?.ChangePercentage ?: 0.0) < 0
    val isZeroChange = Math.abs(item?.ChangePercentage ?: 0.0) < 0.001

    val backgroundModifier = when (theme) {
        "dark" -> GlanceModifier.background(ColorProvider(R.color.widget_dark_bg))
        "light" -> GlanceModifier.background(ColorProvider(R.color.white))
        "trend" -> {
            val colorRes = when {
                isZeroChange -> R.color.widget_trend_neutral
                isNegative -> R.color.widget_trend_negative
                else -> R.color.widget_trend_positive
            }
            GlanceModifier.background(ColorProvider(colorRes))
        }
        "app_color" -> {
            GlanceModifier.background(ColorProvider(appColorSeed.seedColor))
        }
        else -> GlanceModifier
    }

    val contentColor = if (theme == "light") ColorProvider(R.color.black) else ColorProvider(R.color.white)
    val secondaryColor = if (theme == "light") ColorProvider(R.color.widget_secondary_light) else ColorProvider(R.color.widget_secondary_dark)

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
                modifier = GlanceModifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = getWidgetLocalizedTitle(item.Symbol, item.Title, isEnglish),
                        style = TextStyle(
                            color = if (theme == "glassy") GlanceTheme.colors.onSurface else contentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = formatPrice(item.CurrentPrice, digitType, item.Symbol),
                        style = TextStyle(
                            color = if (theme == "glassy") GlanceTheme.colors.onSurfaceVariant else secondaryColor,
                            fontSize = 12.sp
                        )
                    )
                }
                
                Spacer(modifier = GlanceModifier.width(8.dp))
                
                val percentColor = when {
                    theme == "trend" -> contentColor
                    theme == "glassy" -> if (isNegative) GlanceTheme.colors.error else GlanceTheme.colors.primary
                    isNegative -> ColorProvider(R.color.widget_negative_red)
                    else -> ColorProvider(R.color.widget_positive_green)
                }
                
                Text(
                    text = formatPercent(item.ChangePercentage, digitType),
                    style = TextStyle(
                        color = percentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
