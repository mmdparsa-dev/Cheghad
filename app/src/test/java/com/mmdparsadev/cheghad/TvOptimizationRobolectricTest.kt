package com.mmdparsadev.cheghad

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.EmaPeriod
import com.mmdparsadev.cheghad.data.models.NewsArticle
import com.mmdparsadev.cheghad.data.models.NewsCategory
import com.mmdparsadev.cheghad.data.models.PriceDirection
import com.mmdparsadev.cheghad.data.repository.NewsRepository
import com.mmdparsadev.cheghad.ui.ExpressiveConnectedButtonGroup
import com.mmdparsadev.cheghad.ui.NewsScreen
import com.mmdparsadev.cheghad.ui.technical.TechnicalCandlestickChart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "en-television")
class TvOptimizationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var appContext: Context

    @Before
    fun setUpTvUiMode() {
        appContext = ApplicationProvider.getApplicationContext()
    }

    private fun sampleCurrency(symbol: String = "USD", price: Double = 100_000.0): CurrencyItem = CurrencyItem(
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

    @Test
    fun isTvDevice_returnsTrueWhenTelevisionUiModeActive() {
        var detectedTv = false
        composeTestRule.setContent {
            detectedTv = isTvDevice()
        }
        composeTestRule.waitForIdle()
        assertTrue(detectedTv)
    }

    @Test
    fun timeRangeReorderSection_hidesTouchDragHandleAndReordersOnTv() {
        var order = listOf(TimeRange.HOUR, TimeRange.DAY, TimeRange.WEEK)

        composeTestRule.setContent {
            MaterialTheme {
                TimeRangeReorderSection(
                    timeRangeOrder = order,
                    onTimeRangeOrderChanged = { order = it },
                    digitType = "en"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithContentDescription("Drag to reorder").assertCountEquals(0)
        composeTestRule.onAllNodesWithContentDescription("Move Down").onFirst().performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(TimeRange.DAY, TimeRange.HOUR, TimeRange.WEEK), order)
    }

    @Test
    fun settingsSwitchRow_togglesInSingleClickOnTv() {
        var checked = false

        composeTestRule.setContent {
            MaterialTheme {
                SettingsSwitchRow(
                    title = "Economic News",
                    isChecked = checked,
                    onCheckedChange = { checked = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Economic News").performClick()
        composeTestRule.waitForIdle()
        assertTrue(checked)
    }

    @Test
    fun technicalChart_tvModeShowsQuickControlsAndStepsCandles() {
        val nowMs = System.currentTimeMillis()
        val candles = (1..30).map { i ->
            val p = 100_000.0 + i * 25.0
            CandleData(
                time = nowMs - (30 - i) * 60_000L,
                open = p - 10.0,
                high = p + 15.0,
                low = p - 15.0,
                close = p,
                volume = 10.0,
                ema25 = p - 5.0,
                ema200 = p - 40.0,
                rsi = 55.0
            )
        }
        var hovered: CandleData? = null
        val prevDesc = appContext.getString(R.string.tech_tv_prev_candle)
        val nextDesc = appContext.getString(R.string.tech_tv_next_candle)
        val zoomInDesc = appContext.getString(R.string.tech_tv_zoom_in)
        val zoomOutDesc = appContext.getString(R.string.tech_tv_zoom_out)
        val resetDesc = appContext.getString(R.string.tech_tv_zoom_reset)

        composeTestRule.setContent {
            MaterialTheme {
                TechnicalCandlestickChart(
                    candles = candles,
                    currency = sampleCurrency(),
                    timeframe = ChartTimeframe.MIN1,
                    enabledEmas = setOf(EmaPeriod.EMA_25, EmaPeriod.EMA_200),
                    showRsi = true,
                    isTvMode = true,
                    digitType = "en",
                    calendarType = "gregorian",
                    onHoverCandleChanged = { hovered = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(prevDesc).assertIsDisplayed().performClick()
        composeTestRule.onNodeWithContentDescription(nextDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(zoomInDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(zoomOutDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(resetDesc).assertIsDisplayed()
        composeTestRule.waitForIdle()

        assertNotNull(hovered)
        composeTestRule.onAllNodesWithText("EMA 25", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("EMA 200", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun expressiveConnectedButtonGroup_worksInTvMode() {
        var selectedIdx = 0

        composeTestRule.setContent {
            MaterialTheme {
                ExpressiveConnectedButtonGroup(
                    itemsCount = 2,
                    selectedIndex = selectedIdx,
                    onSelect = { selectedIdx = it }
                ) { index, _ ->
                    Text(if (index == 0) "Tab A" else "Tab B")
                }
            }
        }
        composeTestRule.onNodeWithText("Tab B").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, selectedIdx)
    }

    @Test
    fun newsScreen_rendersInTvMode() {
        val sampleArticle = NewsArticle(
            id = "art-1",
            title = "TV Market Brief",
            summary = "Currency rates steady today.",
            agency = NewsRepository.AGENCIES.first(),
            pubDate = "2026-11-27",
            timeAgo = "now",
            category = NewsCategory.Economic,
            link = "https://example.com",
            pubTimestamp = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MaterialTheme {
                NewsScreen(
                    innerPadding = PaddingValues(0.dp),
                    digitType = "en",
                    newsArticles = listOf(sampleArticle),
                    isRefreshing = false,
                    onRefresh = {},
                    disabledCategories = emptySet(),
                    disabledAgencies = emptySet(),
                    newsEnabled = true
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("TV Market Brief").assertIsDisplayed()
    }
}
