package com.mmdparsadev.cheghad.ui.technical

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.zIndex
import com.mmdparsadev.cheghad.ui.TvFocusableIconButton
import com.mmdparsadev.cheghad.ui.rememberTvFocusState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartDrawing
import com.mmdparsadev.cheghad.data.models.ChartPoint
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.DrawingToolType
import com.mmdparsadev.cheghad.data.models.EmaAssetAnalysis
import com.mmdparsadev.cheghad.data.models.EmaPeriod
import com.mmdparsadev.cheghad.data.models.EmaTouchStatus
import com.mmdparsadev.cheghad.data.repository.TechnicalRepository
import com.mmdparsadev.cheghad.formatPrice
import com.mmdparsadev.cheghad.getLocalizedTitle
import com.mmdparsadev.cheghad.isTvDevice
import com.mmdparsadev.cheghad.toLocalizedDigits
import com.mmdparsadev.cheghad.ui.theme.getFontFamilyForText
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ExportFormat {
    PNG, PDF
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalChartScreen(
    initialCurrency: CurrencyItem,
    allCurrencies: List<CurrencyItem>,
    digitType: String = "fa",
    calendarType: String = "jalali",
    bullishColor: Color = Color(0xFF00C853),
    bearishColor: Color = Color(0xFFFF3D00),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { TechnicalRepository() }

    var currentCurrency by remember(initialCurrency.symbol) { mutableStateOf(initialCurrency) }
    var selectedTimeframeName by rememberSaveable { mutableStateOf<String?>(ChartTimeframe.D1.name) }
    var activeExportTimeframeName by rememberSaveable { mutableStateOf(ChartTimeframe.D1.name) }
    val selectedTimeframe = selectedTimeframeName?.let { tfName ->
        ChartTimeframe.entries.find { it.name == tfName }
    }
    val activeExportTimeframe = ChartTimeframe.entries.find { it.name == activeExportTimeframeName }
        ?: ChartTimeframe.D1
    var enabledEmas by remember { mutableStateOf(setOf(EmaPeriod.EMA_200)) }
    var lastNonEmptyEmas by remember { mutableStateOf(setOf(EmaPeriod.EMA_200)) }
    var showEmaMenu by remember { mutableStateOf(false) }
    var showRsi by rememberSaveable { mutableStateOf(true) }

    var candles by remember { mutableStateOf<List<CandleData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hoveredCandle by remember { mutableStateOf<CandleData?>(null) }
    var showExportSheet by remember { mutableStateOf(false) }
    var selectedExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    // Drawing state
    var drawings by remember { mutableStateOf<List<ChartDrawing>>(emptyList()) }
    var undoStack by remember { mutableStateOf<List<List<ChartDrawing>>>(emptyList()) }
    val drawingsAlpha = remember { Animatable(1f) }
    var activeTool by remember { mutableStateOf(DrawingToolType.NONE) }
    var activeColor by remember { mutableStateOf(Color(0xFFFFEB3B)) }
    var showTextDialog by remember { mutableStateOf(false) }
    var textTargetPoint by remember { mutableStateOf<ChartPoint?>(null) }
    var inputNoteText by remember { mutableStateOf("") }
    var isFullScreen by rememberSaveable { mutableStateOf(false) }
    var isLandscape by rememberSaveable { mutableStateOf(false) }
    var showDrawToolsInLandscape by rememberSaveable { mutableStateOf(false) }
    var selectedCarouselTab by rememberSaveable { mutableIntStateOf(0) }

    val isTv = isTvDevice()
    val configuration = LocalConfiguration.current
    val isSystemLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isEffectiveLandscape = !isTv && (isLandscape || isSystemLandscape)
    val hideSecondaryPanels = isFullScreen || isEffectiveLandscape
    val view = LocalView.current

    // Restore orientation only when leaving TechnicalChartScreen (not mid-configuration change)
    DisposableEffect(activity) {
        onDispose {
            if (activity != null && !activity.isChangingConfigurations) {
                runCatching {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }
        }
    }

    // Immersive system bars in landscape so the chart uses the entire widescreen viewport
    DisposableEffect(activity, view, isEffectiveLandscape, isTv) {
        val window = activity?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        if (isEffectiveLandscape && insetsController != null) {
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else if (!isTv && insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            if (!isTv && activity != null && !activity.isChangingConfigurations && insetsController != null) {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    fun toggleLandscape(enable: Boolean) {
        isLandscape = enable
        if (enable) {
            isFullScreen = true
            runCatching {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        } else {
            showDrawToolsInLandscape = false
            activeTool = DrawingToolType.NONE
            isFullScreen = false
            runCatching {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    fun toggleEmaPeriod(period: EmaPeriod) {
        val next = if (period in enabledEmas) enabledEmas - period else enabledEmas + period
        enabledEmas = next
        if (next.isNotEmpty()) {
            lastNonEmptyEmas = next
        }
    }

    fun toggleHeaderEma() {
        if (enabledEmas.isNotEmpty()) {
            lastNonEmptyEmas = enabledEmas
            enabledEmas = emptySet()
        } else {
            enabledEmas = lastNonEmptyEmas.ifEmpty { setOf(EmaPeriod.EMA_200) }
        }
    }

    // Clear drawings when switching to a different asset
    LaunchedEffect(currentCurrency.symbol) {
        drawings = emptyList()
        undoStack = emptyList()
    }

    // Map of candles for assets to evaluate EMA 200 touches for carousel 5
    var recentCandlesMap by remember { mutableStateOf<Map<String, List<CandleData>>>(emptyMap()) }
    var emaAnalyses by remember { mutableStateOf<List<EmaAssetAnalysis>>(emptyList()) }

    // Fetch candles when currency or timeframe changes
    LaunchedEffect(currentCurrency.symbol, selectedTimeframe) {
        val tf = selectedTimeframe ?: return@LaunchedEffect
        activeExportTimeframeName = tf.name
        isLoading = true
        candles = repository.getCandles(currentCurrency, tf)
        isLoading = false

        // Update cache for carousel 5 evaluation
        val updatedMap = recentCandlesMap.toMutableMap()
        updatedMap[currentCurrency.symbol] = candles
        recentCandlesMap = updatedMap
        emaAnalyses = repository.evaluateEmaStatus(allCurrencies, updatedMap)
    }

    // Initial evaluation for EMA 200 carousel
    LaunchedEffect(allCurrencies) {
        if (allCurrencies.isNotEmpty()) {
            emaAnalyses = repository.evaluateEmaStatus(allCurrencies, recentCandlesMap)
        }
    }

    val latestCandle = hoveredCandle ?: candles.lastOrNull()
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isEnglish = stringResource(R.string.tech_lang_code) == "en"
    val tomanShort = stringResource(R.string.tech_toman_short)
    val tomanSuffix = stringResource(R.string.tech_toman_suffix)

    val hasAnyEma = enabledEmas.isNotEmpty()
    val emaHeaderColor = Color(0xFF2196F3)
    val rsiColor = Color(0xFF7C4DFF)

    val animatedEmaBg by animateColorAsState(
        targetValue = if (hasAnyEma) emaHeaderColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "emaBg"
    )
    val animatedEmaBorder by animateColorAsState(
        targetValue = if (hasAnyEma) emaHeaderColor else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "emaBorder"
    )
    val animatedRsiBg by animateColorAsState(
        targetValue = if (showRsi) rsiColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rsiBg"
    )
    val animatedRsiBorder by animateColorAsState(
        targetValue = if (showRsi) rsiColor else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rsiBorder"
    )

    val chartMarginH by animateDpAsState(
        targetValue = if (hideSecondaryPanels || isTv) 0.dp else 10.dp,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "chartMarginH"
    )
    val chartMarginV by animateDpAsState(
        targetValue = if (hideSecondaryPanels || isTv) 0.dp else 4.dp,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "chartMarginV"
    )
    val chartCornerRadius by animateDpAsState(
        targetValue = if (hideSecondaryPanels) 0.dp else (if (isTv) 12.dp else 16.dp),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "chartCornerRadius"
    )

    androidx.activity.compose.BackHandler(isFullScreen || isLandscape || showDrawToolsInLandscape) {
        if (isLandscape) {
            toggleLandscape(false)
        } else {
            isFullScreen = false
            showDrawToolsInLandscape = false
        }
    }

    @Composable
    fun EmaDropdownMenu() {
        DropdownMenu(
            expanded = showEmaMenu,
            onDismissRequest = { showEmaMenu = false }
        ) {
            for (period in EmaPeriod.entries) {
                val style = emaVisualStyle(period, isDarkTheme)
                val checked = period in enabledEmas
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Canvas(modifier = Modifier.size(width = 16.dp, height = 8.dp)) {
                                drawLine(
                                    color = style.color,
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f),
                                    strokeWidth = style.strokeWidthDp.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    pathEffect = style.dashIntervals?.let { PathEffect.dashPathEffect(it, 0f) }
                                )
                            }
                            Text(
                                text = period.label,
                                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    trailingIcon = {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null
                        )
                    },
                    onClick = { toggleEmaPeriod(period) }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.tech_ema_preset_25_200)) },
                onClick = {
                    val preset = setOf(EmaPeriod.EMA_25, EmaPeriod.EMA_200)
                    enabledEmas = preset
                    lastNonEmptyEmas = preset
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.tech_ema_enable_all)) },
                onClick = {
                    val all = EmaPeriod.entries.toSet()
                    enabledEmas = all
                    lastNonEmptyEmas = all
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.tech_ema_disable_all)) },
                onClick = { enabledEmas = emptySet() }
            )
        }
    }

    @Composable
    fun TimeframeChipsRow(
        compact: Boolean,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = if (compact) 4.dp else 10.dp,
                    vertical = if (isTv) 5.dp else if (compact) 2.dp else 3.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(if (isTv) 8.dp else if (compact) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartTimeframe.entries.forEach { tf ->
                val isSelected = selectedTimeframe == tf
                val tfFocus = rememberTvFocusState(
                    isTv = isTv,
                    focusedScale = 1.08f,
                    focusedBorderWidth = 2.5.dp,
                    unfocusedBorderWidth = 1.dp,
                    unfocusedBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                Surface(
                    onClick = { selectedTimeframeName = tf.name },
                    interactionSource = tfFocus.interactionSource,
                    shape = RoundedCornerShape(if (compact) 10.dp else 14.dp),
                    color = if (tfFocus.isFocused || isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(tfFocus.borderWidth, tfFocus.borderColor),
                    modifier = Modifier
                        .height(if (isTv) 36.dp else if (compact) 26.dp else 28.dp)
                        .zIndex(if (tfFocus.isFocused) 1f else 0f)
                        .scale(tfFocus.scale)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = if (isTv) 14.dp else if (compact) 8.dp else 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val tfTitle = tf.localizedTitle(isEnglish)
                        Text(
                            text = tfTitle,
                            fontSize = if (isTv) 13.sp else if (compact) 10.sp else 11.sp,
                            fontWeight = if (isSelected || tfFocus.isFocused) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected || tfFocus.isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            fontFamily = getFontFamilyForText(tfTitle)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun HeaderEmaAndRsiControls(
        compact: Boolean,
        openMenuFromEma: Boolean
    ) {
        val emaFocus = rememberTvFocusState(
            isTv = isTv,
            focusedScale = 1.08f,
            unfocusedBorderColor = animatedEmaBorder
        )
        Box {
            Surface(
                onClick = {
                    if (openMenuFromEma) showEmaMenu = true else toggleHeaderEma()
                },
                interactionSource = emaFocus.interactionSource,
                shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
                color = animatedEmaBg,
                border = BorderStroke(emaFocus.borderWidth, emaFocus.borderColor),
                modifier = Modifier
                    .padding(end = 2.dp)
                    .scale(emaFocus.scale)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = if (isTv) 10.dp else if (compact) 6.dp else 7.dp,
                        vertical = if (isTv) 5.dp else if (compact) 4.dp else 5.dp
                    )
                ) {
                    if (!compact) {
                        Box(
                            modifier = Modifier
                                .size(if (isTv) 8.dp else 7.dp)
                                .clip(CircleShape)
                                .background(if (hasAnyEma) emaHeaderColor else MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    val emaBadge = when {
                        compact && enabledEmas.isNotEmpty() -> "EMA (${enabledEmas.size})".toLocalizedDigits(digitType)
                        !compact && enabledEmas.size > 1 -> "EMA ${enabledEmas.size}".toLocalizedDigits(digitType)
                        else -> "EMA"
                    }
                    Text(
                        text = emaBadge,
                        fontSize = if (isTv) 12.sp else if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasAnyEma) emaHeaderColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (openMenuFromEma) {
                EmaDropdownMenu()
            }
        }

        val rsiFocus = rememberTvFocusState(
            isTv = isTv,
            focusedScale = 1.08f,
            unfocusedBorderColor = animatedRsiBorder
        )
        Surface(
            onClick = { showRsi = !showRsi },
            interactionSource = rsiFocus.interactionSource,
            shape = RoundedCornerShape(if (compact) 8.dp else 12.dp),
            color = animatedRsiBg,
            border = BorderStroke(rsiFocus.borderWidth, rsiFocus.borderColor),
            modifier = Modifier
                .padding(end = 2.dp)
                .scale(rsiFocus.scale)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    horizontal = if (isTv) 10.dp else if (compact) 6.dp else 7.dp,
                    vertical = if (isTv) 5.dp else if (compact) 4.dp else 5.dp
                )
            ) {
                if (!compact) {
                    Box(
                        modifier = Modifier
                            .size(if (isTv) 8.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (showRsi) rsiColor else MaterialTheme.colorScheme.outline)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "RSI",
                    fontSize = if (isTv) 12.sp else if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showRsi) rsiColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (isTv) Modifier.padding(horizontal = 32.dp, vertical = 10.dp) else Modifier
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Header Bar: Dedicated Landscape Single-Row Bar OR Fullscreen/Normal Portrait Header
            if (isEffectiveLandscape) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isLandscape) toggleLandscape(false) else onBack()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.tech_back),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.widthIn(min = 72.dp, max = 160.dp)) {
                            Text(
                                text = currentCurrency.symbol,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            val lsPrice = latestCandle?.close ?: currentCurrency.currentPrice
                            AnimatedPriceText(
                                price = lsPrice,
                                symbol = currentCurrency.symbol,
                                digitType = digitType,
                                suffix = tomanShort,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                defaultColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Inline Timeframes take full horizontal center space in landscape
                        TimeframeChipsRow(
                            compact = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Drawing Tools toggle icon in landscape (keeps canvas spacious unless drawing)
                        Surface(
                            onClick = { showDrawToolsInLandscape = !showDrawToolsInLandscape },
                            shape = RoundedCornerShape(8.dp),
                            color = if (showDrawToolsInLandscape || activeTool != DrawingToolType.NONE) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (showDrawToolsInLandscape || activeTool != DrawingToolType.NONE) {
                                    MaterialTheme.colorScheme.primary
                                } else Color.Transparent
                            ),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = stringResource(R.string.tech_drawing_tools),
                                    tint = if (showDrawToolsInLandscape || activeTool != DrawingToolType.NONE) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        HeaderEmaAndRsiControls(compact = true, openMenuFromEma = true)

                        if (isLandscape) {
                            IconButton(
                                onClick = { toggleLandscape(false) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StayCurrentPortrait,
                                    contentDescription = stringResource(R.string.tech_portrait_mode),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showExportSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.tech_export_chart),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                AnimatedContent(
                    targetState = isFullScreen,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { -it / 2 })
                            .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { -it / 2 })
                    },
                    label = "headerAnim"
                ) { full ->
                    if (full) {
                        // Fullscreen compact header (Portrait/TV)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isTv) 12.dp else 8.dp, vertical = if (isTv) 6.dp else 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TvFocusableIconButton(
                                        onClick = {
                                            isFullScreen = false
                                            if (isLandscape) toggleLandscape(false)
                                        },
                                        isTv = isTv,
                                        modifier = Modifier.size(if (isTv) 40.dp else 36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FullscreenExit,
                                            contentDescription = stringResource(R.string.tech_exit_fullscreen),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    val localizedTitle = getLocalizedTitle(currentCurrency.symbol, currentCurrency.title)
                                    Text(
                                        text = localizedTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isTv) 15.sp else 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = getFontFamilyForText(localizedTitle)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${currentCurrency.symbol})",
                                        fontSize = if (isTv) 12.sp else 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val fsPrice = latestCandle?.close ?: currentCurrency.currentPrice
                                    AnimatedPriceText(
                                        price = fsPrice,
                                        symbol = currentCurrency.symbol,
                                        digitType = digitType,
                                        suffix = tomanShort,
                                        fontSize = if (isTv) 14.sp else 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        defaultColor = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(if (isTv) 6.dp else 2.dp)
                                ) {
                                    HeaderEmaAndRsiControls(compact = true, openMenuFromEma = true)

                                    if (!isTv) {
                                        IconButton(
                                            onClick = { toggleLandscape(!isLandscape) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isLandscape) Icons.Default.StayCurrentPortrait else Icons.Default.StayCurrentLandscape,
                                                contentDescription = if (isLandscape) stringResource(R.string.tech_portrait_mode) else stringResource(R.string.tech_landscape_view),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    TvFocusableIconButton(
                                        onClick = { showExportSheet = true },
                                        isTv = isTv,
                                        modifier = Modifier.size(if (isTv) 38.dp else 32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = stringResource(R.string.tech_export_chart),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(if (isTv) 20.dp else 18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Normal Top Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.background,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = if (isTv) 12.dp else 8.dp, vertical = if (isTv) 8.dp else 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    TvFocusableIconButton(
                                        onClick = {
                                            if (isLandscape) toggleLandscape(false)
                                            onBack()
                                        },
                                        isTv = isTv
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.tech_back),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(if (isTv) 6.dp else 0.dp))
                                    Column {
                                        val localizedTitle = getLocalizedTitle(currentCurrency.symbol, currentCurrency.title)
                                        Text(
                                            text = localizedTitle,
                                            fontSize = if (isTv) 17.sp else 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontFamily = getFontFamilyForText(localizedTitle),
                                            maxLines = 1
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentCurrency.symbol,
                                                fontSize = if (isTv) 12.sp else 11.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "•",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val tfLabel = selectedTimeframe?.localizedTitle(isEnglish) ?: stringResource(R.string.tech_custom_zoom)
                                            Text(
                                                text = tfLabel,
                                                fontSize = if (isTv) 12.sp else 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = getFontFamilyForText(tfLabel)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(if (isTv) 6.dp else 2.dp)
                                ) {
                                    HeaderEmaAndRsiControls(compact = false, openMenuFromEma = false)

                                    if (!isTv) {
                                        IconButton(
                                            onClick = { toggleLandscape(!isLandscape) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ScreenRotation,
                                                contentDescription = stringResource(R.string.tech_landscape_view),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    TvFocusableIconButton(
                                        onClick = { isFullScreen = true },
                                        isTv = isTv,
                                        modifier = Modifier.size(if (isTv) 38.dp else 34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fullscreen,
                                            contentDescription = stringResource(R.string.tech_fullscreen),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    TvFocusableIconButton(
                                        onClick = { showExportSheet = true },
                                        isTv = isTv,
                                        modifier = Modifier.size(if (isTv) 38.dp else 34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = stringResource(R.string.tech_export_chart),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Timeframe Chips Row (Shown below header only when not in landscape single-row mode)
            if (!isEffectiveLandscape) {
                TimeframeChipsRow(compact = false, modifier = Modifier.fillMaxWidth())
            }

            // 2.5 Multi-EMA Individual Toggles & Presets Bar (visible in portrait normal view)
            if (!isEffectiveLandscape) {
                AnimatedVisibility(
                    visible = !hideSecondaryPanels,
                    enter = expandVertically(animationSpec = tween(260, easing = FastOutSlowInEasing)) + fadeIn(tween(220)),
                    exit = shrinkVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeOut(tween(160))
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = if (isTv) 5.dp else 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(if (isTv) 8.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (period in EmaPeriod.entries) {
                        val isActive = period in enabledEmas
                        val style = emaVisualStyle(period, isDarkTheme)
                        val isSubtleLine = period == EmaPeriod.EMA_50 || period == EmaPeriod.EMA_100
                        val chipAccentText = if (isSubtleLine) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            style.color
                        }
                        val unfocusedBorder = if (isActive) {
                            if (isSubtleLine) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else style.color
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        }
                        val emaFocus = rememberTvFocusState(
                            isTv = isTv,
                            focusedScale = 1.08f,
                            focusedBorderWidth = 2.5.dp,
                            unfocusedBorderWidth = if (isActive) 1.3.dp else 1.dp,
                            unfocusedBorderColor = unfocusedBorder
                        )

                        Surface(
                            onClick = { toggleEmaPeriod(period) },
                            interactionSource = emaFocus.interactionSource,
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                emaFocus.isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                isActive -> style.color.copy(alpha = if (isSubtleLine) 0.12f else 0.16f)
                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = BorderStroke(emaFocus.borderWidth, emaFocus.borderColor),
                            modifier = Modifier
                                .height(if (isTv) 34.dp else 26.dp)
                                .zIndex(if (emaFocus.isFocused) 1f else 0f)
                                .scale(emaFocus.scale)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = if (isTv) 10.dp else 8.dp)
                            ) {
                                Canvas(modifier = Modifier.size(width = if (isTv) 16.dp else 14.dp, height = 6.dp)) {
                                    drawLine(
                                        color = if (isActive || emaFocus.isFocused) style.color else style.color.copy(alpha = 0.4f),
                                        start = Offset(0f, size.height / 2f),
                                        end = Offset(size.width, size.height / 2f),
                                        strokeWidth = style.strokeWidthDp.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        pathEffect = style.dashIntervals?.let { PathEffect.dashPathEffect(it, 0f) }
                                    )
                                }
                                Text(
                                    text = period.label,
                                    fontSize = if (isTv) 12.sp else 10.sp,
                                    fontWeight = if (isActive || emaFocus.isFocused) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive || emaFocus.isFocused) chipAccentText else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Quick Multi-EMA Presets
                    val allEmaSet = EmaPeriod.entries.toSet()
                    val duoPreset = setOf(EmaPeriod.EMA_25, EmaPeriod.EMA_200)
                    val presets = listOf(
                        stringResource(R.string.tech_ema_preset_25_200) to duoPreset,
                        stringResource(R.string.tech_ema_chip_all) to allEmaSet,
                        stringResource(R.string.tech_ema_chip_off) to emptySet()
                    )
                    for ((presetTitle, presetSet) in presets) {
                        val matchesPreset = enabledEmas == presetSet
                        val presetFocus = rememberTvFocusState(
                            isTv = isTv,
                            focusedScale = 1.08f,
                            unfocusedBorderColor = if (matchesPreset) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        Surface(
                            onClick = {
                                enabledEmas = presetSet
                                if (presetSet.isNotEmpty()) lastNonEmptyEmas = presetSet
                            },
                            interactionSource = presetFocus.interactionSource,
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                presetFocus.isFocused -> MaterialTheme.colorScheme.primaryContainer
                                matchesPreset -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            border = BorderStroke(presetFocus.borderWidth, presetFocus.borderColor),
                            modifier = Modifier
                                .height(if (isTv) 34.dp else 26.dp)
                                .zIndex(if (presetFocus.isFocused) 1f else 0f)
                                .scale(presetFocus.scale)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = if (isTv) 12.dp else 8.dp)
                            ) {
                                Text(
                                    text = presetTitle,
                                    fontSize = if (isTv) 12.sp else 9.5.sp,
                                    fontWeight = if (matchesPreset || presetFocus.isFocused) FontWeight.Bold else FontWeight.Medium,
                                    color = if (matchesPreset || presetFocus.isFocused) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = getFontFamilyForText(presetTitle)
                                )
                            }
                        }
                    }
                }
            }
            }

            // 3. Drawing Toolbar (always visible in portrait non-TV; collapsible on demand in landscape)
            if (!isTv && (!isEffectiveLandscape || showDrawToolsInLandscape)) {
                DrawingToolbar(
                    activeTool = activeTool,
                    onToolSelected = { activeTool = it },
                    activeColor = activeColor,
                    onColorSelected = { activeColor = it },
                    canUndo = undoStack.isNotEmpty() || drawings.isNotEmpty(),
                    onUndo = {
                        coroutineScope.launch {
                            drawingsAlpha.snapTo(0.25f)
                            if (undoStack.isNotEmpty()) {
                                drawings = undoStack.last()
                                undoStack = undoStack.dropLast(1)
                            } else if (drawings.isNotEmpty()) {
                                drawings = emptyList()
                            }
                            drawingsAlpha.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
                        }
                    },
                    canClear = drawings.isNotEmpty(),
                    onClear = {
                        if (drawings.isNotEmpty()) {
                            coroutineScope.launch {
                                drawingsAlpha.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                undoStack = undoStack + listOf(drawings)
                                drawings = emptyList()
                                drawingsAlpha.snapTo(1f)
                            }
                        }
                    }
                )
            }

            // 4. Main Candlestick Chart Area (Fills container with animated margin and border radius)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = chartMarginH, vertical = chartMarginV),
                contentAlignment = Alignment.Center
            ) {
                TechnicalCandlestickChart(
                    candles = candles,
                    currency = currentCurrency,
                    timeframe = selectedTimeframe,
                    enabledEmas = enabledEmas,
                    showRsi = showRsi,
                    digitType = digitType,
                    calendarType = calendarType,
                    activeTool = activeTool,
                    activeColor = activeColor,
                    drawings = drawings,
                    drawingsAlpha = drawingsAlpha.value,
                    cornerRadius = chartCornerRadius,
                    isTvMode = isTv,
                    isLandscapeMode = isEffectiveLandscape,
                    bullishColor = bullishColor,
                    bearishColor = bearishColor,
                    modifier = Modifier.fillMaxSize(),
                    onHoverCandleChanged = { hoveredCandle = it },
                    onZoomChanged = { selectedTimeframeName = null },
                    onDrawingCreated = { newD ->
                        undoStack = undoStack + listOf(drawings)
                        drawings = drawings + newD
                    },
                    onRequestTextInput = { pt ->
                        textTargetPoint = pt
                        inputNoteText = ""
                        showTextDialog = true
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            // 5. HUD Info Bar (Hidden immediately in landscape to maximize chart height)
            if (!isEffectiveLandscape) {
                AnimatedVisibility(
                    visible = !hideSecondaryPanels,
                    enter = expandVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(200))
                ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val candleTime = latestCandle?.time ?: System.currentTimeMillis()
                            val dateStr = formatCandleDateTime(candleTime, digitType, calendarType)
                            AnimatedContent(
                                targetState = dateStr,
                                transitionSpec = {
                                    fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                                },
                                label = "HudDate"
                            ) { targetDate ->
                                Text(
                                    text = targetDate,
                                    fontSize = if (isTv) 13.sp else 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val hudPrice = latestCandle?.close ?: currentCurrency.currentPrice
                            val hudPriceLabel = if (hoveredCandle != null) {
                                stringResource(R.string.tech_candle_price_label)
                            } else {
                                stringResource(R.string.tech_current_price_label)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedContent(
                                    targetState = hudPriceLabel,
                                    transitionSpec = {
                                        fadeIn(tween(150)) togetherWith fadeOut(tween(120))
                                    },
                                    label = "HudPriceLabel"
                                ) { label ->
                                    Text(
                                        text = label,
                                        fontSize = if (isTv) 14.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontFamily = getFontFamilyForText(label)
                                    )
                                }
                                AnimatedPriceText(
                                    price = hudPrice,
                                    symbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    suffix = tomanSuffix,
                                    fontSize = if (isTv) 15.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    defaultColor = MaterialTheme.colorScheme.primary,
                                    fontFamily = getFontFamilyForText(tomanSuffix)
                                )
                            }
                        }

                        if (latestCandle != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                HudItem(stringResource(R.string.tech_ohlc_open), latestCandle.open, digitType, isTv = isTv)
                                HudItem(stringResource(R.string.tech_ohlc_high), latestCandle.high, digitType, isTv = isTv)
                                HudItem(stringResource(R.string.tech_ohlc_low), latestCandle.low, digitType, isTv = isTv)
                                HudItem(stringResource(R.string.tech_ohlc_close), latestCandle.close, digitType, isTv = isTv)
                            }
                        }
                    }
                }
            }
            }

            // 6. Five Bottom Carousels (Hidden immediately in landscape to maximize chart height)
            val currenciesList = remember(allCurrencies) { allCurrencies.filter { it.category == CurrencyType.Currency } }
            val goldList = remember(allCurrencies) { allCurrencies.filter { it.category == CurrencyType.GoldAndCoin } }
            val cryptoList = remember(allCurrencies) { allCurrencies.filter { it.category == CurrencyType.Crypto } }
            val commoditiesList = remember(allCurrencies) {
                allCurrencies.filter {
                    it.symbol in listOf("BRENT", "BOURSE", "XAU", "MESGHAL") || it.category == CurrencyType.GoldAndCoin
                }
            }

            if (!isEffectiveLandscape) {
                AnimatedVisibility(
                    visible = !hideSecondaryPanels,
                    enter = expandVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(200))
                ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp,
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Category Tabs (5 Carousels)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = if (isTv) 5.dp else 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(if (isTv) 6.dp else 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tabs = listOf(
                                stringResource(R.string.tech_tab_currencies),
                                stringResource(R.string.tech_tab_gold),
                                stringResource(R.string.tech_tab_crypto),
                                stringResource(R.string.tech_tab_commodities),
                                stringResource(R.string.tech_tab_ema_status)
                            )
                            tabs.forEachIndexed { index, tabTitle ->
                                val isTabSelected = selectedCarouselTab == index
                                val tabInteraction = remember { MutableInteractionSource() }
                                val isTabFocused by tabInteraction.collectIsFocusedAsState()
                                val tabScale by animateFloatAsState(
                                    targetValue = if (isTv && isTabFocused) 1.08f else 1.0f,
                                    label = "tabScale"
                                )

                                Surface(
                                    onClick = { selectedCarouselTab = index },
                                    interactionSource = tabInteraction,
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isTv && isTabFocused -> MaterialTheme.colorScheme.primaryContainer
                                        isTabSelected -> {
                                            if (index == 4) Color(0xFFFFB300).copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.primaryContainer
                                        }
                                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                                    },
                                    border = BorderStroke(
                                        if (isTv && isTabFocused) 2.5.dp else 1.dp,
                                        when {
                                            isTv && isTabFocused -> MaterialTheme.colorScheme.primary
                                            isTabSelected -> {
                                                if (index == 4) Color(0xFFFFB300)
                                                else MaterialTheme.colorScheme.primary
                                            }
                                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        }
                                    ),
                                    modifier = Modifier
                                        .height(if (isTv) 36.dp else 28.dp)
                                        .zIndex(if (isTv && isTabFocused) 1f else 0f)
                                        .scale(tabScale)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = if (isTv) 14.dp else 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tabTitle,
                                            fontSize = if (isTv) 13.sp else 11.sp,
                                            fontWeight = if (isTabSelected || (isTv && isTabFocused)) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isTabSelected || (isTv && isTabFocused)) {
                                                if (index == 4) Color(0xFFFFB300)
                                                else MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontFamily = getFontFamilyForText(tabTitle)
                                        )
                                    }
                                }
                            }
                        }

                        // Active Carousel Reel with smooth directional slide & fade transition
                        AnimatedContent(
                            targetState = selectedCarouselTab,
                            transitionSpec = {
                                val isForward = targetState > initialState
                                (slideInHorizontally(animationSpec = tween(320, easing = FastOutSlowInEasing)) { width -> if (isForward) width / 2 else -width / 2 } +
                                        fadeIn(animationSpec = tween(320)))
                                    .togetherWith(
                                        slideOutHorizontally(animationSpec = tween(260, easing = FastOutSlowInEasing)) { width -> if (isForward) -width / 2 else width / 2 } +
                                                fadeOut(animationSpec = tween(200))
                                    )
                            },
                            label = "carouselSwitchAnim"
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> CarouselSection(
                                    title = "",
                                    items = currenciesList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    currencySuffix = tomanShort,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                1 -> CarouselSection(
                                    title = "",
                                    items = goldList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    currencySuffix = tomanShort,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                2 -> CarouselSection(
                                    title = "",
                                    items = cryptoList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    currencySuffix = tomanShort,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                3 -> CarouselSection(
                                    title = "",
                                    items = commoditiesList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    currencySuffix = tomanShort,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                4 -> EmaTouchCarouselSection(
                                    analyses = emaAnalyses,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    currencySuffix = tomanShort,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }

    // Export Options Modal Bottom Sheet
    if (showExportSheet) {
        fun buildExportBitmap() = renderChartToBitmap(
            candles = candles,
            enabledEmas = enabledEmas,
            showRsi = showRsi,
            isDark = isDarkTheme,
            drawings = drawings,
            bullishColor = bullishColor,
            bearishColor = bearishColor
        )

        ModalBottomSheet(
            onDismissRequest = {
                showExportSheet = false
                selectedExportFormat = null
            },
            sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = selectedExportFormat,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .then(if (isTv) Modifier.widthIn(max = 600.dp) else Modifier),
                    transitionSpec = {
                        if (targetState != null) {
                            (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { it / 2 } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { -it / 2 } + fadeOut(tween(200)))
                        } else {
                            (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { -it / 2 } + fadeIn(tween(300)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it / 2 } + fadeOut(tween(200)))
                        }
                    },
                    label = "exportAnim"
                ) { format ->
                    when (format) {
                        null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                val exportTitle = stringResource(R.string.tech_export_sheet_title)
                                Text(
                                    text = exportTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = getFontFamilyForText(exportTitle)
                                )
                                val exportTf = (selectedTimeframe ?: activeExportTimeframe).localizedTitle(isEnglish)
                                Text(
                                    text = stringResource(
                                        R.string.tech_export_sheet_subtitle,
                                        getLocalizedTitle(currentCurrency.symbol, currentCurrency.title),
                                        currentCurrency.symbol,
                                        exportTf
                                    ),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                ExportOptionCard(
                                    title = stringResource(R.string.tech_export_png_title),
                                    subtitle = stringResource(R.string.tech_export_png_desc),
                                    icon = Icons.Default.Image,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    isTv = isTv,
                                    showTrailingChevron = true,
                                    onClick = { selectedExportFormat = ExportFormat.PNG }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ExportOptionCard(
                                    title = stringResource(R.string.tech_export_pdf_title),
                                    subtitle = stringResource(R.string.tech_export_pdf_desc),
                                    icon = Icons.Default.PictureAsPdf,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
                                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    isTv = isTv,
                                    showTrailingChevron = true,
                                    onClick = { selectedExportFormat = ExportFormat.PDF }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                        ExportFormat.PNG -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TvFocusableIconButton(onClick = { selectedExportFormat = null }, isTv = isTv) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.tech_back),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        val pngSheetTitle = stringResource(R.string.tech_export_png_sheet_title)
                                        Text(
                                            text = pngSheetTitle,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText(pngSheetTitle)
                                        )
                                        Text(
                                            text = stringResource(R.string.tech_export_choose_how),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                ExportOptionCard(
                                    title = stringResource(R.string.tech_export_share),
                                    subtitle = stringResource(R.string.tech_export_share_png_desc),
                                    icon = Icons.Default.Share,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    iconBgColor = MaterialTheme.colorScheme.primary,
                                    iconTint = MaterialTheme.colorScheme.onPrimary,
                                    isTv = isTv,
                                    onClick = {
                                        showExportSheet = false
                                        selectedExportFormat = null
                                        TechnicalExportHelper.sharePng(
                                            context = context,
                                            bitmap = buildExportBitmap(),
                                            currency = currentCurrency,
                                            timeframe = activeExportTimeframe
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ExportOptionCard(
                                    title = stringResource(R.string.tech_export_save_files),
                                    subtitle = stringResource(R.string.tech_export_save_png_desc),
                                    icon = Icons.Default.FileDownload,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    isTv = isTv,
                                    onClick = {
                                        showExportSheet = false
                                        selectedExportFormat = null
                                        TechnicalExportHelper.savePngToDevice(
                                            context = context,
                                            bitmap = buildExportBitmap(),
                                            currency = currentCurrency,
                                            timeframe = activeExportTimeframe
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                        ExportFormat.PDF -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TvFocusableIconButton(onClick = { selectedExportFormat = null }, isTv = isTv) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.tech_back),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        val pdfSheetTitle = stringResource(R.string.tech_export_pdf_title)
                                        Text(
                                            text = pdfSheetTitle,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText(pdfSheetTitle)
                                        )
                                        Text(
                                            text = stringResource(R.string.tech_export_choose_how_pdf),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                ExportOptionCard(
                                    title = stringResource(R.string.tech_export_share),
                                    subtitle = stringResource(R.string.tech_export_share_pdf_desc),
                                    icon = Icons.Default.Share,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    iconBgColor = MaterialTheme.colorScheme.secondary,
                                    iconTint = MaterialTheme.colorScheme.onSecondary,
                                    isTv = isTv,
                                    onClick = {
                                        showExportSheet = false
                                        selectedExportFormat = null
                                        TechnicalExportHelper.sharePdf(
                                            context = context,
                                            chartBitmap = buildExportBitmap(),
                                            currency = currentCurrency,
                                            timeframe = activeExportTimeframe,
                                            candles = candles,
                                            enabledEmas = enabledEmas
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ExportOptionCard(
                                    title = stringResource(R.string.tech_export_save_files),
                                    subtitle = stringResource(R.string.tech_export_save_pdf_desc),
                                    icon = Icons.Default.FileDownload,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    iconBgColor = MaterialTheme.colorScheme.surfaceVariant,
                                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    isTv = isTv,
                                    onClick = {
                                        showExportSheet = false
                                        selectedExportFormat = null
                                        TechnicalExportHelper.savePdfToDevice(
                                            context = context,
                                            chartBitmap = buildExportBitmap(),
                                            currency = currentCurrency,
                                            timeframe = activeExportTimeframe,
                                            candles = candles,
                                            enabledEmas = enabledEmas
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Text Annotation Input Dialog
    if (showTextDialog && textTargetPoint != null) {
        AlertDialog(
            onDismissRequest = {
                showTextDialog = false
                textTargetPoint = null
                inputNoteText = ""
            },
            title = {
                val noteTitle = stringResource(R.string.tech_note_dialog_title)
                Text(
                    text = noteTitle,
                    fontWeight = FontWeight.Bold,
                    fontFamily = getFontFamilyForText(noteTitle)
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.tech_note_dialog_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputNoteText,
                        onValueChange = { inputNoteText = it },
                        placeholder = { Text(stringResource(R.string.tech_note_dialog_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputNoteText.isNotBlank() && textTargetPoint != null) {
                            val newTextDrawing = ChartDrawing.Text(
                                id = UUID.randomUUID().toString(),
                                position = textTargetPoint!!,
                                text = inputNoteText.trim(),
                                color = activeColor.toArgb().toLong() and 0xFFFFFFFFL
                            )
                            undoStack = undoStack + listOf(drawings)
                            drawings = drawings + newTextDrawing
                        }
                        showTextDialog = false
                        textTargetPoint = null
                        inputNoteText = ""
                    }
                ) {
                    Text(stringResource(R.string.tech_note_dialog_confirm), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTextDialog = false
                        textTargetPoint = null
                        inputNoteText = ""
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Smooth directional animated numeric text for financial metrics.
 * - Slides vertically based on whether value increased or decreased.
 * - Flashes green (increase) or red (decrease) on change.
 */
@Composable
fun AnimatedNumericText(
    value: Double,
    displayText: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    defaultColor: Color = MaterialTheme.colorScheme.onSurface,
    fontFamily: FontFamily? = null,
    enableFlash: Boolean = true
) {
    var previousValue by remember { mutableDoubleStateOf(value) }
    var isFirstComposition by remember { mutableStateOf(true) }
    val flashAnim = remember { Animatable(0f) }
    var flashColor by remember { mutableStateOf(defaultColor) }

    LaunchedEffect(value) {
        if (isFirstComposition) {
            isFirstComposition = false
            previousValue = value
            return@LaunchedEffect
        }
        if (enableFlash && value != previousValue) {
            flashColor = if (value > previousValue) Color(0xFF00C853) else Color(0xFFFF3D00)
            try {
                flashAnim.snapTo(1f)
                flashAnim.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
            } catch (_: Exception) {}
        }
        previousValue = value
    }

    val currentColor = if (enableFlash && flashAnim.value > 0f) {
        lerp(defaultColor, flashColor, flashAnim.value)
    } else {
        defaultColor
    }

    AnimatedContent(
        targetState = Pair(value, displayText),
        transitionSpec = {
            val isIncreasing = targetState.first >= initialState.first
            if (isIncreasing) {
                (slideInVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> height / 2 } + fadeIn(tween(140)))
                    .togetherWith(
                        slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeOut(tween(120))
                    )
            } else {
                (slideInVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeIn(tween(140)))
                    .togetherWith(
                        slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> height / 2 } + fadeOut(tween(120))
                    )
            }.using(SizeTransform(clip = false))
        },
        modifier = modifier,
        label = "AnimatedNumericText"
    ) { (_, text) ->
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = currentColor,
            fontFamily = fontFamily
        )
    }
}

/**
 * Animated price text that formats price and provides directional slide and color flash animations.
 */
@Composable
fun AnimatedPriceText(
    price: Double,
    symbol: String,
    digitType: String,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    fontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    defaultColor: Color = MaterialTheme.colorScheme.onSurface,
    fontFamily: FontFamily? = null,
    enableFlash: Boolean = true
) {
    val formattedPrice = formatPrice(price, digitType, symbol)
    val displayText = buildString {
        if (prefix.isNotEmpty()) append(prefix)
        append(formattedPrice)
        if (suffix.isNotEmpty()) append(suffix)
    }
    AnimatedNumericText(
        value = price,
        displayText = displayText,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        defaultColor = defaultColor,
        fontFamily = fontFamily,
        enableFlash = enableFlash
    )
}

/**
 * Animated percentage text that slides directionally when percentage changes.
 */
@Composable
fun AnimatedPercentageText(
    percentage: Double,
    digitType: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 10.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    upColor: Color = Color(0xFF00C853),
    downColor: Color = Color(0xFFFF3D00)
) {
    val isNegative = percentage < 0
    val targetColor = if (isNegative) downColor else upColor
    val formatted = String.format(Locale.US, "%+.1f%%", percentage)
    val displayText = formatted.toLocalizedDigits(digitType)

    AnimatedNumericText(
        value = percentage,
        displayText = displayText,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        defaultColor = targetColor,
        enableFlash = false
    )
}

@Composable
private fun HudItem(
    label: String,
    value: Double,
    digitType: String,
    isTv: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = if (isTv) 12.sp else 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            fontFamily = getFontFamilyForText(label)
        )
        val formattedVal = if (abs(value) >= 1.0) String.format(Locale.US, "%,.1f", value) else String.format(Locale.US, "%.4f", value)
        val displayStr = formattedVal.toLocalizedDigits(digitType)
        AnimatedNumericText(
            value = value,
            displayText = displayStr,
            fontSize = if (isTv) 15.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            defaultColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CarouselSection(
    title: String,
    items: List<CurrencyItem>,
    selectedSymbol: String,
    digitType: String,
    currencySuffix: String = "",
    showTitle: Boolean = true,
    isTv: Boolean = false,
    onSelect: (CurrencyItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (showTitle && title.isNotEmpty()) {
            Text(
                text = title,
                fontSize = if (isTv) 15.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontFamily = getFontFamilyForText(title)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = if (isTv) 16.dp else 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isTv) 12.dp else 8.dp)
        ) {
            items(items, key = { it.symbol }) { item ->
                val isSelected = item.symbol == selectedSymbol
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val cardScale by animateFloatAsState(
                    targetValue = if (isTv && isFocused) 1.06f else 1.0f,
                    label = "carouselCardScale"
                )
                val isNegative = item.changePercentage < 0
                val upColor = Color(0xFF00C853)
                val downColor = Color(0xFFFF3D00)

                Surface(
                    onClick = { onSelect(item) },
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(16.dp),
                    color = when {
                        isTv && isFocused -> MaterialTheme.colorScheme.primaryContainer
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        if (isTv && isFocused) 2.5.dp else 1.2.dp,
                        when {
                            isTv && isFocused -> MaterialTheme.colorScheme.primary
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        }
                    ),
                    modifier = Modifier
                        .width(if (isTv) 165.dp else 135.dp)
                        .scale(cardScale)
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = if (isTv) 12.dp else 10.dp,
                            vertical = if (isTv) 10.dp else 8.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.symbol,
                                fontSize = if (isTv) 14.sp else 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected || (isTv && isFocused)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            AnimatedPercentageText(
                                percentage = item.changePercentage,
                                digitType = digitType,
                                fontSize = if (isTv) 12.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                upColor = upColor,
                                downColor = downColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        AnimatedPriceText(
                            price = item.currentPrice,
                            symbol = item.symbol,
                            digitType = digitType,
                            suffix = currencySuffix,
                            fontSize = if (isTv) 14.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            defaultColor = if (isSelected || (isTv && isFocused)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5th Carousel: Special Horizontal Reel ("Horizontal Carousel") for EMA 200 touch status.
 * - Items that reached EMA 200 from above -> Light Red
 * - Items that reached EMA 200 from below -> Light Green
 */
@Composable
private fun EmaTouchCarouselSection(
    analyses: List<EmaAssetAnalysis>,
    selectedSymbol: String,
    digitType: String,
    currencySuffix: String = "",
    showTitle: Boolean = true,
    isTv: Boolean = false,
    onSelect: (CurrencyItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (showTitle) {
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .padding(vertical = 12.dp)
                } else {
                    Modifier.padding(vertical = 4.dp)
                }
            )
    ) {
        if (showTitle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300))
                )
                Spacer(modifier = Modifier.width(6.dp))
                val carouselTitle = stringResource(R.string.tech_ema_carousel_title)
                Text(
                    text = carouselTitle,
                    fontSize = if (isTv) 15.sp else 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = getFontFamilyForText(carouselTitle)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Sort items so touched ones appear first
        val sortedAnalyses = remember(analyses) {
            analyses.sortedWith(
                compareBy<EmaAssetAnalysis> {
                    when (it.status) {
                        EmaTouchStatus.TOUCHED_FROM_ABOVE, EmaTouchStatus.TOUCHED_FROM_BELOW -> 0
                        else -> 1
                    }
                }.thenBy { abs(it.distancePercentage) }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = if (isTv) 16.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isTv) 14.dp else 10.dp)
        ) {
            items(sortedAnalyses, key = { it.currencyItem.symbol }) { analysis ->
                val isSelected = analysis.currencyItem.symbol == selectedSymbol
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val cardScale by animateFloatAsState(
                    targetValue = if (isTv && isFocused) 1.06f else 1.0f,
                    label = "emaCardScale"
                )

                // Determine background and border colors according to specification:
                // "Currencies reaching EMA 200 from above should be light red. Currencies reaching EMA 200 from below should be light green"
                val (cardBgColor, borderColor, statusBadge, statusIcon, statusColor) = when (analysis.status) {
                    EmaTouchStatus.TOUCHED_FROM_ABOVE -> {
                        // Light Red tint
                        Tuple5(
                            Color(0x38FF5252), // Pale red background
                            Color(0xFFFF5252), // Red border
                            stringResource(R.string.tech_ema_touched_above),
                            Icons.AutoMirrored.Filled.TrendingDown,
                            Color(0xFFFF1744)
                        )
                    }
                    EmaTouchStatus.TOUCHED_FROM_BELOW -> {
                        // Light Green tint
                        Tuple5(
                            Color(0x384CAF50), // Pale green background
                            Color(0xFF4CAF50), // Green border
                            stringResource(R.string.tech_ema_touched_below),
                            Icons.AutoMirrored.Filled.TrendingUp,
                            Color(0xFF00C853)
                        )
                    }
                    EmaTouchStatus.ABOVE_EMA -> {
                        Tuple5(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            stringResource(R.string.tech_ema_above),
                            Icons.AutoMirrored.Filled.TrendingUp,
                            Color(0xFF4CAF50)
                        )
                    }
                    else -> {
                        Tuple5(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            stringResource(R.string.tech_ema_below),
                            Icons.AutoMirrored.Filled.TrendingDown,
                            Color(0xFFFF5252)
                        )
                    }
                }

                Surface(
                    onClick = { onSelect(analysis.currencyItem) },
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(18.dp),
                    color = when {
                        isTv && isFocused -> MaterialTheme.colorScheme.primaryContainer
                        else -> cardBgColor
                    },
                    border = BorderStroke(
                        if (isTv && isFocused) 2.5.dp else if (isSelected) 2.dp else 1.2.dp,
                        if (isTv && isFocused) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.primary else borderColor
                    ),
                    modifier = Modifier
                        .width(if (isTv) 210.dp else 185.dp)
                        .scale(cardScale)
                ) {
                    Column(
                        modifier = Modifier.padding(if (isTv) 14.dp else 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = analysis.currencyItem.symbol,
                                fontSize = if (isTv) 15.sp else 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(if (isTv) 14.dp else 12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    AnimatedPercentageText(
                                        percentage = analysis.distancePercentage,
                                        digitType = digitType,
                                        fontSize = if (isTv) 12.sp else 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        upColor = statusColor,
                                        downColor = statusColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        AnimatedPriceText(
                            price = analysis.currentPrice,
                            symbol = analysis.currencyItem.symbol,
                            digitType = digitType,
                            suffix = currencySuffix,
                            fontSize = if (isTv) 15.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            defaultColor = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EMA: ",
                                fontSize = if (isTv) 12.sp else 10.sp,
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.SemiBold
                            )
                            AnimatedPriceText(
                                price = analysis.ema200,
                                symbol = analysis.currencyItem.symbol,
                                digitType = digitType,
                                fontSize = if (isTv) 12.sp else 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                defaultColor = Color(0xFFFFB300)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status pill
                        Text(
                            text = statusBadge,
                            fontSize = if (isTv) 11.sp else 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = getFontFamilyForText(statusBadge)
                        )
                    }
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

@Composable
fun DrawingToolbar(
    activeTool: DrawingToolType,
    onToolSelected: (DrawingToolType) -> Unit,
    activeColor: Color,
    onColorSelected: (Color) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canClear: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toolbarScope = rememberCoroutineScope()
    val undoRotation = remember { Animatable(0f) }
    val undoScale = remember { Animatable(1f) }
    val clearRotation = remember { Animatable(0f) }
    val clearScale = remember { Animatable(1f) }

    val drawingColors = listOf(
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF00E676), // Green
        Color(0xFFFF5252), // Red
        Color(0xFF00E5FF), // Cyan
        Color(0xFFFF9100), // Orange
        Color(0xFFE040FB), // Magenta / Purple
        Color(0xFFFFFFFF)  // White
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)) {
            // Row 1: Tool Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tools = listOf(
                    DrawingToolType.NONE to (Icons.Default.PanTool to stringResource(R.string.tech_tool_pan)),
                    DrawingToolType.TREND_LINE to (Icons.AutoMirrored.Filled.ShowChart to stringResource(R.string.tech_tool_trend)),
                    DrawingToolType.HORIZONTAL_LINE to (Icons.Default.HorizontalRule to stringResource(R.string.tech_tool_hline)),
                    DrawingToolType.RAY to (Icons.Default.NorthEast to stringResource(R.string.tech_tool_ray)),
                    DrawingToolType.RECTANGLE to (Icons.Default.CropSquare to stringResource(R.string.tech_tool_rect)),
                    DrawingToolType.FIBONACCI to (Icons.Default.AutoGraph to stringResource(R.string.tech_tool_fib)),
                    DrawingToolType.BRUSH to (Icons.Default.Brush to stringResource(R.string.tech_tool_brush)),
                    DrawingToolType.TEXT to (Icons.Default.TextFields to stringResource(R.string.tech_tool_text))
                )

                tools.forEach { (tool, iconAndName) ->
                    val (icon, title) = iconAndName
                    val isSelected = activeTool == tool

                    val animatedBg by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                        animationSpec = tween(240, easing = FastOutSlowInEasing),
                        label = "toolBg"
                    )
                    val animatedBorderColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        animationSpec = tween(240, easing = FastOutSlowInEasing),
                        label = "toolBorder"
                    )
                    val animatedContentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(240, easing = FastOutSlowInEasing),
                        label = "toolContent"
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "toolScale"
                    )

                    Surface(
                        onClick = { onToolSelected(tool) },
                        shape = RoundedCornerShape(12.dp),
                        color = animatedBg,
                        border = BorderStroke(1.dp, animatedBorderColor),
                        modifier = Modifier
                            .height(32.dp)
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                modifier = Modifier.size(15.dp),
                                tint = animatedContentColor
                            )
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedContentColor,
                                fontFamily = getFontFamilyForText(title)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Undo Button with animated feedback
                IconButton(
                    onClick = {
                        if (canUndo) {
                            toolbarScope.launch {
                                launch {
                                    undoRotation.snapTo(0f)
                                    undoRotation.animateTo(-360f, tween(360, easing = FastOutSlowInEasing))
                                    undoRotation.snapTo(0f)
                                }
                                launch {
                                    undoScale.animateTo(0.65f, tween(90))
                                    undoScale.animateTo(1.25f, tween(130))
                                    undoScale.animateTo(1.0f, tween(100))
                                }
                            }
                            onUndo()
                        }
                    },
                    enabled = canUndo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(R.string.tech_tool_undo),
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                rotationZ = undoRotation.value
                                scaleX = undoScale.value
                                scaleY = undoScale.value
                            },
                        tint = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }

                // Clear All Button with animated feedback
                IconButton(
                    onClick = {
                        if (canClear) {
                            toolbarScope.launch {
                                launch {
                                    clearRotation.animateTo(-22f, tween(60))
                                    clearRotation.animateTo(22f, tween(70))
                                    clearRotation.animateTo(-12f, tween(60))
                                    clearRotation.animateTo(12f, tween(60))
                                    clearRotation.animateTo(0f, tween(50))
                                }
                                launch {
                                    clearScale.animateTo(0.65f, tween(90))
                                    clearScale.animateTo(1.3f, tween(130))
                                    clearScale.animateTo(1.0f, tween(100))
                                }
                            }
                            onClear()
                        }
                    },
                    enabled = canClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = stringResource(R.string.tech_tool_clear),
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                rotationZ = clearRotation.value
                                scaleX = clearScale.value
                                scaleY = clearScale.value
                            },
                        tint = if (canClear) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }
            }

            // Row 2: Color Palette & Hint (shown when drawing tool is active with smooth expand/collapse)
            AnimatedVisibility(
                visible = activeTool != DrawingToolType.NONE,
                enter = expandVertically(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(280)),
                exit = shrinkVertically(
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(200))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colorLabel = stringResource(R.string.tech_tool_color)
                    Text(
                        text = colorLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontFamily = getFontFamilyForText(colorLabel)
                    )

                    drawingColors.forEach { col ->
                        val isSelected = activeColor == col
                        val animatedCircleScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.18f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "colorCircleScale"
                        )
                        val animatedBorderWidth by animateDpAsState(
                            targetValue = if (isSelected) 2.5.dp else 1.dp,
                            animationSpec = tween(200),
                            label = "colorBorderWidth"
                        )
                        val animatedBorderColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                            animationSpec = tween(200),
                            label = "colorBorderColor"
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = animatedCircleScale
                                    scaleY = animatedCircleScale
                                }
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = animatedBorderWidth,
                                    color = animatedBorderColor,
                                    shape = CircleShape
                                )
                                .clickable { onColorSelected(col) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (col == Color.White) Color.Black else Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    AnimatedContent(
                        targetState = activeTool,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "hintAnim"
                    ) { tool ->
                        val hintText = when (tool) {
                            DrawingToolType.HORIZONTAL_LINE, DrawingToolType.TEXT -> stringResource(R.string.tech_tool_hint_tap)
                            else -> stringResource(R.string.tech_tool_hint_drag)
                        }
                        Text(
                            text = hintText,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontFamily = getFontFamilyForText(hintText)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    borderColor: Color,
    iconBgColor: Color,
    iconTint: Color,
    isTv: Boolean,
    showTrailingChevron: Boolean = false,
    onClick: () -> Unit
) {
    val tvFocus = rememberTvFocusState(
        isTv = isTv,
        focusedScale = 1.02f,
        unfocusedBorderColor = borderColor
    )
    Surface(
        onClick = onClick,
        interactionSource = tvFocus.interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = if (tvFocus.isFocused) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else containerColor,
        border = BorderStroke(tvFocus.borderWidth, tvFocus.borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(tvFocus.scale)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = if (showTrailingChevron) null else title, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = if (isTv) 15.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = getFontFamilyForText(title)
                )
                Text(
                    text = subtitle,
                    fontSize = if (isTv) 12.sp else 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (showTrailingChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
