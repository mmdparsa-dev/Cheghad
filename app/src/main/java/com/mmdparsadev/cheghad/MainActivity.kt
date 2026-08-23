package com.mmdparsadev.cheghad

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.UiModeManager
import com.mmdparsadev.cheghad.ui.ExpressiveConnectedButtonGroup
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.mmdparsadev.cheghad.utils.HapticUtils
import com.mmdparsadev.cheghad.utils.HapticType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mmdparsadev.cheghad.data.api.ApiClient
import com.mmdparsadev.cheghad.data.repository.CurrencyRepository
import com.mmdparsadev.cheghad.data.models.*
import com.mmdparsadev.cheghad.ui.theme.*
import androidx.compose.material3.MaterialTheme
import com.mmdparsadev.cheghad.ui.viewmodel.CurrencyUiState
import com.mmdparsadev.cheghad.ui.viewmodel.CurrencyViewModel
import com.mmdparsadev.cheghad.worker.CurrencySyncWorker
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.concurrent.TimeUnit

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.core.app.NotificationCompat
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.focusable

import androidx.core.content.ContextCompat
import com.mmdparsadev.cheghad.data.repository.NewsRepository
import com.mmdparsadev.cheghad.data.update.GitHubRelease
import com.mmdparsadev.cheghad.data.update.UpdateManager
import com.mmdparsadev.cheghad.data.update.UpdateWorker
import com.mmdparsadev.cheghad.ui.ConnectivityStatusBanner
import com.mmdparsadev.cheghad.ui.ExpressiveLoadingIndicator
import com.mmdparsadev.cheghad.ui.ExpressivePullToRefreshBox
import com.mmdparsadev.cheghad.ui.UpdateDialog
import com.mmdparsadev.cheghad.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val LocalAdaptiveDpScale = staticCompositionLocalOf { 1.0f }
val LocalAdaptiveSpScale = staticCompositionLocalOf { 1.0f }

@Composable
fun isTvDevice(): Boolean {
    val context = LocalContext.current
    val uiModeManager = remember(context) {
        context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    }
    return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}

enum class TimeRange(val stringRes: Int, val id: String) {
    HOUR(R.string.range_hour, "HOUR"),
    DAY(R.string.range_day, "DAY"),
    WEEK(R.string.range_week, "WEEK"),
    MONTH(R.string.range_month, "MONTH"),
    YEAR(R.string.range_year, "YEAR")
}

enum class CheghadDestination(
    val id: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    Market("home", R.string.nav_market, Icons.Default.Dashboard),
    Calculator("calculator", R.string.nav_calculator, Icons.Default.Calculate),
    News("news", R.string.nav_news, Icons.Default.Newspaper),
    Alarms("portfolio", R.string.nav_portfolio, Icons.Default.Notifications),
    Settings("settings", R.string.nav_settings, Icons.Default.Settings)
}

class MainActivity : AppCompatActivity() {

