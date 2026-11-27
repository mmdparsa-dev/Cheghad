package com.mmdparsadev.cheghad.ui.technical

import com.mmdparsadev.cheghad.data.api.ApiClient
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.EmaPeriod
import com.mmdparsadev.cheghad.data.repository.TechnicalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TechnicalChartEnhancementsTest {

    @Test
    fun chartTimeframe_includes1mAnd5mWithExpectedResolutions() {
        assertEquals("1", ChartTimeframe.MIN1.resolution)
        assertEquals(60L, ChartTimeframe.MIN1.secondsPerCandle)
        assertEquals("1m", ChartTimeframe.MIN1.titleEn)

        assertEquals("5", ChartTimeframe.MIN5.resolution)
        assertEquals(300L, ChartTimeframe.MIN5.secondsPerCandle)
        assertEquals("5m", ChartTimeframe.MIN5.titleEn)
        assertEquals("1m", ChartTimeframe.MIN1.localizedTitle(isEnglish = true))
        assertEquals("۱ دقیقه", ChartTimeframe.MIN1.localizedTitle(isEnglish = false))

        // Ensure Monthly timeframe remains intact
        assertEquals("1M", ChartTimeframe.M1.resolution)
    }

    @Test
    fun apiClient_mapsIntraday1mAnd5mSeparatelyFromMonth() {
        assertEquals(21_000L to "1", ApiClient.mapRangeToTgjuParams("1m"))
        assertEquals(105_000L to "5", ApiClient.mapRangeToTgjuParams("5m"))
        assertEquals((30L * 86400L) to "D", ApiClient.mapRangeToTgjuParams("MONTH"))
    }

    @Test
    fun computeIndicators_populatesMultiEmaAndRsiCleanly() {
        val repo = TechnicalRepository()
        // Ascending prices -> shorter EMAs must react faster and stay above longer EMAs
        val candles = (1..260).map { i ->
            CandleData(
                time = i * 60_000L,
                open = i.toDouble(),
                high = i + 1.0,
                low = i - 0.5,
                close = i.toDouble(),
                volume = 100.0
            )
        }

        val computed = repo.computeIndicators(candles)
        val last = computed.last()

        val ema25 = requireNotNull(last.ema25)
        val ema50 = requireNotNull(last.ema50)
        val ema100 = requireNotNull(last.ema100)
        val ema200 = requireNotNull(last.ema200)
        assertNotNull(last.rsi)

        // On steady uptrend: close > ema25 > ema50 > ema100 > ema200
        assertTrue(last.close > ema25)
        assertTrue(ema25 > ema50)
        assertTrue(ema50 > ema100)
        assertTrue(ema100 > ema200)

        for (period in EmaPeriod.entries) {
            assertNotNull(period.extractFrom(last))
        }
    }

    @Test
    fun computeIndicators_handlesShortSeriesWithoutCrashing() {
        val repo = TechnicalRepository()
        val shortCandles = (1..10).map { i ->
            CandleData(
                time = i * 60_000L,
                open = 100.0,
                high = 102.0,
                low = 99.0,
                close = 100.0 + i,
                volume = 10.0
            )
        }

        val computed = repo.computeIndicators(shortCandles)
        assertEquals(10, computed.size)
        assertNotNull(computed.first().ema25)
        assertNotNull(computed.last().ema200)
    }

    @Test
    fun formatCandleDateTime_displaysDateAndTimeAcrossDigitTypes() {
        val sampleTs = 1795770000_000L
        val en = formatCandleDateTime(sampleTs, digitType = "en", calendarType = "gregorian")
        val fa = formatCandleDateTime(sampleTs, digitType = "fa", calendarType = "gregorian")

        // Format is yyyy/MM/dd HH:mm (exact date AND time)
        assertTrue(Regex("""\d{4}/\d{2}/\d{2} \d{2}:\d{2}""").matches(en))
        assertTrue(fa.contains("/") && fa.contains(":") && fa.any { it in '۰'..'۹' })
    }
}
