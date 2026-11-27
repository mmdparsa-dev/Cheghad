package com.mmdparsadev.cheghad

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.CurrencyType
import com.mmdparsadev.cheghad.data.models.EmaPeriod
import com.mmdparsadev.cheghad.data.models.NewsCategory
import com.mmdparsadev.cheghad.data.models.PriceDirection
import com.mmdparsadev.cheghad.data.repository.NewsRepository
import com.mmdparsadev.cheghad.ui.NewsScreen
import com.mmdparsadev.cheghad.ui.technical.TechnicalCandlestickChart
import com.mmdparsadev.cheghad.ui.technical.formatCandleDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class CheghadDeviceInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun localizedContext(base: Context, languageTag: String): Context {
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(languageTag))
        return base.createConfigurationContext(config)
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
    fun resources_enAndFa_includeAllTranslationsAndFixes() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val en = localizedContext(base, "en")
        val fa = localizedContext(base, "fa")

        assertEquals("Third row", en.getString(R.string.third_row_customize))
        assertEquals("ردیف سوم", fa.getString(R.string.third_row_customize))

        assertEquals("All data is sourced from tgju.com", en.getString(R.string.market_footer_source))
        assertEquals("تمام اطلاعات از tgju.com گرفته شده", fa.getString(R.string.market_footer_source))

        // Verify Technical Chart resources translate cleanly across locales
        val technicalKeys = listOf(
            R.string.tech_custom_zoom,
            R.string.tech_tab_currencies,
            R.string.tech_tab_ema_status,
            R.string.tech_tool_pan,
            R.string.tech_tool_trend,
            R.string.tech_candle_price_label,
            R.string.tech_current_price_label,
            R.string.tech_ohlc_open,
            R.string.tech_export_sheet_title,
            R.string.tech_pdf_header_title,
            R.string.tech_ema_enable_all
        )
        for (resId in technicalKeys) {
            assertNotEquals(
                "Expected distinct EN and FA translation for resId=$resId",
                en.getString(resId),
                fa.getString(resId)
            )
        }
    }

    @Test
    fun jalaliAndGregorianFormatting_worksOnAndroidIcuCalendar() {
        // 2026-11-27T10:30:00Z
        val timestampMs = 1_795_775_400_000L
        val jalali = formatCandleDateTime(timestampMs, digitType = "fa", calendarType = "jalali")
        val gregorian = formatCandleDateTime(timestampMs, digitType = "en", calendarType = "gregorian")

        assertTrue("Expected Jalali year 1405 in '$jalali'", jalali.contains("۱۴۰۵"))
        assertTrue("Expected Gregorian 2026 in '$gregorian'", gregorian.startsWith("2026/"))
        assertTrue("Expected time HH:mm in '$jalali'", jalali.contains(':'))
        assertTrue("Expected time HH:mm in '$gregorian'", gregorian.contains(':'))
    }

    @Test
    fun technicalChart_rendersMultiEmaIndicatorsOnDevice() {
        val currency = sampleCurrency()
        val nowMs = System.currentTimeMillis()
        val candles = (1..220).map { i ->
            val p = 100_000.0 + i * 50.0
            CandleData(
                time = nowMs - (220 - i) * 60_000L,
                open = p - 20.0,
                high = p + 30.0,
                low = p - 30.0,
                close = p,
                volume = 10.0,
                ema25 = p - 10.0,
                ema50 = p - 20.0,
                ema100 = p - 40.0,
                ema200 = p - 80.0,
                rsi = 58.4
            )
        }

        composeTestRule.setContent {
            MaterialTheme {
                TechnicalCandlestickChart(
                    candles = candles,
                    currency = currency,
                    timeframe = ChartTimeframe.MIN1,
                    enabledEmas = EmaPeriod.entries.toSet(),
                    showRsi = true,
                    digitType = "en",
                    calendarType = "gregorian"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("EMA 25", substring = true).onFirst().performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("EMA 50", substring = true).onFirst().performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("EMA 100", substring = true).onFirst().performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("EMA 200", substring = true).onFirst().performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText("RSI (14)", substring = true).onFirst().performScrollTo().assertIsDisplayed()
    }

    @Test
    fun calculatorScreen_displaysOverflowInsteadOfInfinityOnDevice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val amountPrefix = context.getString(R.string.calc_amount)
        val tomanModeLabel = context.getString(R.string.calc_mode_toman_to_asset)
        val tomanHint = context.getString(R.string.calc_toman_hint)

        composeTestRule.setContent {
            MaterialTheme {
                CurrencyCalculatorScreen(
                    items = listOf(sampleCurrency()),
                    digitType = "en",
                    innerPadding = PaddingValues(0.dp)
                )
            }
        }
        composeTestRule.waitForIdle()

        // Mode 0: Asset -> Toman overflow
        composeTestRule.onAllNodesWithText(amountPrefix, substring = true).onFirst()
            .performScrollTo()
            .performTextReplacement("9".repeat(310))
        composeTestRule.onNodeWithText("Overflow").performScrollTo().assertIsDisplayed()

        // Switch to Mode 1: Toman -> Asset overflow
        composeTestRule.onNodeWithText(tomanModeLabel).performScrollTo().performClick()
        composeTestRule.onNodeWithText(tomanHint).performScrollTo().performTextReplacement("9".repeat(315))
        composeTestRule.onAllNodesWithText("Overflow").onFirst().performScrollTo().assertIsDisplayed()
    }

    @Test
    fun timeRangeReorderSection_moveDownReordersCleanlyOnDevice() {
        var latestOrder: List<TimeRange> = listOf(TimeRange.DAY, TimeRange.WEEK, TimeRange.MONTH, TimeRange.YEAR)

        composeTestRule.setContent {
            MaterialTheme {
                TimeRangeReorderSection(
                    timeRangeOrder = latestOrder,
                    onTimeRangeOrderChanged = { latestOrder = it },
                    digitType = "en"
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithContentDescription("Move Down").onFirst().performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(TimeRange.WEEK, TimeRange.DAY, TimeRange.MONTH, TimeRange.YEAR),
            latestOrder
        )
    }

    @Test
    fun newsScreen_rendersDisabledStateWhenNoCategoryOrAgencyEnabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val disabledText = context.getString(R.string.news_disabled_status)
        val allCats = NewsCategory.entries.filter { it != NewsCategory.All }.map { it.name }.toSet()
        val allAgencies = NewsRepository.AGENCIES.map { it.id }.toSet()

        composeTestRule.setContent {
            MaterialTheme {
                NewsScreen(
                    innerPadding = PaddingValues(0.dp),
                    digitType = "en",
                    newsArticles = emptyList(),
                    isRefreshing = false,
                    onRefresh = {},
                    disabledCategories = allCats,
                    disabledAgencies = allAgencies,
                    newsEnabled = false
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(disabledText).assertIsDisplayed()
    }
}
