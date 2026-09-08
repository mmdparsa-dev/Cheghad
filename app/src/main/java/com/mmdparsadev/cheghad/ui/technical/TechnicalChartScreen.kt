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
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartDrawing
import com.mmdparsadev.cheghad.data.models.ChartPoint
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.DrawingToolType
import com.mmdparsadev.cheghad.data.models.EmaAssetAnalysis
import com.mmdparsadev.cheghad.data.models.EmaTouchStatus
import com.mmdparsadev.cheghad.data.repository.TechnicalRepository
import com.mmdparsadev.cheghad.formatPrice
import com.mmdparsadev.cheghad.getLocalizedTitle
import com.mmdparsadev.cheghad.ui.theme.getFontFamilyForText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ExportFormat {
    PNG, PDF
}

@Composable
fun isTvDevice(): Boolean {
    val context = LocalContext.current
    val uiModeManager = remember(context) {
        context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    }
    return uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicalChartScreen(
    initialCurrency: CurrencyItem,
    allCurrencies: List<CurrencyItem>,
    digitType: String = "fa",
    bullishColor: Color = Color(0xFF00C853),
    bearishColor: Color = Color(0xFFFF3D00),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { TechnicalRepository() }

    var currentCurrency by remember { mutableStateOf(initialCurrency) }
    var selectedTimeframe by remember { mutableStateOf<ChartTimeframe?>(ChartTimeframe.D1) }
    var activeExportTimeframe by remember { mutableStateOf(ChartTimeframe.D1) }
    var showEma by remember { mutableStateOf(true) }
    var showRsi by remember { mutableStateOf(true) }

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
    var isFullScreen by remember { mutableStateOf(false) }
    var selectedCarouselTab by remember { mutableIntStateOf(0) }

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
        activeExportTimeframe = tf
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

    val upColor = bullishColor
    val downColor = bearishColor
    val emaColor = Color(0xFFFFB300)
    val rsiColor = Color(0xFF7C4DFF)

    val animatedEmaBg by animateColorAsState(
        targetValue = if (showEma) emaColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "emaBg"
    )
    val animatedEmaBorder by animateColorAsState(
        targetValue = if (showEma) emaColor else Color.Transparent,
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

    val isTv = isTvDevice()

    val chartMarginH by animateDpAsState(
        targetValue = if (isFullScreen || isTv) 0.dp else 10.dp,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "chartMarginH"
    )
    val chartMarginV by animateDpAsState(
        targetValue = if (isFullScreen || isTv) 0.dp else 4.dp,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "chartMarginV"
    )
    val chartCornerRadius by animateDpAsState(
        targetValue = if (isFullScreen) 0.dp else (if (isTv) 12.dp else 16.dp),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "chartCornerRadius"
    )

    androidx.activity.compose.BackHandler(isFullScreen) {
        isFullScreen = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
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
            // 1. Header Bar with AnimatedContent
            AnimatedContent(
                targetState = isFullScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { -it / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { -it / 2 })
                },
                label = "headerAnim"
            ) { full ->
                if (full) {
                    // Fullscreen compact header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isFullScreen = false }) {
                                    Icon(
                                        imageVector = Icons.Default.FullscreenExit,
                                        contentDescription = "خروج از تمام‌صفحه",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                val localizedTitle = getLocalizedTitle(currentCurrency.symbol, currentCurrency.title)
                                Text(
                                    text = localizedTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = getFontFamilyForText(localizedTitle)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${currentCurrency.symbol})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val fsPrice = latestCandle?.close ?: currentCurrency.currentPrice
                                AnimatedPriceText(
                                    price = fsPrice,
                                    symbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    suffix = " ت",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    defaultColor = MaterialTheme.colorScheme.primary
                                )
                            }

                            // EMA, RSI & Export
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    onClick = { showEma = !showEma },
                                    shape = RoundedCornerShape(8.dp),
                                    color = animatedEmaBg,
                                    border = BorderStroke(1.dp, animatedEmaBorder),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "EMA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showEma) emaColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    onClick = { showRsi = !showRsi },
                                    shape = RoundedCornerShape(8.dp),
                                    color = animatedRsiBg,
                                    border = BorderStroke(1.dp, animatedRsiBorder),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "RSI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (showRsi) rsiColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { showExportSheet = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "خروجی",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
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
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "بازگشت",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Column {
                                    val localizedTitle = getLocalizedTitle(currentCurrency.symbol, currentCurrency.title)
                                    Text(
                                        text = localizedTitle,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontFamily = getFontFamilyForText(localizedTitle)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentCurrency.symbol,
                                            fontSize = 11.sp,
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
                                        Text(
                                            text = selectedTimeframe?.titleFa ?: "زوم سفارشی",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = getFontFamilyForText("زوم سفارشی")
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Animated EMA button
                                Surface(
                                    onClick = { showEma = !showEma },
                                    shape = RoundedCornerShape(12.dp),
                                    color = animatedEmaBg,
                                    border = BorderStroke(1.dp, animatedEmaBorder),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (showEma) emaColor else MaterialTheme.colorScheme.outline)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "EMA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showEma) emaColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Animated RSI button
                                Surface(
                                    onClick = { showRsi = !showRsi },
                                    shape = RoundedCornerShape(12.dp),
                                    color = animatedRsiBg,
                                    border = BorderStroke(1.dp, animatedRsiBorder),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (showRsi) rsiColor else MaterialTheme.colorScheme.outline)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "RSI",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showRsi) rsiColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Fullscreen button
                                IconButton(
                                    onClick = { isFullScreen = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "تمام‌صفحه",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Share button
                                IconButton(
                                    onClick = { showExportSheet = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "خروجی چارت",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Timeframe Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartTimeframe.entries.forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    val chipScale by animateFloatAsState(
                        targetValue = if (isTv && isFocused) 1.1f else 1.0f,
                        label = "chipScale"
                    )

                    Surface(
                        onClick = { selectedTimeframe = tf },
                        interactionSource = interactionSource,
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isTv && isFocused -> MaterialTheme.colorScheme.primaryContainer
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = BorderStroke(
                            if (isTv && isFocused) 2.5.dp else 1.dp,
                            when {
                                isTv && isFocused -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            }
                        ),
                        modifier = Modifier
                            .height(if (isTv) 34.dp else 28.dp)
                            .scale(chipScale)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = if (isTv) 14.dp else 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf.titleFa,
                                fontSize = if (isTv) 13.sp else 11.sp,
                                fontWeight = if (isSelected || (isTv && isFocused)) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected || (isTv && isFocused)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                fontFamily = getFontFamilyForText(tf.titleFa)
                            )
                        }
                    }
                }
            }

            // 3. Drawing Toolbar with animated undo and clear (Hidden in TV mode to prioritize widescreen chart area)
            if (!isTv) {
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
                    showEma = showEma,
                    showRsi = showRsi,
                    digitType = digitType,
                    activeTool = activeTool,
                    activeColor = activeColor,
                    drawings = drawings,
                    drawingsAlpha = drawingsAlpha.value,
                    cornerRadius = chartCornerRadius,
                    isTvMode = isTv,
                    bullishColor = bullishColor,
                    bearishColor = bearishColor,
                    modifier = Modifier.fillMaxSize(),
                    onHoverCandleChanged = { hoveredCandle = it },
                    onZoomChanged = { selectedTimeframe = null },
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

            // 5. HUD Info Bar (Collapsible with smooth animation, located directly below candlestick chart)
            AnimatedVisibility(
                visible = !isFullScreen,
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
                            val formattedDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(candleTime))
                            val dateStr = if (digitType == "fa") toPersianDigits(formattedDate) else formattedDate
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
                            val hudPriceLabel = if (hoveredCandle != null) "قیمت کندل: " else "قیمت فعلی: "

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
                                    suffix = " تومان",
                                    fontSize = if (isTv) 15.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    defaultColor = MaterialTheme.colorScheme.primary,
                                    fontFamily = getFontFamilyForText("تومان")
                                )
                            }
                        }

                        if (latestCandle != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                HudItem("باز", latestCandle.open, digitType, isTv = isTv)
                                HudItem("بالا", latestCandle.high, digitType, isTv = isTv)
                                HudItem("پایین", latestCandle.low, digitType, isTv = isTv)
                                HudItem("بسته", latestCandle.close, digitType, isTv = isTv)
                            }
                        }
                    }
                }
            }

            // 6. Five Bottom Carousels (Collapsible with smooth animation)
            val currenciesList = remember(allCurrencies) { allCurrencies.filter { it.category == CurrencyType.Currency } }
            val goldList = remember(allCurrencies) { allCurrencies.filter { it.category == CurrencyType.GoldAndCoin } }
            val cryptoList = remember(allCurrencies) { allCurrencies.filter { it.category == CurrencyType.Crypto } }
            val commoditiesList = remember(allCurrencies) {
                allCurrencies.filter {
                    it.symbol in listOf("BRENT", "BOURSE", "XAU", "MESGHAL") || it.category == CurrencyType.GoldAndCoin
                }
            }

            AnimatedVisibility(
                visible = !isFullScreen,
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
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tabs = listOf(
                                "۱. ارزها",
                                "۲. طلا و سکه",
                                "۳. رمزارزها",
                                "۴. کالاها",
                                "۵. وضعیت EMA 200"
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
                                        .height(if (isTv) 34.dp else 28.dp)
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
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                1 -> CarouselSection(
                                    title = "",
                                    items = goldList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                2 -> CarouselSection(
                                    title = "",
                                    items = cryptoList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                3 -> CarouselSection(
                                    title = "",
                                    items = commoditiesList,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
                                    showTitle = false,
                                    isTv = isTv,
                                    onSelect = { currentCurrency = it }
                                )
                                4 -> EmaTouchCarouselSection(
                                    analyses = emaAnalyses,
                                    selectedSymbol = currentCurrency.symbol,
                                    digitType = digitType,
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

    // Export Options Modal Bottom Sheet
    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showExportSheet = false
                selectedExportFormat = null
            },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AnimatedContent(
                targetState = selectedExportFormat,
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
                        // Level 1: Choose format (PNG or PDF)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = "خروجی و اشتراک‌گذاری نمودار تکنیکال",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = getFontFamilyForText("خروجی")
                            )
                            Text(
                                text = "${currentCurrency.title} (${currentCurrency.symbol}) - تایم‌فریم ${selectedTimeframe?.titleFa ?: activeExportTimeframe.titleFa}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // PNG Export Option Card
                            Surface(
                                onClick = { selectedExportFormat = ExportFormat.PNG },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "خروجی تصویر با کیفیت بالا (PNG)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText("خروجی")
                                        )
                                        Text(
                                            text = "شامل تصویر چارت، کندل‌ها، اندیکاتورها و رسم‌های تحلیلی",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // PDF Export Option Card
                            Surface(
                                onClick = { selectedExportFormat = ExportFormat.PDF },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "سند رسمی گزارش تحلیلی (PDF)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText("گزارش")
                                        )
                                        Text(
                                            text = "فایل PDF شامل چارت تکنیکال، ارقام آماری OHLC، EMA 200 و RSI",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    ExportFormat.PNG -> {
                        // Level 2 for PNG: Share OR Save to Files
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { selectedExportFormat = null }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "بازگشت",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "خروجی تصویر چارت (PNG)",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = getFontFamilyForText("خروجی")
                                    )
                                    Text(
                                        text = "نحوه دریافت تصویر را انتخاب کنید:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action 1: Share
                            Surface(
                                onClick = {
                                    showExportSheet = false
                                    selectedExportFormat = null
                                    val bitmap = renderChartToBitmap(
                                        candles = candles,
                                        showEma = showEma,
                                        showRsi = showRsi,
                                        drawings = drawings,
                                        bullishColor = bullishColor,
                                        bearishColor = bearishColor
                                    )
                                    TechnicalExportHelper.sharePng(
                                        context = context,
                                        bitmap = bitmap,
                                        currency = currentCurrency,
                                        timeframe = activeExportTimeframe
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "اشتراک‌گذاری",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "اشتراک‌گذاری (Share)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText("اشتراک‌گذاری")
                                        )
                                        Text(
                                            text = "ارسال تصویر چارت به سایر برنامه‌ها (تلگرام، ایتا و...)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action 2: Save to Files
                            Surface(
                                onClick = {
                                    showExportSheet = false
                                    selectedExportFormat = null
                                    val bitmap = renderChartToBitmap(
                                        candles = candles,
                                        showEma = showEma,
                                        showRsi = showRsi,
                                        drawings = drawings,
                                        bullishColor = bullishColor,
                                        bearishColor = bearishColor
                                    )
                                    TechnicalExportHelper.savePngToDevice(
                                        context = context,
                                        bitmap = bitmap,
                                        currency = currentCurrency,
                                        timeframe = activeExportTimeframe
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = "ذخیره در فایل‌ها",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "ذخیره در فایل‌ها (Save to Files)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText("ذخیره")
                                        )
                                        Text(
                                            text = "ذخیره مستقیم در گالری و پوشه تصاویر دستگاه (Pictures/Cheghad)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    ExportFormat.PDF -> {
                        // Level 2 for PDF: Share OR Save to Files
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { selectedExportFormat = null }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "بازگشت",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = "سند رسمی گزارش تحلیلی (PDF)",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = getFontFamilyForText("گزارش")
                                    )
                                    Text(
                                        text = "نحوه دریافت فایل PDF را انتخاب کنید:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action 1: Share
                            Surface(
                                onClick = {
                                    showExportSheet = false
                                    selectedExportFormat = null
                                    val bitmap = renderChartToBitmap(
                                        candles = candles,
                                        showEma = showEma,
                                        showRsi = showRsi,
                                        drawings = drawings,
                                        bullishColor = bullishColor,
                                        bearishColor = bearishColor
                                    )
                                    TechnicalExportHelper.sharePdf(
                                        context = context,
                                        chartBitmap = bitmap,
                                        currency = currentCurrency,
                                        timeframe = activeExportTimeframe,
                                        candles = candles
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.secondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "اشتراک‌گذاری",
                                            tint = MaterialTheme.colorScheme.onSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "اشتراک‌گذاری (Share)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText("اشتراک‌گذاری")
                                        )
                                        Text(
                                            text = "ارسال فایل PDF به پیام‌رسان‌ها، ایمیل و برنامه‌ها",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action 2: Save to Files
                            Surface(
                                onClick = {
                                    showExportSheet = false
                                    selectedExportFormat = null
                                    val bitmap = renderChartToBitmap(
                                        candles = candles,
                                        showEma = showEma,
                                        showRsi = showRsi,
                                        drawings = drawings,
                                        bullishColor = bullishColor,
                                        bearishColor = bearishColor
                                    )
                                    TechnicalExportHelper.savePdfToDevice(
                                        context = context,
                                        chartBitmap = bitmap,
                                        currency = currentCurrency,
                                        timeframe = activeExportTimeframe,
                                        candles = candles
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = "ذخیره در دانلودها",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "ذخیره در فایل‌ها (Save to Files)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = getFontFamilyForText("ذخیره")
                                        )
                                        Text(
                                            text = "ذخیره مستقیم سند در پوشه دانلودهای دستگاه (Downloads/Cheghad)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
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
                Text(
                    text = "درج یادداشت روی نمودار",
                    fontWeight = FontWeight.Bold,
                    fontFamily = getFontFamilyForText("یادداشت")
                )
            },
            text = {
                Column {
                    Text(
                        text = "متن یا برچسب تحلیلی مورد نظر را وارد کنید:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputNoteText,
                        onValueChange = { inputNoteText = it },
                        placeholder = { Text("مثال: مقاومت هفتگی / هدف قیمتی") },
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
                    Text("ثبت", fontWeight = FontWeight.Bold)
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
                    Text("انصراف")
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
    val displayText = if (digitType == "fa") toPersianDigits(formatted) else formatted

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
        val formattedVal = String.format(Locale.US, "%,.1f", value)
        val displayStr = if (digitType == "fa") toPersianDigits(formattedVal) else formattedVal
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
                            suffix = " ت",
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
 * 5th Carousel: Special Horizontal Reel ("گردون خوابیده") for EMA 200 touch status.
 * - Items that reached EMA 200 from above -> Light Red
 * - Items that reached EMA 200 from below -> Light Green
 */
@Composable
private fun EmaTouchCarouselSection(
    analyses: List<EmaAssetAnalysis>,
    selectedSymbol: String,
    digitType: String,
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
                Text(
                    text = "۵. گردون خوابیده وضعیت برخورد با EMA 200",
                    fontSize = if (isTv) 15.sp else 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = getFontFamilyForText("گردون")
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

                // Determine background and border colors according to user specification:
                // "ارزهایی که در بالای رشد به ای ام آی ۲۰۰ رسیده باشند قرمز کم رنگ شوند . ارزهایی که از پایبن به ema 200 رسیده باشند سبز کم رنگ شوند"
                val (cardBgColor, borderColor, statusBadgeFa, statusIcon, statusColor) = when (analysis.status) {
                    EmaTouchStatus.TOUCHED_FROM_ABOVE -> {
                        // Light Red tint
                        Tuple5(
                            Color(0x38FF5252), // Pale red background
                            Color(0xFFFF5252), // Red border
                            "رسیده به EMA 200 (از بالا)",
                            Icons.Default.TrendingDown,
                            Color(0xFFFF1744)
                        )
                    }
                    EmaTouchStatus.TOUCHED_FROM_BELOW -> {
                        // Light Green tint
                        Tuple5(
                            Color(0x384CAF50), // Pale green background
                            Color(0xFF4CAF50), // Green border
                            "رسیده به EMA 200 (از پایین)",
                            Icons.Default.TrendingUp,
                            Color(0xFF00C853)
                        )
                    }
                    EmaTouchStatus.ABOVE_EMA -> {
                        Tuple5(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            "بالای میانگین ۲۰۰",
                            Icons.Default.TrendingUp,
                            Color(0xFF4CAF50)
                        )
                    }
                    else -> {
                        Tuple5(
                            MaterialTheme.colorScheme.surfaceContainerLow,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            "پایین میانگین ۲۰۰",
                            Icons.Default.TrendingDown,
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
                            suffix = " ت",
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
                            text = statusBadgeFa,
                            fontSize = if (isTv) 11.sp else 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontFamily = getFontFamilyForText(statusBadgeFa)
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

internal fun toPersianDigits(input: String): String {
    return input.map { ch ->
        if (ch in '0'..'9') ('۰'.code + (ch.code - '0'.code)).toChar() else ch
    }.joinToString("")
}

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
                    DrawingToolType.NONE to (Icons.Default.PanTool to "پیمایش"),
                    DrawingToolType.TREND_LINE to (Icons.Default.ShowChart to "خط روند"),
                    DrawingToolType.HORIZONTAL_LINE to (Icons.Default.HorizontalRule to "خط افقی"),
                    DrawingToolType.RAY to (Icons.Default.NorthEast to "پیکان"),
                    DrawingToolType.RECTANGLE to (Icons.Default.CropSquare to "مستطیل"),
                    DrawingToolType.FIBONACCI to (Icons.Default.AutoGraph to "فیبوناچی"),
                    DrawingToolType.BRUSH to (Icons.Default.Brush to "قلم"),
                    DrawingToolType.TEXT to (Icons.Default.TextFields to "متن")
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
                        contentDescription = "بازگردانی",
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
                        contentDescription = "پاک کردن",
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
                    Text(
                        text = "رنگ:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontFamily = getFontFamilyForText("رنگ")
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
                        Text(
                            text = when (tool) {
                                DrawingToolType.HORIZONTAL_LINE, DrawingToolType.TEXT -> "روی چارت لمس کنید"
                                else -> "روی چارت بکشید"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontFamily = getFontFamilyForText("روی چارت")
                        )
                    }
                }
            }
        }
    }
}
