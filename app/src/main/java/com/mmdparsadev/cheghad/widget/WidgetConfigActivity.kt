package com.mmdparsadev.cheghad.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.database.AppDatabase
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.getLocalizedTitle
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmdparsadev.cheghad.ui.viewmodel.SettingsViewModel
import com.mmdparsadev.cheghad.data.repository.SettingsRepository
import com.mmdparsadev.cheghad.ui.theme.AppThemeColor
import com.mmdparsadev.cheghad.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(SettingsRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an invalid widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            val userSettings by settingsViewModel.settings.collectAsStateWithLifecycle()
            if (!userSettings.isLoaded) return@setContent

            val appThemeMode = userSettings.themeMode
            val colorSeedName = userSettings.colorSeed
            val selectedAppColor = AppThemeColor.entries.find { it.name == colorSeedName } ?: AppThemeColor.DEFAULT

            MyApplicationTheme(
                themeMode = appThemeMode,
                seedColor = if (selectedAppColor == AppThemeColor.DEFAULT) null else selectedAppColor.seedColor,
                animate = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(appWidgetId, colorSeedName) {
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(appWidgetId: Int, appColorSeed: String, onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    
    var currencies by remember { mutableStateOf<List<CurrencyItem>>(emptyList()) }
    var selectedCurrencyId by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableStateOf("glassy") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        currencies = db.currencyDao().getAllCurrencies()
        
        try {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            val state = androidx.glance.appwidget.state.getAppWidgetState<androidx.datastore.preferences.core.Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
            selectedCurrencyId = state[stringPreferencesKey("selected_currency_id")] ?: ""
            selectedTheme = state[stringPreferencesKey("widget_theme")] ?: "glassy"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (selectedCurrencyId.isEmpty() && currencies.isNotEmpty()) {
            selectedCurrencyId = currencies.first().id
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            TopAppBar(
                title = { Text(stringResource(R.string.widget_config_title)) }
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.widget_config_select_currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(currencies) { item ->
                    CurrencySelectionItem(
                        item = item,
                        isSelected = item.id == selectedCurrencyId,
                        onClick = { selectedCurrencyId = item.id }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.widget_config_select_theme),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    ThemeSelectionSection(
                        selectedTheme = selectedTheme,
                        appColorSeed = appColorSeed,
                        onThemeSelected = { selectedTheme = it }
                    )
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                                prefs.toMutablePreferences().apply {
                                    set(stringPreferencesKey("selected_currency_id"), selectedCurrencyId)
                                    set(stringPreferencesKey("widget_theme"), selectedTheme)
                                }
                            }
                            
                            // Detect which widget to update
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val providerName = appWidgetManager.getAppWidgetInfo(appWidgetId)?.provider?.className
                            
                            when (providerName) {
                                MinimalBadgeWidgetReceiver::class.java.name -> MinimalBadgeWidget().update(context, glanceId)
                                PriceDeltaWidgetReceiver::class.java.name -> PriceDeltaWidget().update(context, glanceId)
                                else -> CurrencyWidget().update(context, glanceId)
                            }

                            Toast.makeText(context, R.string.widget_config_success, Toast.LENGTH_SHORT).show()
                            onFinished()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Error saving settings", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.widget_config_save))
            }
        }
    }
}

@Composable
fun CurrencySelectionItem(item: CurrencyItem, isSelected: Boolean, onClick: () -> Unit) {
    val motionScheme = MaterialTheme.motionScheme
    val title = getLocalizedTitle(item.symbol, item.title)
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "backgroundColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "borderColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(24.dp), // Capsule-like rounded corners
        color = backgroundColor,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.symbol.take(1).uppercase(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.symbol, 
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ThemeSelectionSection(selectedTheme: String, appColorSeed: String, onThemeSelected: (String) -> Unit) {
    val colorOption = AppThemeColor.entries.find { it.name == appColorSeed } ?: AppThemeColor.DEFAULT
    
    val themes = listOf(
        "glassy" to stringResource(R.string.widget_theme_glassy),
        "dark" to stringResource(R.string.widget_theme_dark),
        "light" to stringResource(R.string.widget_theme_light),
        "trend" to stringResource(R.string.widget_theme_trend),
        "app_color" to "بر اساس رنگ برنامه"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        themes.forEach { (id, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedTheme == id, onClick = { onThemeSelected(id) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label)
                
                if (id == "app_color") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colorOption.seedColor, CircleShape)
                    )
                }
            }
        }
    }
}