    private val viewModel: CurrencyViewModel by viewModels {
        val database = com.mmdparsadev.cheghad.data.database.AppDatabase.getDatabase(applicationContext)
        val alarmRepository = com.mmdparsadev.cheghad.data.repository.AlarmRepository(database.alarmDao())
        CurrencyViewModel.provideFactory(
            CurrencyRepository(ApiClient.CheghadApiService, ApiClient.KifpoolApiService, database.currencyDao()),
            alarmRepository,
            applicationContext
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.provideFactory(
            com.mmdparsadev.cheghad.data.repository.SettingsRepository(applicationContext)
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupBackgroundSync()

        splashScreen.setKeepOnScreenCondition {
            !settingsViewModel.settings.value.isLoaded
        }

        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        setContent {
            val userSettings by settingsViewModel.settings.collectAsStateWithLifecycle()
            if (!userSettings.isLoaded) return@setContent

            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp

            val isTv = isTvDevice()

            val dpScale = remember(screenWidthDp, isTv) {
                if (isTv) {
                    0.75f
                } else {
                    when {
                        screenWidthDp >= 1200 -> 1.50f
                        screenWidthDp >= 840 -> 1.35f
                        screenWidthDp >= 600 -> 1.12f
                        screenWidthDp <= 320 -> 0.80f
                        screenWidthDp <= 360 -> 0.88f
                        else -> 1.0f
                    }
                }
            }

            val spScale = remember(screenWidthDp, isTv) {
                if (isTv) {
                    0.80f
                } else {
                    when {
                        screenWidthDp >= 1200 -> 1.50f
                        screenWidthDp >= 840 -> 1.30f
                        screenWidthDp >= 600 -> 1.10f
                        screenWidthDp <= 320 -> 0.78f
                        screenWidthDp <= 360 -> 0.85f
                        else -> 0.95f
                    }
                }
            }

            CompositionLocalProvider(
                LocalAdaptiveDpScale provides dpScale,
                LocalAdaptiveSpScale provides spScale
            ) {
                val appThemeMode = userSettings.themeMode
                val calendarType = userSettings.calendarType
                val colorSchemeMode = userSettings.colorSchemeMode
                val digitType = userSettings.digitType
                val colorSeedName = userSettings.colorSeed
                val selectedAppColor = AppThemeColor.entries.find { it.name == colorSeedName } ?: AppThemeColor.DEFAULT

                var isFirstLaunch by remember { mutableStateOf(sharedPrefs.getBoolean("first_launch", true)) }
                var currentScreen by remember { mutableStateOf("home") }

                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current
                var isEditingHome by remember { mutableStateOf(false) }
                var isEditingCustomSort by remember { mutableStateOf(false) }
                var marketCustomOrder by remember {
                    mutableStateOf(
                        sharedPrefs.getString("market_custom_order", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                    )
                }
                var bottomListSortOrder by remember { mutableStateOf("default") } // "default", "profitable", "loss-making", "custom"

                var selectedItemForDetail by remember { mutableStateOf<CurrencyItem?>(null) }
                var selectedAlarmForEdit by remember { mutableStateOf<AlarmEntity?>(null) }

                LaunchedEffect(Unit) {
                    viewModel.triggeredAlarmFlow.collect { (alarm, currentPrice) ->
                        val directionText = if (alarm.isAbove) context.getString(R.string.alarm_direction_above) else context.getString(R.string.alarm_direction_below)
                        val message = "قیمت ${alarm.title} (${alarm.symbol}) به $directionText ${alarm.targetPrice} تومان رسید (قیمت فعلی: $currentPrice)"

                        try {
                            val channelId = "price_alerts_channel"
                            val channelName = "Price Alerts"

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(
                                    channelId,
                                    channelName,
                                    NotificationManager.IMPORTANCE_HIGH
                                ).apply {
                                    description = "Channel for asset price alerts"
                                }
                                val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.createNotificationChannel(channel)
                            }

                            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .setContentTitle(context.getString(R.string.alarm_triggered_title))
                                .setContentText(message)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)

                            notificationManager.notify(alarm.id.toInt(), notificationBuilder.build())
                        } catch (e: Exception) {
                        }

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
                var homeItemSymbols by remember {
                    mutableStateOf(
                        (sharedPrefs.getString("home_items", "USD,EUR,GOLD,BTC,ETH") ?: "USD,EUR,GOLD,BTC,ETH").split(",")
                    )
                }

                var timeRangeOrder by remember {
                    mutableStateOf(
                        (sharedPrefs.getString("time_range_order", "DAY,WEEK,MONTH,YEAR") ?: "DAY,WEEK,MONTH,YEAR")
                            .split(",")
                            .mapNotNull { id -> TimeRange.entries.find { it.id == id } }
                            .let { if (it.isEmpty()) listOf(TimeRange.DAY, TimeRange.WEEK, TimeRange.MONTH, TimeRange.YEAR) else it }
                    )
                }

                var disabledNewsCategories by remember {
                    mutableStateOf(sharedPrefs.getStringSet("disabled_news_categories", emptySet<String>()) ?: emptySet<String>())
                }

                var disabledNewsAgencies by remember {
                    mutableStateOf(sharedPrefs.getStringSet("disabled_news_agencies", emptySet<String>()) ?: emptySet<String>())
                }

                var showAgenciesSheet by remember { mutableStateOf(false) }

                var availableUpdateRelease by remember { mutableStateOf<GitHubRelease?>(null) }
                var isCheckingUpdatesManually by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    delay(1500) // Delay startup tasks to improve initial UI responsiveness

                    // Request notification permission for Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // Schedule background WorkManager periodic check
                    UpdateWorker.schedulePeriodicCheck(context)

                    val currentVersion = BuildConfig.VERSION_NAME
                    UpdateManager.checkForUpdate(currentVersion, userSettings.downloadBetaVersions).onSuccess { release ->
                        if (release != null) {
                            availableUpdateRelease = release
                            UpdateWorker.sendUpdateNotification(context, release)
                        }
                    }
                }

                LaunchedEffect(currentScreen, userSettings.newsEnabled, disabledNewsAgencies) {
                    if (currentScreen == "news" && userSettings.newsEnabled) {
                        viewModel.fetchNews(disabledNewsAgencies, userSettings.newsEnabled)
                    }
                }

                LaunchedEffect(uiState.showSuccessMessage) {
                    if (uiState.showSuccessMessage) {
                        Toast.makeText(context, context.getString(R.string.success_updated), Toast.LENGTH_SHORT).show()
                        viewModel.clearSuccessMessage()
                    }
                }

                LaunchedEffect(uiState.errorMessageResId) {
                    uiState.errorMessageResId?.let { errorId ->
                        Toast.makeText(context, context.getString(errorId), Toast.LENGTH_LONG).show()
                        viewModel.clearErrorMessage()
                    }
                }

                val homeScrollState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

                MyApplicationTheme(
                    themeMode = appThemeMode,
                    seedColor = if (selectedAppColor == AppThemeColor.DEFAULT) null else selectedAppColor.seedColor,
                    animate = false
                ) {
                    val motionScheme = MaterialTheme.motionScheme
                    if (isFirstLaunch) {
                        WelcomeScreen(
                            onComplete = { langCode, theme ->
                                val newDigitType = if (langCode == "en") "en" else "fa"
                                settingsViewModel.setDigitType(newDigitType)
                                settingsViewModel.setThemeMode(theme)
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
                                sharedPrefs.edit().putBoolean("first_launch", false).apply()
                                isFirstLaunch = false
                            }
                        )
                    } else {
                        val isTv = isTvDevice()

                        @Composable
                        fun CheghadAppContent(innerPadding: PaddingValues) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    val screenOrder = listOf("home", "calculator", "news", "portfolio", "settings")
                                    val initialIndex = screenOrder.indexOf(initialState)
                                    val targetIndex = screenOrder.indexOf(targetState)
                                    val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

                                    if (targetIndex > initialIndex) {
                                        (slideInHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> (width * 0.1f).toInt() } + fadeIn(animationSpec = tween(300))).togetherWith(
                                            slideOutHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> -(width * 0.1f).toInt() } + fadeOut(animationSpec = tween(150))
                                        )
                                    } else {
                                        (slideInHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> -(width * 0.1f).toInt() } + fadeIn(animationSpec = tween(300))).togetherWith(
                                            slideOutHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> (width * 0.1f).toInt() } + fadeOut(animationSpec = tween(150))
                                        )
                                    }
                                },
                                label = "ScreenTransition"
                            ) { screen ->
                                if (screen == "home") {
                                    var draggedItemId by remember { mutableStateOf<String?>(null) }
                                    var initialIndex by remember { mutableStateOf<Int?>(null) }
                                    var totalDragY by remember { mutableStateOf(0f) }

                                    var itemHeightPx by remember { mutableStateOf(0f) }
                                    val density = LocalDensity.current

                                    // Auto-scroll state
                                    var pointerYInViewport by remember { mutableStateOf(0f) }
                                    var listViewportHeight by remember { mutableStateOf(0f) }
                                    var listTopOnScreen by remember { mutableStateOf(0f) }

                                    val selectedCat = uiState.selectedCategory
                                    val filteredItems = uiState.items.filter { item ->
                                        when (selectedCat) {
                                            "currency" -> item.category == CurrencyType.Currency
                                            "gold_and_coin" -> item.category == CurrencyType.GoldAndCoin
                                            "crypto" -> item.category == CurrencyType.Crypto
                                            else -> true
                                        }
                                    }

                                    val sortedItems = remember(bottomListSortOrder, marketCustomOrder, filteredItems) {
                                        when (bottomListSortOrder) {
                                            "profitable" -> filteredItems.sortedByDescending { it.changePercentage }
                                            "loss-making" -> filteredItems.sortedBy { it.changePercentage }
                                            "custom" -> {
                                                val orderMap = marketCustomOrder.withIndex().associate { it.value to it.index }
                                                filteredItems.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
                                            }
                                            else -> filteredItems
                                        }
                                    }

                                    // Critical fix: Ensure drag logic sees the LATEST state
                                    val currentSortedItemsState = rememberUpdatedState(sortedItems)
                                    val currentMarketOrderState = rememberUpdatedState(marketCustomOrder)

                                    // Auto-scroll logic when dragging near edges
                                    LaunchedEffect(draggedItemId, pointerYInViewport, listViewportHeight) {
                                        if (draggedItemId != null && listViewportHeight > 0) {
                                            val threshold = with(density) { 80.dp.toPx() }
                                            val maxScrollSpeed = 15f

                                            while (true) {
                                                var scrollAmount = 0f
                                                if (pointerYInViewport < threshold && pointerYInViewport > 0) {
                                                    // Scroll UP
                                                    val strength = (threshold - pointerYInViewport) / threshold
                                                    scrollAmount = -maxScrollSpeed * strength
                                                } else if (pointerYInViewport > listViewportHeight - threshold) {
                                                    // Scroll DOWN
                                                    val strength = (pointerYInViewport - (listViewportHeight - threshold)) / threshold
                                                    scrollAmount = maxScrollSpeed * strength
                                                }

                                                if (scrollAmount != 0f) {
                                                    homeScrollState.dispatchRawDelta(scrollAmount)
                                                    totalDragY += scrollAmount
                                                }
                                                yield()
                                                delay(10)
                                            }
                                        }
                                    }

                                    ExpressivePullToRefreshBox(
                                        isRefreshing = uiState.isLoading,
                                        onRefresh = { viewModel.refreshData() },
                                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                                    ) {
                                        LazyColumn(
                                            state = homeScrollState,
                                            modifier = Modifier.fillMaxSize().onGloballyPositioned {
                                                listViewportHeight = it.size.height.toFloat()
                                                listTopOnScreen = it.localToWindow(Offset.Zero).y
                                            }
                                        ) {
                                            item { Spacer(modifier = Modifier.height(adaptiveDp(16f))) }
                                            item {
                                                TopAppBar(
                                                    uiState = uiState,
                                                    isEditingHome = isEditingHome,
                                                    calendarType = calendarType,
                                                    digitType = digitType,
                                                    onRefresh = { viewModel.refreshData() },
                                                    onEditHome = { isEditingHome = !isEditingHome },
                                                    modifier = Modifier.padding(horizontal = adaptiveDp(16f))
                                                )
                                            }
                                            item { Spacer(modifier = Modifier.height(adaptiveDp(16f))) }
                                            item {
                                                BentoGrid(
                                                    items = uiState.items,
                                                    homeSymbols = homeItemSymbols,
                                                    isEditing = isEditingHome,
                                                    colorSchemeMode = colorSchemeMode,
                                                    digitType = digitType,
                                                    onSymbolsChanged = { newSymbols ->
                                                        homeItemSymbols = newSymbols
                                                        sharedPrefs.edit().putString("home_items", newSymbols.joinToString(",")).apply()
                                                    },
                                                    onClickItem = { item ->
                                                        selectedItemForDetail = item
                                                    },
                                                    modifier = Modifier.padding(horizontal = adaptiveDp(16f))
                                                )
                                            }
                                            item { Spacer(modifier = Modifier.height(adaptiveDp(20f))) }
                                            item {
                                                CategoryChips(
                                                    selectedCategory = uiState.selectedCategory,
                                                    onCategorySelected = { cat ->
                                                        viewModel.setCategory(cat)
                                                        coroutineScope.launch {
                                                            homeScrollState.animateScrollToItem(6)
                                                        }
                                                    },
                                                    modifier = Modifier.padding(horizontal = adaptiveDp(16f))
                                                )
                                            }
                                            item { Spacer(modifier = Modifier.height(adaptiveDp(16f))) }

                                            val categoryTitleRes = when(selectedCat) {
                                                "currency" -> R.string.category_currency
                                                "gold_and_coin" -> R.string.category_gold_and_coin
                                                "crypto" -> R.string.category_crypto
                                                else -> R.string.all_markets
                                            }

                                            item(key = "cat_header_$selectedCat") {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = adaptiveDp(16f)),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val title = stringResource(categoryTitleRes)
                                                    Text(
                                                        text = title,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        fontFamily = getFontFamilyForText(title)
                                                    )

                                                    var isSortMenuExpanded by remember { mutableStateOf(false) }
                                                    val currentSortStringRes = when (bottomListSortOrder) {
                                                        "profitable" -> R.string.sort_profitable
                                                        "loss-making" -> R.string.sort_loss_making
                                                        "custom" -> R.string.sort_custom
                                                        else -> R.string.sort_default
                                                    }
                                                    val currentSortText = stringResource(currentSortStringRes)
                                                    val rotationAngle by animateFloatAsState(
                                                        targetValue = if (isSortMenuExpanded) 180f else 0f,
                                                        animationSpec = motionScheme.defaultSpatialSpec(),
                                                        label = "SortArrowRotation"
                                                    )

                                                    Box {
                                                        val isSortActive = bottomListSortOrder != "default"
                                                        Surface(
                                                            onClick = { isSortMenuExpanded = true },
                                                            shape = CircleShape,
                                                            color = if (isSortActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            contentColor = if (isSortActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                                            tonalElevation = 2.dp,
                                                            border = BorderStroke(1.dp, if (isSortActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                                                    contentDescription = "Sort",
                                                                    tint = if (isSortActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                                Text(
                                                                    text = currentSortText,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isSortActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                                    fontFamily = getFontFamilyForText(currentSortText)
                                                                )
                                                                if (bottomListSortOrder == "custom") {
                                                                    Surface(
                                                                        onClick = { isEditingCustomSort = !isEditingCustomSort },
                                                                        shape = CircleShape,
                                                                        color = if (isEditingCustomSort) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                                                                        modifier = Modifier.size(24.dp)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Edit,
                                                                            contentDescription = "Edit Custom Sort",
                                                                            tint = if (isEditingCustomSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                            modifier = Modifier.padding(5.dp).size(14.dp)
                                                                        )
                                                                    }
                                                                }
                                                                Icon(
                                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                                    contentDescription = null,
                                                                    tint = if (isSortActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    modifier = Modifier
                                                                        .size(18.dp)
                                                                        .graphicsLayer { rotationZ = rotationAngle }
                                                                )
                                                            }
                                                        }

                                                        MaterialTheme(
                                                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(20.dp))
                                                        ) {
                                                            DropdownMenu(
                                                                expanded = isSortMenuExpanded,
                                                                onDismissRequest = { isSortMenuExpanded = false },
                                                                modifier = Modifier
                                                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                                                    .width(180.dp)
                                                            ) {
                                                                listOf(
                                                                    "default" to R.string.sort_default,
                                                                    "profitable" to R.string.sort_profitable,
                                                                    "loss-making" to R.string.sort_loss_making,
                                                                    "custom" to R.string.sort_custom
                                                                ).forEach { (mode, stringRes) ->
                                                                    val isSelected = bottomListSortOrder == mode
                                                                    val itemText = stringResource(stringRes)
                                                                    DropdownMenuItem(
                                                                        text = {
                                                                            Text(
                                                                                text = itemText,
                                                                                fontSize = 13.sp,
                                                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                                                fontFamily = getFontFamilyForText(itemText)
                                                                            )
                                                                        },
                                                                        onClick = {
                                                                            bottomListSortOrder = mode
                                                                            isSortMenuExpanded = false
                                                                        },
                                                                        leadingIcon = {
                                                                            if (isSelected) {
                                                                                Icon(
                                                                                    imageVector = Icons.Default.Check,
                                                                                    contentDescription = "Selected",
                                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                                    modifier = Modifier.size(18.dp)
                                                                                )
                                                                            } else {
                                                                                Spacer(modifier = Modifier.size(18.dp))
                                                                            }
                                                                        },
                                                                        modifier = Modifier
                                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                            .clip(RoundedCornerShape(12.dp))
                                                                            .then(
                                                                                if (isSelected) {
                                                                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                                                                                } else Modifier
                                                                            )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            item { Spacer(modifier = Modifier.height(12.dp)) }

                                            itemsIndexed(sortedItems, key = { _, item -> item.id }) { index, item ->
                                                val isDragging = draggedItemId == item.id
                                                var itemTopInWindow by remember { mutableStateOf(0f) }

                                                // Compensation offset to keep item under finger after swap
                                                val dragOffset = if (isDragging && itemHeightPx > 0 && initialIndex != null) {
                                                    totalDragY - (index - initialIndex!!) * itemHeightPx
                                                } else 0f

                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = adaptiveDp(16f))
                                                        .zIndex(if (isDragging) 100f else 1f)
                                                        .graphicsLayer {
                                                            if (isDragging) {
                                                                translationY = dragOffset
                                                                scaleX = 1.06f
                                                                scaleY = 1.06f
                                                                shadowElevation = 16.dp.toPx()
                                                            } else {
                                                                translationY = 0f
                                                                scaleX = 1f
                                                                scaleY = 1f
                                                                shadowElevation = 0f
                                                            }
                                                        }
                                                        .animateItem()
                                                ) {
                                                    AssetListItem(
                                                        item = item,
                                                        colorSchemeMode = colorSchemeMode,
                                                        digitType = digitType,
                                                        onClick = { selectedItemForDetail = item },
                                                        onLongClick = { viewModel.hideCurrencyForItem(item.id) },
                                                        isReordering = isEditingCustomSort,
                                                        isDragging = isDragging,
                                                        modifier = Modifier.pointerInput(item.id, isEditingCustomSort, bottomListSortOrder) {
                                                            if (isEditingCustomSort && bottomListSortOrder == "custom") {
                                                                detectDragGesturesAfterLongPress(
                                                                    onDragStart = { offset ->
                                                                        HapticUtils.vibrate(context, HapticType.MEDIUM)
                                                                        draggedItemId = item.id
                                                                        initialIndex = currentSortedItemsState.value.indexOfFirst { it.id == item.id }
                                                                        totalDragY = 0f

                                                                        pointerYInViewport = offset.y + (itemTopInWindow - listTopOnScreen)
                                                                    },
                                                                    onDrag = { change, dragAmount ->
                                                                        change.consume()
                                                                        totalDragY += dragAmount.y

                                                                        // Update pointer Y in viewport
                                                                        pointerYInViewport += dragAmount.y

                                                                        if (itemHeightPx > 0) {
                                                                            val latestList = currentSortedItemsState.value
                                                                            val nowIdx = latestList.indexOfFirst { it.id == item.id }
                                                                            val startIdx = initialIndex ?: nowIdx

                                                                            val targetIdx = (startIdx + (totalDragY / itemHeightPx).roundToInt()).coerceIn(0, latestList.lastIndex)

                                                                            if (targetIdx != nowIdx) {
                                                                                HapticUtils.vibrate(context, HapticType.LIGHT)
                                                                                val newList = currentMarketOrderState.value.toMutableList()
                                                                                filteredItems.forEach { if (it.id !in newList) newList.add(it.id) }

                                                                                val id1 = item.id
                                                                                val id2 = latestList[targetIdx].id
                                                                                val idx1 = newList.indexOf(id1)
                                                                                val idx2 = newList.indexOf(id2)

                                                                                if (idx1 != -1 && idx2 != -1) {
                                                                                    val tmp = newList[idx1]
                                                                                    newList[idx1] = newList[idx2]
                                                                                    newList[idx2] = tmp
                                                                                    marketCustomOrder = newList
                                                                                }
                                                                            }
                                                                        }
                                                                    },
                                                                    onDragEnd = {
                                                                        sharedPrefs.edit().putString("market_custom_order", marketCustomOrder.joinToString(",")).apply()
                                                                        draggedItemId = null
                                                                        initialIndex = null
                                                                        totalDragY = 0f
                                                                        pointerYInViewport = 0f
                                                                        HapticUtils.vibrate(context, HapticType.SUCCESS)
                                                                    },
                                                                    onDragCancel = {
                                                                        draggedItemId = null
                                                                        initialIndex = null
                                                                        totalDragY = 0f
                                                                        pointerYInViewport = 0f
                                                                    }
                                                                )
                                                            }
                                                        }
                                                            .onGloballyPositioned { coords ->
                                                                itemTopInWindow = coords.localToWindow(Offset.Zero).y
                                                                if (itemHeightPx == 0f || !isDragging) {
                                                                    itemHeightPx = coords.size.height.toFloat() + with(density) { 8.dp.toPx() }
                                                                }
                                                            }
                                                    )
                                                }
                                                if (index < sortedItems.size - 1) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                }
                                            }
                                            item { Spacer(modifier = Modifier.height(adaptiveDp(16f))) }
                                        }
                                    }
                                } else if (screen == "news") {
                                    com.mmdparsadev.cheghad.ui.NewsScreen(
                                        innerPadding = innerPadding,
                                        digitType = digitType,
                                        newsArticles = uiState.newsArticles,
                                        isRefreshing = uiState.isNewsLoading,
                                        onRefresh = { viewModel.fetchNews(disabledNewsAgencies, userSettings.newsEnabled) },
                                        disabledCategories = disabledNewsCategories,
                                        disabledAgencies = disabledNewsAgencies,
                                        newsEnabled = userSettings.newsEnabled,
                                        onOpenAgenciesSettings = {
                                            settingsViewModel.setNewsEnabled(true)
                                            showAgenciesSheet = true
                                            currentScreen = "settings"
                                        }
                                    )
                                } else if (screen == "calculator") {
                                    CurrencyCalculatorScreen(
                                        items = uiState.items,
                                        digitType = digitType,
                                        innerPadding = innerPadding
                                    )
                                } else if (screen == "settings") {
                                    SettingsScreen(
                                        innerPadding = innerPadding,
                                        onLanguageSelected = { langCode ->
                                            val newDigitType = if (langCode == "en") "en" else "fa"
                                            settingsViewModel.setDigitType(newDigitType)
                                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
                                        },
                                        appThemeMode = appThemeMode,
                                        onThemeSelected = { settingsViewModel.setThemeMode(it) },
                                        calendarType = calendarType,
                                        onCalendarSelected = { settingsViewModel.setCalendarType(it) },
                                        colorSchemeMode = colorSchemeMode,
                                        onColorSchemeSelected = { settingsViewModel.setColorSchemeMode(it) },
                                        colorSeedName = colorSeedName,
                                        onColorSeedSelected = { settingsViewModel.setColorSeed(it) },
                                        digitType = digitType,
                                        onDigitTypeSelected = { settingsViewModel.setDigitType(it) },
                                        timeRangeOrder = timeRangeOrder,
                                        onTimeRangeOrderChanged = { newOrder ->
                                            timeRangeOrder = newOrder
                                            sharedPrefs.edit().putString("time_range_order", newOrder.joinToString(",") { it.id }).apply()
                                        },
                                        disabledNewsCategories = disabledNewsCategories,
                                        onDisabledNewsCategoriesChanged = { newSet ->
                                            disabledNewsCategories = newSet
                                            sharedPrefs.edit().putStringSet("disabled_news_categories", newSet).apply()
                                        },
                                        disabledNewsAgencies = disabledNewsAgencies,
                                        onDisabledNewsAgenciesChanged = { newSet ->
                                            disabledNewsAgencies = newSet
                                            sharedPrefs.edit().putStringSet("disabled_news_agencies", newSet).apply()
                                        },
                                        newsEnabled = userSettings.newsEnabled,
                                        onNewsEnabledChanged = { settingsViewModel.setNewsEnabled(it) },
                                        showAgenciesSheet = showAgenciesSheet,
                                        onShowAgenciesSheetChanged = { showAgenciesSheet = it },
                                        isCheckingUpdates = isCheckingUpdatesManually,
                                        onCheckForUpdates = {
                                            isCheckingUpdatesManually = true
                                            coroutineScope.launch {
                                                val currentVersion = BuildConfig.VERSION_NAME
                                                val result = UpdateManager.checkForUpdate(currentVersion, userSettings.downloadBetaVersions)
                                                isCheckingUpdatesManually = false
                                                result.fold(
                                                    onSuccess = { release ->
                                                        if (release != null) {
                                                            availableUpdateRelease = release
                                                            UpdateWorker.sendUpdateNotification(context, release)
                                                        } else {
                                                            Toast.makeText(context, "شما از آخرین نسخه برنامه ($currentVersion) استفاده می‌کنید.", Toast.LENGTH_LONG).show()
                                                        }
                                                    },
                                                    onFailure = {
                                                        Toast.makeText(context, "خطا در برقراری ارتباط با گیت‌هاب", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        },
                                        downloadBetaVersions = userSettings.downloadBetaVersions,
                                        onDownloadBetaVersionsChanged = { settingsViewModel.setDownloadBetaVersions(it) },
                                        lockscreenWidgetCurrencyId = userSettings.lockscreenWidgetCurrencyId,
                                        onLockscreenWidgetCurrencySelected = { settingsViewModel.setLockscreenWidgetCurrencyId(it) },
                                        lockscreenWidgetTheme = userSettings.lockscreenWidgetTheme,
                                        onLockscreenWidgetThemeSelected = { settingsViewModel.setLockscreenWidgetTheme(it) },
                                        allCurrencies = uiState.items
                                    )
                                } else {
                                    // Alarms screen
                                    AlarmsScreen(
                                        alarms = uiState.alarms,
                                        innerPadding = innerPadding,
                                        colorSchemeMode = colorSchemeMode,
                                        digitType = digitType,
                                        onDeleteAlarm = { alarm ->
                                            viewModel.deleteAlarm(alarm)
                                        },
                                        onEditAlarm = { alarm ->
                                            selectedAlarmForEdit = alarm
                                        }
                                    )
                                }
                            }
                        }

                        if (isTv) {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = MaterialTheme.colorScheme.background,
                                topBar = {
                                    Column {
                                        ConnectivityStatusBanner(uiState = uiState)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                BottomNavigationBar(currentScreen = currentScreen, onScreenSelected = { currentScreen = it })
                                            }
                                            // دکمه به‌روزرسانی سریع اکسپرسیو مخصوص اندروید تی‌وی
                                            Surface(
                                                onClick = { viewModel.refreshData() },
                                                shape = RoundedCornerShape(24.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                                tonalElevation = 4.dp,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .height(44.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    if (uiState.isLoading) {
                                                        ExpressiveLoadingIndicator(
                                                            modifier = Modifier.size(20.dp),
                                                            color = MaterialTheme.colorScheme.primary,
                                                            isRefreshing = true
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.Refresh,
                                                            contentDescription = stringResource(R.string.button_refresh),
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = stringResource(R.string.button_refresh),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontFamily = getFontFamilyForText(stringResource(R.string.button_refresh))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                CheghadAppContent(innerPadding)
                            }
                        } else {
                            NavigationSuiteScaffold(
                                navigationSuiteItems = {
                                    CheghadDestination.entries.forEach { destination ->
                                        item(
                                            icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                                            label = {
                                                Text(
                                                    text = stringResource(destination.labelRes),
                                                    fontSize = 12.sp,
                                                    fontFamily = getFontFamilyForText(stringResource(destination.labelRes))
                                                )
                                            },
                                            selected = currentScreen == destination.id,
                                            onClick = {
                                                HapticUtils.vibrate(context, HapticType.LIGHT)
                                                currentScreen = destination.id
                                            }
                                        )
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.background,
                                navigationSuiteColors = NavigationSuiteDefaults.colors(
                                    navigationBarContainerColor = MaterialTheme.colorScheme.surface,
                                    navigationRailContainerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Scaffold(
                                    modifier = Modifier.fillMaxSize(),
                                    containerColor = MaterialTheme.colorScheme.background,
                                    topBar = {
                                        ConnectivityStatusBanner(uiState = uiState)
                                    }
                                ) { innerPadding ->
                                    CheghadAppContent(innerPadding)
                                }
                            }
                        }
                    }

                    // Detail view Dialog
                    selectedItemForDetail?.let { item ->
                        AssetDetailDialog(
                            item = item,
                            timeRangeOrder = timeRangeOrder,
                            historyPoints = uiState.historyPoints[item.symbol] ?: emptyList(),
                            isHistoryLoading = uiState.isHistoryLoading,
                            calendarType = calendarType,
                            colorSchemeMode = colorSchemeMode,
                            digitType = digitType,
                            onFetchHistory = { range -> viewModel.fetchHistory(item.symbol, range.id, item.currentPrice, item.changePercentage) },
                            onDismiss = { selectedItemForDetail = null },
                            onSaveAlarm = { price, isAbove ->
                                viewModel.addAlarm(
                                    symbol = item.symbol,
                                    title = item.title,
                                    targetPrice = price,
                                    isAbove = isAbove
                                )
                                selectedItemForDetail = null
                                Toast.makeText(context, context.getString(R.string.alarm_created_success), Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Edit alarm Dialog
                    selectedAlarmForEdit?.let { alarm ->
                        EditAlarmDialog(
                            alarm = alarm,
                            onDismiss = { selectedAlarmForEdit = null },
                            onSaveAlarm = { updatedAlarm ->
                                viewModel.updateAlarm(updatedAlarm)
                                selectedAlarmForEdit = null
                                Toast.makeText(context, context.getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Update Dialog
                    availableUpdateRelease?.let { release ->
                        UpdateDialog(
                            release = release,
                            digitType = digitType,
                            onDismiss = { availableUpdateRelease = null }
                        )
                    }
                }
            }
        }
    }

    private fun setupBackgroundSync() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<CurrencySyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "CurrencySync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (e: Exception) {
        }
    }
}

fun String.toLocalizedDigits(digitType: String): String {
    if (this.isEmpty() || digitType == "en") return this
    val builder = StringBuilder(this.length)
    for (i in 0 until this.length) {
        val ch = this[i]
        if (ch in '0'..'9') {
            val digit = ch - '0'
            val newChar = if (digitType == "fa") ('\u06F0' + digit) else ('\u0660' + digit)
            builder.append(newChar)
        } else if (digitType == "fa" && ch == '%') {
            builder.append('٪')
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

fun formatPrice(price: Double, digitType: String = "fa", symbol: String? = null): String {
    val isBtc = symbol?.equals("BTC", ignoreCase = true) == true
    val formatted = if (isBtc) {
        java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }.format(price)
    } else if (price >= 1.0) {
        java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(price)
    } else {
        String.format(java.util.Locale.US, "%.4f", price)
    }
    return formatted.toLocalizedDigits(digitType)
}

fun formatTargetPrice(price: Double): String {
    return if (price <= 0.0) "" else if (price % 1.0 == 0.0) {
        price.toLong().toString()
    } else {
        java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
            isGroupingUsed = false
        }.format(price)
    }
}

fun formatPercent(percent: Double, digitType: String = "fa"): String {
    val formattedValue = String.format(java.util.Locale.US, "%.2f", percent)
    if (formattedValue == "0.00" || formattedValue == "-0.00") {
        return "0.00%".toLocalizedDigits(digitType)
    }
    val sign = if (percent > 0) "+" else ""
    val formatted = "$sign$formattedValue%"
    return formatted.toLocalizedDigits(digitType)
}

fun formatTimeAgo(context: Context, timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    if (diff <= 0) return context.getString(R.string.time_just_now)
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 2 -> context.getString(R.string.time_just_now)
        minutes < 60 -> context.getString(R.string.time_minutes_ago, minutes.toInt())
        hours < 24 -> context.getString(R.string.time_hours_ago, hours.toInt())
        else -> context.getString(R.string.time_days_ago, days.toInt())
    }
}

fun getLocalizedTitle(symbol: String, rawTitle: String): String {
    val isEnglish = java.util.Locale.getDefault().language == "en"
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
    }
    return when {
        s == "USD" || s == "USDT" -> "دلار آمریکا"
        s == "EUR" -> "یورو"
        s == "GBP" -> "پوند انگلیس"
        s == "CAD" -> "دلار کانادا"
        s == "AUD" -> "دلار استرالیا"
        s == "AED" -> "درهم امارات"
        s == "TRY" -> "لیر ترکیه"
        s == "CHF" -> "فرانک سوئیس"
        s == "CNY" -> "یوان چین"
        s == "IQD" -> "دینار عراق"
        s == "SEK" -> "کرون سوئد"
        s == "SAR" -> "ریال عربستان"
        s == "QAR" -> "ریال قطر"
        s == "OMR" -> "ریال عمان"
        s == "RUB" -> "روبل روسیه"
        s == "BTC" -> "بیت‌کوین"
        s == "ETH" -> "اتریوم"
        s == "SOL" -> "سولانا"
        s == "BNB" -> "بایننس کوین"
        s == "XRP" -> "ریپل"
        s == "DOGE" -> "دوج‌کوین"
        s == "ADA" -> "کاردانو"
        s == "TRX" -> "ترون"
        s == "AVAX" -> "آوالانچ"
        s == "DOT" -> "پولکادات"
        s == "LINK" -> "چین‌لینک"
        s == "MATIC" || s == "POL" -> "پلی‌گون"
        s == "LTC" -> "لایت‌کوین"
        s == "SHIB" -> "شیبا اینو"
        s == "TON" -> "تون‌کوین"
        s == "NEAR" -> "نیر پروتکل"
        s == "PEPE" -> "پپه"
        s == "SUI" -> "سویی"
        s == "UNI" -> "یونی‌سواپ"
        s == "USOON" -> "توکن نفت (USOON)"
        s == "GOLD" || s == "PAXG" -> "سکه امامی"
        s == "XAU" -> "انس طلا"
        s == "BAHAR" -> "سکه بهار آزادی"
        s == "NIM" -> "نیم سکه"
        s == "RAB" -> "ربع سکه"
        s == "GERAMI" -> "سکه گرمی"
        s == "18AYAR" || s == "GOLD18K" -> "طلای ۱۸ عیار"
        s == "MESGHAL" -> "مثقال طلا"
        s == "BRENT" -> "نفت برنت"
        s == "BOURSE" -> "شاخص بورس"
        else -> when {
            rawTitle.contains("US Dollar", ignoreCase = true) || rawTitle.contains("Dollar", ignoreCase = true) -> "دلار آمریکا"
            rawTitle.contains("Euro", ignoreCase = true) -> "یورو"
            rawTitle.contains("Pound", ignoreCase = true) -> "پوند انگلیس"
            rawTitle.contains("Canadian", ignoreCase = true) -> "دلار کانادا"
            rawTitle.contains("Australian", ignoreCase = true) -> "دلار استرالیا"
            rawTitle.contains("Dirham", ignoreCase = true) -> "درهم امارات"
            rawTitle.contains("Lira", ignoreCase = true) -> "لیر ترکیه"
            rawTitle.contains("Franc", ignoreCase = true) -> "فرانک سوئیس"
            rawTitle.contains("Yuan", ignoreCase = true) -> "یوان چین"
            rawTitle.contains("Dinar", ignoreCase = true) -> "دینار عراق"
            rawTitle.contains("Krona", ignoreCase = true) -> "کرون سوئد"
            rawTitle.contains("Riyal", ignoreCase = true) -> "ریال عربستان"
            rawTitle.contains("Ruble", ignoreCase = true) -> "روبل روسیه"
            rawTitle.contains("Gold Coin", ignoreCase = true) || rawTitle.contains("Emami", ignoreCase = true) -> "سکه امامی"
            rawTitle.contains("Gold Ounce", ignoreCase = true) -> "انس طلا"
            rawTitle.contains("Gold Mesghal", ignoreCase = true) -> "مثقال طلا"
            rawTitle.contains("18k Gold", ignoreCase = true) -> "طلای ۱۸ عیار"
            rawTitle.contains("Brent Oil", ignoreCase = true) -> "نفت برنت"
            rawTitle.contains("Bourse", ignoreCase = true) -> "شاخص بورس"
            rawTitle.contains("Bitcoin", ignoreCase = true) -> "بیت‌کوین"
            rawTitle.contains("Ethereum", ignoreCase = true) -> "اتریوم"
            else -> rawTitle
        }
    }
}

@Composable
fun adaptiveSp(baseSp: Float): TextUnit {
    return (baseSp * LocalAdaptiveSpScale.current).sp
}

@Composable
fun adaptiveDp(baseDp: Float): Dp {
    return (baseDp * LocalAdaptiveDpScale.current).dp
}

@Composable
fun AutoResizingText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    var currentFontSize by remember(text) { mutableStateOf(fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        fontSize = currentFontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = false,
        lineHeight = lineHeight,
        onTextLayout = { layoutResult ->
            if (layoutResult.didOverflowWidth && currentFontSize.value > 10f) {
                currentFontSize = (currentFontSize.value * 0.92f).sp
            } else {
                readyToDraw = true
            }
        },
        style = LocalTextStyle.current.merge(
            TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    )
}

@Composable
fun Material3CircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = color.copy(alpha = 0.2f),
    strokeWidth: androidx.compose.ui.unit.Dp = 2.5.dp,
    amplitude: androidx.compose.ui.unit.Dp = 2.dp,
    waveCount: Int = 6
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CircularWavyProgress")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "IndicatorRotation"
    )

    androidx.compose.foundation.Canvas(modifier = modifier.rotate(rotation)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokePx = strokeWidth.toPx()
        val ampPx = amplitude.toPx()
        val baseRadius = (minOf(w, h) - strokePx - 2 * ampPx) / 2f

        drawCircle(
            color = trackColor,
            radius = baseRadius,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx)
        )

        val path = androidx.compose.ui.graphics.Path()
        val steps = 120
        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * 2 * Math.PI.toFloat()
            val r = baseRadius + ampPx * kotlin.math.sin(waveCount * angle - phase)
            val x = cx + r * kotlin.math.cos(angle)
            val y = cy + r * kotlin.math.sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokePx,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

@Composable
fun WaveformSyncButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cornerRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isLoading) 24.dp else 16.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "ShapeAnim"
    )

    val bgColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isLoading) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = androidx.compose.animation.core.tween(400),
        label = "BgColorAnim"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .clickable(onClick = {
                HapticUtils.vibrate(context, HapticType.MEDIUM)
                onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                (androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy)) +
                        androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200))).togetherWith(
                    androidx.compose.animation.scaleOut(androidx.compose.animation.core.tween(200)) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                )
            },
            label = "SyncAnim"
        ) { loading ->
            if (loading) {
                Material3CircularWavyProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeWidth = 3.dp,
                    amplitude = 2.5.dp,
                    waveCount = 5
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    uiState: CurrencyUiState,
    isEditingHome: Boolean,
    calendarType: String = "jalali",
    digitType: String = "fa",
    onRefresh: () -> Unit,
    onEditHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.motionScheme
    val context = androidx.compose.ui.platform.LocalContext.current
    val formattedToday = remember(calendarType, digitType) {
        val isEng = java.util.Locale.getDefault().language == "en"
        val dateText = if (calendarType == "jalali") {
            try {
                val uLocale = android.icu.util.ULocale("fa_IR@calendar=persian")
                val cal = android.icu.util.Calendar.getInstance(uLocale)
                val year = cal.get(android.icu.util.Calendar.YEAR)
                val month = cal.get(android.icu.util.Calendar.MONTH) + 1
                val day = cal.get(android.icu.util.Calendar.DAY_OF_MONTH)
                val monthNames = if (isEng) {
                    listOf("Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand")
                } else {
                    listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
                }
                "$day ${monthNames[month - 1]} $year"
            } catch (e: Exception) {
                val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
                sdf.format(java.util.Date())
            }
        } else {
            val sdf = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        }
        dateText.toLocalizedDigits(digitType)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                val appTitle = androidx.compose.ui.res.stringResource(R.string.app_name)
                val liveMarketStr = androidx.compose.ui.res.stringResource(R.string.live_market)
                Text(
                    text = appTitle,
                    fontSize = adaptiveSp(20f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = getFontFamilyForText(appTitle)
                )
                val timeDisplay = if (uiState.lastUpdatedTime.isNotEmpty()) {
                    "$formattedToday • ${uiState.lastUpdatedTime.toLocalizedDigits(digitType)}"
                } else {
                    "$formattedToday • $liveMarketStr"
                }
                Text(
                    text = timeDisplay,
                    fontSize = adaptiveSp(10f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    fontFamily = getFontFamilyForText(timeDisplay)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(adaptiveDp(12f))) {
            val editCornerRadius by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isEditingHome) adaptiveDp(22f) else adaptiveDp(12f),
                animationSpec = motionScheme.defaultSpatialSpec(),
                label = "EditShapeAnim"
            )

            val editBgColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isEditingHome) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer,
                animationSpec = motionScheme.defaultEffectsSpec(),
                label = "EditBgColorAnim"
            )

            Box(
                modifier = Modifier
                    .size(adaptiveDp(44f))
                    .clip(RoundedCornerShape(editCornerRadius))
                    .background(editBgColor)
                    .clickable(onClick = onEditHome),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isEditingHome,
                    transitionSpec = {
                        (scaleIn(animationSpec = motionScheme.defaultSpatialSpec()) +
                                fadeIn(motionScheme.fastEffectsSpec())).togetherWith(
                            scaleOut(motionScheme.fastEffectsSpec()) + fadeOut(motionScheme.fastEffectsSpec())
                        )
                    },
                    label = "EditAnim"
                ) { editing ->
                    Icon(
                        imageVector = if (editing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = "Edit Home",
                        tint = if (editing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(adaptiveDp(24f))
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("all", "currency", "gold_and_coin", "crypto")
    val context = LocalContext.current
    val categoryLabels = listOf(
        R.string.all_markets,
        R.string.category_currency,
        R.string.category_gold_and_coin,
        R.string.category_crypto
    )
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    ExpressiveConnectedButtonGroup(
        modifier = modifier,
        itemsCount = categories.size,
        selectedIndex = selectedIndex,
        onSelect = {
            HapticUtils.vibrate(context, HapticType.LIGHT)
            onCategorySelected(categories[it])
        },
        spacing = 4.dp,
        height = 42.dp
    ) { index, isSelected ->
        Text(
            text = androidx.compose.ui.res.stringResource(categoryLabels[index]),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val backgroundColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer, tween(300), label = "bg_color")
    val textColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer, tween(300), label = "text_color")
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable {
                HapticUtils.vibrate(context, HapticType.LIGHT)
                onClick()
            }
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.animation.AnimatedVisibility(visible = selected) {
            Row {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = getFontFamilyForText(text))
    }
}

data class HomeRowConfig(
    val id: Int,
    val isMerged: Boolean,
    val style: String,
    val isColored: Boolean
)

@Composable
fun RenderCard(
    style: String,
    item: com.mmdparsadev.cheghad.data.models.CurrencyItem?,
    isMerged: Boolean,
    onClickItem: (com.mmdparsadev.cheghad.data.models.CurrencyItem) -> Unit,
    isEditing: Boolean,
    digitType: String,
    colorSchemeMode: String,
    upColor: Color,
    downColor: Color,
    coloredCardsMode: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTv = isTvDevice()
    val motionScheme = MaterialTheme.motionScheme
    if (style == "hero" && isMerged) {
        val usdChange = item?.changePercentage ?: 0.0
        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
        val usdIsUp = usdChange >= 0.0
        val usdIsGreen = if (colorSchemeMode == "inverted") !usdIsUp else usdIsUp

        val isZeroChange = item == null || Math.abs(usdChange) < 0.001
        val targetHeroBgColor = if (coloredCardsMode) {
            if (isZeroChange) {
                if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
            } else if (usdIsGreen) {
                if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
            } else {
                if (isDark) Color(0xFF381A1F) else Color(0xFFFFEBEE)
            }
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
        val heroBgColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetHeroBgColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "HeroBgColor"
        )

        val targetHeroContentColor = if (coloredCardsMode) {
            if (isZeroChange) {
                if (isDark) Color.White else Color.Black
            } else if (usdIsGreen) {
                if (isDark) Color(0xFFE8F5E9) else Color(0xFF1B5E20)
            } else {
                if (isDark) Color(0xFFFFEBEE) else Color(0xFF8C1D18)
            }
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val heroContentColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetHeroContentColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "HeroContentColor"
        )

        val heroTrendColor = if (isZeroChange) {
            if (isDark) Color.LightGray else Color.Gray
        } else if (usdIsGreen) {
            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        } else {
            if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
        }

        val targetHeroBorderColor = if (coloredCardsMode) {
            heroTrendColor.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
        val heroBorderColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetHeroBorderColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "HeroBorderColor"
        )

        val targetHeroIconBg = if (coloredCardsMode) {
            heroTrendColor.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
        val heroIconBg by androidx.compose.animation.animateColorAsState(
            targetValue = targetHeroIconBg,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "HeroIconBg"
        )

        val targetHeroIconColor = if (coloredCardsMode) {
            heroTrendColor
        } else {
            MaterialTheme.colorScheme.primary
        }
        val heroIconColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetHeroIconColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "HeroIconColor"
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(adaptiveDp(32f)))
                .background(heroBgColor)
                .border(adaptiveDp(1f), heroBorderColor, RoundedCornerShape(adaptiveDp(32f)))
                .clickable {
                    if (!isEditing) {
                        item?.let {
                            HapticUtils.vibrate(context, HapticType.LIGHT)
                            onClickItem(it)
                        }
                    }
                }
                .padding(
                    top = adaptiveDp(22f),
                    bottom = if (isTv) adaptiveDp(54f) else adaptiveDp(24f),
                    start = adaptiveDp(24f),
                    end = adaptiveDp(24f)
                )
        ) {
            Column(
                modifier = if (isTv) Modifier.fillMaxHeight() else Modifier,
                verticalArrangement = if (isTv) Arrangement.SpaceBetween else Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(adaptiveDp(52f))
                                .clip(RoundedCornerShape(adaptiveDp(24f)))
                                .background(heroIconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconVector = when (item?.symbol) {
                                "EUR" -> Icons.Default.Euro
                                "GOLD", "XAU" -> Icons.Default.Paid
                                "BTC" -> Icons.Default.CurrencyBitcoin
                                "ETH" -> Icons.Default.Paid
                                else -> Icons.Default.AttachMoney
                            }
                            Icon(iconVector, contentDescription = null, tint = heroIconColor, modifier = Modifier.size(adaptiveDp(28f)))
                        }
                        Spacer(modifier = Modifier.width(adaptiveDp(12f)))
                        Column {
                            Text(getLocalizedTitle(item?.symbol ?: "USD", item?.title ?: "دلار آمریکا"), fontSize = adaptiveSp(22f), fontWeight = FontWeight.ExtraBold, color = heroContentColor, fontFamily = getFontFamilyForText(getLocalizedTitle(item?.symbol ?: "USD", item?.title ?: "دلار آمریکا")))
                            Text(item?.symbol ?: "USD", fontSize = adaptiveSp(14f), fontWeight = FontWeight.Medium, color = heroContentColor.copy(alpha = 0.7f), fontFamily = getFontFamilyForText(item?.symbol ?: "USD"))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(adaptiveDp(24f)))
                            .background(heroTrendColor.copy(alpha = 0.18f))
                            .padding(horizontal = adaptiveDp(12f), vertical = adaptiveDp(6f))
                    ) {
                        val percentStr = if (item != null) formatPercent(usdChange, digitType) else "------"
                        androidx.compose.animation.AnimatedContent(
                            targetState = percentStr,
                            transitionSpec = {
                                fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                            },
                            label = "PercentAnim"
                        ) { pStr ->
                            AutoResizingText(pStr, fontSize = adaptiveSp(13f), fontWeight = FontWeight.Bold, color = heroTrendColor, fontFamily = getFontFamilyForText(pStr))
                        }
                    }
                }
                
                if (!isTv) Spacer(modifier = Modifier.height(adaptiveDp(20f)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val formattedPrice = if (item != null) formatPrice(item.currentPrice, digitType, item.symbol) else "------"
                    val unitStr = androidx.compose.ui.res.stringResource(R.string.currency_toman)
                    androidx.compose.animation.AnimatedContent(
                        targetState = formattedPrice,
                        transitionSpec = {
                            androidx.compose.animation.fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(
                                fadeOut(motionScheme.defaultEffectsSpec())
                            )
                        },
                        label = "PriceAnim",
                        modifier = Modifier.alignByBaseline().weight(1f, fill = false)
                    ) { price ->
                        AutoResizingText(price, fontSize = adaptiveSp(46f), fontWeight = FontWeight.Bold, color = heroContentColor, fontFamily = getFontFamilyForText(price))
                    }
                    Spacer(modifier = Modifier.width(adaptiveDp(6f)))
                    Text(unitStr, fontSize = adaptiveSp(17f), fontWeight = FontWeight.Medium, color = heroContentColor.copy(alpha = 0.7f), modifier = Modifier.alignByBaseline(), fontFamily = getFontFamilyForText(unitStr))
                }
                
                if (isTv) Spacer(modifier = Modifier.height(adaptiveDp(8f)))
            }
        }
    } else if (style == "small" && !isMerged) {
        val iconStr = when (item?.symbol) {
            "EUR" -> "€"
            "GBP" -> "£"
            "GOLD" -> "ج"
            "BTC" -> "₿"
            "ETH" -> "Ξ"
            else -> item?.symbol?.take(1) ?: "$"
        }

        val change = item?.changePercentage ?: 0.0
        val isZeroChange = item == null || Math.abs(change) < 0.001
        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
        val isGreen = change >= 0.0

        val targetSmallBgColor = if (coloredCardsMode) {
            if (isZeroChange) {
                if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
            } else if (isGreen) {
                if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
            } else {
                if (isDark) Color(0xFF381A1F) else Color(0xFFFFEBEE)
            }
        } else {
            MaterialTheme.colorScheme.surface
        }
        val smallBgColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetSmallBgColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "SmallBgColor"
        )

        val targetSmallContentColor = if (coloredCardsMode) {
            if (isZeroChange) {
                if (isDark) Color.White else Color.Black
            } else if (isGreen) {
                if (isDark) Color(0xFFE8F5E9) else Color(0xFF1B5E20)
            } else {
                if (isDark) Color(0xFFFFEBEE) else Color(0xFF8C1D18)
            }
        } else {
            MaterialTheme.colorScheme.onBackground
        }
        val smallContentColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetSmallContentColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "SmallContentColor"
        )

        val targetSmallBorderColor = if (coloredCardsMode) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
        val smallBorderColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetSmallBorderColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "SmallBorderColor"
        )

        SmallCard(
            modifier = modifier.fillMaxWidth().clickable {
                if (!isEditing) {
                    item?.let {
                        HapticUtils.vibrate(context, HapticType.LIGHT)
                        onClickItem(it)
                    }
                }
            },
            icon = iconStr,
            value = if (item != null) formatPrice(item.currentPrice, digitType, item.symbol) else "------",
            trend = if (item != null) formatPercent(item.changePercentage, digitType) else "------",
            trendColor = run {
                if (isZeroChange) {
                    if (isDark) Color.LightGray else Color.Gray
                } else if (isGreen) upColor else downColor
            },
            backgroundColor = smallBgColor,
            contentColor = smallContentColor,
            borderColor = smallBorderColor
        )
    } else {
        val isEngCard = java.util.Locale.getDefault().language == "en"
        val unitToman = androidx.compose.ui.res.stringResource(R.string.currency_toman)
        val changeVal = item?.changePercentage ?: 0.0
        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
        val isUp = changeVal >= 0.0
        val isGreen = if (colorSchemeMode == "inverted") !isUp else isUp

        val isZeroChange = item == null || Math.abs(changeVal) < 0.001
        val targetCardBgColor = if (coloredCardsMode) {
            if (isZeroChange) {
                if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
            } else if (isGreen) {
                if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
            } else {
                if (isDark) Color(0xFF381A1F) else Color(0xFFFFEBEE)
            }
        } else {
            MaterialTheme.colorScheme.surface
        }
        val cardBgColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetCardBgColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "CardBgColor"
        )

        val targetCardContentColor = if (coloredCardsMode) {
            if (isZeroChange) {
                if (isDark) Color.White else Color.Black
            } else if (isGreen) {
                if (isDark) Color(0xFFE8F5E9) else Color(0xFF1B5E20)
            } else {
                if (isDark) Color(0xFFFFEBEE) else Color(0xFF8C1D18)
            }
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val cardContentColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetCardContentColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "CardContentColor"
        )

        val cardTrendColor = if (isZeroChange) {
            if (isDark) Color.LightGray else Color.Gray
        } else if (isGreen) {
            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        } else {
            if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
        }

        val targetCardBorderColor = if (coloredCardsMode) {
            Color.Transparent
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
        val cardBorderColor by androidx.compose.animation.animateColorAsState(
            targetValue = targetCardBorderColor,
            animationSpec = motionScheme.defaultEffectsSpec(),
            label = "CardBorderColor"
        )

        SecondaryCard(
            modifier = modifier.fillMaxWidth().clickable {
                if (!isEditing) {
                    item?.let {
                        HapticUtils.vibrate(context, HapticType.LIGHT)
                        onClickItem(it)
                    }
                }
            },
            title = getLocalizedTitle(item?.symbol ?: "GOLD", item?.title ?: "سکه امامی"),
            subtitle = if (item?.symbol == "GOLD") {
                if (isEngCard) "Emami Coin / New Design" else "سکه امامی / طرح جدید"
            } else if (item?.symbol == "XAU") {
                if (isEngCard) "Gold Ounce / Global Price" else "انس طلا / قیمت جهانی"
            } else {
                "${getLocalizedTitle(item?.symbol ?: "", item?.title ?: "")} / $unitToman"
            },
            value = if (item != null) formatPrice(item.currentPrice, digitType, item.symbol) else "------",
            trend = if (item != null) (if (isZeroChange) "—" else if (changeVal >= 0) "↑" else "↓") else "------",
            trendColor = cardTrendColor,
            backgroundColor = cardBgColor,
            contentColor = cardContentColor,
            borderColor = cardBorderColor
        )
    }
}

@Composable
fun BentoGrid(
    items: List<CurrencyItem>,
    homeSymbols: List<String>,
    isEditing: Boolean = false,
    colorSchemeMode: String = "standard",
    digitType: String = "fa",
    onSymbolsChanged: (List<String>) -> Unit = {},
    onClickItem: (CurrencyItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.motionScheme
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }

    var row1Merged by remember {
        mutableStateOf(sharedPrefs.getBoolean("row1_merged", true))
    }
    var row2Merged by remember {
        mutableStateOf(sharedPrefs.getBoolean("row2_merged", false))
    }
    var row3Merged by remember {
        mutableStateOf(sharedPrefs.getBoolean("row3_merged", false))
    }

    var row1Colored by remember {
        mutableStateOf(sharedPrefs.getBoolean("row1_colored", true))
    }
    var row2Colored by remember {
        mutableStateOf(sharedPrefs.getBoolean("row2_colored", true))
    }
    var row3Colored by remember {
        mutableStateOf(sharedPrefs.getBoolean("row3_colored", true))
    }

    val onRowMergeToggled: (Int, Boolean) -> Unit = { rowId, merged ->
        when (rowId) {
            1 -> {
                row1Merged = merged
                sharedPrefs.edit().putBoolean("row1_merged", merged).apply()
            }
            2 -> {
                row2Merged = merged
                sharedPrefs.edit().putBoolean("row2_merged", merged).apply()
            }
            3 -> {
                row3Merged = merged
                sharedPrefs.edit().putBoolean("row3_merged", merged).apply()
            }
        }
    }

    val onRowColorToggled: (Int, Boolean) -> Unit = { rowId, colored ->
        when (rowId) {
            1 -> {
                row1Colored = colored
                sharedPrefs.edit().putBoolean("row1_colored", colored).apply()
            }
            2 -> {
                row2Colored = colored
                sharedPrefs.edit().putBoolean("row2_colored", colored).apply()
            }
            3 -> {
                row3Colored = colored
                sharedPrefs.edit().putBoolean("row3_colored", colored).apply()
            }
        }
    }

    val rowsConfig = remember(row1Merged, row2Merged, row3Merged, row1Colored, row2Colored, row3Colored) {
        listOf(
            HomeRowConfig(id = 1, isMerged = row1Merged, style = "hero", isColored = row1Colored),
            HomeRowConfig(id = 2, isMerged = row2Merged, style = "secondary", isColored = row2Colored),
            HomeRowConfig(id = 3, isMerged = row3Merged, style = "small", isColored = row3Colored)
        )
    }

    val slotIndices = remember(row1Merged, row2Merged, row3Merged) {
        var currentSlotIndex = 0
        val indices = mutableListOf<List<Int>>()
        for (row in rowsConfig) {
            if (row.isMerged) {
                indices.add(listOf(currentSlotIndex))
                currentSlotIndex += 1
            } else {
                indices.add(listOf(currentSlotIndex, currentSlotIndex + 1))
                currentSlotIndex += 2
            }
        }
        indices
    }

    val totalSlots = remember(row1Merged, row2Merged, row3Merged) {
        (if (row1Merged) 1 else 2) + (if (row2Merged) 1 else 2) + (if (row3Merged) 1 else 2)
    }

    val finalHomeSymbols = remember(homeSymbols, totalSlots, items) {
        val list = homeSymbols.toMutableList()
        while (list.size < totalSlots) {
            val nextAvailable = items.firstOrNull { it.symbol !in list }?.symbol ?: "USD"
            list.add(nextAvailable)
        }
        list
    }

    val upColor = if (colorSchemeMode == "inverted") MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    val downColor = if (colorSchemeMode == "inverted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var replaceSlotIndex by remember { mutableStateOf<Int?>(null) }

    // Logic to track item positions for smooth swapping
    val itemDisplacements = remember { mutableStateMapOf<String, Offset>() }

    var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val slotCoordinates = remember { mutableStateListOf<LayoutCoordinates?>().apply { repeat(10) { add(null) } } }

    @Composable
    fun DraggableCardContainer(
        slotIndex: Int,
        symbol: String?,
        shape: RoundedCornerShape,
        modifier: Modifier = Modifier,
        content: @Composable BoxScope.() -> Unit
    ) {
        val motionScheme = MaterialTheme.motionScheme
        val isDragging = draggedIndex == slotIndex

        // Calculate where the item should be visually
        val targetDisplacement = if (isDragging) dragOffset else (itemDisplacements[symbol ?: ""] ?: Offset.Zero)

        val animatedOffset by animateOffsetAsState(
            targetValue = targetDisplacement,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
            label = "CardPosition",
            finishedListener = {
                if (!isDragging && symbol != null) {
                    itemDisplacements.remove(symbol)
                }
            }
        )

        Box(
            modifier = modifier
                .onGloballyPositioned { coords ->
                    if (slotIndex < slotCoordinates.size) {
                        slotCoordinates[slotIndex] = coords
                    }
                }
                .zIndex(if (isDragging) 100f else 1f)
                .graphicsLayer {
                    translationX = animatedOffset.x
                    translationY = animatedOffset.y

                    if (isDragging) {
                        scaleX = 1.08f
                        scaleY = 1.08f
                        shadowElevation = 16.dp.toPx()
                        alpha = 0.95f
                    } else {
                        scaleX = 1f
                        scaleY = 1f
                        shadowElevation = 0f
                        alpha = 1f
                    }
                }
                .then(
                    if (isEditing) {
                        Modifier.border(
                            width = adaptiveDp(1.5f),
                            color = if (isDragging) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            shape = shape
                        )
                    } else Modifier
                )
        ) {
            content()
            AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + scaleIn(initialScale = 0.7f),
                exit = fadeOut() + scaleOut(targetScale = 0.7f),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .padding(adaptiveDp(12f))
                        .size(adaptiveDp(24f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(adaptiveDp(14f)).align(Alignment.Center)
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isEditing,
                enter = fadeIn() + scaleIn(initialScale = 0.7f),
                exit = fadeOut() + scaleOut(targetScale = 0.7f),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .padding(adaptiveDp(12f))
                        .size(adaptiveDp(24f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable {
                            replaceSlotIndex = slotIndex
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Replace",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(adaptiveDp(14f)).align(Alignment.Center)
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                parentCoordinates = coords
            }
            .then(
                if (isEditing) {
                    Modifier.pointerInput(finalHomeSymbols, totalSlots) {
                        detectDragGestures(
                            onDragStart = { startPosition ->
                                val activeParentCoords = parentCoordinates
                                if (activeParentCoords != null && activeParentCoords.isAttached) {
                                    for (i in 0 until totalSlots) {
                                        val slotCoords = slotCoordinates[i]
                                        if (slotCoords != null && slotCoords.isAttached) {
                                            val bounds = activeParentCoords.localBoundingBoxOf(slotCoords)
                                            if (bounds.contains(startPosition)) {
                                                draggedIndex = i
                                                dragOffset = Offset.Zero
                                                break
                                            }
                                        }
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount

                                val activeParentCoords = parentCoordinates
                                val activeDraggedIndex = draggedIndex
                                if (activeDraggedIndex != null && activeParentCoords != null && activeParentCoords.isAttached) {
                                    val draggedCoords = slotCoordinates[activeDraggedIndex]
                                    if (draggedCoords != null && draggedCoords.isAttached) {
                                        val draggedBounds = activeParentCoords.localBoundingBoxOf(draggedCoords)
                                        val draggedCenter = draggedBounds.center + dragOffset

                                        for (j in 0 until totalSlots) {
                                            if (j != activeDraggedIndex) {
                                                val otherCoords = slotCoordinates[j]
                                                if (otherCoords != null && otherCoords.isAttached) {
                                                    val otherBounds = activeParentCoords.localBoundingBoxOf(otherCoords)
                                                    if (otherBounds.contains(draggedCenter)) {
                                                        val newList = finalHomeSymbols.toMutableList()
                                                        val idDragged = newList[activeDraggedIndex]
                                                        val idOther = newList[j]

                                                        newList[activeDraggedIndex] = idOther
                                                        newList[j] = idDragged

                                                        val originalCenterI = otherBounds.center
                                                        val originalCenterJ = draggedBounds.center
                                                        val jumpOffset = originalCenterI - originalCenterJ

                                                        // Important: Give the 'other' item a displacement so it can animate back
                                                        itemDisplacements[idOther] = -jumpOffset

                                                        // Adjust dragged item offset to keep it under finger
                                                        dragOffset -= jumpOffset

                                                        HapticUtils.vibrate(context, HapticType.LIGHT)
                                                        onSymbolsChanged(newList)
                                                        draggedIndex = j
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                draggedIndex = null
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = Offset.Zero
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(adaptiveDp(12f))
        ) {
            rowsConfig.forEachIndexed { rowIndex, rowConfig ->
                androidx.compose.animation.AnimatedVisibility(
                    visible = isEditing,
                    enter = androidx.compose.animation.fadeIn(motionScheme.defaultEffectsSpec()) + expandVertically(motionScheme.defaultSpatialSpec()),
                    exit = fadeOut(motionScheme.defaultEffectsSpec()) + shrinkVertically(motionScheme.defaultSpatialSpec())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = adaptiveDp(4f), vertical = adaptiveDp(2f)),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                id = when (rowConfig.id) {
                                    1 -> R.string.first_row_customize
                                    2 -> R.string.second_row_customize
                                    else -> R.string.third_row_customize
                                }
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(adaptiveDp(8f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    HapticUtils.vibrate(context, HapticType.LIGHT)
                                    onRowColorToggled(rowConfig.id, !rowConfig.isColored)
                                },
                                contentPadding = PaddingValues(horizontal = adaptiveDp(8f), vertical = adaptiveDp(4f)),
                                modifier = Modifier.height(adaptiveDp(28f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(adaptiveDp(16f)),
                                    tint = if (rowConfig.isColored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(adaptiveDp(4f)))
                                Text(
                                    text = stringResource(
                                        id = if (rowConfig.isColored) {
                                            R.string.colorful_customize
                                        } else {
                                            R.string.not_colorful_customize
                                        }
                                    ),
                                    fontSize = adaptiveSp(11f),
                                    fontWeight = FontWeight.Bold,
                                    color = if (rowConfig.isColored) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }

                            TextButton(
                                onClick = {
                                    HapticUtils.vibrate(context, HapticType.LIGHT)
                                    onRowMergeToggled(rowConfig.id, !rowConfig.isMerged)
                                },
                                contentPadding = PaddingValues(horizontal = adaptiveDp(8f), vertical = adaptiveDp(4f)),
                                modifier = Modifier.height(adaptiveDp(28f))
                            ) {
                                Icon(
                                    imageVector = if (rowConfig.isMerged) Icons.AutoMirrored.Filled.CallSplit else Icons.AutoMirrored.Filled.CallMerge,
                                    contentDescription = null,
                                    modifier = Modifier.size(adaptiveDp(16f))
                                )
                                Spacer(modifier = Modifier.width(adaptiveDp(4f)))
                                Text(
                                    text = stringResource(
                                        id = if (rowConfig.isMerged) {
                                            R.string.integration_customize
                                        } else {
                                            R.string.separation_customize
                                        }
                                    ),
                                    fontSize = adaptiveSp(11f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                val slots = slotIndices.getOrNull(rowIndex) ?: emptyList()
                androidx.compose.animation.AnimatedContent(
                    targetState = rowConfig.isMerged,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(
                            animationSpec = motionScheme.defaultEffectsSpec()
                        ) + scaleIn(
                            initialScale = 0.95f,
                            animationSpec = motionScheme.defaultSpatialSpec()
                        )).togetherWith(
                            fadeOut(
                                animationSpec = motionScheme.defaultEffectsSpec()
                            ) + scaleOut(
                                targetScale = 0.95f,
                                animationSpec = motionScheme.defaultSpatialSpec()
                            )
                        )
                    },
                    label = "RowMergeTransition"
                ) { isMergedState ->
                    val isTv = isTvDevice()
                    if (isTv) {
                        // چیدمان مخصوص اندروید تی‌وی: ردیف اول (بزرگ) در سمت راست/کنار و ردیف‌های دوم و سوم در جلو (سمت چپ) به صورت ستونی
                        if (rowConfig.id == 1) {
                            // ردیف اول بزرگ سمت راست قرار می‌گیرد
                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                                horizontalArrangement = Arrangement.spacedBy(adaptiveDp(16f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // کادر اصلی بزرگ ردیف اول
                                Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                                    if (isMergedState) {
                                        val slotIdx = slots.getOrNull(0) ?: 0
                                        val symbol = finalHomeSymbols.getOrNull(slotIdx)
                                        val item = items.find { it.symbol == symbol }
                                        DraggableCardContainer(
                                            slotIndex = slotIdx,
                                            symbol = symbol,
                                            shape = RoundedCornerShape(adaptiveDp(28f)),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            RenderCard(
                                                style = rowConfig.style,
                                                item = item,
                                                isMerged = true,
                                                onClickItem = onClickItem,
                                                isEditing = isEditing,
                                                digitType = digitType,
                                                colorSchemeMode = colorSchemeMode,
                                                upColor = upColor,
                                                downColor = downColor,
                                                coloredCardsMode = rowConfig.isColored,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxSize().height(IntrinsicSize.Max),
                                            horizontalArrangement = Arrangement.spacedBy(adaptiveDp(12f))
                                        ) {
                                            slots.forEach { slotIdx ->
                                                val symbol = finalHomeSymbols.getOrNull(slotIdx)
                                                val item = items.find { it.symbol == symbol }
                                                DraggableCardContainer(
                                                    slotIndex = slotIdx,
                                                    symbol = symbol,
                                                    shape = RoundedCornerShape(adaptiveDp(28f)),
                                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                                ) {
                                                    RenderCard(
                                                        style = rowConfig.style,
                                                        item = item,
                                                        isMerged = false,
                                                        onClickItem = onClickItem,
                                                        isEditing = isEditing,
                                                        digitType = digitType,
                                                        colorSchemeMode = colorSchemeMode,
                                                        upColor = upColor,
                                                        downColor = downColor,
                                                        coloredCardsMode = rowConfig.isColored,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // ردیف‌های دوم و سوم در کنار آن به صورت ستونی چیده می‌شوند
                                Column(
                                    modifier = Modifier.weight(1.8f).fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(adaptiveDp(12f))
                                ) {
                                    // نمایش کارت‌های ردیف ۲ و ۳ به صورت افقی/جلوتر
                                    rowsConfig.filter { it.id > 1 }.forEach { subRow ->
                                        val subSlots = slotIndices.getOrNull(subRow.id - 1) ?: emptyList()
                                        if (subRow.isMerged) {
                                            val slotIdx = subSlots.getOrNull(0) ?: 0
                                            val symbol = finalHomeSymbols.getOrNull(slotIdx)
                                            val item = items.find { it.symbol == symbol }
                                            DraggableCardContainer(
                                                slotIndex = slotIdx,
                                                symbol = symbol,
                                                shape = RoundedCornerShape(adaptiveDp(28f)),
                                                modifier = Modifier.fillMaxWidth().weight(1f)
                                            ) {
                                                RenderCard(
                                                    style = subRow.style,
                                                    item = item,
                                                    isMerged = true,
                                                    onClickItem = onClickItem,
                                                    isEditing = isEditing,
                                                    digitType = digitType,
                                                    colorSchemeMode = colorSchemeMode,
                                                    upColor = upColor,
                                                    downColor = downColor,
                                                    coloredCardsMode = subRow.isColored,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().weight(1f).height(IntrinsicSize.Max),
                                                horizontalArrangement = Arrangement.spacedBy(adaptiveDp(12f))
                                            ) {
                                                subSlots.forEach { slotIdx ->
                                                    val symbol = finalHomeSymbols.getOrNull(slotIdx)
                                                    val item = items.find { it.symbol == symbol }
                                                    DraggableCardContainer(
                                                        slotIndex = slotIdx,
                                                        symbol = symbol,
                                                        shape = RoundedCornerShape(adaptiveDp(28f)),
                                                        modifier = Modifier.weight(1f).fillMaxHeight()
                                                    ) {
                                                        RenderCard(
                                                            style = subRow.style,
                                                            item = item,
                                                            isMerged = false,
                                                            onClickItem = onClickItem,
                                                            isEditing = isEditing,
                                                            digitType = digitType,
                                                            colorSchemeMode = colorSchemeMode,
                                                            upColor = upColor,
                                                            downColor = downColor,
                                                            coloredCardsMode = subRow.isColored,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // ردیف‌های ۲ و ۳ چون در ستون کنار ردیف اول رندر شدند، اینجا خالی می‌مانند تا تکرار نشوند
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    } else {
                        // حالت موبایل استاندارد
                        if (isMergedState) {
                            val slotIdx = slots.getOrNull(0) ?: 0
                            val symbol = finalHomeSymbols.getOrNull(slotIdx)
                            val item = items.find { it.symbol == symbol }

                            DraggableCardContainer(
                                slotIndex = slotIdx,
                                symbol = symbol,
                                shape = RoundedCornerShape(adaptiveDp(28f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RenderCard(
                                    style = rowConfig.style,
                                    item = item,
                                    isMerged = true,
                                    onClickItem = onClickItem,
                                    isEditing = isEditing,
                                    digitType = digitType,
                                    colorSchemeMode = colorSchemeMode,
                                    upColor = upColor,
                                    downColor = downColor,
                                    coloredCardsMode = rowConfig.isColored
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(adaptiveDp(12f))
                            ) {
                                slots.forEach { slotIdx ->
                                    val symbol = finalHomeSymbols.getOrNull(slotIdx)
                                    val item = items.find { it.symbol == symbol }

                                    DraggableCardContainer(
                                        slotIndex = slotIdx,
                                        symbol = symbol,
                                        shape = RoundedCornerShape(adaptiveDp(28f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RenderCard(
                                            style = rowConfig.style,
                                            item = item,
                                            isMerged = false,
                                            onClickItem = onClickItem,
                                            isEditing = isEditing,
                                            digitType = digitType,
                                            colorSchemeMode = colorSchemeMode,
                                            upColor = upColor,
                                            downColor = downColor,
                                            coloredCardsMode = rowConfig.isColored
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (replaceSlotIndex != null) {
        val slotIdx = replaceSlotIndex!!
        val availableItems = items.filter { it.symbol !in finalHomeSymbols.take(totalSlots) }

        AlertDialog(
            onDismissRequest = { replaceSlotIndex = null },
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.replace_hero_title),
                    fontSize = adaptiveSp(18f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                if (availableItems.isEmpty()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.no_new_options),
                        fontSize = adaptiveSp(14f),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = adaptiveDp(280f)),
                        verticalArrangement = Arrangement.spacedBy(adaptiveDp(8f))
                    ) {
                        items(availableItems) { item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(adaptiveDp(12f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newList = finalHomeSymbols.toMutableList()
                                        if (slotIdx < newList.size) {
                                            newList[slotIdx] = item.symbol
                                            onSymbolsChanged(newList)
                                        }
                                        replaceSlotIndex = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(adaptiveDp(12f)),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.symbol,
                                        fontSize = adaptiveSp(12f),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(adaptiveDp(8f))
                                    ) {
                                        Text(
                                            text = getLocalizedTitle(item.symbol, item.title),
                                            fontSize = adaptiveSp(14f),
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (item.category) {
                                                com.mmdparsadev.cheghad.data.models.CurrencyType.Crypto -> "🪙"
                                                com.mmdparsadev.cheghad.data.models.CurrencyType.GoldAndCoin -> "🥇"
                                                else -> "💵"
                                            },
                                            fontSize = adaptiveSp(16f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    replaceSlotIndex = null
                }) {
                    Text(androidx.compose.ui.res.stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun Sparkline() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(32.dp)
    ) {
        Box(modifier = Modifier.size(4.dp, 12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
        Box(modifier = Modifier.size(4.dp, 20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
        Box(modifier = Modifier.size(4.dp, 32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.size(4.dp, 24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Box(modifier = Modifier.size(4.dp, 16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
    }
}

@Composable
fun SecondaryCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    value: String,
    trend: String,
    trendColor: Color,
    backgroundColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    borderColor: Color = Color.Transparent
) {
    val isTv = isTvDevice()
    val motionScheme = MaterialTheme.motionScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(adaptiveDp(28f)))
            .background(backgroundColor)
            .border(adaptiveDp(1f), borderColor, RoundedCornerShape(adaptiveDp(28f)))
            .padding(
                top = adaptiveDp(18f),
                bottom = if (isTv) adaptiveDp(44f) else adaptiveDp(22f),
                start = adaptiveDp(20f),
                end = adaptiveDp(20f)
            )
    ) {
        Column(
            modifier = if (isTv) Modifier.fillMaxHeight() else Modifier,
            verticalArrangement = if (isTv) Arrangement.SpaceBetween else Arrangement.Top
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = adaptiveSp(14f), fontWeight = FontWeight.Bold, color = contentColor, fontFamily = getFontFamilyForText(title))
                androidx.compose.animation.AnimatedContent(
                    targetState = trend,
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(
                            fadeOut(motionScheme.defaultEffectsSpec())
                        )
                    },
                    label = "TrendAnim"
                ) { trnd ->
                    AutoResizingText(trnd, fontSize = adaptiveSp(14f), fontWeight = FontWeight.Black, color = trendColor, fontFamily = getFontFamilyForText(trnd), modifier = Modifier.weight(1f, fill = false))
                }
            }

            if (!isTv) Spacer(modifier = Modifier.height(adaptiveDp(18f)))

            Column {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(
                            fadeOut(motionScheme.defaultEffectsSpec())
                        )
                    },
                    label = "ValueAnim"
                ) { valStr ->
                    AutoResizingText(valStr, fontSize = adaptiveSp(18f), fontWeight = FontWeight.Bold, color = contentColor, fontFamily = getFontFamilyForText(valStr))
                }
                Spacer(modifier = Modifier.height(adaptiveDp(2f)))
                Text(subtitle, fontSize = adaptiveSp(11f), fontWeight = FontWeight.Medium, color = contentColor.copy(alpha = 0.7f), fontFamily = getFontFamilyForText(subtitle))
            }
            
            if (isTv) Spacer(modifier = Modifier.height(adaptiveDp(6f)))
        }
    }
}

@Composable
fun SmallCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    trend: String,
    trendColor: Color,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    val isTv = isTvDevice()
    val motionScheme = MaterialTheme.motionScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(adaptiveDp(28f)))
            .background(backgroundColor)
            .border(adaptiveDp(1f), borderColor, RoundedCornerShape(adaptiveDp(28f)))
            .padding(
                top = adaptiveDp(16f),
                bottom = if (isTv) adaptiveDp(42f) else adaptiveDp(20f),
                start = adaptiveDp(18f),
                end = adaptiveDp(18f)
            )
    ) {
        Column(
            modifier = if (isTv) Modifier.fillMaxHeight() else Modifier,
            verticalArrangement = if (isTv) Arrangement.SpaceBetween else Arrangement.Top
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(adaptiveDp(28f)).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    AutoResizingText(icon, fontSize = adaptiveSp(12f), fontWeight = FontWeight.Bold, color = contentColor, fontFamily = getFontFamilyForText(icon))
                }
                AutoResizingText(trend, fontSize = adaptiveSp(12f), fontWeight = FontWeight.Bold, color = trendColor, fontFamily = getFontFamilyForText(trend), modifier = Modifier.weight(1f, fill = false))
            }
            if (!isTv) Spacer(modifier = Modifier.height(adaptiveDp(10f)))
            AutoResizingText(value, fontSize = adaptiveSp(15f), fontWeight = FontWeight.Bold, color = contentColor, fontFamily = getFontFamilyForText(value))
            
            if (isTv) Spacer(modifier = Modifier.height(adaptiveDp(6f)))
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: String, onScreenSelected: (String) -> Unit) {
    val isTv = isTvDevice()
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isTv) 0.dp else 8.dp,
        tonalElevation = if (isTv) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isTv) Modifier.navigationBarsPadding() else Modifier)
                .padding(vertical = if (isTv) 8.dp else 12.dp, horizontal = if (isTv) 24.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                title = androidx.compose.ui.res.stringResource(R.string.nav_market),
                icon = Icons.Default.Dashboard,
                isSelected = currentScreen == "home",
                onClick = { onScreenSelected("home") }
            )
            NavBarItem(
                title = androidx.compose.ui.res.stringResource(R.string.nav_calculator),
                icon = Icons.Default.Calculate,
                isSelected = currentScreen == "calculator",
                onClick = { onScreenSelected("calculator") }
            )
            NavBarItem(
                title = androidx.compose.ui.res.stringResource(R.string.nav_news),
                icon = Icons.Default.Newspaper,
                isSelected = currentScreen == "news",
                onClick = { onScreenSelected("news") }
            )
            NavBarItem(
                title = androidx.compose.ui.res.stringResource(R.string.nav_portfolio),
                icon = Icons.Default.Notifications,
                isSelected = currentScreen == "portfolio",
                onClick = { onScreenSelected("portfolio") }
            )
            NavBarItem(
                title = androidx.compose.ui.res.stringResource(R.string.nav_settings),
                icon = Icons.Default.Settings,
                isSelected = currentScreen == "settings",
                onClick = { onScreenSelected("settings") }
            )
        }
    }
}

@Composable
fun NavBarItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val isTv = isTvDevice()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val alphaAnim by animateFloatAsState(targetValue = if (isSelected || (isTv && isFocused)) 1f else 0.6f, label = "AlphaAnim")
    val scaleAnim by animateFloatAsState(targetValue = if (isTv && isFocused) 1.12f else 1f, label = "ScaleAnim")
    val bgColor by animateColorAsState(if (isSelected || (isTv && isFocused)) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent, label = "BgColorAnim")
    val iconColor by animateColorAsState(if (isSelected || (isTv && isFocused)) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface, label = "IconColorAnim")
    val fontWeightAnim = if (isSelected || (isTv && isFocused)) FontWeight.Bold else FontWeight.Medium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
            }
            .clip(RoundedCornerShape(16.dp))
            .focusable(interactionSource = interactionSource)
            .border(
                width = if (isTv && isFocused) 2.dp else 0.dp,
                color = if (isTv && isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onClick()
                }
            )
            .padding(horizontal = if (isTv) 16.dp else 12.dp, vertical = if (isTv) 8.dp else 6.dp)
            .alpha(alphaAnim)
    ) {
        Box(
            modifier = Modifier
                .width(if (isTv) 56.dp else 64.dp)
                .height(if (isTv) 28.dp else 32.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = if (isTv) 11.sp else 12.sp, fontWeight = fontWeightAnim, color = iconColor)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetListItem(
    item: CurrencyItem,
    modifier: Modifier = Modifier,
    colorSchemeMode: String = "standard",
    digitType: String = "fa",
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isReordering: Boolean = false,
    isDragging: Boolean = false
) {
    val context = LocalContext.current
    val motionScheme = MaterialTheme.motionScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(adaptiveDp(1f), MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .combinedClickable(
                enabled = !isReordering,
                onClick = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onClick()
                },
                onLongClick = {
                    HapticUtils.vibrate(context, HapticType.MEDIUM)
                    onLongClick()
                }
            )
            .padding(horizontal = adaptiveDp(24f), vertical = adaptiveDp(14f)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (isReordering) {
                Icon(
                    imageVector = Icons.Default.Reorder,
                    contentDescription = "Reorder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = adaptiveDp(12f)).size(adaptiveDp(20f))
                )
            }
            Box(
                modifier = Modifier
                    .size(adaptiveDp(40f))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(item.symbol.take(3), fontSize = adaptiveSp(10f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(adaptiveDp(12f)))
            Column {
                Text(getLocalizedTitle(item.symbol, item.title), fontSize = adaptiveSp(14f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(item.symbol, fontSize = adaptiveSp(12f), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val formattedPrice = formatPrice(item.currentPrice, digitType, item.symbol)
            Text(
                text = formattedPrice,
                fontSize = adaptiveSp(14f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            val isZeroChange = Math.abs(item.changePercentage) < 0.001
            val isDark = MaterialTheme.colorScheme.background.red < 0.5f
            val isNegative = item.changePercentage < 0
            val upColor = if (colorSchemeMode == "inverted") MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
            val downColor = if (colorSchemeMode == "inverted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            val color = if (isZeroChange) {
                if (isDark) Color.LightGray else Color.Gray
            } else if (isNegative) downColor else upColor
            Text(
                text = formatPercent(item.changePercentage, digitType),
                fontSize = adaptiveSp(12f),
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun WelcomeScreen(onComplete: (lang: String, theme: String) -> Unit) {
    val context = LocalContext.current
    var selectedLang by remember { mutableStateOf("fa") }
    var selectedTheme by remember { mutableStateOf("system") }
    val scrollState = rememberScrollState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            val cardMaxHeight = maxHeight - adaptiveDp(32f)
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = cardMaxHeight)
                    .padding(adaptiveDp(16f)),
                shape = RoundedCornerShape(adaptiveDp(32f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = adaptiveDp(6f)),
                border = BorderStroke(adaptiveDp(1f), MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(adaptiveDp(28f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(adaptiveDp(80f))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(adaptiveDp(44f))
                        )
                    }

                    Spacer(modifier = Modifier.height(adaptiveDp(20f)))

                    Text(
                        text = "چقد",
                        fontSize = adaptiveSp(28f),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = getFontFamilyForText("چقد")
                    )

                    Spacer(modifier = Modifier.height(adaptiveDp(6f)))

                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.welcome_title),
                        fontSize = adaptiveSp(16f),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.welcome_title))
                    )

                    Spacer(modifier = Modifier.height(adaptiveDp(6f)))

                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.welcome_desc),
                        fontSize = adaptiveSp(13f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.welcome_desc))
                    )

                    Spacer(modifier = Modifier.height(adaptiveDp(28f)))

                    Text(
                        text = "زبان مورد نظر خود را انتخاب کنید / Select Language",
                        fontSize = adaptiveSp(12f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(adaptiveDp(12f)))

                    // Persian language card
                    Card(
                        onClick = { selectedLang = "fa" },
                        shape = RoundedCornerShape(adaptiveDp(24f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLang == "fa") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (selectedLang == "fa") adaptiveDp(2f) else adaptiveDp(1f),
                            color = if (selectedLang == "fa") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = adaptiveDp(18f), vertical = adaptiveDp(14f)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇮🇷", fontSize = adaptiveSp(22f))
                                Spacer(modifier = Modifier.width(adaptiveDp(14f)))
                                Column {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(R.string.persian),
                                        fontSize = adaptiveSp(16f),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.persian))
                                    )
                                    Text(
                                        text = "فارسی",
                                        fontSize = adaptiveSp(12f),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = getFontFamilyForText("فارسی")
                                    )
                                }
                            }
                            RadioButton(
                                selected = selectedLang == "fa",
                                onClick = {
                                    HapticUtils.vibrate(context, HapticType.LIGHT)
                                    selectedLang = "fa"
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(adaptiveDp(12f)))

                    // English language card
                    Card(
                        onClick = { selectedLang = "en" },
                        shape = RoundedCornerShape(adaptiveDp(24f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLang == "en") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (selectedLang == "en") adaptiveDp(2f) else adaptiveDp(1f),
                            color = if (selectedLang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = adaptiveDp(18f), vertical = adaptiveDp(14f)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇬🇧", fontSize = adaptiveSp(22f))
                                Spacer(modifier = Modifier.width(adaptiveDp(14f)))
                                Column {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(R.string.english),
                                        fontSize = adaptiveSp(16f),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "English",
                                        fontSize = adaptiveSp(12f),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            RadioButton(
                                selected = selectedLang == "en",
                                onClick = {
                                    HapticUtils.vibrate(context, HapticType.LIGHT)
                                    selectedLang = "en"
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(adaptiveDp(20f)))
                    Text(
                        text = "پوسته / Theme",
                        fontSize = adaptiveSp(12f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(adaptiveDp(12f)))
                    val themeOptions = listOf("system" to "سیستم", "light" to "روشن", "dark" to "تاریک")
                    val themeIcons = listOf(Icons.Default.SettingsSuggest, Icons.Default.LightMode, Icons.Default.DarkMode)
                    val selectedThemeIndex = themeOptions.indexOfFirst { it.first == selectedTheme }.coerceAtLeast(0)

                    ExpressiveConnectedButtonGroup(
                        itemsCount = themeOptions.size,
                        selectedIndex = selectedThemeIndex,
                        onSelect = { selectedTheme = themeOptions[it].first }
                    ) { index, isSelected ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = themeIcons[index],
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(adaptiveDp(16f))
                            )
                            Spacer(modifier = Modifier.width(adaptiveDp(6f)))
                            Text(
                                text = themeOptions[index].second,
                                fontSize = adaptiveSp(12f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(adaptiveDp(28f)))

                    Button(
                        onClick = {
                            HapticUtils.vibrate(context, HapticType.LIGHT)
                            onComplete(selectedLang, selectedTheme)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(adaptiveDp(56f)),
                        shape = RoundedCornerShape(adaptiveDp(20f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = adaptiveDp(2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.continue_btn),
                                fontSize = adaptiveSp(16f),
                                fontWeight = FontWeight.Bold,
                                fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.continue_btn))
                            )
                            Spacer(modifier = Modifier.width(adaptiveDp(8f)))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(adaptiveDp(20f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditHomeBottomSheet(
    items: List<CurrencyItem>,
    currentSymbols: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    var selectedSymbols by remember { mutableStateOf(currentSymbols) }

    val density = LocalDensity.current
    val itemHeightDp = 64.dp
    val itemHeightPx = with(density) { itemHeightDp.toPx() }

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.edit_home),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.reorder_help),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Selected / Drag to Reorder
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.selected_items_title) + " (${selectedSymbols.size}/5)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Reorder List Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(selectedSymbols, key = { _, symbol -> symbol }) { index, symbol ->
                        val item = items.find { it.symbol == symbol }
                        if (item != null) {
                            val isDragging = draggedIndex == index

                            Row(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .height(itemHeightDp)
                                    .graphicsLayer {
                                        translationY = if (isDragging) dragOffsetY else 0f
                                        scaleX = if (isDragging) 1.05f else 1f
                                        scaleY = if (isDragging) 1.05f else 1f
                                        shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                                    }
                                    .background(
                                        if (isDragging) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(24.dp)
                                    )
                                    .border(
                                        width = if (isDragging) 2.dp else 1.dp,
                                        color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Drag handle
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .pointerInput(index) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    draggedIndex = index
                                                    dragOffsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount.y

                                                    val targetIndex = draggedIndex
                                                    if (targetIndex != null) {
                                                        val indexDiff = (dragOffsetY / itemHeightPx).roundToInt()
                                                        val newIndex = (targetIndex + indexDiff).coerceIn(0, selectedSymbols.lastIndex)
                                                        if (newIndex != targetIndex) {
                                                            val mutable = selectedSymbols.toMutableList()
                                                            val temp = mutable[targetIndex]
                                                            mutable.removeAt(targetIndex)
                                                            mutable.add(newIndex, temp)
                                                            selectedSymbols = mutable

                                                            draggedIndex = newIndex
                                                            dragOffsetY -= (newIndex - targetIndex) * itemHeightPx
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggedIndex = null
                                                    dragOffsetY = 0f
                                                },
                                                onDragCancel = {
                                                    draggedIndex = null
                                                    dragOffsetY = 0f
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Reorder,
                                        contentDescription = "Drag to reorder",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = item.symbol,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                    )
                                }

                                // Slot role
                                val roleTextRes = when (index) {
                                    0 -> R.string.slot_hero
                                    1 -> R.string.slot_gold
                                    2 -> R.string.slot_btc
                                    3 -> R.string.slot_eth
                                    4 -> R.string.slot_eur
                                    else -> null
                                }
                                if (roleTextRes != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(roleTextRes),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Available items to select
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.available_items_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(items) { item ->
                    val isSelected = selectedSymbols.contains(item.symbol)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val mutable = selectedSymbols.toMutableList()
                                if (isSelected) {
                                    mutable.remove(item.symbol)
                                } else if (selectedSymbols.size < 5) {
                                    mutable.add(item.symbol)
                                }
                                selectedSymbols = mutable
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = item.symbol,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onSave(selectedSymbols)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedSymbols.size == 5,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.continue_btn),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(adaptiveDp(28f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = adaptiveDp(2f))
    ) {
        Column(modifier = Modifier.padding(adaptiveDp(16f))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(adaptiveDp(38f))
                        .clip(RoundedCornerShape(adaptiveDp(10f)))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(adaptiveDp(20f))
                    )
                }
                Spacer(modifier = Modifier.width(adaptiveDp(12f)))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = adaptiveSp(16f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(adaptiveDp(2f)))
                        Text(
                            text = subtitle,
                            fontSize = adaptiveSp(11f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(adaptiveDp(14f)))
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    badgeColor: Color? = null,
    shape: Shape = RoundedCornerShape(adaptiveDp(24f))
) {
    val context = LocalContext.current
    Surface(
        onClick = {
            HapticUtils.vibrate(context, HapticType.LIGHT)
            onCheckedChange(!isChecked)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isChecked) adaptiveDp(2f) else adaptiveDp(1f),
            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = adaptiveDp(20f), vertical = adaptiveDp(12f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (badgeColor != null) {
                Box(
                    modifier = Modifier
                        .size(adaptiveDp(10f))
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(adaptiveDp(10f)))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = adaptiveSp(14f),
                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(adaptiveDp(2f)))
                    Text(
                        text = description,
                        fontSize = adaptiveSp(11f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(adaptiveDp(8f)))

            Switch(
                checked = isChecked,
                onCheckedChange = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onCheckedChange(it)
                },
                thumbContent = if (isChecked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    onLanguageSelected: (String) -> Unit,
    appThemeMode: String,
    onThemeSelected: (String) -> Unit,
    calendarType: String = "jalali",
    onCalendarSelected: (String) -> Unit = {},
    colorSchemeMode: String = "standard",
    onColorSchemeSelected: (String) -> Unit = {},
    colorSeedName: String = "DEFAULT",
    onColorSeedSelected: (String) -> Unit = {},
    digitType: String = "fa",
    onDigitTypeSelected: (String) -> Unit = {},
    timeRangeOrder: List<TimeRange>,
    onTimeRangeOrderChanged: (List<TimeRange>) -> Unit,
    disabledNewsCategories: Set<String> = emptySet(),
    onDisabledNewsCategoriesChanged: (Set<String>) -> Unit = {},
    disabledNewsAgencies: Set<String> = emptySet(),
    onDisabledNewsAgenciesChanged: (Set<String>) -> Unit = {},
    newsEnabled: Boolean = false,
    onNewsEnabledChanged: (Boolean) -> Unit = {},
    showAgenciesSheet: Boolean = false,
    onShowAgenciesSheetChanged: (Boolean) -> Unit = {},
    isCheckingUpdates: Boolean = false,
    onCheckForUpdates: () -> Unit = {},
    downloadBetaVersions: Boolean = false,
    onDownloadBetaVersionsChanged: (Boolean) -> Unit = {},
    lockscreenWidgetCurrencyId: String = "USD",
    onLockscreenWidgetCurrencySelected: (String) -> Unit = {},
    lockscreenWidgetTheme: String = "glassy",
    onLockscreenWidgetThemeSelected: (String) -> Unit = {},
    allCurrencies: List<CurrencyItem> = emptyList()
) {
    val context = LocalContext.current

    if (showAgenciesSheet) {
        ModalBottomSheet(
            onDismissRequest = { onShowAgenciesSheetChanged(false) },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = adaptiveDp(8f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = adaptiveDp(20f))
                    .padding(bottom = adaptiveDp(32f))
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.settings_news_agencies_title),
                    fontSize = adaptiveSp(20f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = adaptiveDp(4f))
                )
                Text(
                    text = stringResource(R.string.settings_news_agencies_subtitle),
                    fontSize = adaptiveSp(12f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = adaptiveDp(20f))
                )

                val agencies = NewsRepository.AGENCIES
                val isEnglish = digitType == "en"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = adaptiveDp(16f)),
                    horizontalArrangement = Arrangement.spacedBy(adaptiveDp(8f))
                ) {
                    FilledTonalButton(
                        onClick = {
                            HapticUtils.vibrate(context, HapticType.MEDIUM)
                            onDisabledNewsAgenciesChanged(emptySet())
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(stringResource(R.string.settings_news_enable_all), fontSize = adaptiveSp(11f))
                    }
                    FilledTonalButton(
                        onClick = {
                            HapticUtils.vibrate(context, HapticType.MEDIUM)
                            onDisabledNewsAgenciesChanged(agencies.map { it.id }.toSet())
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.settings_news_disable_all), fontSize = adaptiveSp(11f))
                    }
                }

                agencies.forEachIndexed { index, agency ->
                    val isChecked = !disabledNewsAgencies.contains(agency.id)
                    val shape = when (index) {
                        0 -> RoundedCornerShape(topStart = adaptiveDp(24f), topEnd = adaptiveDp(24f), bottomStart = adaptiveDp(4f), bottomEnd = adaptiveDp(4f))
                        agencies.lastIndex -> RoundedCornerShape(topStart = adaptiveDp(4f), topEnd = adaptiveDp(4f), bottomStart = adaptiveDp(24f), bottomEnd = adaptiveDp(24f))
                        else -> RoundedCornerShape(adaptiveDp(4f))
                    }
                    SettingsSwitchRow(
                        title = if (isEnglish) agency.nameEn else agency.nameFa,
                        isChecked = isChecked,
                        badgeColor = agency.brandColor,
                        shape = shape,
                        onCheckedChange = { checked ->
                            HapticUtils.vibrate(context, HapticType.LIGHT)
                            val newSet = if (checked) disabledNewsAgencies - agency.id else disabledNewsAgencies + agency.id
                            onDisabledNewsAgenciesChanged(newSet)
                        }
                    )
                    if (index < agencies.lastIndex) {
                        Spacer(modifier = Modifier.height(adaptiveDp(3f)))
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = adaptiveDp(16f))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(adaptiveDp(12f)))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(adaptiveDp(44f))
                    .clip(RoundedCornerShape(adaptiveDp(12f)))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(adaptiveDp(24f))
                )
            }
            Spacer(modifier = Modifier.width(adaptiveDp(14f)))
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.settings_title),
                    fontSize = adaptiveSp(22f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.settings_subtitle),
                    fontSize = adaptiveSp(12f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(20f)))

        // Language section
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLang = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "fa" else "fa"
        val langOptions = listOf("fa", "en")
        val langLabels = listOf(R.string.persian, R.string.english)
        val selectedLangIndex = langOptions.indexOf(currentLang).coerceAtLeast(0)

        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.language),
            icon = Icons.Default.Translate
        ) {
            ExpressiveConnectedButtonGroup(
                itemsCount = langOptions.size,
                selectedIndex = selectedLangIndex,
                onSelect = { onLanguageSelected(langOptions[it]) }
            ) { index, isSelected ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (index == 0) Icons.Default.Language else Icons.Default.Translate,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(adaptiveDp(16f))
                    )
                    Spacer(modifier = Modifier.width(adaptiveDp(6f)))
                    Text(
                        text = androidx.compose.ui.res.stringResource(langLabels[index]),
                        fontSize = adaptiveSp(12f),
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // Number Digits System Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.digits_system_title),
            icon = Icons.Default.FormatListNumbered
        ) {
            val digitOptions = listOf("fa", "en", "ar")
            val digitLabels = listOf(R.string.digits_fa, R.string.digits_en, R.string.digits_ar)
            val selectedIndex = digitOptions.indexOf(digitType).coerceAtLeast(0)

            ExpressiveConnectedButtonGroup(
                itemsCount = digitOptions.size,
                selectedIndex = selectedIndex,
                onSelect = { onDigitTypeSelected(digitOptions[it]) }
            ) { index, isSelected ->
                Text(
                    text = androidx.compose.ui.res.stringResource(digitLabels[index]),
                    fontSize = adaptiveSp(12f),
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // Calendar System Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.calendar_title),
            icon = Icons.Default.DateRange
        ) {
            val calendarOptions = listOf("jalali", "gregorian")
            val calendarLabels = listOf(R.string.calendar_jalali, R.string.calendar_gregorian)
            val selectedIndex = calendarOptions.indexOf(calendarType).coerceAtLeast(0)

            ExpressiveConnectedButtonGroup(
                itemsCount = calendarOptions.size,
                selectedIndex = selectedIndex,
                onSelect = { onCalendarSelected(calendarOptions[it]) }
            ) { index, isSelected ->
                Text(
                    text = androidx.compose.ui.res.stringResource(calendarLabels[index]),
                    fontSize = adaptiveSp(12f),
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // Price Change Colors Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.trend_color_title),
            icon = Icons.Default.Palette
        ) {
            val colorOptions = listOf("standard", "inverted")
            val colorLabels = listOf(R.string.trend_color_standard, R.string.trend_color_inverted)
            val selectedIndex = colorOptions.indexOf(colorSchemeMode).coerceAtLeast(0)

            ExpressiveConnectedButtonGroup(
                itemsCount = colorOptions.size,
                selectedIndex = selectedIndex,
                onSelect = { onColorSchemeSelected(colorOptions[it]) }
            ) { index, isSelected ->
                Text(
                    text = androidx.compose.ui.res.stringResource(colorLabels[index]),
                    fontSize = adaptiveSp(12f),
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // App Theme Color Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.settings_color_title),
            icon = Icons.Default.Palette
        ) {
            val colorOptions = AppThemeColor.entries

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(adaptiveDp(16f))
            ) {
                colorOptions.forEach { colorOption ->
                    val isSelected = colorOption.name == colorSeedName

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onColorSeedSelected(colorOption.name) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(adaptiveDp(48f))
                                .clip(CircleShape)
                                .background(
                                    if (colorOption == com.mmdparsadev.cheghad.ui.theme.AppThemeColor.DEFAULT)
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary))
                                    else Brush.linearGradient(listOf(colorOption.seedColor, colorOption.seedColor))
                                )
                                .border(
                                    width = if (isSelected) adaptiveDp(3f) else adaptiveDp(1f),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(adaptiveDp(24f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(adaptiveDp(6f)))

                        Text(
                            text = androidx.compose.ui.res.stringResource(colorOption.stringRes),
                            fontSize = adaptiveSp(10f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // Theme section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.theme),
            icon = Icons.Default.BrightnessMedium
        ) {
            val themeOptions = listOf("system", "light", "dark")
            val themeLabels = listOf(R.string.system_default, R.string.light_mode, R.string.dark_mode)
            val themeIcons = listOf(Icons.Default.SettingsSuggest, Icons.Default.LightMode, Icons.Default.DarkMode)
            val selectedThemeIndex = themeOptions.indexOf(appThemeMode).coerceAtLeast(0)

            ExpressiveConnectedButtonGroup(
                itemsCount = themeOptions.size,
                selectedIndex = selectedThemeIndex,
                onSelect = { onThemeSelected(themeOptions[it]) }
            ) { index, isSelected ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = themeIcons[index],
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(adaptiveDp(16f))
                    )
                    Spacer(modifier = Modifier.width(adaptiveDp(6f)))
                    Text(
                        text = androidx.compose.ui.res.stringResource(themeLabels[index]),
                        fontSize = adaptiveSp(12f),
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // Lock Screen Widgets Section
        SettingsCard(
            title = stringResource(R.string.settings_lockscreen_widgets_title),
            icon = Icons.Default.Lock
        ) {
            Column {
                Text(
                    text = stringResource(R.string.settings_lockscreen_widgets_currency_label),
                    fontSize = adaptiveSp(13f),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = adaptiveDp(8f))
                )

                ExpressiveConnectedButtonGroup(
                    itemsCount = allCurrencies.size,
                    selectedIndex = allCurrencies.indexOfFirst { it.id == lockscreenWidgetCurrencyId }.coerceAtLeast(0),
                    onSelect = { onLockscreenWidgetCurrencySelected(allCurrencies[it].id) },
                    scrollable = true,
                    height = adaptiveDp(40f),
                    spacing = adaptiveDp(4f)
                ) { index, isSelected ->
                    val item = allCurrencies[index]
                    val title = getLocalizedTitle(item.symbol, item.title)
                    Text(
                        text = title,
                        fontSize = adaptiveSp(12f),
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontFamily = getFontFamilyForText(title)
                    )
                }

                Spacer(modifier = Modifier.height(adaptiveDp(16f)))

                Text(
                    text = stringResource(R.string.settings_lockscreen_widgets_theme_label),
                    fontSize = adaptiveSp(13f),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = adaptiveDp(8f))
                )

                val widgetThemeOptions = listOf("glassy", "dark", "light", "trend", "app_color")
                val widgetThemeLabels = listOf(
                    R.string.widget_theme_glassy,
                    R.string.widget_theme_dark,
                    R.string.widget_theme_light,
                    R.string.widget_theme_trend,
                    R.string.widget_theme_app_color
                )
                val selectedWidgetThemeIndex = widgetThemeOptions.indexOf(lockscreenWidgetTheme).coerceAtLeast(0)

                ExpressiveConnectedButtonGroup(
                    itemsCount = widgetThemeOptions.size,
                    selectedIndex = selectedWidgetThemeIndex,
                    onSelect = { onLockscreenWidgetThemeSelected(widgetThemeOptions[it]) }
                ) { index, isSelected ->
                    Text(
                        text = stringResource(widgetThemeLabels[index]),
                        fontSize = adaptiveSp(11f),
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(adaptiveDp(16f)))

        // Time Range Reordering section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.settings_time_ranges),
            subtitle = androidx.compose.ui.res.stringResource(R.string.reorder_ranges_help),
            icon = Icons.Default.Tune
        ) {
            var draggedIndex by remember { mutableStateOf<Int?>(null) }
            var dragOffsetY by remember { mutableStateOf(0f) }
            val itemHeightDp = adaptiveDp(52f)
            val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(adaptiveDp(24f)))
                    .padding(adaptiveDp(8f))
            ) {
                timeRangeOrder.forEachIndexed { index, range ->
                    val isDragging = draggedIndex == index

                    val density = LocalDensity.current
                    val shadowElevationPx = if (isDragging) with(density) { adaptiveDp(8f).toPx() } else 0f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffsetY else 0f
                                scaleX = if (isDragging) 1.03f else 1f
                                scaleY = if (isDragging) 1.03f else 1f
                                shadowElevation = shadowElevationPx
                            }
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(adaptiveDp(24f))
                            )
                            .border(
                                width = if (isDragging) adaptiveDp(1.5f) else adaptiveDp(1f),
                                color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(adaptiveDp(24f))
                            )
                            .padding(horizontal = adaptiveDp(12f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(adaptiveDp(36f))
                                .pointerInput(index) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggedIndex = index
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y

                                            val targetIndex = draggedIndex
                                            if (targetIndex != null) {
                                                val indexDiff = (dragOffsetY / itemHeightPx).roundToInt()
                                                val newIndex = (targetIndex + indexDiff).coerceIn(0, timeRangeOrder.lastIndex)
                                                if (newIndex != targetIndex) {
                                                    val mutable = timeRangeOrder.toMutableList()
                                                    val temp = mutable[targetIndex]
                                                    mutable.removeAt(targetIndex)
                                                    mutable.add(newIndex, temp)
                                                    onTimeRangeOrderChanged(mutable)

                                                    draggedIndex = newIndex
                                                    dragOffsetY -= (newIndex - targetIndex) * itemHeightPx
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggedIndex = null
                                            dragOffsetY = 0f
                                        },
                                        onDragCancel = {
                                            draggedIndex = null
                                            dragOffsetY = 0f
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reorder,
                                contentDescription = "Drag to reorder",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(adaptiveDp(12f)))

                        Text(
                            text = androidx.compose.ui.res.stringResource(range.stringRes),
                            fontSize = adaptiveSp(14f),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (index < timeRangeOrder.lastIndex) {
                        Spacer(modifier = Modifier.height(adaptiveDp(6f)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // News Categories Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.settings_news_categories_title),
            subtitle = androidx.compose.ui.res.stringResource(R.string.settings_news_categories_subtitle),
            icon = Icons.Default.Category
        ) {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_news_master_toggle),
                isChecked = newsEnabled,
                onCheckedChange = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onNewsEnabledChanged(it)
                }
            )

            Spacer(modifier = Modifier.height(adaptiveDp(8f)))

            val categories = listOf(
                com.mmdparsadev.cheghad.data.models.NewsCategory.Economic to R.string.news_category_economic,
                com.mmdparsadev.cheghad.data.models.NewsCategory.CurrencyGold to R.string.news_category_currency,
                com.mmdparsadev.cheghad.data.models.NewsCategory.Bourse to R.string.news_category_bourse,
                com.mmdparsadev.cheghad.data.models.NewsCategory.Crypto to R.string.news_category_crypto,
                com.mmdparsadev.cheghad.data.models.NewsCategory.World to R.string.news_category_world
            )

            categories.forEachIndexed { index, (cat, titleRes) ->
                val isChecked = !disabledNewsCategories.contains(cat.name)
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = adaptiveDp(24f), topEnd = adaptiveDp(24f), bottomStart = adaptiveDp(4f), bottomEnd = adaptiveDp(4f))
                    categories.lastIndex -> RoundedCornerShape(topStart = adaptiveDp(4f), topEnd = adaptiveDp(4f), bottomStart = adaptiveDp(24f), bottomEnd = adaptiveDp(24f))
                    else -> RoundedCornerShape(adaptiveDp(4f))
                }
                SettingsSwitchRow(
                    title = stringResource(titleRes),
                    isChecked = isChecked,
                    shape = shape,
                    onCheckedChange = { checked ->
                        HapticUtils.vibrate(context, HapticType.LIGHT)
                        val newSet = if (checked) disabledNewsCategories - cat.name else disabledNewsCategories + cat.name
                        onDisabledNewsCategoriesChanged(newSet)
                    }
                )
                if (index < categories.lastIndex) {
                    Spacer(modifier = Modifier.height(adaptiveDp(3f)))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // News Agencies Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.settings_news_agencies_title),
            subtitle = androidx.compose.ui.res.stringResource(R.string.settings_news_agencies_subtitle),
            icon = Icons.Default.Newspaper
        ) {
            Surface(
                onClick = { onShowAgenciesSheetChanged(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(adaptiveDp(24f))),
                shape = RoundedCornerShape(adaptiveDp(24f)),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(adaptiveDp(1f), MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = adaptiveDp(16f), vertical = adaptiveDp(12f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_manage_agencies),
                        fontSize = adaptiveSp(13f),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(adaptiveDp(14f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Check for Updates Section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.settings_update_check_title),
            subtitle = stringResource(R.string.version_label, BuildConfig.VERSION_NAME),
            icon = Icons.Default.SystemUpdate
        ) {
            Column {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_update_beta_title),
                    description = stringResource(R.string.settings_update_beta_subtitle),
                    isChecked = downloadBetaVersions,
                    onCheckedChange = onDownloadBetaVersionsChanged
                )

                Spacer(modifier = Modifier.height(adaptiveDp(16f)))

                Surface(
                    onClick = onCheckForUpdates,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCheckingUpdates) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isCheckingUpdates) stringResource(R.string.settings_update_checking) else stringResource(R.string.settings_update_check_btn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About & Version Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "چقد • Cheghad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.app_description),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        R.string.version_label,
                        com.mmdparsadev.cheghad.BuildConfig.VERSION_NAME
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        val uriHandler = LocalUriHandler.current

        Spacer(modifier = Modifier.height(adaptiveDp(24f)))

        // Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.settings_footer_made_by),
                fontSize = adaptiveSp(12f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(adaptiveDp(4f)))
            Text(
                text = stringResource(R.string.settings_footer_github),
                fontSize = adaptiveSp(11f),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://github.com/mmdparsa-dev/Cheghad")
                }
            )
        }

        Spacer(modifier = Modifier.height(adaptiveDp(48f)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveAssetChart(
    points: List<Double>,
    labels: List<String>,
    chartColor: Color,
    activeIndex: Int?,
    digitType: String = "fa",
    symbol: String? = null,
    onActiveIndexChanged: (Int?) -> Unit
) {
    val motionScheme = MaterialTheme.motionScheme
    val minPrice = points.minOrNull() ?: 0.0
    val maxPrice = points.maxOrNull() ?: 1.0
    val midPrice = (maxPrice + minPrice) / 2.0

    val deltaY = if (maxPrice == minPrice) 1.0 else maxPrice - minPrice

    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val verticalGridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryGlowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val primaryLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    var animationPlayed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val animationProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "ChartAnim"
    )
    androidx.compose.runtime.LaunchedEffect(points) {
        animationPlayed = false
        kotlinx.coroutines.delay(50)
        animationPlayed = true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        // Y-axis labels
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatPrice(maxPrice, digitType, symbol),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = formatPrice(midPrice, digitType, symbol),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = formatPrice(minPrice, digitType, symbol),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(18.dp)) // Reserve space for X-axis labels
        }

        // Main chart column
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Chart Canvas + Interaction Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(points) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val dragAmount = event.changes.firstOrNull()
                                if (dragAmount != null && dragAmount.pressed) {
                                    val x = dragAmount.position.x
                                    val width = size.width.toFloat()
                                    if (width > 0) {
                                        val idx = ((x / width) * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
                                        onActiveIndexChanged(idx)
                                    }
                                    dragAmount.consume()
                                } else {
                                    onActiveIndexChanged(null)
                                }
                            }
                        }
                    }
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw horizontal grid lines
                    val gridLinesCount = 3
                    for (i in 0 until gridLinesCount) {
                        val y = (i.toFloat() / (gridLinesCount - 1)) * height
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Draw vertical grid lines at start, middle, and end if enough points
                    if (points.size >= 2) {
                        val xIndices = listOf(0, points.size / 2, points.size - 1)
                        xIndices.forEach { idx ->
                            val x = (idx.toFloat() / (points.size - 1)) * width
                            drawLine(
                                color = verticalGridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }

                    // Build path
                    if (points.size >= 2) {
                        val path = androidx.compose.ui.graphics.Path()
                        points.forEachIndexed { i, price ->
                            val x = (i.toDouble() / (points.size - 1)) * width
                            val normalizedY = (price - minPrice) / deltaY
                            val finalY = (1.0 - normalizedY) * height
                            val y = finalY * animationProgress + height * (1 - animationProgress)

                            if (i == 0) {
                                path.moveTo(x.toFloat(), y.toFloat())
                            } else {
                                path.lineTo(x.toFloat(), y.toFloat())
                            }
                        }

                        // Draw gradient fill under curve
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(path)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(chartColor.copy(alpha = 0.25f * animationProgress), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw actual curve line
                        drawPath(
                            path = path,
                            color = chartColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }

                    // Draw interactive scrubbing overlay elements
                    if (activeIndex != null && activeIndex < points.size) {
                        val activeIdx = activeIndex
                        val x = (activeIdx.toDouble() / (points.size - 1)) * width
                        val y = (1.0 - (points[activeIdx] - minPrice) / deltaY) * height

                        // Highlight vertical line
                        drawLine(
                            color = primaryLineColor,
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        // Outer glowing circle
                        drawCircle(
                            color = primaryGlowColor,
                            radius = 9.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )

                        // Inner solid circle
                        drawCircle(
                            color = primaryColor,
                            radius = 4.5.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // X-axis labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Show labels at start, middle, and end
                if (labels.isNotEmpty()) {
                    Text(labels.first(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                    if (labels.size >= 3) {
                        Text(labels[labels.size / 2], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                    }
                    Text(labels.last(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailDialog(
    item: com.mmdparsadev.cheghad.data.models.CurrencyItem,
    timeRangeOrder: List<TimeRange>,
    historyPoints: List<Double>,
    isHistoryLoading: Boolean,
    calendarType: String = "jalali",
    colorSchemeMode: String = "standard",
    digitType: String = "fa",
    onFetchHistory: (TimeRange) -> Unit,
    onDismiss: () -> Unit,
    onSaveAlarm: (targetPrice: Double, isAbove: Boolean) -> Unit
) {
    val context = LocalContext.current
    val motionScheme = MaterialTheme.motionScheme
    var targetPriceStr by remember { mutableStateOf(formatTargetPrice(item.currentPrice)) }
    var isAbove by remember { mutableStateOf(true) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.DAY) }

    LaunchedEffect(item.symbol, selectedTimeRange) {
        onFetchHistory(selectedTimeRange)
    }

    val points = if (historyPoints.isNotEmpty()) historyPoints else listOf(item.currentPrice, item.currentPrice)

    val isJalali = calendarType == "jalali"
    val isEnglish = Locale.getDefault().language == "en"

    val daysOfWeek = if (isJalali) {
        if (isEnglish) listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
        else listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    } else if (isEnglish) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    } else {
        listOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
    }

    val monthNames = if (isJalali) {
        if (isEnglish) listOf("Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar", "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand")
        else listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
    } else if (isEnglish) {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    } else {
        listOf("ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن", "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر")
    }

    val labels = remember(item.symbol, points.size, selectedTimeRange, calendarType) {
        val list = mutableListOf<String>()
        val count = points.size
        if (count == 0) return@remember list

        val nowMs = System.currentTimeMillis()
        val durationMs = when (selectedTimeRange) {
            TimeRange.HOUR -> 3600_000L
            TimeRange.DAY -> 86400_000L
            TimeRange.WEEK -> 7L * 86400_000L
            TimeRange.MONTH -> 30L * 86400_000L
            TimeRange.YEAR -> 365L * 86400_000L
        }

        val cal = java.util.Calendar.getInstance()

        for (i in 0 until count) {
            val pointTime = if (count <= 1) nowMs else nowMs - durationMs * (count - 1 - i) / (count - 1)
            cal.timeInMillis = pointTime

            when (selectedTimeRange) {
                TimeRange.HOUR -> {
                    list.add(String.format(Locale.US, "%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)))
                }
                TimeRange.DAY -> {
                    list.add(String.format(Locale.US, "%02d:00", cal.get(java.util.Calendar.HOUR_OF_DAY)))
                }
                TimeRange.WEEK -> {
                    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                    val dayIndex = if (dayOfWeek == java.util.Calendar.SATURDAY) 0 else dayOfWeek
                    list.add(daysOfWeek.getOrElse(dayIndex % 7) { "" })
                }
                TimeRange.MONTH -> {
                    if (isJalali) {
                        try {
                            val uLocale = android.icu.util.ULocale("fa_IR@calendar=persian")
                            val pCal = android.icu.util.Calendar.getInstance(uLocale)
                            pCal.timeInMillis = pointTime
                            list.add("${pCal.get(android.icu.util.Calendar.DAY_OF_MONTH)}")
                        } catch (e: Exception) {
                            list.add("${cal.get(java.util.Calendar.DAY_OF_MONTH)}")
                        }
                    } else {
                        list.add("${cal.get(java.util.Calendar.DAY_OF_MONTH)}")
                    }
                }
                TimeRange.YEAR -> {
                    if (isJalali) {
                        try {
                            val uLocale = android.icu.util.ULocale("fa_IR@calendar=persian")
                            val pCal = android.icu.util.Calendar.getInstance(uLocale)
                            pCal.timeInMillis = pointTime
                            val m = pCal.get(android.icu.util.Calendar.MONTH)
                            list.add(monthNames.getOrElse(m) { "" })
                        } catch (e: Exception) {
                            val m = cal.get(java.util.Calendar.MONTH)
                            list.add(monthNames.getOrElse(m) { "" })
                        }
                    } else {
                        val m = cal.get(java.util.Calendar.MONTH)
                        list.add(monthNames.getOrElse(m) { "" })
                    }
                }
            }
        }
        list
    }

    var activeIndex by remember { mutableStateOf<Int?>(null) }

    val displayPrice = if (activeIndex != null && activeIndex!! < points.size) points[activeIndex!!] else item.currentPrice
    val displayLabel = if (activeIndex != null && activeIndex!! < labels.size) {
        val timePrefix = if (isEnglish) {
            when(selectedTimeRange) {
                TimeRange.HOUR, TimeRange.DAY -> "at "
                TimeRange.WEEK -> "on "
                TimeRange.MONTH -> "on date "
                TimeRange.YEAR -> "in "
            }
        } else {
            when(selectedTimeRange) {
                TimeRange.HOUR, TimeRange.DAY -> "ساعت "
                TimeRange.WEEK -> "روز "
                TimeRange.MONTH -> "تاریخ "
                TimeRange.YEAR -> "ماه "
            }
        }
        if (isEnglish) "Price $timePrefix${labels[activeIndex!!]}" else "قیمت در $timePrefix${labels[activeIndex!!]}"
    } else {
        if (isEnglish) "Current Price" else "قیمت فعلی"
    }

    val formattedDisplayPrice = formatPrice(displayPrice, digitType, item.symbol)

    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = adaptiveDp(28f), topEnd = adaptiveDp(28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = adaptiveDp(24f), vertical = adaptiveDp(8f))
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(adaptiveDp(48f))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.symbol.take(3),
                            fontSize = adaptiveSp(12f),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(adaptiveDp(12f)))
                    Column {
                        Text(getLocalizedTitle(item.symbol, item.title), fontSize = adaptiveSp(16f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(item.symbol, fontSize = adaptiveSp(12f), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(adaptiveDp(16f)))

            // Price section
            val upColor = if (colorSchemeMode == "inverted") MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
            val downColor = if (colorSchemeMode == "inverted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    androidx.compose.animation.AnimatedContent(
                        targetState = displayLabel.toLocalizedDigits(digitType),
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(animationSpec = motionScheme.defaultEffectsSpec()) +
                                    slideInVertically(animationSpec = motionScheme.defaultEffectsSpec()) { height -> height / 2 })
                                .togetherWith(
                                    fadeOut(animationSpec = motionScheme.fastEffectsSpec()) +
                                            slideOutVertically(animationSpec = motionScheme.fastEffectsSpec()) { height -> -height / 2 }
                                )
                        },
                        label = "DialogLabelAnim"
                    ) { localizedLabel ->
                        Text(
                            text = localizedLabel,
                            fontSize = adaptiveSp(12f),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(adaptiveDp(4f)))
                    androidx.compose.animation.AnimatedContent(
                        targetState = formattedDisplayPrice,
                        transitionSpec = {
                            androidx.compose.animation.fadeIn(motionScheme.defaultEffectsSpec()).togetherWith(
                                androidx.compose.animation.fadeOut(motionScheme.defaultEffectsSpec())
                            )
                        },
                        label = "DialogPriceAnim"
                    ) { priceStr ->
                        Text(
                            text = stringResource(R.string.currency_toman) + " " + priceStr,
                            fontSize = adaptiveSp(22f),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                val isZeroChange = Math.abs(item.changePercentage) < 0.001
                val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                val isNegative = item.changePercentage < 0
                val changeColor = if (isZeroChange) {
                    if (isDark) Color.LightGray else Color.Gray
                } else if (isNegative) downColor else upColor
                AnimatedContent(
                    targetState = formatPercent(item.changePercentage, digitType),
                    label = "DialogPercentAnim"
                ) { pctStr ->
                    Text(
                        text = pctStr,
                        fontSize = adaptiveSp(14f),
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(adaptiveDp(16f)))

            // Chart Canvas
            val isZeroChange = Math.abs(item.changePercentage) < 0.001
            val isDark = MaterialTheme.colorScheme.background.red < 0.5f
            val chartColor = if (isZeroChange) {
                if (isDark) Color.LightGray else Color.Gray
            } else if (item.changePercentage >= 0) upColor else downColor

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adaptiveDp(200f)),
                contentAlignment = Alignment.Center
            ) {
                InteractiveAssetChart(
                    points = points,
                    labels = labels,
                    chartColor = chartColor,
                    activeIndex = activeIndex,
                    digitType = digitType,
                    symbol = item.symbol,
                    onActiveIndexChanged = { activeIndex = it }
                )

                if (isHistoryLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(adaptiveDp(24f))),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(adaptiveDp(32f)),
                            strokeWidth = adaptiveDp(3f),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(adaptiveDp(16f)))

            // Time Range Buttons
            val selectedTimeRangeIndex = timeRangeOrder.indexOf(selectedTimeRange).coerceAtLeast(0)
            ExpressiveConnectedButtonGroup(
                itemsCount = timeRangeOrder.size,
                selectedIndex = selectedTimeRangeIndex,
                onSelect = { selectedTimeRange = timeRangeOrder[it] }
            ) { index, isSelected ->
                Text(
                    text = androidx.compose.ui.res.stringResource(timeRangeOrder[index].stringRes),
                    fontSize = adaptiveSp(11f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(adaptiveDp(24f)))

            // Alarm Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(adaptiveDp(18f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(adaptiveDp(1f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(adaptiveDp(16f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(adaptiveDp(20f))
                        )
                        Spacer(modifier = Modifier.width(adaptiveDp(8f)))
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.alarm_create_title),
                            fontSize = adaptiveSp(14f),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(adaptiveDp(12f)))

                    // Price Input
                    OutlinedTextField(
                        value = targetPriceStr,
                        onValueChange = { targetPriceStr = it },
                        label = { Text(androidx.compose.ui.res.stringResource(R.string.alarm_target_price)) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(adaptiveDp(24f))
                    )

                    Spacer(modifier = Modifier.height(adaptiveDp(12f)))

                    // Condition Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(adaptiveDp(8f))
                    ) {
                        ConditionCard(
                            text = androidx.compose.ui.res.stringResource(R.string.alarm_condition_above),
                            isSelected = isAbove,
                            onClick = { isAbove = true },
                            modifier = Modifier.weight(1f)
                        )
                        ConditionCard(
                            text = androidx.compose.ui.res.stringResource(R.string.alarm_condition_below),
                            isSelected = !isAbove,
                            onClick = { isAbove = false },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(adaptiveDp(16f)))

                    // Save button
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            val price = parseTargetPrice(targetPriceStr)
                            if (price == null || price <= 0.0) {
                                HapticUtils.vibrate(context, HapticType.ERROR)
                                Toast.makeText(context, context.getString(R.string.alarm_invalid_price), Toast.LENGTH_SHORT).show()
                            } else {
                                HapticUtils.vibrate(context, HapticType.SUCCESS)
                                onSaveAlarm(price, isAbove)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(adaptiveDp(56f)),
                        shape = RoundedCornerShape(adaptiveDp(20f))
                    ) {
                        Text(androidx.compose.ui.res.stringResource(R.string.alarm_button_save))
                    }
                }
            }

            Spacer(modifier = Modifier.height(adaptiveDp(16f)))
        }
    }
}

@Composable
fun EditAlarmDialog(
    alarm: com.mmdparsadev.cheghad.data.models.AlarmEntity,
    onDismiss: () -> Unit,
    onSaveAlarm: (updatedAlarm: com.mmdparsadev.cheghad.data.models.AlarmEntity) -> Unit
) {
    val context = LocalContext.current
    var targetPriceStr by remember { mutableStateOf(formatTargetPrice(alarm.targetPrice)) }
    var isAbove by remember { mutableStateOf(alarm.isAbove) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.edit_alarm_title, getLocalizedTitle(alarm.symbol, alarm.title)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = {
                        HapticUtils.vibrate(context, HapticType.LIGHT)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = targetPriceStr,
                    onValueChange = { targetPriceStr = it },
                    label = { Text(androidx.compose.ui.res.stringResource(R.string.alarm_target_price)) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConditionCard(
                        text = androidx.compose.ui.res.stringResource(R.string.alarm_condition_above),
                        isSelected = isAbove,
                        onClick = { isAbove = true },
                        modifier = Modifier.weight(1f)
                    )
                    ConditionCard(
                        text = androidx.compose.ui.res.stringResource(R.string.alarm_condition_below),
                        isSelected = !isAbove,
                        onClick = { isAbove = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = {
                        val price = parseTargetPrice(targetPriceStr)
                        if (price == null || price <= 0.0) {
                            Toast.makeText(context, context.getString(R.string.alarm_invalid_price), Toast.LENGTH_SHORT).show()
                        } else {
                            onSaveAlarm(alarm.copy(targetPrice = price, isAbove = isAbove, isActive = true))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(R.string.save_changes))
                }
            }
        }
    }
}

@Composable
fun ConditionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AlarmsScreen(
    alarms: List<com.mmdparsadev.cheghad.data.models.AlarmEntity>,
    innerPadding: PaddingValues,
    colorSchemeMode: String = "standard",
    digitType: String = "fa",
    onDeleteAlarm: (com.mmdparsadev.cheghad.data.models.AlarmEntity) -> Unit,
    onEditAlarm: (com.mmdparsadev.cheghad.data.models.AlarmEntity) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.nav_portfolio),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.alerts_subtitle),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.alarms_empty_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.alarms_empty_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = alarms.size,
                    key = { index -> alarms[index].id }
                ) { index ->
                    val alarm = alarms[index]
                    AlarmItemCard(
                        modifier = Modifier.animateItem(),
                        alarm = alarm,
                        digitType = digitType,
                        onDelete = { onDeleteAlarm(alarm) },
                        onEdit = { onEditAlarm(alarm) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun AlarmItemCard(
    modifier: Modifier = Modifier,
    alarm: com.mmdparsadev.cheghad.data.models.AlarmEntity,
    digitType: String = "fa",
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val formattedPrice = formatPrice(alarm.targetPrice, digitType, alarm.symbol)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = alarm.symbol.take(3),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getLocalizedTitle(alarm.symbol, alarm.title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${alarm.symbol})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (alarm.isAbove) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (alarm.isAbove) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val conditionText = if (alarm.isAbove) {
                        androidx.compose.ui.res.stringResource(R.string.alarm_condition_above)
                    } else {
                        androidx.compose.ui.res.stringResource(R.string.alarm_condition_below)
                    }
                    val tomanUnit = androidx.compose.ui.res.stringResource(R.string.currency_toman)
                    Text(
                        text = "$conditionText: $formattedPrice $tomanUnit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Status indicator
                val statusText = if (alarm.isActive) {
                    androidx.compose.ui.res.stringResource(R.string.alarm_active)
                } else {
                    androidx.compose.ui.res.stringResource(R.string.alarm_inactive)
                }
                val statusColor = if (alarm.isActive) Color(0xFF4CAF50) else Color.Gray

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Edit button
                IconButton(onClick = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onEdit()
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Alarm",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                IconButton(onClick = {
                    HapticUtils.vibrate(context, HapticType.LIGHT)
                    onDelete()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Serializable
data class CalculationHistoryItem(
    val assetSymbol: String,
    val amount: String,
    val result: String,
    val mode: Int, // 0: Asset to Toman, 1: Toman to Asset
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CurrencyCalculatorScreen(
    items: List<CurrencyItem>,
    digitType: String,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    var calcSelectedItemId by rememberSaveable { mutableStateOf(items.firstOrNull()?.id ?: "") }
    val selectedItem = items.find { it.id == calcSelectedItemId } ?: items.firstOrNull()

    var quantityInput by rememberSaveable { mutableStateOf("1") }
    var tomanInput by rememberSaveable { mutableStateOf("") }
    var calculationMode by rememberSaveable { mutableStateOf(0) } // 0: Asset to Toman, 1: Toman to Asset

    var historyJson by rememberSaveable { mutableStateOf("[]") }
    val historyList = remember(historyJson) {
        try {
            Json.decodeFromString<List<CalculationHistoryItem>>(historyJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val quantity = quantityInput.toDoubleOrNull() ?: 0.0
    val currentPrice = selectedItem?.currentPrice ?: 0.0
    val totalToman = quantity * currentPrice

    val tomanEntered = tomanInput.toDoubleOrNull() ?: 0.0

    val addToHistory: (String, String, String, Int) -> Unit = { symbol, amount, result, mode ->
        val newItem = CalculationHistoryItem(symbol, amount, result, mode)
        val current = historyList.toMutableList()
        current.add(0, newItem)
        if (current.size > 20) current.removeAt(current.size - 1)
        historyJson = Json.encodeToString(current)
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
    var showHistorySheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.calc_title),
                    fontSize = adaptiveSp(20f),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.calc_subtitle),
                    fontSize = adaptiveSp(13f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // History Capsule
            val latestItem = historyList.firstOrNull()
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .clickable { showHistorySheet = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    AnimatedContent(
                        targetState = latestItem,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut()
                            )
                        },
                        label = "HistoryCapsuleAnim"
                    ) { item ->
                        if (item != null) {
                            val displayText = if (item.mode == 0)
                                "${item.amount} ${item.assetSymbol}"
                            else
                                formatPrice(item.amount.toDoubleOrNull() ?: 0.0, digitType)
                            Text(
                                text = displayText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 80.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.history_title),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.history_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (historyList.isNotEmpty()) {
                            TextButton(onClick = {
                                HapticUtils.vibrate(context, HapticType.LIGHT)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showHistorySheet = false
                                        historyJson = "[]"
                                    }
                                }
                            }) {
                                Text(stringResource(R.string.clear_history), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (historyList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.history_empty),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(historyList) { historyItem ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (historyItem.mode == 0)
                                                    "${historyItem.amount} ${historyItem.assetSymbol}"
                                                else
                                                    "${formatPrice(historyItem.amount.toDoubleOrNull() ?: 0.0, digitType)} ${stringResource(R.string.currency_toman)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()).format(
                                                    Date(historyItem.timestamp)
                                                ),
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp).padding(horizontal = 8.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = if (historyItem.mode == 0)
                                                "${historyItem.result} ${stringResource(R.string.currency_toman)}"
                                            else
                                                historyItem.result,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mode Selector Tabs (Connected Button Group)
        val modeLabels = listOf(
            stringResource(R.string.calc_mode_asset_to_toman),
            stringResource(R.string.calc_mode_toman_to_asset)
        )
        ExpressiveConnectedButtonGroup(
            itemsCount = modeLabels.size,
            selectedIndex = calculationMode,
            onSelect = { calculationMode = it },
            height = 48.dp
        ) { index, isSelected ->
            Text(
                text = modeLabels[index],
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }

        AnimatedContent(
            targetState = calculationMode,
            transitionSpec = {
                val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> (width * 0.1f).toInt() } + fadeIn(animationSpec = tween(300))).togetherWith(
                        slideOutHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> -(width * 0.1f).toInt() } + fadeOut(animationSpec = tween(150))
                    )
                } else {
                    (slideInHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> -(width * 0.1f).toInt() } + fadeIn(animationSpec = tween(300))).togetherWith(
                        slideOutHorizontally(animationSpec = tween(300, easing = emphasizedEasing)) { width -> (width * 0.1f).toInt() } + fadeOut(animationSpec = tween(150))
                    )
                }.using(
                    SizeTransform(clip = false)
                )
            },
            label = "CalculatorModeTransition"
        ) { mode ->
            if (mode == 0) {
                // Asset to Toman Converter
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.calc_select_asset),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        ExpressiveConnectedButtonGroup(
                            itemsCount = items.size,
                            selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0),
                            onSelect = { index -> calcSelectedItemId = items[index].id },
                            scrollable = true,
                            height = 44.dp,
                            spacing = 4.dp
                        ) { index, isSelected ->
                            val item = items[index]
                            val itemTitle = getLocalizedTitle(item.symbol, item.title)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.symbol.take(2),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = itemTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontFamily = getFontFamilyForText(itemTitle)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it },
                            label = { Text("${stringResource(R.string.calc_amount)} (${selectedItem?.symbol ?: ""})") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Result Box (Premium look)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.calc_total_value),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AnimatedContent(
                                        targetState = totalToman,
                                        transitionSpec = {
                                            (slideInVertically { it } + fadeIn()).togetherWith(
                                                slideOutVertically { -it } + fadeOut())
                                        },
                                        label = "ResultAnimation"
                                    ) { value ->
                                        Text(
                                            text = "${formatPrice(value, digitType)} ${stringResource(R.string.currency_toman)}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        if (quantityInput.isNotEmpty()) {
                                            HapticUtils.vibrate(context, HapticType.MEDIUM)
                                            addToHistory(
                                                selectedItem?.symbol ?: "",
                                                quantityInput,
                                                formatPrice(totalToman, digitType),
                                                0
                                            )
                                        }
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Save")
                                }
                            }
                        }
                    }
                }
            } else {
                // Toman to all Assets Converter
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.calc_toman_amount),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = tomanInput,
                            onValueChange = { tomanInput = it },
                            label = { Text(stringResource(R.string.calc_toman_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            trailingIcon = {
                                if (tomanInput.isNotEmpty()) {
                                    IconButton(onClick = {
                                        HapticUtils.vibrate(context, HapticType.MEDIUM)
                                        addToHistory("", tomanInput, tomanInput, 1)
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Save")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.calc_equivalents),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        items.forEach { item ->
                            val calculatedAmount = if (item.currentPrice > 0) tomanEntered / item.currentPrice else 0.0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            item.symbol.take(3),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                AnimatedContent(
                                    targetState = calculatedAmount,
                                    transitionSpec = {
                                        (slideInVertically { it } + fadeIn()).togetherWith(
                                            slideOutVertically { -it } + fadeOut())
                                    },
                                    label = "EquivalentAnimation"
                                ) { amount ->
                                    Text(
                                        text = String.format(Locale.US, "%.4f", amount).toLocalizedDigits(digitType) + " " + item.symbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (historyList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.history_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = {
                                    HapticUtils.vibrate(context, HapticType.MEDIUM)
                                    historyJson = "[]"
                                }) {
                                    Text(stringResource(R.string.clear_history), fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            historyList.forEach { historyItem ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = if (historyItem.mode == 0)
                                                    "${historyItem.amount} ${historyItem.assetSymbol}"
                                                else
                                                    "${formatPrice(historyItem.amount.toDoubleOrNull() ?: 0.0, digitType)} ${stringResource(R.string.currency_toman)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                                    Date(historyItem.timestamp)
                                                ),
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = if (historyItem.mode == 0)
                                                "${historyItem.result} ${stringResource(R.string.currency_toman)}"
                                            else
                                                historyItem.result,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun parseTargetPrice(priceStr: String): Double? {
    var str = priceStr.replace(",", "").replace("،", "").replace("٫", ".")
    val persianNumbers = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    for (i in persianNumbers.indices) {
        str = str.replace(persianNumbers[i], i.toString())
    }
    return str.toDoubleOrNull()
}
