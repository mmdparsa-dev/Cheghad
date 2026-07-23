package com.mmdparsadev.cheghad

import com.mmdparsadev.cheghad.ui.ExpressiveConnectedButtonGroup
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults

import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mmdparsadev.cheghad.data.api.ApiClient
import com.mmdparsadev.cheghad.data.repository.CurrencyRepository
import com.mmdparsadev.cheghad.data.models.*
import com.mmdparsadev.cheghad.ui.theme.*
import com.mmdparsadev.cheghad.ui.viewmodel.CurrencyUiState
import com.mmdparsadev.cheghad.ui.viewmodel.CurrencyViewModel
import com.mmdparsadev.cheghad.worker.CurrencySyncWorker
import java.util.concurrent.TimeUnit

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import java.text.NumberFormat
import java.util.Locale


enum class TimeRange(val stringRes: Int, val id: String) {
    HOUR(R.string.range_hour, "HOUR"),
    DAY(R.string.range_day, "DAY"),
    WEEK(R.string.range_week, "WEEK"),
    MONTH(R.string.range_month, "MONTH"),
    YEAR(R.string.range_year, "YEAR")
}

class MainActivity : AppCompatActivity() {

    private val ViewModel: CurrencyViewModel by viewModels {
        val database = com.mmdparsadev.cheghad.data.database.AppDatabase.getDatabase(applicationContext)
        val alarmRepository = com.mmdparsadev.cheghad.data.repository.AlarmRepository(database.alarmDao())
        CurrencyViewModel.ProvideFactory(
            CurrencyRepository(ApiClient.CheghadApiService, ApiClient.KifpoolApiService, database.currencyDao()),
            alarmRepository,
            applicationContext
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        SetupBackgroundSync()
        
        val sharedPrefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        
        setContent {
            var isFirstLaunch by remember { mutableStateOf(sharedPrefs.getBoolean("first_launch", true)) }
            var currentScreen by remember { mutableStateOf("home") }

            val UiState by ViewModel.UiState.collectAsState()
            val Context = LocalContext.current
            var isEditingHome by remember { mutableStateOf(false) }
            var bottomListSortOrder by remember { mutableStateOf("default") } // "default", "profitable", "loss-making"
            
            var selectedItemForDetail by remember { mutableStateOf<com.mmdparsadev.cheghad.data.models.CurrencyItem?>(null) }
            var selectedAlarmForEdit by remember { mutableStateOf<com.mmdparsadev.cheghad.data.models.AlarmEntity?>(null) }

            LaunchedEffect(Unit) {
                ViewModel.triggeredAlarmFlow.collect { (alarm, currentPrice) ->
                    val directionText = if (alarm.isAbove) "بالاتر از" else "پایین‌تر از"
                    val message = "قیمت ${alarm.title} (${alarm.symbol}) به $directionText ${alarm.targetPrice} تومان رسید (قیمت فعلی: $currentPrice)"
                    
                    try {
                        val channelId = "price_alerts_channel"
                        val channelName = "Price Alerts"
                        
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            val channel = android.app.NotificationChannel(
                                channelId,
                                channelName,
                                android.app.NotificationManager.IMPORTANCE_HIGH
                            ).apply {
                                description = "Channel for asset price alerts"
                            }
                            val notificationManager = Context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            notificationManager.createNotificationChannel(channel)
                        }

                        val notificationManager = Context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(Context, channelId)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle(Context.getString(R.string.alarm_triggered_title))
                            .setContentText(message)
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true)
                        
                        notificationManager.notify(alarm.id.toInt(), notificationBuilder.build())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    Toast.makeText(Context, message, Toast.LENGTH_LONG).show()
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
                        .mapNotNull { id -> TimeRange.values().find { it.id == id } }
                        .let { if (it.isEmpty()) listOf(TimeRange.DAY, TimeRange.WEEK, TimeRange.MONTH, TimeRange.YEAR) else it }
                )
            }
            
            var appThemeMode by remember { 
                mutableStateOf(sharedPrefs.getString("app_theme_mode", "system") ?: "system") 
            }

            var calendarType by remember {
                mutableStateOf(sharedPrefs.getString("calendar_type", "jalali") ?: "jalali")
            }

            var colorSchemeMode by remember {
                mutableStateOf(sharedPrefs.getString("color_scheme_mode", "standard") ?: "standard")
            }

            val currentAppLocale = AppCompatDelegate.getApplicationLocales().get(0) ?: java.util.Locale.getDefault()
            val isEnglishLocale = currentAppLocale.language.startsWith("en", ignoreCase = true)
            val defaultDigitType = if (isEnglishLocale) "en" else "fa"

            var digitType by remember {
                mutableStateOf(sharedPrefs.getString("digit_type", null) ?: defaultDigitType)
            }

            var disabledNewsCategories by remember {
                mutableStateOf(sharedPrefs.getStringSet("disabled_news_categories", emptySet()) ?: emptySet())
            }

            var disabledNewsAgencies by remember {
                mutableStateOf(sharedPrefs.getStringSet("disabled_news_agencies", emptySet()) ?: emptySet())
            }

            LaunchedEffect(UiState.ShowSuccessMessage) {
                if (UiState.ShowSuccessMessage) {
                    Toast.makeText(Context, Context.getString(R.string.success_updated), Toast.LENGTH_SHORT).show()
                    ViewModel.ClearSuccessMessage()
                }
            }

            LaunchedEffect(UiState.ErrorMessageResId) {
                UiState.ErrorMessageResId?.let { ErrorId ->
                    Toast.makeText(Context, Context.getString(ErrorId), Toast.LENGTH_LONG).show()
                    ViewModel.ClearErrorMessage()
                }
            }

            val homeScrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()

            MyApplicationTheme(themeMode = appThemeMode) {
                if (isFirstLaunch) {
                    WelcomeScreen(
                        onComplete = { langCode, theme ->
                            val newDigitType = if (langCode == "en") "en" else "fa"
                            digitType = newDigitType
                            sharedPrefs.edit().putString("digit_type", newDigitType).apply()
                            sharedPrefs.edit().putString("theme", theme).apply()
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
                            sharedPrefs.edit().putBoolean("first_launch", false).apply()
                            isFirstLaunch = false
                        }
                    )
                } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = { BottomNavigationBar(currentScreen = currentScreen, onScreenSelected = { currentScreen = it }) }
                ) { innerPadding ->
                    androidx.compose.animation.AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(400)) +
                            androidx.compose.animation.slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth / 4 })) togetherWith
                            (androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(400)) +
                            androidx.compose.animation.slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth / 4 }))
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                    if (screen == "home") {
                        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                            isRefreshing = UiState.IsLoading,
                            onRefresh = { ViewModel.RefreshData() },
                            modifier = Modifier.fillMaxSize().padding(innerPadding)
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(homeScrollState)
                        ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TopAppBar(
                            UiState = UiState,
                            isEditingHome = isEditingHome,
                            calendarType = calendarType,
                            digitType = digitType,
                            OnRefresh = { ViewModel.RefreshData() },
                            OnEditHome = { isEditingHome = !isEditingHome }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BentoGrid(
                            Items = UiState.Items,
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
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        CategoryChips(
                            selectedCategory = UiState.SelectedCategory,
                            onCategorySelected = { cat ->
                                ViewModel.SetCategory(cat)
                                coroutineScope.launch {
                                    homeScrollState.animateScrollTo(500)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AnimatedContent(
                            targetState = UiState.SelectedCategory,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                                 slideInVertically(animationSpec = tween(350, easing = FastOutSlowInEasing), initialOffsetY = { 30 }))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200, easing = FastOutLinearInEasing))
                                )
                            },
                            label = "CategoryTransition"
                        ) { selectedCat ->
                            val categoryTitleRes = when(selectedCat) {
                                "currency" -> R.string.category_currency
                                "gold_and_coin" -> R.string.category_gold_and_coin
                                "crypto" -> R.string.category_crypto
                                else -> R.string.all_markets
                            }
                            val categoryFilter = when (selectedCat) {
                                "currency" -> com.mmdparsadev.cheghad.data.models.CurrencyType.Currency
                                "gold_and_coin" -> com.mmdparsadev.cheghad.data.models.CurrencyType.GoldAndCoin
                                "crypto" -> com.mmdparsadev.cheghad.data.models.CurrencyType.Crypto
                                else -> null
                            }
                            val filteredItems = if (categoryFilter == null) UiState.Items else UiState.Items.filter { it.Category == categoryFilter }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(categoryTitleRes),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(categoryTitleRes))
                                    )
                                    
                                    var isSortMenuExpanded by remember { mutableStateOf(false) }
                                    val currentSortStringRes = when (bottomListSortOrder) {
                                        "profitable" -> R.string.sort_profitable
                                        "loss-making" -> R.string.sort_loss_making
                                        else -> R.string.sort_default
                                    }
                                    val currentSortText = androidx.compose.ui.res.stringResource(currentSortStringRes)
                                    val rotationAngle by animateFloatAsState(
                                        targetValue = if (isSortMenuExpanded) 180f else 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "SortArrowRotation"
                                    )

                                    Box {
                                        Surface(
                                            onClick = { isSortMenuExpanded = true },
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            tonalElevation = 2.dp,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                                    contentDescription = "Sort",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = currentSortText,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontFamily = getFontFamilyForText(currentSortText)
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                    "loss-making" to R.string.sort_loss_making
                                                ).forEach { (mode, stringRes) ->
                                                    val isSelected = bottomListSortOrder == mode
                                                    val itemText = androidx.compose.ui.res.stringResource(stringRes)
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
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val sortedItems = when (bottomListSortOrder) {
                                    "profitable" -> filteredItems.sortedByDescending { it.ChangePercentage }
                                    "loss-making" -> filteredItems.sortedBy { it.ChangePercentage }
                                    else -> filteredItems
                                }
                                
                                sortedItems.forEach { item ->
                                    AssetListItem(
                                        item = item,
                                        colorSchemeMode = colorSchemeMode,
                                        digitType = digitType,
                                        onClick = { selectedItemForDetail = item },
                                        onLongClick = { ViewModel.HideCurrencyForItem(item.Id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                        }
                        }
                    } else if (screen == "news") {
                        com.mmdparsadev.cheghad.ui.NewsScreen(
                            innerPadding = innerPadding,
                            digitType = digitType,
                            newsArticles = UiState.NewsArticles,
                            isRefreshing = UiState.IsNewsLoading,
                            onRefresh = { ViewModel.FetchNews() },
                            disabledCategories = disabledNewsCategories,
                            disabledAgencies = disabledNewsAgencies
                        )
                    } else if (screen == "calculator") {
                        CurrencyCalculatorScreen(
                            items = UiState.Items,
                            digitType = digitType,
                            innerPadding = innerPadding
                        )
                    } else if (screen == "settings") {
                        SettingsScreen(
                            innerPadding = innerPadding,
                            onLanguageSelected = { langCode ->
                                val newDigitType = if (langCode == "en") "en" else "fa"
                                digitType = newDigitType
                                sharedPrefs.edit().putString("digit_type", newDigitType).apply()
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
                            },
                            appThemeMode = appThemeMode,
                            onThemeSelected = { selectedTheme ->
                                appThemeMode = selectedTheme
                                sharedPrefs.edit().putString("app_theme_mode", selectedTheme).apply()
                            },
                            calendarType = calendarType,
                            onCalendarSelected = { selectedCalendar ->
                                calendarType = selectedCalendar
                                sharedPrefs.edit().putString("calendar_type", selectedCalendar).apply()
                            },
                            colorSchemeMode = colorSchemeMode,
                            onColorSchemeSelected = { selectedColorScheme ->
                                colorSchemeMode = selectedColorScheme
                                sharedPrefs.edit().putString("color_scheme_mode", selectedColorScheme).apply()
                            },
                            digitType = digitType,
                            onDigitTypeSelected = { selectedDigitType ->
                                digitType = selectedDigitType
                                sharedPrefs.edit().putString("digit_type", selectedDigitType).apply()
                            },
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
                            }
                        )
                    } else {
                        // Alarms screen
                        AlarmsScreen(
                            alarms = UiState.Alarms,
                            innerPadding = innerPadding,
                            colorSchemeMode = colorSchemeMode,
                            digitType = digitType,
                            onDeleteAlarm = { alarm ->
                                ViewModel.DeleteAlarm(alarm)
                            },
                            onEditAlarm = { alarm ->
                                selectedAlarmForEdit = alarm
                            }
                        )
                    }
                    }
                }

                // Detail view Dialog
                selectedItemForDetail?.let { item ->
                    AssetDetailDialog(
                        item = item,
                        timeRangeOrder = timeRangeOrder,
                        historyPoints = UiState.HistoryPoints[item.Symbol] ?: emptyList(),
                        isHistoryLoading = UiState.IsHistoryLoading,
                        calendarType = calendarType,
                        colorSchemeMode = colorSchemeMode,
                        digitType = digitType,
                        onFetchHistory = { range -> ViewModel.FetchHistory(item.Symbol, range.id, item.CurrentPrice, item.ChangePercentage) },
                        onDismiss = { selectedItemForDetail = null },
                        onSaveAlarm = { price, isAbove ->
                            ViewModel.AddAlarm(
                                symbol = item.Symbol,
                                title = item.Title,
                                targetPrice = price,
                                isAbove = isAbove
                            )
                            selectedItemForDetail = null
                            Toast.makeText(Context, Context.getString(R.string.alarm_created_success), Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Edit alarm Dialog
                selectedAlarmForEdit?.let { alarm ->
                    EditAlarmDialog(
                        alarm = alarm,
                        onDismiss = { selectedAlarmForEdit = null },
                        onSaveAlarm = { updatedAlarm ->
                            ViewModel.UpdateAlarm(updatedAlarm)
                            selectedAlarmForEdit = null
                            Toast.makeText(Context, "تغییرات با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                

                }
            }
        }
    }
    
    private fun SetupBackgroundSync() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val SyncRequest = PeriodicWorkRequestBuilder<CurrencySyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "CurrencySync",
                ExistingPeriodicWorkPolicy.KEEP,
                SyncRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
            s == "GOLD" || s == "XAU" || s == "PAXG" -> "Emami Gold Coin"
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
        s == "GOLD" || s == "XAU" || s == "PAXG" -> "سکه امامی"
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val scale = when {
        screenWidthDp >= 840 -> 1.22f
        screenWidthDp >= 600 -> 1.10f
        screenWidthDp <= 340 -> 0.90f
        else -> 1.0f
    }
    return (baseSp * scale).sp
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
            .clickable(onClick = onClick),
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
    UiState: CurrencyUiState,
    isEditingHome: Boolean,
    calendarType: String = "jalali",
    digitType: String = "fa",
    OnRefresh: () -> Unit,
    OnEditHome: () -> Unit
) {
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
        modifier = Modifier.fillMaxWidth(),
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontFamily = getFontFamilyForText(appTitle)
                )
                val timeDisplay = if (UiState.LastUpdatedTime.isNotEmpty()) {
                    "$formattedToday • ${UiState.LastUpdatedTime.toLocalizedDigits(digitType)}"
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
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            
                        val editCornerRadius by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isEditingHome) 22.dp else 12.dp,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                ),
                label = "EditShapeAnim"
            )

            val editBgColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isEditingHome) MaterialTheme.colorScheme.primary 
                              else MaterialTheme.colorScheme.primaryContainer,
                animationSpec = androidx.compose.animation.core.tween(400),
                label = "EditBgColorAnim"
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(editCornerRadius))
                    .background(editBgColor)
                    .clickable(onClick = OnEditHome),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedContent(
                    targetState = isEditingHome,
                    transitionSpec = {
                        (androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy)) + 
                         androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200))).togetherWith(
                            androidx.compose.animation.scaleOut(androidx.compose.animation.core.tween(200)) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                        )
                    },
                    label = "EditAnim"
                ) { editing ->
                    Icon(
                        imageVector = if (editing) Icons.Default.Check else Icons.Default.Edit, 
                        contentDescription = "Edit Home",
                        tint = if (editing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("all", "currency", "gold_and_coin", "crypto")
    val categoryLabels = listOf(
        R.string.all_markets,
        R.string.category_currency,
        R.string.category_gold_and_coin,
        R.string.category_crypto
    )
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    ExpressiveConnectedButtonGroup(
        itemsCount = categories.size,
        selectedIndex = selectedIndex,
        onSelect = { onCategorySelected(categories[it]) },
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
    val backgroundColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer, tween(300), label = "bg_color")
    val textColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer, tween(300), label = "text_color")
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() }
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

@Composable
fun BentoGrid(
    Items: List<com.mmdparsadev.cheghad.data.models.CurrencyItem>,
    homeSymbols: List<String>,
    isEditing: Boolean = false,
    colorSchemeMode: String = "standard",
    digitType: String = "fa",
    onSymbolsChanged: (List<String>) -> Unit = {},
    onClickItem: (com.mmdparsadev.cheghad.data.models.CurrencyItem) -> Unit = {}
) {
    val usdItem = Items.find { it.Symbol == homeSymbols.getOrNull(0) ?: "USD" }
    val eurItem = Items.find { it.Symbol == homeSymbols.getOrNull(1) ?: "EUR" }
    val goldItem = Items.find { it.Symbol == homeSymbols.getOrNull(2) ?: "GOLD" }
    val btcItem = Items.find { it.Symbol == homeSymbols.getOrNull(3) ?: "BTC" }
    val ethItem = Items.find { it.Symbol == homeSymbols.getOrNull(4) ?: "ETH" }

    val upColor = if (colorSchemeMode == "inverted") MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
    val downColor = if (colorSchemeMode == "inverted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var replaceSlotIndex by remember { mutableStateOf<Int?>(null) }

    var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val slotCoordinates = remember { mutableStateListOf<LayoutCoordinates?>().apply { repeat(5) { add(null) } } }

    @Composable
    fun DraggableCardContainer(
        slotIndex: Int,
        shape: RoundedCornerShape,
        modifier: Modifier = Modifier,
        content: @Composable BoxScope.() -> Unit
    ) {
        val isDragging = draggedIndex == slotIndex
        Box(
            modifier = modifier
                .onGloballyPositioned { coords ->
                    slotCoordinates[slotIndex] = coords
                }
                .zIndex(if (isDragging) 10f else 1f)
                .graphicsLayer {
                    if (isDragging) {
                        translationX = dragOffset.x
                        translationY = dragOffset.y
                        scaleX = 1.05f
                        scaleY = 1.05f
                        alpha = 0.9f
                    }
                }
                .then(
                    if (isEditing) {
                        Modifier.border(
                            width = 1.5.dp,
                            color = if (isDragging) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = shape
                        )
                    } else Modifier
                )
        ) {
            content()
            if (isEditing) {
                // Drag handle at top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp).align(Alignment.Center)
                    )
                }

                // Replace/Swap button at top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(24.dp)
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
                        modifier = Modifier.size(14.dp).align(Alignment.Center)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                parentCoordinates = coords
            }
            .then(
                if (isEditing) {
                    Modifier.pointerInput(homeSymbols) {
                        detectDragGestures(
                            onDragStart = { startPosition ->
                                val activeParentCoords = parentCoordinates
                                if (activeParentCoords != null && activeParentCoords.isAttached) {
                                    for (i in 0..4) {
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
                                        
                                        for (j in 0..4) {
                                            if (j != activeDraggedIndex) {
                                                val otherCoords = slotCoordinates[j]
                                                if (otherCoords != null && otherCoords.isAttached) {
                                                    val otherBounds = activeParentCoords.localBoundingBoxOf(otherCoords)
                                                    if (otherBounds.contains(draggedCenter)) {
                                                        // Swap homeSymbols
                                                        val newList = homeSymbols.toMutableList()
                                                        val temp = newList[activeDraggedIndex]
                                                        newList[activeDraggedIndex] = newList[j]
                                                        newList[j] = temp
                                                        onSymbolsChanged(newList)
                                                        
                                                        // Adjust dragOffset to avoid snapping/jumping
                                                        val originalCenterI = otherBounds.center
                                                        val originalCenterJ = draggedBounds.center
                                                        dragOffset -= (originalCenterI - originalCenterJ)
                                                        
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Card (slot 0)
            DraggableCardContainer(
                slotIndex = 0,
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                    Crossfade(targetState = usdItem, animationSpec = tween(400), label = "USD") { item ->
                        val usdChange = item?.ChangePercentage ?: 0.0
                        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                        val usdIsUp = usdChange >= 0.0
                        val usdIsGreen = if (colorSchemeMode == "inverted") !usdIsUp else usdIsUp
                        
                        val isZeroChange = item == null || Math.abs(usdChange) < 0.001
                        val heroBgColor = if (isZeroChange) {
                            if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
                        } else if (usdIsGreen) {
                            if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
                        } else {
                            if (isDark) Color(0xFF381A1F) else Color(0xFFFFEBEE)
                        }
                        
                        val heroContentColor = if (isZeroChange) {
                            if (isDark) Color.White else Color.Black
                        } else if (usdIsGreen) {
                            if (isDark) Color(0xFFE8F5E9) else Color(0xFF1B5E20)
                        } else {
                            if (isDark) Color(0xFFFFEBEE) else Color(0xFF8C1D18)
                        }
                        
                        val heroTrendColor = if (isZeroChange) {
                            if (isDark) Color.LightGray else Color.Gray
                        } else if (usdIsGreen) {
                            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                        } else {
                            if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                                .background(heroBgColor)
                                .border(1.dp, heroTrendColor.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                                .clickable { if (!isEditing) { item?.let { onClickItem(it) } } }
                                .padding(24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(heroTrendColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = heroTrendColor, modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(getLocalizedTitle(item?.Symbol ?: "USD", item?.Title ?: "دلار آمریکا"), fontSize = adaptiveSp(22f), fontWeight = FontWeight.ExtraBold, color = heroContentColor, fontFamily = getFontFamilyForText(getLocalizedTitle(item?.Symbol ?: "USD", item?.Title ?: "دلار آمریکا")))
                                            Text(item?.Symbol ?: "USD", fontSize = adaptiveSp(14f), fontWeight = FontWeight.Medium, color = heroContentColor.copy(alpha = 0.7f), fontFamily = getFontFamilyForText(item?.Symbol ?: "USD"))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(heroTrendColor.copy(alpha = 0.18f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        val percentStr = if (item != null) formatPercent(usdChange, digitType) else "------"
                                        androidx.compose.animation.AnimatedContent(
                                            targetState = percentStr,
                                            transitionSpec = {
                                                androidx.compose.animation.fadeIn().togetherWith(androidx.compose.animation.fadeOut())
                                            },
                                            label = "PercentAnim"
                                        ) { pStr ->
                                            Text(pStr, fontSize = adaptiveSp(13f), fontWeight = FontWeight.Bold, color = heroTrendColor, fontFamily = getFontFamilyForText(pStr))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        val formattedPrice = if (item != null) formatPrice(item.CurrentPrice, digitType, item.Symbol) else "------"
                                        val unitStr = androidx.compose.ui.res.stringResource(R.string.currency_toman)
                                        androidx.compose.animation.AnimatedContent(
                                            targetState = formattedPrice,
                                            transitionSpec = {
                                                if (targetState > initialState) {
                                                    (androidx.compose.animation.slideInVertically { height -> height } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutVertically { height -> -height } + androidx.compose.animation.fadeOut())
                                                } else {
                                                    (androidx.compose.animation.slideInVertically { height -> -height } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutVertically { height -> height } + androidx.compose.animation.fadeOut())
                                                }
                                            },
                                            label = "PriceAnim",
                                            modifier = Modifier.alignByBaseline()
                                        ) { price ->
                                            Text(price, fontSize = adaptiveSp(46f), fontWeight = FontWeight.Bold, color = heroContentColor, fontFamily = getFontFamilyForText(price))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(unitStr, fontSize = adaptiveSp(17f), fontWeight = FontWeight.Medium, color = heroContentColor.copy(alpha = 0.7f), modifier = Modifier.alignByBaseline(), fontFamily = getFontFamilyForText(unitStr))
                                    }
                                }
                            }
                        }
                    }
                }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gold Card (slot 2)
                DraggableCardContainer(
                    slotIndex = 2,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Crossfade(targetState = goldItem, animationSpec = tween(400), label = "Gold") { item ->
                        val isEngCard = java.util.Locale.getDefault().language == "en"
                        val unitToman = androidx.compose.ui.res.stringResource(R.string.currency_toman)
                        val goldChange = item?.ChangePercentage ?: 0.0
                        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                        val goldIsUp = goldChange >= 0.0
                        val goldIsGreen = if (colorSchemeMode == "inverted") !goldIsUp else goldIsUp
                        
                        val isZeroChange = item == null || Math.abs(goldChange) < 0.001
                        val goldBgColor = if (isZeroChange) {
                            if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
                        } else if (goldIsGreen) {
                            if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
                        } else {
                            if (isDark) Color(0xFF381A1F) else Color(0xFFFFEBEE)
                        }
                        val goldContentColor = if (isZeroChange) {
                            if (isDark) Color.White else Color.Black
                        } else if (goldIsGreen) {
                            if (isDark) Color(0xFFE8F5E9) else Color(0xFF1B5E20)
                        } else {
                            if (isDark) Color(0xFFFFEBEE) else Color(0xFF8C1D18)
                        }
                        val goldTrendColor = if (isZeroChange) {
                            if (isDark) Color.LightGray else Color.Gray
                        } else if (goldIsGreen) {
                            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                        } else {
                            if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
                        }

                        SecondaryCard(
                            modifier = Modifier.fillMaxWidth().clickable { if (!isEditing) { item?.let { onClickItem(it) } } },
                            title = getLocalizedTitle(item?.Symbol ?: "GOLD", item?.Title ?: "سکه امامی"),
                            subtitle = if (item?.Symbol == "GOLD" || item?.Symbol == "XAU") { if (isEngCard) "Emami Coin / New Design" else "سکه امامی / طرح جدید" } else "${getLocalizedTitle(item?.Symbol ?: "", item?.Title ?: "")} / $unitToman",
                            value = if (item != null) formatPrice(item.CurrentPrice, digitType, item.Symbol) else "------",
                            trend = if (item != null) (if (goldChange >= 0) "↑" else "↓") else "------",
                            trendColor = goldTrendColor,
                            backgroundColor = goldBgColor,
                            contentColor = goldContentColor
                        )
                    }
                }

                // Bitcoin Card (slot 3)
                DraggableCardContainer(
                    slotIndex = 3,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Crossfade(targetState = btcItem, animationSpec = tween(400), label = "BTC") { item ->
                        val isEngCard = java.util.Locale.getDefault().language == "en"
                        val unitToman = androidx.compose.ui.res.stringResource(R.string.currency_toman)
                        val btcChange = item?.ChangePercentage ?: 0.0
                        val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                        val btcIsUp = btcChange >= 0.0
                        val btcIsGreen = if (colorSchemeMode == "inverted") !btcIsUp else btcIsUp
                        val isZeroChange = item == null || Math.abs(btcChange) < 0.001
                        val btcBgColor = if (isZeroChange) {
                            if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE)
                        } else if (btcIsGreen) {
                            if (isDark) Color(0xFF1B382B) else Color(0xFFE8F5E9)
                        } else {
                            if (isDark) Color(0xFF381A1F) else Color(0xFFFFEBEE)
                        }
                        val btcContentColor = if (isZeroChange) {
                            if (isDark) Color.White else Color.Black
                        } else if (btcIsGreen) {
                            if (isDark) Color(0xFFE8F5E9) else Color(0xFF1B5E20)
                        } else {
                            if (isDark) Color(0xFFFFEBEE) else Color(0xFF8C1D18)
                        }
                        val btcTrendColor = if (isZeroChange) {
                            if (isDark) Color.LightGray else Color.Gray
                        } else if (btcIsGreen) {
                            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                        } else {
                            if (isDark) Color(0xFFE57373) else Color(0xFFC62828)
                        }

                        SecondaryCard(
                            modifier = Modifier.fillMaxWidth().clickable { if (!isEditing) { item?.let { onClickItem(it) } } },
                            title = getLocalizedTitle(item?.Symbol ?: "BTC", item?.Title ?: "بیت‌کوین"),
                            subtitle = if (item?.Symbol == "BTC") { if (isEngCard) "Bitcoin / Toman" else "بیت‌کوین / تومان" } else "${getLocalizedTitle(item?.Symbol ?: "", item?.Title ?: "")} / $unitToman",
                            value = if (item != null) formatPrice(item.CurrentPrice, digitType, item?.Symbol ?: "BTC") else "------",
                            trend = if (item != null) (if (btcChange >= 0) "↑" else "↓") else "------",
                            trendColor = btcTrendColor,
                            backgroundColor = btcBgColor,
                            contentColor = btcContentColor
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ethereum Card (slot 4)
                DraggableCardContainer(
                    slotIndex = 4,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Crossfade(targetState = ethItem, animationSpec = tween(400), label = "ETH") { item ->
                        SmallCard(
                            modifier = Modifier.fillMaxWidth().clickable { if (!isEditing) { item?.let { onClickItem(it) } } },
                            icon = if (item?.Symbol == "ETH") "Ξ" else (item?.Symbol?.take(1) ?: "Ξ"),
                            value = if (item != null) formatPrice(item.CurrentPrice, digitType, item.Symbol) else "------",
                            trend = if (item != null) formatPercent(item.ChangePercentage, digitType) else "------",
                            trendColor = run {
                                val change = item?.ChangePercentage ?: 0.0
                                val isZeroChange = item == null || Math.abs(change) < 0.001
                                val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                                if (isZeroChange) {
                                    if (isDark) Color.LightGray else Color.Gray
                                } else if (change >= 0) upColor else downColor
                            }
                        )
                    }
                }

                // Euro Card (slot 1)
                DraggableCardContainer(
                    slotIndex = 1,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Crossfade(targetState = eurItem, animationSpec = tween(400), label = "EUR") { item ->
                        SmallCard(
                            modifier = Modifier.fillMaxWidth().clickable { if (!isEditing) { item?.let { onClickItem(it) } } },
                            icon = if (item?.Symbol == "EUR") "€" else (item?.Symbol?.take(1) ?: "€"),
                            value = if (item != null) formatPrice(item.CurrentPrice, digitType, item.Symbol) else "------",
                            trend = if (item != null) formatPercent(item.ChangePercentage, digitType) else "------",
                            trendColor = run {
                                val change = item?.ChangePercentage ?: 0.0
                                val isZeroChange = item == null || Math.abs(change) < 0.001
                                val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                                if (isZeroChange) {
                                    if (isDark) Color.LightGray else Color.Gray
                                } else if (change >= 0) upColor else downColor
                            }
                        )
                    }
                }
            }
        }
    }

    if (replaceSlotIndex != null) {
        val slotIdx = replaceSlotIndex!!
        val availableItems = Items.filter { it.Symbol !in homeSymbols }
        
        AlertDialog(
            onDismissRequest = { replaceSlotIndex = null },
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.replace_hero_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                if (availableItems.isEmpty()) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.no_new_options),
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableItems) { item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newList = homeSymbols.toMutableList()
                                        if (slotIdx < newList.size) {
                                            newList[slotIdx] = item.Symbol
                                            onSymbolsChanged(newList)
                                        }
                                        replaceSlotIndex = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.Symbol,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = getLocalizedTitle(item.Symbol, item.Title),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (item.Category) {
                                                com.mmdparsadev.cheghad.data.models.CurrencyType.Crypto -> "🪙"
                                                com.mmdparsadev.cheghad.data.models.CurrencyType.GoldAndCoin -> "🥇"
                                                else -> "💵"
                                            },
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { replaceSlotIndex = null }) {
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
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = adaptiveSp(14f), fontWeight = FontWeight.Bold, color = contentColor, fontFamily = getFontFamilyForText(title))
                androidx.compose.animation.AnimatedContent(targetState = trend, label = "TrendAnim") { trnd ->
                    Text(trnd, fontSize = adaptiveSp(14f), fontWeight = FontWeight.Black, color = trendColor, fontFamily = getFontFamilyForText(trnd))
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            androidx.compose.animation.AnimatedContent(
                targetState = value,
                transitionSpec = {
                    if (targetState > initialState) {
                        (androidx.compose.animation.slideInVertically { height -> height } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutVertically { height -> -height } + androidx.compose.animation.fadeOut())
                    } else {
                        (androidx.compose.animation.slideInVertically { height -> -height } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutVertically { height -> height } + androidx.compose.animation.fadeOut())
                    }
                },
                label = "ValueAnim"
            ) { valStr ->
                Text(valStr, fontSize = adaptiveSp(18f), fontWeight = FontWeight.Bold, color = contentColor, fontFamily = getFontFamilyForText(valStr))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = adaptiveSp(11f), fontWeight = FontWeight.Medium, color = contentColor.copy(alpha = 0.7f), fontFamily = getFontFamilyForText(subtitle))
        }
    }
}

@Composable
fun SmallCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    trend: String,
    trendColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = adaptiveSp(12f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontFamily = getFontFamilyForText(icon))
                }
                Text(trend, fontSize = adaptiveSp(12f), fontWeight = FontWeight.Bold, color = trendColor, fontFamily = getFontFamilyForText(trend))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(value, fontSize = adaptiveSp(15f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontFamily = getFontFamilyForText(value))
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: String, onScreenSelected: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp),
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
    val alphaAnim by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isSelected) 1f else 0.6f, label = "AlphaAnim")
    val bgColor by androidx.compose.animation.animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent, label = "BgColorAnim")
    val iconColor by androidx.compose.animation.animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface, label = "IconColorAnim")
    val fontWeightAnim = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null, // Disable default ripple for cleaner pill animation
                onClick = onClick
            )
            .alpha(alphaAnim)
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(32.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, fontSize = 12.sp, fontWeight = fontWeightAnim, color = iconColor)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssetListItem(
    item: com.mmdparsadev.cheghad.data.models.CurrencyItem,
    colorSchemeMode: String = "standard",
    digitType: String = "fa",
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(item.Symbol.take(3), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(getLocalizedTitle(item.Symbol, item.Title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(item.Symbol, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val formattedPrice = formatPrice(item.CurrentPrice, digitType, item.Symbol)
            Text(
                text = formattedPrice, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            val isNegative = item.ChangePercentage < 0
            val upColor = if (colorSchemeMode == "inverted") MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
            val downColor = if (colorSchemeMode == "inverted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            val color = if (isNegative) downColor else upColor
            Text(
                text = formatPercent(item.ChangePercentage, digitType), 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold, 
                color = color
            )
        }
    }
}

@Composable
fun WelcomeScreen(onComplete: (lang: String, theme: String) -> Unit) {
    var selectedLang by remember { mutableStateOf("fa") }
    var selectedTheme by remember { mutableStateOf("system") }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "چقد",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = getFontFamilyForText("چقد")
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.welcome_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.welcome_title))
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.welcome_desc),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.welcome_desc))
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "زبان مورد نظر خود را انتخاب کنید / Select Language",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Persian language card
                    Card(
                        onClick = { selectedLang = "fa" },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLang == "fa") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (selectedLang == "fa") 2.dp else 1.dp,
                            color = if (selectedLang == "fa") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇮🇷", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(R.string.persian),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.persian))
                                    )
                                    Text(
                                        text = "فارسی",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = getFontFamilyForText("فارسی")
                                    )
                                }
                            }
                            RadioButton(
                                selected = selectedLang == "fa",
                                onClick = { selectedLang = "fa" },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // English language card
                    Card(
                        onClick = { selectedLang = "en" },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLang == "en") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = if (selectedLang == "en") 2.dp else 1.dp,
                            color = if (selectedLang == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇬🇧", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(R.string.english),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "English",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            RadioButton(
                                selected = selectedLang == "en",
                                onClick = { selectedLang = "en" },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "پوسته / Theme",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ExpressiveConnectedButtonGroup(
                        itemsCount = 3,
                        selectedIndex = listOf("system", "light", "dark").indexOf(selectedTheme),
                        onSelect = { selectedTheme = listOf("system", "light", "dark")[it] }
                    ) { index, isSelected ->
                        val labels = listOf("System", "Light", "Dark")
                        val icons = listOf(Icons.Default.SettingsSuggest, Icons.Default.LightMode, Icons.Default.DarkMode)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(icons[index], contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(labels[index], fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { onComplete(selectedLang, selectedTheme) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(R.string.continue_btn),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = getFontFamilyForText(androidx.compose.ui.res.stringResource(R.string.continue_btn))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
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
    Items: List<com.mmdparsadev.cheghad.data.models.CurrencyItem>,
    currentSymbols: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                        val item = Items.find { it.Symbol == symbol }
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
                                        text = item.Title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = item.Symbol,
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
                items(Items) { item ->
                    val isSelected = selectedSymbols.contains(item.Symbol)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val mutable = selectedSymbols.toMutableList()
                                if (isSelected) {
                                    mutable.remove(item.Symbol)
                                } else if (selectedSymbols.size < 5) {
                                    mutable.add(item.Symbol)
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
                                text = item.Title,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = item.Symbol,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FilledTonalButton(
                onClick = { onSave(selectedSymbols) },
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
fun ExpressiveConnectedButtonGroup(
    modifier: Modifier = Modifier,
    itemsCount: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    spacing: androidx.compose.ui.unit.Dp = 4.dp,
    height: androidx.compose.ui.unit.Dp = 46.dp,
    content: @Composable (index: Int, isSelected: Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cornerFull = 23.dp
        val cornerFlat = 6.dp

        for (index in 0 until itemsCount) {
            val isSelected = index == selectedIndex
            val isPrevSelected = selectedIndex == index - 1
            val isNextSelected = selectedIndex == index + 1

            val topStartAnimated by animateDpAsState(
                targetValue = if (isSelected || index == 0 || isPrevSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "topStart_$index"
            )
            val bottomStartAnimated by animateDpAsState(
                targetValue = if (isSelected || index == 0 || isPrevSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "bottomStart_$index"
            )
            val topEndAnimated by animateDpAsState(
                targetValue = if (isSelected || index == itemsCount - 1 || isNextSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "topEnd_$index"
            )
            val bottomEndAnimated by animateDpAsState(
                targetValue = if (isSelected || index == itemsCount - 1 || isNextSelected) cornerFull else cornerFlat,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "bottomEnd_$index"
            )

            val animatedShape = RoundedCornerShape(
                topStart = topStartAnimated,
                bottomStart = bottomStartAnimated,
                topEnd = topEndAnimated,
                bottomEnd = bottomEndAnimated
            )

            Button(
                onClick = { onSelect(index) },
                shape = animatedShape,
                modifier = Modifier
                    .weight(1f)
                    .height(height),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                border = if (isSelected) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                },
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                content(index, isSelected)
            }
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
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
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
    badgeColor: Color? = null
) {
    Surface(
        onClick = { onCheckedChange(!isChecked) },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isChecked) 2.dp else 1.dp,
            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (badgeColor != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

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
    digitType: String = "fa",
    onDigitTypeSelected: (String) -> Unit = {},
    timeRangeOrder: List<TimeRange>,
    onTimeRangeOrderChanged: (List<TimeRange>) -> Unit,
    disabledNewsCategories: Set<String> = emptySet(),
    onDisabledNewsCategoriesChanged: (Set<String>) -> Unit = {},
    disabledNewsAgencies: Set<String> = emptySet(),
    onDisabledNewsAgenciesChanged: (Set<String>) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.settings_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.settings_subtitle),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(langLabels[index]),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(themeLabels[index]),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Range Reordering section
        SettingsCard(
            title = androidx.compose.ui.res.stringResource(R.string.settings_time_ranges),
            subtitle = androidx.compose.ui.res.stringResource(R.string.reorder_ranges_help),
            icon = Icons.Default.Tune
        ) {
            var draggedIndex by remember { mutableStateOf<Int?>(null) }
            var dragOffsetY by remember { mutableStateOf(0f) }
            val itemHeightDp = 52.dp
            val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                timeRangeOrder.forEachIndexed { index, range ->
                    val isDragging = draggedIndex == index
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffsetY else 0f
                                scaleX = if (isDragging) 1.03f else 1f
                                scaleY = if (isDragging) 1.03f else 1f
                                shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                            }
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.surfaceVariant 
                                else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = if (isDragging) 1.5.dp else 1.dp,
                                color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
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
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = androidx.compose.ui.res.stringResource(range.stringRes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (index < timeRangeOrder.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
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
            val categories = listOf(
                com.mmdparsadev.cheghad.data.models.NewsCategory.Economic to R.string.news_category_economic,
                com.mmdparsadev.cheghad.data.models.NewsCategory.CurrencyGold to R.string.news_category_currency,
                com.mmdparsadev.cheghad.data.models.NewsCategory.Bourse to R.string.news_category_bourse,
                com.mmdparsadev.cheghad.data.models.NewsCategory.Crypto to R.string.news_category_crypto,
                com.mmdparsadev.cheghad.data.models.NewsCategory.World to R.string.news_category_world
            )

            categories.forEachIndexed { index, (cat, titleRes) ->
                val isChecked = !disabledNewsCategories.contains(cat.name)
                SettingsSwitchRow(
                    title = androidx.compose.ui.res.stringResource(titleRes),
                    isChecked = isChecked,
                    onCheckedChange = { checked ->
                        val newSet = if (checked) disabledNewsCategories - cat.name else disabledNewsCategories + cat.name
                        onDisabledNewsCategoriesChanged(newSet)
                    }
                )
                if (index < categories.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
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
            val agencies = com.mmdparsadev.cheghad.data.repository.NewsRepository.AGENCIES

            agencies.forEachIndexed { index, agency ->
                val isChecked = !disabledNewsAgencies.contains(agency.id)
                SettingsSwitchRow(
                    title = agency.nameFa,
                    isChecked = isChecked,
                    badgeColor = agency.brandColor,
                    onCheckedChange = { checked ->
                        val newSet = if (checked) disabledNewsAgencies - agency.id else disabledNewsAgencies + agency.id
                        onDisabledNewsAgenciesChanged(newSet)
                    }
                )
                if (index < agencies.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                    text = androidx.compose.ui.res.stringResource(R.string.version_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
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
    var targetPriceStr by remember { mutableStateOf(formatTargetPrice(item.CurrentPrice)) }
    var isAbove by remember { mutableStateOf(true) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.DAY) }
    
    LaunchedEffect(item.Symbol, selectedTimeRange) {
        onFetchHistory(selectedTimeRange)
    }
    
    val points = if (historyPoints.isNotEmpty()) historyPoints else listOf(item.CurrentPrice, item.CurrentPrice)
    
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
    
    val labels = remember(item.Symbol, points.size, selectedTimeRange, calendarType) {
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
    
    val displayPrice = if (activeIndex != null && activeIndex!! < points.size) points[activeIndex!!] else item.CurrentPrice
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

    val formattedDisplayPrice = formatPrice(displayPrice, digitType, item.Symbol)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.Symbol.take(3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(getLocalizedTitle(item.Symbol, item.Title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(item.Symbol, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price section
            val upColor = if (colorSchemeMode == "inverted") MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
            val downColor = if (colorSchemeMode == "inverted") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = displayLabel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.animation.AnimatedContent(
                        targetState = formattedDisplayPrice,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (androidx.compose.animation.slideInVertically { height -> height } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutVertically { height -> -height } + androidx.compose.animation.fadeOut())
                            } else {
                                (androidx.compose.animation.slideInVertically { height -> -height } + androidx.compose.animation.fadeIn()).togetherWith(androidx.compose.animation.slideOutVertically { height -> height } + androidx.compose.animation.fadeOut())
                            }
                        },
                        label = "DialogPriceAnim"
                    ) { priceStr ->
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.currency_toman) + " " + priceStr,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                
                val isNegative = item.ChangePercentage < 0
                val changeColor = if (isNegative) downColor else upColor
                androidx.compose.animation.AnimatedContent(
                    targetState = formatPercent(item.ChangePercentage, digitType),
                    label = "DialogPercentAnim"
                ) { pctStr ->
                    Text(
                        text = pctStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Canvas
            val isZeroChange = Math.abs(item.ChangePercentage) < 0.001
            val isDark = MaterialTheme.colorScheme.background.red < 0.5f
            val chartColor = if (isZeroChange) {
                if (isDark) Color.LightGray else Color.Gray
            } else if (item.ChangePercentage >= 0) upColor else downColor
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                InteractiveAssetChart(
                    points = points,
                    labels = labels,
                    chartColor = chartColor,
                    activeIndex = activeIndex,
                    digitType = digitType,
                    symbol = item.Symbol,
                    onActiveIndexChanged = { activeIndex = it }
                )
                
                if (isHistoryLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Range Buttons
            val selectedTimeRangeIndex = timeRangeOrder.indexOf(selectedTimeRange).coerceAtLeast(0)
            ExpressiveConnectedButtonGroup(
                itemsCount = timeRangeOrder.size,
                selectedIndex = selectedTimeRangeIndex,
                onSelect = { selectedTimeRange = timeRangeOrder[it] }
            ) { index, isSelected ->
                Text(
                    text = androidx.compose.ui.res.stringResource(timeRangeOrder[index].stringRes),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Alarm Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.alarm_create_title),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

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
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Condition Selectors
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

                    // Save button
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            val price = targetPriceStr.toDoubleOrNull()
                            if (price == null || price <= 0.0) {
                                Toast.makeText(context, context.getString(R.string.alarm_invalid_price), Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveAlarm(price, isAbove)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(R.string.alarm_button_save))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditAlarmDialog(
    alarm: com.mmdparsadev.cheghad.data.models.AlarmEntity,
    onDismiss: () -> Unit,
    onSaveAlarm: (updatedAlarm: com.mmdparsadev.cheghad.data.models.AlarmEntity) -> Unit
) {
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
                    IconButton(onClick = onDismiss) {
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
                        val price = targetPriceStr.toDoubleOrNull()
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
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Alarm",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                IconButton(onClick = onDelete) {
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

@Composable
fun CurrencyCalculatorScreen(
    items: List<CurrencyItem>,
    digitType: String,
    innerPadding: PaddingValues
) {
    var selectedItem by remember(items) { mutableStateOf(items.firstOrNull()) }
    var quantityInput by remember { mutableStateOf("1") }
    var tomanInput by remember { mutableStateOf("") }
    var calculationMode by remember { mutableStateOf(0) } // 0: Asset to Toman, 1: Toman to Asset

    val quantity = quantityInput.toDoubleOrNull() ?: 0.0
    val currentPrice = selectedItem?.CurrentPrice ?: 0.0
    val totalToman = quantity * currentPrice

    val tomanEntered = tomanInput.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header (Simplified: Center text, No Icon, No background color)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ماشین‌حساب و مبدل ارزی",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "محاسبه ارزش دارایی‌ها و تبدیل آنلاین به تومان",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Mode Selector Tabs (Connected Button Group)
        val modeLabels = listOf("ارز به تومان", "تومان به ارز")
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

        if (calculationMode == 0) {
            // Asset to Toman Converter
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "انتخاب دارایی / ارز",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        itemsIndexed(items) { index, item ->
                            val isSelected = selectedItem?.Symbol == item.Symbol
                            val cornerFull = 23.dp
                            val cornerFlat = 6.dp
                            val itemShape = when {
                                items.size == 1 -> CircleShape
                                index == 0 -> RoundedCornerShape(topStart = cornerFull, bottomStart = cornerFull, topEnd = cornerFlat, bottomEnd = cornerFlat)
                                index == items.size - 1 -> RoundedCornerShape(topStart = cornerFlat, bottomStart = cornerFlat, topEnd = cornerFull, bottomEnd = cornerFull)
                                else -> RoundedCornerShape(cornerFlat)
                            }
                            val itemTitle = getLocalizedTitle(item.Symbol, item.Title)

                            Button(
                                onClick = { selectedItem = item },
                                shape = itemShape,
                                modifier = Modifier.height(44.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                border = if (isSelected) {
                                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                },
                                colors = if (isSelected) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f),
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                elevation = null
                            ) {
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
                                            text = item.Symbol.take(2),
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
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("مقدار (${selectedItem?.Symbol ?: ""})") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Result Box (Premium look)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Text("ارزش کل به تومان:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatPrice(totalToman, digitType)} تومان",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
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
                        text = "مبلغ به تومان",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tomanInput,
                        onValueChange = { tomanInput = it },
                        label = { Text("مثلاً ۱۰,۰۰۰,۰۰۰ تومان") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "معادل در سایر ارزها:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    items.forEach { item ->
                        val calculatedAmount = if (item.CurrentPrice > 0) tomanEntered / item.CurrentPrice else 0.0
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
                                        item.Symbol.take(3),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(item.Title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Text(
                                text = String.format(Locale.US, "%.4f", calculatedAmount).toLocalizedDigits(digitType) + " " + item.Symbol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
