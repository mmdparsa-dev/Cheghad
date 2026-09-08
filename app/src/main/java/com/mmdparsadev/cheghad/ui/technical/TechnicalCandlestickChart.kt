package com.mmdparsadev.cheghad.ui.technical

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartDrawing
import com.mmdparsadev.cheghad.data.models.ChartPoint
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.DrawingToolType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Converts timestamp to screen X coordinate within the visible window.
 */
fun timeToScreenX(
    time: Long,
    visibleCandles: List<CandleData>,
    chartWidth: Float
): Float? {
    if (visibleCandles.isEmpty()) return null
    val candleWidth = chartWidth / visibleCandles.size
    if (visibleCandles.size == 1) return candleWidth / 2f

    val firstTime = visibleCandles.first().time
    val lastTime = visibleCandles.last().time

    if (time <= firstTime) {
        val dt = (visibleCandles[1].time - firstTime).coerceAtLeast(1L)
        val diff = (firstTime - time).toFloat() / dt
        return (candleWidth / 2f) - (diff * candleWidth)
    }
    if (time >= lastTime) {
        val dt = (lastTime - visibleCandles[visibleCandles.size - 2].time).coerceAtLeast(1L)
        val diff = (time - lastTime).toFloat() / dt
        val lastX = ((visibleCandles.size - 1) * candleWidth) + (candleWidth / 2f)
        return lastX + (diff * candleWidth)
    }

    for (i in 0 until visibleCandles.size - 1) {
        val t0 = visibleCandles[i].time
        val t1 = visibleCandles[i + 1].time
        if (time in t0..t1) {
            val frac = if (t1 > t0) (time - t0).toFloat() / (t1 - t0) else 0f
            val x0 = (i * candleWidth) + (candleWidth / 2f)
            val x1 = ((i + 1) * candleWidth) + (candleWidth / 2f)
            return x0 + frac * (x1 - x0)
        }
    }
    return ((visibleCandles.size - 1) * candleWidth) + (candleWidth / 2f)
}

/**
 * Converts screen X and Y to ChartPoint(time, price) based on visible price/candle scale.
 */
fun screenToChartPoint(
    screenX: Float,
    screenY: Float,
    chartWidth: Float,
    mainHeight: Float,
    minPrice: Double,
    maxPrice: Double,
    visibleCandles: List<CandleData>
): ChartPoint {
    val priceRange = if (maxPrice > minPrice) maxPrice - minPrice else 1.0
    val ratio = ((mainHeight - screenY) / mainHeight).toDouble().coerceIn(0.0, 1.0)
    val price = minPrice + (ratio * priceRange)

    val candleWidth = if (visibleCandles.isNotEmpty()) chartWidth / visibleCandles.size else 1f
    val idx = (screenX / candleWidth).toInt().coerceIn(0, (visibleCandles.size - 1).coerceAtLeast(0))
    val time = if (visibleCandles.isNotEmpty()) visibleCandles[idx].time else System.currentTimeMillis()

    return ChartPoint(time, price)
}

/**
 * Draws all technical drawings and shapes on the Compose DrawScope.
 */
