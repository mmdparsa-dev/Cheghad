package com.mmdparsadev.cheghad.ui.technical

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartDrawing
import com.mmdparsadev.cheghad.data.models.ChartPoint
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.EmaPeriod
import com.mmdparsadev.cheghad.data.models.PriceDirection
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "en-land-xxhdpi")
class TechnicalChartLandscapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var appContext: Context

    @Before
    fun setUp() {
        appContext = ApplicationProvider.getApplicationContext()
    }

    private fun sampleCurrency(symbol: String = "USD", price: Double = 100_000.0) = CurrencyItem(
        id = symbol,
        symbol = symbol,
        title = "US Dollar",
        currentPrice = price,
        previousPrice = price * 0.99,
        changeAmount = price * 0.01,
        changePercentage = 1.0,
        priceDirection = PriceDirection.Up,
        lastUpdatedTimestamp = System.currentTimeMillis(),
        iconUrl = "",
        category = CurrencyType.Currency
    )

    private fun sampleCandles(count: Int = 50): List<CandleData> {
        val nowMs = System.currentTimeMillis()
        return (1..count).map { i ->
            val p = 100_000.0 + i * 25.0
            CandleData(
                time = nowMs - (count - i) * 60_000L,
                open = p - 10.0,
                high = p + 15.0,
                low = p - 15.0,
                close = p,
                volume = 10.0,
                ema25 = p - 5.0,
                ema50 = p - 12.0,
                ema100 = p - 24.0,
                ema200 = p - 45.0,
                rsi = 56.0
            )
        }
    }

    @Test
    fun candlestickChart_zeroAndTinyBounds_neverThrowsCoerceInException() {
        val candles = sampleCandles()
        val sampleDrawings = listOf(
            ChartDrawing.HorizontalLine(id = "h1", price = candles.last().close),
            ChartDrawing.Fibonacci(
                id = "f1",
                start = ChartPoint(candles.first().time, candles.first().low),
                end = ChartPoint(candles.last().time, candles.last().high)
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                // Zero-height frame + tiny 24.dp transition frame + wide landscape frame
                Box(modifier = Modifier.size(width = 800.dp, height = 0.dp)) {
                    TechnicalCandlestickChart(
                        candles = candles,
                        currency = sampleCurrency(),
                        enabledEmas = EmaPeriod.entries.toSet(),
                        showRsi = true,
                        drawings = sampleDrawings,
                        isLandscapeMode = true
                    )
                }
                Box(modifier = Modifier.width(800.dp).height(24.dp)) {
                    TechnicalCandlestickChart(
                        candles = candles,
                        currency = sampleCurrency(),
                        enabledEmas = EmaPeriod.entries.toSet(),
                        showRsi = true,
                        drawings = sampleDrawings,
                        isLandscapeMode = true
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun candlestickChart_landscapeModeSupportsDragCrosshairWithoutCrashing() {
        val candles = sampleCandles()
        composeTestRule.setContent {
            MaterialTheme {
                TechnicalCandlestickChart(
                    candles = candles,
                    currency = sampleCurrency(),
                    timeframe = ChartTimeframe.MIN5,
                    enabledEmas = setOf(EmaPeriod.EMA_25, EmaPeriod.EMA_200),
                    showRsi = true,
                    isLandscapeMode = true,
                    digitType = "en",
                    calendarType = "gregorian",
                    modifier = Modifier.width(760.dp).height(260.dp)
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("EMA 25", substring = true).onFirst().performTouchInput {
            down(center)
            moveBy(androidx.compose.ui.geometry.Offset(-40f, 0f))
            up()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun technicalChartScreen_landscapeShowsInlineTimeframesAndToggleableDrawingToolbar() {
        val drawToolsDesc = appContext.getString(R.string.tech_drawing_tools)
        val trendToolLabel = appContext.getString(R.string.tech_tool_trend)
        val exportDesc = appContext.getString(R.string.tech_export_chart)
        val currency = sampleCurrency()

        composeTestRule.setContent {
            MaterialTheme {
                TechnicalChartScreen(
                    initialCurrency = currency,
                    allCurrencies = listOf(currency),
                    digitType = "en",
                    calendarType = "gregorian",
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Landscape single-row bar keeps inline timeframes and hides DrawingToolbar until toggled
        composeTestRule.onNodeWithContentDescription(exportDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(drawToolsDesc).assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription(trendToolLabel).let { nodes ->
            assert(nodes.fetchSemanticsNodes().isEmpty()) {
                "Expected DrawingToolbar collapsed by default in landscape"
            }
        }

        // Expanding drawing tools on demand works cleanly
        composeTestRule.onNodeWithContentDescription(drawToolsDesc).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText(trendToolLabel).onFirst().assertIsDisplayed()
    }
}