fun DrawScope.drawChartDrawings(
    drawings: List<ChartDrawing>,
    timeToX: (Long) -> Float?,
    priceToY: (Double) -> Float,
    chartWidth: Float,
    mainHeight: Float,
    digitType: String,
    drawingsAlpha: Float = 1f
) {
    if (drawingsAlpha <= 0f) return
    for (drawing in drawings) {
        val baseColor = Color(drawing.color)
        val color = baseColor.copy(alpha = baseColor.alpha * drawingsAlpha)
        val strokePx = drawing.strokeWidth.dp.toPx()

        when (drawing) {
            is ChartDrawing.TrendLine -> {
                val x1 = timeToX(drawing.start.time)
                val x2 = timeToX(drawing.end.time)
                if (x1 != null && x2 != null) {
                    val y1 = priceToY(drawing.start.price)
                    val y2 = priceToY(drawing.end.price)
                    drawLine(
                        color = color,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(x1, y1))
                    drawCircle(color = color, radius = 3.5.dp.toPx(), center = Offset(x2, y2))
                }
            }
            is ChartDrawing.HorizontalLine -> {
                val y = priceToY(drawing.price)
                if (y in 0f..mainHeight) {
                    drawLine(
                        color = color,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = strokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                    val priceText = formatPriceShort(drawing.price, digitType)
                    val labelAlphaInt = (255 * drawingsAlpha).toInt().coerceIn(0, 255)
                    drawContext.canvas.nativeCanvas.drawText(
                        priceText,
                        chartWidth + 3.dp.toPx(),
                        y + 4.dp.toPx(),
                        Paint().apply {
                            this.color = (drawing.color.toInt() and 0x00FFFFFF) or (labelAlphaInt shl 24)
                            textSize = 8.5.sp.toPx()
                            isAntiAlias = true
                            typeface = Typeface.DEFAULT_BOLD
                        }
                    )
                }
            }
            is ChartDrawing.Ray -> {
                val x1 = timeToX(drawing.start.time)
                val x2 = timeToX(drawing.end.time)
                if (x1 != null && x2 != null) {
                    val y1 = priceToY(drawing.start.price)
                    val y2 = priceToY(drawing.end.price)
                    drawLine(
                        color = color,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    val dx = x2 - x1
                    val dy = y2 - y1
                    val angle = kotlin.math.atan2(dy, dx)
                    val arrowLen = 16.dp.toPx()
                    val arrowAngle = 0.45f
                    val xLeft = x2 - arrowLen * kotlin.math.cos(angle - arrowAngle).toFloat()
                    val yLeft = y2 - arrowLen * kotlin.math.sin(angle - arrowAngle).toFloat()
                    val xRight = x2 - arrowLen * kotlin.math.cos(angle + arrowAngle).toFloat()
                    val yRight = y2 - arrowLen * kotlin.math.sin(angle + arrowAngle).toFloat()

                    drawLine(color = color, start = Offset(x2, y2), end = Offset(xLeft, yLeft), strokeWidth = strokePx, cap = StrokeCap.Round)
                    drawLine(color = color, start = Offset(x2, y2), end = Offset(xRight, yRight), strokeWidth = strokePx, cap = StrokeCap.Round)
                }
            }
            is ChartDrawing.Rectangle -> {
                val x1 = timeToX(drawing.start.time)
                val x2 = timeToX(drawing.end.time)
                if (x1 != null && x2 != null) {
                    val y1 = priceToY(drawing.start.price)
                    val y2 = priceToY(drawing.end.price)
                    val left = min(x1, x2)
                    val top = min(y1, y2)
                    val rectW = abs(x2 - x1)
                    val rectH = abs(y2 - y1)

                    if (drawing.filled) {
                        drawRect(
                            color = color.copy(alpha = 0.18f * drawingsAlpha),
                            topLeft = Offset(left, top),
                            size = Size(rectW, rectH)
                        )
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(rectW, rectH),
                        style = Stroke(width = strokePx)
                    )
                }
            }
            is ChartDrawing.Fibonacci -> {
                val x1 = timeToX(drawing.start.time)
                val x2 = timeToX(drawing.end.time)
                if (x1 != null && x2 != null) {
                    val p0 = drawing.start.price
                    val p1 = drawing.end.price
                    val diff = p1 - p0
                    val minX = min(x1, x2)
                    val maxX = max(chartWidth, max(x1, x2))

                    val fibLevels = listOf(
                        0.000 to "0.0%",
                        0.236 to "23.6%",
                        0.382 to "38.2%",
                        0.500 to "50.0%",
                        0.618 to "61.8%",
                        0.786 to "78.6%",
                        1.000 to "100.0%"
                    )

                    val y382 = priceToY(p0 + diff * 0.382)
                    val y618 = priceToY(p0 + diff * 0.618)
                    drawRect(
                        color = color.copy(alpha = 0.14f * drawingsAlpha),
                        topLeft = Offset(minX, min(y382, y618)),
                        size = Size(maxX - minX, abs(y618 - y382))
                    )

                    val fibAlphaInt = (255 * drawingsAlpha).toInt().coerceIn(0, 255)
                    for ((ratio, label) in fibLevels) {
                        val levelPrice = p0 + diff * ratio
                        val y = priceToY(levelPrice)
                        val isGolden = ratio == 0.618 || ratio == 0.500
                        drawLine(
                            color = if (isGolden) Color(0xFFFFD54F).copy(alpha = drawingsAlpha) else color.copy(alpha = 0.75f * drawingsAlpha),
                            start = Offset(minX, y),
                            end = Offset(maxX, y),
                            strokeWidth = if (isGolden) 1.6.dp.toPx() else 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                        val levelText = "$label (${formatPriceShort(levelPrice, digitType)})"
                        drawContext.canvas.nativeCanvas.drawText(
                            levelText,
                            minX + 8.dp.toPx(),
                            y - 4.dp.toPx(),
                            Paint().apply {
                                val rawColor = if (isGolden) android.graphics.Color.rgb(255, 213, 79) else drawing.color.toInt()
                                this.color = (rawColor and 0x00FFFFFF) or (fibAlphaInt shl 24)
                                textSize = 8.5.sp.toPx()
                                isAntiAlias = true
                                typeface = Typeface.DEFAULT_BOLD
                            }
                        )
                    }
                }
            }
            is ChartDrawing.Brush -> {
                if (drawing.points.size >= 2) {
                    val brushPath = Path()
                    var started = false
                    for (pt in drawing.points) {
                        val x = timeToX(pt.time)
                        if (x != null) {
                            val y = priceToY(pt.price)
                            if (!started) {
                                brushPath.moveTo(x, y)
                                started = true
                            } else {
                                brushPath.lineTo(x, y)
                            }
                        }
                    }
                    if (started) {
                        drawPath(
                            path = brushPath,
                            color = color,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            is ChartDrawing.Text -> {
                val x = timeToX(drawing.position.time)
                if (x != null) {
                    val y = priceToY(drawing.position.price)
                    val textAlphaInt = (255 * drawingsAlpha).toInt().coerceIn(0, 255)
                    val textPaint = Paint().apply {
                        this.color = (drawing.color.toInt() and 0x00FFFFFF) or (textAlphaInt shl 24)
                        textSize = drawing.fontSizeSp.sp.toPx()
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    val textBounds = Rect()
                    textPaint.getTextBounds(drawing.text, 0, drawing.text.length, textBounds)
                    val textW = textBounds.width().toFloat()
                    val textH = textBounds.height().toFloat()

                    drawRoundRect(
                        color = Color(0xDD1E293B).copy(alpha = 0.86f * drawingsAlpha),
                        topLeft = Offset(x - 6.dp.toPx(), y - textH - 8.dp.toPx()),
                        size = Size(textW + 12.dp.toPx(), textH + 12.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                    drawRoundRect(
                        color = color.copy(alpha = 0.6f * drawingsAlpha),
                        topLeft = Offset(x - 6.dp.toPx(), y - textH - 8.dp.toPx()),
                        size = Size(textW + 12.dp.toPx(), textH + 12.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        drawing.text,
                        x,
                        y,
                        textPaint
                    )
                }
            }
        }
    }
}

/**
 * Formats timestamp dynamically based on the current visible time span.
 */
fun formatTimeAxisLabel(timestamp: Long, timeSpanMs: Long, digitType: String): String {
    val pattern = when {
        timeSpanMs < 36 * 3600 * 1000L -> "HH:mm"
        timeSpanMs < 14 * 86400 * 1000L -> "MM/dd HH:mm"
        timeSpanMs < 120 * 86400 * 1000L -> "yyyy/MM/dd"
        else -> "yyyy/MM"
    }
    val formatted = SimpleDateFormat(pattern, Locale.US).format(Date(timestamp))
    return if (digitType == "fa") toPersianDigits(formatted) else formatted
}

@Composable
fun TechnicalCandlestickChart(
    candles: List<CandleData>,
    currency: CurrencyItem,
    timeframe: ChartTimeframe? = null,
    showEma: Boolean = true,
    showRsi: Boolean = true,
    digitType: String = "fa",
    activeTool: DrawingToolType = DrawingToolType.NONE,
    activeColor: Color = Color(0xFFFFEB3B),
    drawings: List<ChartDrawing> = emptyList(),
    drawingsAlpha: Float = 1f,
    cornerRadius: Dp = 16.dp,
    isTvMode: Boolean = false,
    bullishColor: Color = Color(0xFF00C853),
    bearishColor: Color = Color(0xFFFF3D00),
    modifier: Modifier = Modifier,
    onHoverCandleChanged: (CandleData?) -> Unit = {},
    onZoomChanged: () -> Unit = {},
    onDrawingCreated: (ChartDrawing) -> Unit = {},
    onRequestTextInput: (ChartPoint) -> Unit = {}
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "در حال بارگذاری داده‌های چارت...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Interactive view state
    var visibleCandlesCount by remember { mutableIntStateOf(candles.size.coerceIn(25, 60)) }
    var panOffset by remember { mutableFloatStateOf(0f) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var draftDrawing by remember { mutableStateOf<ChartDrawing?>(null) }

    // Animated visibility for EMA and RSI indicators
    val emaAlpha by animateFloatAsState(
        targetValue = if (showEma) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "emaAlpha"
    )
    val rsiAnimProgress by animateFloatAsState(
        targetValue = if (showRsi) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "rsiAnimProgress"
    )

    // Reset zoom to default window when explicit timeframe is chosen
    LaunchedEffect(timeframe) {
        if (timeframe != null) {
            visibleCandlesCount = candles.size.coerceIn(25, 60)
            panOffset = 0f
        }
    }

    val upColor = bullishColor
    val downColor = bearishColor
    val emaColor = Color(0xFFFFB300) // Amber / Gold
    val rsiColor = Color(0xFF7C4DFF) // Purple
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val crosshairColor = MaterialTheme.colorScheme.primary

    val activeColorLong = remember(activeColor) {
        activeColor.toArgb().toLong() and 0xFFFFFFFFL
    }

    // Gesture detection
    val totalCandles = candles.size
    val maxPan = max(0, totalCandles - visibleCandlesCount)
    val currentPanIndex = (panOffset / 10f).roundToInt().coerceIn(0, maxPan)

    // Candlestick window
    val startIndex = max(0, totalCandles - visibleCandlesCount - currentPanIndex)
    val endIndex = min(totalCandles, startIndex + visibleCandlesCount)
    val visibleCandles = remember(startIndex, endIndex, candles) {
        candles.subList(startIndex, endIndex)
    }

    // Price scale bounds calculation
    val (calcMinPrice, calcMaxPrice) = remember(visibleCandles, showEma) {
        if (visibleCandles.isEmpty()) {
            0.0 to 1.0
        } else {
            var minP = Double.MAX_VALUE
            var maxP = Double.MIN_VALUE
            for (c in visibleCandles) {
                if (c.low < minP) minP = c.low
                if (c.high > maxP) maxP = c.high
                if (showEma && c.ema200 != null && c.ema200 > 0) {
                    if (c.ema200 < minP) minP = c.ema200
                    if (c.ema200 > maxP) maxP = c.ema200
                }
            }
            if (maxP == minP) {
                maxP += 1.0
                minP -= 1.0
            }
            val pad = (maxP - minP) * 0.02
            (minP - pad) to (maxP + pad)
        }
    }

    val priceAxisWidthDp = if (isTvMode) 70.dp else 46.dp
    val topPaddingDp = 0.dp
    val timeAxisHeightDp = if (isTvMode) 28.dp else 18.dp

    // Gesture Modifier according to activeTool
    val gestureModifier = when (activeTool) {
        DrawingToolType.NONE -> {
            Modifier
                .pointerInput(totalCandles) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1f) {
                            val newCount = (visibleCandlesCount / zoom).roundToInt()
                            val clamped = newCount.coerceIn(15, min(totalCandles, 140))
                            if (clamped != visibleCandlesCount) {
                                visibleCandlesCount = clamped
                                onZoomChanged()
                            }
                        }
                        if (pan.x != 0f) {
                            panOffset = (panOffset + pan.x * 0.2f).coerceIn(0f, max(0, totalCandles - visibleCandlesCount) * 10f)
                        }
                    }
                }
                .pointerInput(totalCandles, visibleCandles.size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width.toFloat() - priceAxisWidthDp.toPx()
                            val offX = offset.x
                            if (width > 0 && offX in 0f..width) {
                                val candleWidth = width / visibleCandles.size
                                val idx = (offX / candleWidth).toInt().coerceIn(0, visibleCandles.size - 1)
                                hoveredIndex = startIndex + idx
                                onHoverCandleChanged(candles.getOrNull(startIndex + idx))
                            }
                        },
                        onDrag = { change, _ ->
                            val width = size.width.toFloat() - priceAxisWidthDp.toPx()
                            val offX = change.position.x
                            if (width > 0 && offX in 0f..width) {
                                val candleWidth = width / visibleCandles.size
                                val idx = (offX / candleWidth).toInt().coerceIn(0, visibleCandles.size - 1)
                                hoveredIndex = startIndex + idx
                                onHoverCandleChanged(candles.getOrNull(startIndex + idx))
                            }
                        },
                        onDragEnd = {
                            hoveredIndex = null
                            onHoverCandleChanged(null)
                        },
                        onDragCancel = {
                            hoveredIndex = null
                            onHoverCandleChanged(null)
                        }
                    )
                }
        }
        DrawingToolType.HORIZONTAL_LINE, DrawingToolType.TEXT -> {
            Modifier.pointerInput(activeTool, activeColorLong, visibleCandles, startIndex, calcMinPrice, calcMaxPrice) {
                detectTapGestures { offset ->
                    val w = (size.width.toFloat() - priceAxisWidthDp.toPx()).coerceAtLeast(1f)
                    val totalH = (size.height.toFloat() - timeAxisHeightDp.toPx()).coerceAtLeast(1f)
                    val mainH = if (rsiAnimProgress > 0.01f) totalH * (1f - 0.24f * rsiAnimProgress) else totalH
                    val offX = offset.x.coerceIn(0f, w)
                    val offY = offset.y.coerceIn(0f, mainH)
                    val pt = screenToChartPoint(offX, offY, w, mainH, calcMinPrice, calcMaxPrice, visibleCandles)

                    if (activeTool == DrawingToolType.TEXT) {
                        onRequestTextInput(pt)
                    } else {
                        val drawing = ChartDrawing.HorizontalLine(
                            id = UUID.randomUUID().toString(),
                            price = pt.price,
                            color = activeColorLong
                        )
                        onDrawingCreated(drawing)
                    }
                }
            }
        }
        else -> {
            Modifier.pointerInput(activeTool, activeColorLong, visibleCandles, startIndex, calcMinPrice, calcMaxPrice) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val w = (size.width.toFloat() - priceAxisWidthDp.toPx()).coerceAtLeast(1f)
                        val totalH = (size.height.toFloat() - timeAxisHeightDp.toPx()).coerceAtLeast(1f)
                        val mainH = if (rsiAnimProgress > 0.01f) totalH * (1f - 0.24f * rsiAnimProgress) else totalH
                        val offX = offset.x.coerceIn(0f, w)
                        val offY = offset.y.coerceIn(0f, mainH)
                        val pt = screenToChartPoint(offX, offY, w, mainH, calcMinPrice, calcMaxPrice, visibleCandles)
                        val id = UUID.randomUUID().toString()

                        draftDrawing = when (activeTool) {
                            DrawingToolType.TREND_LINE -> ChartDrawing.TrendLine(id, pt, pt, activeColorLong)
                            DrawingToolType.RAY -> ChartDrawing.Ray(id, pt, pt, activeColorLong)
                            DrawingToolType.RECTANGLE -> ChartDrawing.Rectangle(id, pt, pt, activeColorLong)
                            DrawingToolType.FIBONACCI -> ChartDrawing.Fibonacci(id, pt, pt, activeColorLong)
                            DrawingToolType.BRUSH -> ChartDrawing.Brush(id, listOf(pt), activeColorLong)
                            else -> null
                        }
                    },
                    onDrag = { change, _ ->
                        val w = (size.width.toFloat() - priceAxisWidthDp.toPx()).coerceAtLeast(1f)
                        val totalH = (size.height.toFloat() - timeAxisHeightDp.toPx()).coerceAtLeast(1f)
                        val mainH = if (rsiAnimProgress > 0.01f) totalH * (1f - 0.24f * rsiAnimProgress) else totalH
                        val offX = change.position.x.coerceIn(0f, w)
                        val offY = change.position.y.coerceIn(0f, mainH)
                        val pt = screenToChartPoint(offX, offY, w, mainH, calcMinPrice, calcMaxPrice, visibleCandles)

                        draftDrawing = when (val curr = draftDrawing) {
                            is ChartDrawing.TrendLine -> curr.copy(end = pt)
                            is ChartDrawing.Ray -> curr.copy(end = pt)
                            is ChartDrawing.Rectangle -> curr.copy(end = pt)
                            is ChartDrawing.Fibonacci -> curr.copy(end = pt)
                            is ChartDrawing.Brush -> curr.copy(points = curr.points + pt)
                            else -> null
                        }
                    },
                    onDragEnd = {
                        draftDrawing?.let {
                            onDrawingCreated(it)
                        }
                        draftDrawing = null
                    },
                    onDragCancel = {
                        draftDrawing = null
                    }
                )
            }
        }
    }

    val allDrawings = remember(drawings, draftDrawing) {
        if (draftDrawing != null) drawings + draftDrawing!! else drawings
    }

    var isChartFocused by remember { mutableStateOf(false) }

    val keyEventModifier = Modifier.onKeyEvent { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
        when (keyEvent.key) {
            Key.DirectionLeft -> {
                // Older candles (chronological backward)
                val currentHover = hoveredIndex ?: (startIndex + visibleCandles.size - 1)
                val newHover = (currentHover - 1).coerceIn(0, totalCandles - 1)
                hoveredIndex = newHover
                if (newHover < startIndex) {
                    panOffset = ((totalCandles - visibleCandlesCount - newHover) * 10f).coerceIn(0f, maxPan * 10f)
                }
                onHoverCandleChanged(candles.getOrNull(newHover))
                true
            }
            Key.DirectionRight -> {
                // Newer candles (chronological forward)
                val currentHover = hoveredIndex ?: startIndex
                val newHover = (currentHover + 1).coerceIn(0, totalCandles - 1)
                hoveredIndex = newHover
                if (newHover >= endIndex) {
                    panOffset = ((totalCandles - visibleCandlesCount - (newHover - visibleCandlesCount + 1)) * 10f).coerceIn(0f, maxPan * 10f)
                }
                onHoverCandleChanged(candles.getOrNull(newHover))
                true
            }
            Key.Plus, Key.Equals, Key.PageUp -> {
                val newCount = (visibleCandlesCount - 5).coerceIn(15, min(totalCandles, 140))
                if (newCount != visibleCandlesCount) {
                    visibleCandlesCount = newCount
                    onZoomChanged()
                }
                true
            }
            Key.Minus, Key.PageDown -> {
                val newCount = (visibleCandlesCount + 5).coerceIn(15, min(totalCandles, 140))
                if (newCount != visibleCandlesCount) {
                    visibleCandlesCount = newCount
                    onZoomChanged()
                }
                true
            }
            Key.DirectionCenter, Key.Enter -> {
                if (hoveredIndex != null) {
                    hoveredIndex = null
                    onHoverCandleChanged(null)
                } else {
                    val defaultIdx = (startIndex + visibleCandles.size - 1).coerceIn(0, totalCandles - 1)
                    hoveredIndex = defaultIdx
                    onHoverCandleChanged(candles.getOrNull(defaultIdx))
                }
                true
            }
            else -> false
        }
    }

    val tvBorderColor by animateColorAsState(
        targetValue = if (isTvMode && isChartFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tvChartBorder"
    )
    val tvBorderWidth = if (isTvMode && isChartFocused) 2.5.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
            .border(
                width = tvBorderWidth,
                color = tvBorderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .onFocusChanged { isChartFocused = it.isFocused }
            .then(if (isTvMode) Modifier.focusable() else Modifier)
            .then(keyEventModifier)
            .then(gestureModifier)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val totalWidth = size.width
            val totalHeight = size.height

            val timeAxisHeight = timeAxisHeightDp.toPx()
            val usablePlotHeight = totalHeight - timeAxisHeight

            val priceAxisWidth = priceAxisWidthDp.toPx()
            val chartWidth = totalWidth - priceAxisWidth

            // Split into Main Chart and RSI Subchart with smooth animation
            val rsiHeight = usablePlotHeight * 0.24f * rsiAnimProgress
            val mainHeight = usablePlotHeight - rsiHeight
            val separatorY = mainHeight

            if (visibleCandles.isEmpty()) return@Canvas

            val minPrice = calcMinPrice
            val maxPrice = calcMaxPrice
            val priceRange = if (maxPrice > minPrice) maxPrice - minPrice else 1.0

            fun priceToY(price: Double): Float {
                val ratio = (price - minPrice) / priceRange
                return (mainHeight - (ratio * mainHeight)).toFloat()
            }

            // 1. Draw Grid Lines for Main Chart
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = (mainHeight / gridSteps) * i
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(totalWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Price label on right
                val priceAtY = maxPrice - ((priceRange / gridSteps) * i)
                val priceText = formatPriceShort(priceAtY, digitType)
                drawContext.canvas.nativeCanvas.drawText(
                    priceText,
                    chartWidth + 3.dp.toPx(),
                    y + 4.dp.toPx(),
                    Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = if (isTvMode) 12.sp.toPx() else 8.5.sp.toPx()
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                    }
                )
            }

            // Vertical axis line separating candles from right price scale
            drawLine(
                color = gridColor.copy(alpha = 0.5f),
                start = Offset(chartWidth, 0f),
                end = Offset(chartWidth, usablePlotHeight),
                strokeWidth = 1.dp.toPx()
            )

            // 2. Draw Candlesticks
            val candleWidth = chartWidth / visibleCandles.size
            val bodyWidth = (candleWidth * 0.82f).coerceAtLeast(2f)

            for (i in visibleCandles.indices) {
                val c = visibleCandles[i]
                val centerX = (i * candleWidth) + (candleWidth / 2f)

                val openY = priceToY(c.open)
                val closeY = priceToY(c.close)
                val highY = priceToY(c.high)
                val lowY = priceToY(c.low)

                val isUp = c.close >= c.open
                val candleColor = if (isUp) upColor else downColor

                // High-Low Wick
                drawLine(
                    color = candleColor,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, lowY),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Candle Body
                val bodyTop = min(openY, closeY)
                val bodyHeight = max(abs(closeY - openY), 1.5f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(centerX - (bodyWidth / 2f), bodyTop),
                    size = Size(bodyWidth, bodyHeight)
                )
            }

            // 3. Draw EMA 200 curve with smooth fade animation
            if (emaAlpha > 0.005f) {
                val emaPath = Path()
                var hasStarted = false

                for (i in visibleCandles.indices) {
                    val ema = visibleCandles[i].ema200
                    if (ema != null && ema > 0) {
                        val x = (i * candleWidth) + (candleWidth / 2f)
                        val y = priceToY(ema)
                        if (!hasStarted) {
                            emaPath.moveTo(x, y)
                            hasStarted = true
                        } else {
                            emaPath.lineTo(x, y)
                        }
                    }
                }

                if (hasStarted) {
                    drawPath(
                        path = emaPath,
                        color = emaColor.copy(alpha = emaAlpha),
                        style = Stroke(width = 2.dp.toPx() * emaAlpha.coerceIn(0.6f, 1f), cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Draw RSI Subchart with smooth expand/collapse and fade animation
            if (rsiAnimProgress > 0.01f && rsiHeight > 2.dp.toPx()) {
                val rsiAlpha = rsiAnimProgress.coerceIn(0f, 1f)
                val rsiTop = separatorY + 4.dp.toPx()
                val rsiBottom = usablePlotHeight - 2.dp.toPx()
                val rsiPlotHeight = (rsiBottom - rsiTop).coerceAtLeast(1f)

                // Separator
                drawLine(
                    color = gridColor.copy(alpha = gridColor.alpha * rsiAlpha),
                    start = Offset(0f, separatorY),
                    end = Offset(totalWidth, separatorY),
                    strokeWidth = 1.dp.toPx()
                )

                fun rsiToY(rsi: Double): Float {
                    val ratio = rsi.coerceIn(0.0, 100.0) / 100.0
                    return (rsiBottom - (ratio * rsiPlotHeight)).toFloat()
                }

                // Reference lines: 70 (Overbought), 50 (Mid), 30 (Oversold)
                val y70 = rsiToY(70.0)
                val y50 = rsiToY(50.0)
                val y30 = rsiToY(30.0)

                // Fill channel between 30 and 70
                drawRect(
                    color = rsiColor.copy(alpha = 0.05f * rsiAlpha),
                    topLeft = Offset(0f, y70),
                    size = Size(chartWidth, (y30 - y70).coerceAtLeast(0f))
                )

                // Overbought 70
                drawLine(
                    color = downColor.copy(alpha = 0.4f * rsiAlpha),
                    start = Offset(0f, y70),
                    end = Offset(totalWidth, y70),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "70",
                    chartWidth + 3.dp.toPx(),
                    y70 + 3.dp.toPx(),
                    Paint().apply {
                        color = android.graphics.Color.argb((120 * rsiAlpha).toInt(), 244, 67, 54)
                        textSize = if (isTvMode) 11.sp.toPx() else 8.sp.toPx()
                        isAntiAlias = true
                    }
                )

                // 50 line
                drawLine(
                    color = gridColor.copy(alpha = gridColor.alpha * rsiAlpha),
                    start = Offset(0f, y50),
                    end = Offset(totalWidth, y50),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Oversold 30
                drawLine(
                    color = upColor.copy(alpha = 0.4f * rsiAlpha),
                    start = Offset(0f, y30),
                    end = Offset(totalWidth, y30),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "30",
                    chartWidth + 3.dp.toPx(),
                    y30 + 3.dp.toPx(),
                    Paint().apply {
                        color = android.graphics.Color.argb((120 * rsiAlpha).toInt(), 76, 175, 80)
                        textSize = if (isTvMode) 11.sp.toPx() else 8.sp.toPx()
                        isAntiAlias = true
                    }
                )

                // Draw RSI Line
                val rsiPath = Path()
                var rsiStarted = false
                for (i in visibleCandles.indices) {
                    val r = visibleCandles[i].rsi
                    if (r != null) {
                        val x = (i * candleWidth) + (candleWidth / 2f)
                        val y = rsiToY(r)
                        if (!rsiStarted) {
                            rsiPath.moveTo(x, y)
                            rsiStarted = true
                        } else {
                            rsiPath.lineTo(x, y)
                        }
                    }
                }
                if (rsiStarted) {
                    drawPath(
                        path = rsiPath,
                        color = rsiColor.copy(alpha = rsiAlpha),
                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 4.5. Draw Technical Analysis Drawings
            drawChartDrawings(
                drawings = allDrawings,
                timeToX = { t -> timeToScreenX(t, visibleCandles, chartWidth) },
                priceToY = { p -> priceToY(p) },
                chartWidth = chartWidth,
                mainHeight = mainHeight,
                digitType = digitType,
                drawingsAlpha = drawingsAlpha
            )

            // 5. Crosshair on touch
            hoveredIndex?.let { hIdx ->
                val localIdx = hIdx - startIndex
                if (localIdx in visibleCandles.indices) {
                    val candle = visibleCandles[localIdx]
                    val crossX = (localIdx * candleWidth) + (candleWidth / 2f)
                    val crossY = priceToY(candle.close)

                    // Vertical crosshair
                    drawLine(
                        color = crosshairColor.copy(alpha = 0.7f),
                        start = Offset(crossX, 0f),
                        end = Offset(crossX, usablePlotHeight),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )

                    // Horizontal crosshair in main chart
                    drawLine(
                        color = crosshairColor.copy(alpha = 0.7f),
                        start = Offset(0f, crossY),
                        end = Offset(totalWidth, crossY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )

                    // Dot at intersection
                    drawCircle(
                        color = crosshairColor,
                        radius = 4.dp.toPx(),
                        center = Offset(crossX, crossY)
                    )

                    // Crosshair Badge 1: Price Badge on the Right Axis
                    val crossPriceText = formatPriceShort(candle.close, digitType)
                    val crossPricePaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = if (isTvMode) 12.sp.toPx() else 8.sp.toPx()
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    val cpBounds = Rect()
                    crossPricePaint.getTextBounds(crossPriceText, 0, crossPriceText.length, cpBounds)
                    val cpW = (cpBounds.width().toFloat() + 10.dp.toPx()).coerceAtMost(if (isTvMode) 68.dp.toPx() else 44.dp.toPx())
                    val cpH = if (isTvMode) 22.dp.toPx() else 15.dp.toPx()
                    val cpX = chartWidth + 2.dp.toPx()
                    val cpY = (crossY - (cpH / 2f)).coerceIn(0f, mainHeight - cpH)

                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(cpX, cpY),
                        size = Size(cpW, cpH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                    drawRoundRect(
                        color = crosshairColor,
                        topLeft = Offset(cpX, cpY),
                        size = Size(cpW, cpH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        crossPriceText,
                        cpX + 4.dp.toPx(),
                        cpY + cpH - (if (isTvMode) 5.dp.toPx() else 3.5f.dp.toPx()),
                        crossPricePaint
                    )

                    // Crosshair Badge 2: Time Badge on Bottom Time Axis
                    val timeSpanForCross = if (visibleCandles.size >= 2) visibleCandles.last().time - visibleCandles.first().time else 86400000L
                    val crossTimeText = formatTimeAxisLabel(candle.time, timeSpanForCross, digitType)
                    val crossTimePaint = Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = if (isTvMode) 12.sp.toPx() else 8.5.sp.toPx()
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    val ctBounds = Rect()
                    crossTimePaint.getTextBounds(crossTimeText, 0, crossTimeText.length, ctBounds)
                    val ctW = ctBounds.width().toFloat() + 12.dp.toPx()
                    val ctH = if (isTvMode) 22.dp.toPx() else 16.dp.toPx()
                    val ctX = (crossX - (ctW / 2f)).coerceIn(0f, chartWidth - ctW)
                    val ctY = usablePlotHeight + 2.dp.toPx()

                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(ctX, ctY),
                        size = Size(ctW, ctH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                    )
                    drawRoundRect(
                        color = crosshairColor,
                        topLeft = Offset(ctX, ctY),
                        size = Size(ctW, ctH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        crossTimeText,
                        ctX + 6.dp.toPx(),
                        ctY + ctH - (if (isTvMode) 5.dp.toPx() else 4.dp.toPx()),
                        crossTimePaint
                    )
                }
            }

            // 6. Time Axis along bottom (X-Axis)
            val timeAxisTop = usablePlotHeight
            drawLine(
                color = gridColor.copy(alpha = 0.5f),
                start = Offset(0f, timeAxisTop),
                end = Offset(totalWidth, timeAxisTop),
                strokeWidth = 1.dp.toPx()
            )

            val tickCount = 4
            val timeSpanMs = if (visibleCandles.size >= 2) visibleCandles.last().time - visibleCandles.first().time else 86400000L
            val timePaint = Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = if (isTvMode) 11.5.sp.toPx() else 8.sp.toPx()
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }

            for (t in 0..tickCount) {
                val candleIdx = ((visibleCandles.size - 1) * t / tickCount).coerceIn(0, visibleCandles.size - 1)
                val c = visibleCandles[candleIdx]
                val cX = (candleIdx * candleWidth) + (candleWidth / 2f)

                // Tick mark
                drawLine(
                    color = gridColor.copy(alpha = 0.6f),
                    start = Offset(cX, timeAxisTop),
                    end = Offset(cX, timeAxisTop + 3.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )

                val label = formatTimeAxisLabel(c.time, timeSpanMs, digitType)
                val bounds = Rect()
                timePaint.getTextBounds(label, 0, label.length, bounds)
                val textW = bounds.width().toFloat()
                val labelX = (cX - (textW / 2f)).coerceIn(0f, chartWidth - textW)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    labelX,
                    timeAxisTop + (if (isTvMode) 18.dp.toPx() else 13.dp.toPx()),
                    timePaint
                )
            }

            // 7. Dynamic Current Price Badge on Right Axis (Y-Axis)
            val latestC = visibleCandles.lastOrNull()
            if (latestC != null) {
                val curY = priceToY(latestC.close).coerceIn(0f, mainHeight)
                val curColor = if (latestC.close >= latestC.open) upColor else downColor
                val curPriceText = formatPriceShort(latestC.close, digitType)
                val badgePaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = if (isTvMode) 12.sp.toPx() else 8.sp.toPx()
                    isAntiAlias = true
                    typeface = Typeface.DEFAULT_BOLD
                }
                val bBounds = Rect()
                badgePaint.getTextBounds(curPriceText, 0, curPriceText.length, bBounds)
                val bW = (bBounds.width().toFloat() + 10.dp.toPx()).coerceAtMost(if (isTvMode) 68.dp.toPx() else 44.dp.toPx())
                val bH = if (isTvMode) 22.dp.toPx() else 15.dp.toPx()
                val bX = chartWidth + 2.dp.toPx()
                val bY = (curY - (bH / 2f)).coerceIn(0f, mainHeight - bH)

                val lastCandleX = ((visibleCandles.size - 1) * candleWidth) + (candleWidth / 2f)
                drawLine(
                    color = curColor.copy(alpha = 0.5f),
                    start = Offset(lastCandleX, curY),
                    end = Offset(bX, curY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                drawRoundRect(
                    color = curColor,
                    topLeft = Offset(bX, bY),
                    size = Size(bW, bH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )
                drawContext.canvas.nativeCanvas.drawText(
                    curPriceText,
                    bX + 4.dp.toPx(),
                    bY + bH - 3.5f.dp.toPx(),
                    badgePaint
                )
            }
        }

        // Indicator badges at top
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeCandle = hoveredIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()

            if (showEma && activeCandle?.ema200 != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = emaColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, emaColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "EMA 200: " + formatPriceShort(activeCandle.ema200, digitType),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = emaColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (showRsi && activeCandle?.rsi != null) {
                val rVal = activeCandle.rsi
                val rColor = when {
                    rVal >= 70 -> downColor
                    rVal <= 30 -> upColor
                    else -> rsiColor
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = rColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, rColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "RSI (14): " + String.format(Locale.US, "%.1f", rVal),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = rColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // TV Quick Controls Bar (On-screen remote helpers for TV)
        if (isTvMode) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = timeAxisHeightDp + 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    TvChartIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "بزرگ‌نمایی",
                        onClick = {
                            val newCount = (visibleCandlesCount - 5).coerceIn(15, min(totalCandles, 140))
                            if (newCount != visibleCandlesCount) {
                                visibleCandlesCount = newCount
                                onZoomChanged()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TvChartIconButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "کوچک‌نمایی",
                        onClick = {
                            val newCount = (visibleCandlesCount + 5).coerceIn(15, min(totalCandles, 140))
                            if (newCount != visibleCandlesCount) {
                                visibleCandlesCount = newCount
                                onZoomChanged()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TvChartIconButton(
                        icon = Icons.Default.RestartAlt,
                        contentDescription = "ریست زوم",
                        onClick = {
                            visibleCandlesCount = candles.size.coerceIn(25, 60)
                            panOffset = 0f
                            onZoomChanged()
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ریموت: ◀ ▶ کندل | OK نشانگر",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TvChartIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (isFocused) 1.15f else 1.0f, label = "tvBtnScale")
    val bgColor by animateColorAsState(
        if (isFocused) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "tvBtnBg"
    )
    val tintColor by animateColorAsState(
        if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tvBtnTint"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = if (isFocused) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .size(30.dp)
            .scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tintColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatPriceShort(price: Double, digitType: String): String {
    val rounded = if (price >= 1000) price.roundToInt().toString() else String.format(Locale.US, "%.2f", price)
    val formatted = java.text.NumberFormat.getNumberInstance(Locale.US).format(price.roundToInt())
    return if (digitType == "fa") {
        formatted.map { ch ->
            if (ch in '0'..'9') ('۰'.code + (ch.code - '0'.code)).toChar() else ch
        }.joinToString("")
    } else formatted
}

/**
 * Utility to generate a high quality static bitmap of the candlestick chart for PNG/PDF exporting.
 */
fun renderChartToBitmap(
    candles: List<CandleData>,
    width: Int = 1200,
    height: Int = 680,
    showEma: Boolean = true,
    showRsi: Boolean = true,
    isDark: Boolean = false,
    drawings: List<ChartDrawing> = emptyList(),
    bullishColor: Color = Color(0xFF00C853),
    bearishColor: Color = Color(0xFFFF3D00)
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgColor = if (isDark) android.graphics.Color.rgb(18, 24, 38) else android.graphics.Color.rgb(248, 250, 252)
    canvas.drawColor(bgColor)

    if (candles.isEmpty()) return bitmap

    val paddingLeft = 20f
    val paddingRight = 100f
    val paddingTop = 40f
    val paddingBottom = 30f

    val chartWidth = width - paddingLeft - paddingRight
    val totalHeight = height - paddingTop - paddingBottom

    val mainHeight = if (showRsi) totalHeight * 0.70f else totalHeight
    val rsiHeight = if (showRsi) totalHeight * 0.28f else 0f
    val separatorY = paddingTop + mainHeight + 10f

    val visibleCandles = candles.takeLast(min(candles.size, 80))
    if (visibleCandles.isEmpty()) return bitmap

    var minPrice = Double.MAX_VALUE
    var maxPrice = Double.MIN_VALUE
    for (c in visibleCandles) {
        if (c.low < minPrice) minPrice = c.low
        if (c.high > maxPrice) maxPrice = c.high
        if (showEma && c.ema200 != null && c.ema200 > 0) {
            if (c.ema200 < minPrice) minPrice = c.ema200
            if (c.ema200 > maxPrice) maxPrice = c.ema200
        }
    }
    if (maxPrice == minPrice) { maxPrice += 1.0; minPrice -= 1.0 }
    val pPad = (maxPrice - minPrice) * 0.05
    minPrice -= pPad
    maxPrice += pPad
    val priceRange = maxPrice - minPrice

    fun toY(p: Double): Float {
        val r = (p - minPrice) / priceRange
        return (paddingTop + mainHeight - (r * mainHeight)).toFloat()
    }

    val gridPaint = Paint().apply {
        color = if (isDark) android.graphics.Color.rgb(40, 50, 70) else android.graphics.Color.rgb(226, 232, 240)
        strokeWidth = 1.5f
    }

    val textPaint = Paint().apply {
        color = if (isDark) android.graphics.Color.rgb(160, 174, 192) else android.graphics.Color.rgb(100, 116, 139)
        textSize = 18f
        isAntiAlias = true
    }

    // Grid
    for (i in 0..4) {
        val y = paddingTop + (mainHeight / 4) * i
        canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint)
        val pAtY = maxPrice - ((priceRange / 4) * i)
        canvas.drawText(String.format(Locale.US, "%,.0f", pAtY), paddingLeft + chartWidth + 10f, y + 6f, textPaint)
    }

    val candleW = chartWidth / visibleCandles.size
    val bodyW = max(candleW * 0.7f, 2f)

    val greenPaint = Paint().apply { color = bullishColor.toArgb(); isAntiAlias = true }
    val redPaint = Paint().apply { color = bearishColor.toArgb(); isAntiAlias = true }

    for (i in visibleCandles.indices) {
        val c = visibleCandles[i]
        val cX = paddingLeft + (i * candleW) + (candleW / 2f)
        val oY = toY(c.open)
        val cY = toY(c.close)
        val hY = toY(c.high)
        val lY = toY(c.low)

        val p = if (c.close >= c.open) greenPaint else redPaint

        // Wick
        p.strokeWidth = 2f
        canvas.drawLine(cX, hY, cX, lY, p)

        // Body
        val bTop = min(oY, cY)
        val bH = max(abs(cY - oY), 2f)
        canvas.drawRect(cX - (bodyW / 2f), bTop, cX + (bodyW / 2f), bTop + bH, p)
    }

    // EMA 200
    if (showEma) {
        val emaPaint = Paint().apply {
            color = android.graphics.Color.rgb(255, 179, 0)
            strokeWidth = 3f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val emaPath = android.graphics.Path()
        var started = false
        for (i in visibleCandles.indices) {
            val ema = visibleCandles[i].ema200
            if (ema != null && ema > 0) {
                val x = paddingLeft + (i * candleW) + (candleW / 2f)
                val y = toY(ema)
                if (!started) { emaPath.moveTo(x, y); started = true } else { emaPath.lineTo(x, y) }
            }
        }
        if (started) canvas.drawPath(emaPath, emaPaint)
    }

    // RSI
    if (showRsi && rsiHeight > 0) {
        val rsiTop = separatorY + 10f
        val rsiBottom = height - paddingBottom
        val rsiPlotH = rsiBottom - rsiTop

        canvas.drawLine(paddingLeft, separatorY, paddingLeft + chartWidth, separatorY, gridPaint)

        fun rToY(rsi: Double): Float {
            val r = rsi.coerceIn(0.0, 100.0) / 100.0
            return (rsiBottom - (r * rsiPlotH)).toFloat()
        }

        val y70 = rToY(70.0)
        val y30 = rToY(30.0)
        canvas.drawLine(paddingLeft, y70, paddingLeft + chartWidth, y70, gridPaint)
        canvas.drawLine(paddingLeft, y30, paddingLeft + chartWidth, y30, gridPaint)
        canvas.drawText("70", paddingLeft + chartWidth + 10f, y70 + 6f, textPaint)
        canvas.drawText("30", paddingLeft + chartWidth + 10f, y30 + 6f, textPaint)

        val rsiPaint = Paint().apply {
            color = android.graphics.Color.rgb(124, 77, 255)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val rsiPath = android.graphics.Path()
        var rStarted = false
        for (i in visibleCandles.indices) {
            val r = visibleCandles[i].rsi
            if (r != null) {
                val x = paddingLeft + (i * candleW) + (candleW / 2f)
                val y = rToY(r)
                if (!rStarted) { rsiPath.moveTo(x, y); rStarted = true } else { rsiPath.lineTo(x, y) }
            }
        }
        if (rStarted) canvas.drawPath(rsiPath, rsiPaint)
    }

    // Render Technical Drawings on static bitmap
    if (drawings.isNotEmpty()) {
        fun bmpTimeToX(time: Long): Float {
            if (visibleCandles.isEmpty()) return paddingLeft
            val firstTime = visibleCandles.first().time
            val lastTime = visibleCandles.last().time
            if (visibleCandles.size == 1) return paddingLeft + (candleW / 2f)

            if (time <= firstTime) {
                val dt = (visibleCandles[1].time - firstTime).coerceAtLeast(1L)
                val diff = (firstTime - time).toFloat() / dt
                return paddingLeft + (candleW / 2f) - (diff * candleW)
            }
            if (time >= lastTime) {
                val dt = (lastTime - visibleCandles[visibleCandles.size - 2].time).coerceAtLeast(1L)
                val diff = (time - lastTime).toFloat() / dt
                val lastX = paddingLeft + ((visibleCandles.size - 1) * candleW) + (candleW / 2f)
                return lastX + (diff * candleW)
            }
            for (i in 0 until visibleCandles.size - 1) {
                val t0 = visibleCandles[i].time
                val t1 = visibleCandles[i + 1].time
                if (time in t0..t1) {
                    val frac = if (t1 > t0) (time - t0).toFloat() / (t1 - t0) else 0f
                    val x0 = paddingLeft + (i * candleW) + (candleW / 2f)
                    val x1 = paddingLeft + ((i + 1) * candleW) + (candleW / 2f)
                    return x0 + frac * (x1 - x0)
                }
            }
            return paddingLeft + chartWidth
        }

        for (d in drawings) {
            val dPaint = Paint().apply {
                color = d.color.toInt()
                strokeWidth = d.strokeWidth * 1.5f
                isAntiAlias = true
            }
            when (d) {
                is ChartDrawing.TrendLine -> {
                    val x1 = bmpTimeToX(d.start.time)
                    val x2 = bmpTimeToX(d.end.time)
                    val y1 = toY(d.start.price)
                    val y2 = toY(d.end.price)
                    dPaint.style = Paint.Style.STROKE
                    canvas.drawLine(x1, y1, x2, y2, dPaint)
                    dPaint.style = Paint.Style.FILL
                    canvas.drawCircle(x1, y1, 5f, dPaint)
                    canvas.drawCircle(x2, y2, 5f, dPaint)
                }
                is ChartDrawing.HorizontalLine -> {
                    val y = toY(d.price)
                    dPaint.style = Paint.Style.STROKE
                    dPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 8f), 0f)
                    canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, dPaint)
                    dPaint.pathEffect = null
                    dPaint.style = Paint.Style.FILL
                    dPaint.textSize = 16f
                    canvas.drawText(String.format(Locale.US, "%,.0f", d.price), paddingLeft + chartWidth + 10f, y + 5f, dPaint)
                }
                is ChartDrawing.Ray -> {
                    val x1 = bmpTimeToX(d.start.time)
                    val x2 = bmpTimeToX(d.end.time)
                    val y1 = toY(d.start.price)
                    val y2 = toY(d.end.price)
                    dPaint.style = Paint.Style.STROKE
                    canvas.drawLine(x1, y1, x2, y2, dPaint)

                    val dx = x2 - x1
                    val dy = y2 - y1
                    val angle = kotlin.math.atan2(dy, dx)
                    val arrowLen = 20f
                    val arrowAngle = 0.45f
                    val xLeft = x2 - arrowLen * kotlin.math.cos(angle - arrowAngle).toFloat()
                    val yLeft = y2 - arrowLen * kotlin.math.sin(angle - arrowAngle).toFloat()
                    val xRight = x2 - arrowLen * kotlin.math.cos(angle + arrowAngle).toFloat()
                    val yRight = y2 - arrowLen * kotlin.math.sin(angle + arrowAngle).toFloat()
                    canvas.drawLine(x2, y2, xLeft, yLeft, dPaint)
                    canvas.drawLine(x2, y2, xRight, yRight, dPaint)
                }
                is ChartDrawing.Rectangle -> {
                    val x1 = bmpTimeToX(d.start.time)
                    val x2 = bmpTimeToX(d.end.time)
                    val y1 = toY(d.start.price)
                    val y2 = toY(d.end.price)
                    val left = min(x1, x2)
                    val top = min(y1, y2)
                    val right = max(x1, x2)
                    val bottom = max(y1, y2)

                    if (d.filled) {
                        val fillPaint = Paint(dPaint).apply {
                            style = Paint.Style.FILL
                            alpha = 40
                        }
                        canvas.drawRect(left, top, right, bottom, fillPaint)
                    }
                    dPaint.style = Paint.Style.STROKE
                    canvas.drawRect(left, top, right, bottom, dPaint)
                }
                is ChartDrawing.Fibonacci -> {
                    val x1 = bmpTimeToX(d.start.time)
                    val x2 = bmpTimeToX(d.end.time)
                    val p0 = d.start.price
                    val p1 = d.end.price
                    val diff = p1 - p0
                    val minX = min(x1, x2)
                    val maxX = paddingLeft + chartWidth

                    val fibLevels = listOf(
                        0.000 to "0.0%",
                        0.236 to "23.6%",
                        0.382 to "38.2%",
                        0.500 to "50.0%",
                        0.618 to "61.8%",
                        0.786 to "78.6%",
                        1.000 to "100.0%"
                    )
                    val y382 = toY(p0 + diff * 0.382)
                    val y618 = toY(p0 + diff * 0.618)
                    val pocketPaint = Paint(dPaint).apply {
                        style = Paint.Style.FILL
                        alpha = 35
                    }
                    canvas.drawRect(minX, min(y382, y618), maxX, max(y382, y618), pocketPaint)

                    dPaint.style = Paint.Style.STROKE
                    dPaint.textSize = 14f
                    for ((ratio, label) in fibLevels) {
                        val lPrice = p0 + diff * ratio
                        val y = toY(lPrice)
                        dPaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
                        canvas.drawLine(minX, y, maxX, y, dPaint)
                        dPaint.pathEffect = null
                        val text = "$label (${String.format(Locale.US, "%,.0f", lPrice)})"
                        canvas.drawText(text, minX + 10f, y - 6f, dPaint)
                    }
                }
                is ChartDrawing.Brush -> {
                    if (d.points.size >= 2) {
                        val path = android.graphics.Path()
                        var started = false
                        for (pt in d.points) {
                            val x = bmpTimeToX(pt.time)
                            val y = toY(pt.price)
                            if (!started) { path.moveTo(x, y); started = true } else { path.lineTo(x, y) }
                        }
                        dPaint.style = Paint.Style.STROKE
                        dPaint.strokeCap = Paint.Cap.ROUND
                        canvas.drawPath(path, dPaint)
                    }
                }
                is ChartDrawing.Text -> {
                    val x = bmpTimeToX(d.position.time)
                    val y = toY(d.position.price)
                    dPaint.style = Paint.Style.FILL
                    dPaint.textSize = 18f
                    dPaint.isFakeBoldText = true

                    val bounds = Rect()
                    dPaint.getTextBounds(d.text, 0, d.text.length, bounds)
                    val bgPaint = Paint().apply {
                        color = android.graphics.Color.argb(220, 30, 41, 59)
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(x - 8f, y - bounds.height() - 10f, x + bounds.width() + 12f, y + 8f, bgPaint)
                    canvas.drawText(d.text, x, y, dPaint)
                }
            }
        }
    }

    return bitmap
}
