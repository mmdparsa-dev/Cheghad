package com.mmdparsadev.cheghad.data.api

import com.mmdparsadev.cheghad.data.models.PriceDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiClientParsingTest {

    @Test
    fun parseTgjuNumber_handlesPersianArabicEnglishAndCommas() {
        assertEquals(2928900.0, ApiClient.parseTgjuNumber("2,928,900")!!, 0.001)
        assertEquals(2928900.0, ApiClient.parseTgjuNumber("۲,۹۲۸,۹۰۰")!!, 0.001)
        assertEquals(1234.56, ApiClient.parseTgjuNumber("١,٢٣٤.٥٦")!!, 0.001)
        assertEquals(97.0712, ApiClient.parseTgjuNumber(" 97.0712 ")!!, 0.0001)
        assertNull(ApiClient.parseTgjuNumber(""))
        assertNull(ApiClient.parseTgjuNumber("   "))
        assertNull(ApiClient.parseTgjuNumber("N/A"))
    }

    @Test
    fun parseTgjuAjaxJson_parsesPricesAndSignedDirections() {
        val sampleJson = """
            {
              "current": {
                "price_dollar_rl": { "p": "۲,۹۲۸,۹۰۰", "dp": 1.25, "dt": "high" },
                "price_eur": { "p": "3,221,500", "dp": "0.85", "dt": "low" },
                "crypto-bitcoin": { "p": "61695.11", "dp": 1.66, "dt": "high" },
                "ons": { "p": "3,679.72", "dp": 0, "dt": "" }
              }
            }
        """.trimIndent()

        val result = ApiClient.parseTgjuAjaxJson(sampleJson)
        assertEquals(2928900.0, result.prices["price_dollar_rl"]!!, 0.001)
        assertEquals(3221500.0, result.prices["price_eur"]!!, 0.001)
        assertEquals(61695.11, result.prices["crypto-bitcoin"]!!, 0.001)
        assertEquals(3679.72, result.prices["ons"]!!, 0.001)

        assertEquals(1.25, result.percentages["price_dollar_rl"]!!, 0.001)
        assertEquals(-0.85, result.percentages["price_eur"]!!, 0.001)
        assertEquals(1.66, result.percentages["crypto-bitcoin"]!!, 0.001)
        assertEquals(0.0, result.percentages["ons"]!!, 0.001)
    }

    @Test
    fun buildCurrencyItems_convertsUnitsAccuratelyWithoutRandomness() {
        val prices = mapOf(
            "price_dollar_rl" to 1_000_000.0, // 100,000 Toman
            "price_eur" to 1_100_000.0,       // 110,000 Toman
            "crypto-bitcoin" to 60_000.0,     // 60,000 USD -> 6,000,000,000 Toman
            "crypto-united-states-oil-tokenized-fund-ondo" to 100.0, // 100 USD -> 10,000,000 Toman
            "ons" to 2_500.0                  // Native USD
        )
        val percentages = mapOf(
            "price_dollar_rl" to 2.0,
            "price_eur" to -1.5,
            "crypto-bitcoin" to 5.0
        )

        val items = ApiClient.buildCurrencyItems(prices, percentages, liveUsdToman = 100_000.0, timestampMs = 123456L)
        val bySymbol = items.associateBy { it.symbol }

        val usd = bySymbol.getValue("USD")
        assertEquals(100_000.0, usd.currentPrice, 0.01)
        assertEquals(PriceDirection.Up, usd.priceDirection)
        assertEquals(2.0, usd.changePercentage, 0.01)

        val eur = bySymbol.getValue("EUR")
        assertEquals(110_000.0, eur.currentPrice, 0.01)
        assertEquals(PriceDirection.Down, eur.priceDirection)
        assertEquals(-1.5, eur.changePercentage, 0.01)

        val btc = bySymbol.getValue("BTC")
        assertEquals(6_000_000_000.0, btc.currentPrice, 1.0)
        assertEquals(PriceDirection.Up, btc.priceDirection)

        val usoon = bySymbol.getValue("USOON")
        assertEquals(10_000_000.0, usoon.currentPrice, 1.0)
        assertEquals(PriceDirection.Unchanged, usoon.priceDirection)

        val xau = bySymbol.getValue("XAU")
        assertEquals(2_500.0, xau.currentPrice, 0.01)
    }

    @Test
    fun sliceHistoryCloses_filtersToWindowOrAnchorsAtLastCandleWhenClosed() {
        val timestamps = listOf(1000L, 2000L, 3000L, 4000L, 5000L)
        val closes = listOf(10.0, 20.0, 30.0, 40.0, 50.0)

        // Direct window match
        val direct = ApiClient.sliceHistoryCloses(timestamps, closes, fromTs = 3000L, toTs = 5000L, windowSeconds = 2000L)
        assertEquals(listOf(30.0, 40.0, 50.0), direct)

        // Weekend gap (now >> latest candle): anchors backward from last candle 5000 by windowSeconds=2000 -> [3000, 5000]
        val weekend = ApiClient.sliceHistoryCloses(timestamps, closes, fromTs = 90000L, toTs = 93600L, windowSeconds = 2000L)
        assertEquals(listOf(30.0, 40.0, 50.0), weekend)
    }

    @Test
    fun sliceHistoryCloses_producesDistinctSlicesAcrossDayWeekMonthYearFromFullHistory() {
        val now = 1_800_000_000L
        val totalDays = 400
        val timestamps = (0..totalDays).map { dayOffset -> now - (totalDays - dayOffset) * 86400L }
        val closes = (0..totalDays).map { 1000.0 + it }

        val daySlice = ApiClient.sliceHistoryCloses(timestamps, closes, fromTs = now - 86400L, toTs = now, windowSeconds = 86400L)
        val weekSlice = ApiClient.sliceHistoryCloses(timestamps, closes, fromTs = now - 7L * 86400L, toTs = now, windowSeconds = 7L * 86400L)
        val monthSlice = ApiClient.sliceHistoryCloses(timestamps, closes, fromTs = now - 30L * 86400L, toTs = now, windowSeconds = 30L * 86400L)
        val yearSlice = ApiClient.sliceHistoryCloses(timestamps, closes, fromTs = now - 365L * 86400L, toTs = now, windowSeconds = 365L * 86400L)

        assertTrue(daySlice.size < weekSlice.size)
        assertTrue(weekSlice.size < monthSlice.size)
        assertTrue(monthSlice.size < yearSlice.size)
        assertEquals(2, daySlice.size)
        assertEquals(8, weekSlice.size)
        assertEquals(31, monthSlice.size)
        assertEquals(366, yearSlice.size)
    }

    @Test
    fun convertHistoryCloses_scalesByAssetUnit() {
        val raw = listOf(100.0, 200.0)

        assertEquals(listOf(10.0, 20.0), ApiClient.convertHistoryCloses("USD", raw) { 100_000.0 })
        assertEquals(listOf(10_000_000.0, 20_000_000.0), ApiClient.convertHistoryCloses("BTC", raw) { 100_000.0 })
        assertEquals(listOf(10_000_000.0, 20_000_000.0), ApiClient.convertHistoryCloses("USOON", raw) { 100_000.0 })
        assertEquals(listOf(100.0, 200.0), ApiClient.convertHistoryCloses("XAU", raw) { null })
        assertNull(ApiClient.convertHistoryCloses("BTC", raw) { null })
    }

    @Test
    fun mapRangeToTgjuParams_mapsAllRangesAccurately() {
        assertEquals(3600L to "5", ApiClient.mapRangeToTgjuParams("HOUR"))
        assertEquals(86400L to "60", ApiClient.mapRangeToTgjuParams("DAY"))
        assertEquals(7L * 86400L to "240", ApiClient.mapRangeToTgjuParams("WEEK"))
        assertEquals(30L * 86400L to "D", ApiClient.mapRangeToTgjuParams("MONTH"))
        assertEquals(365L * 86400L to "W", ApiClient.mapRangeToTgjuParams("YEAR"))
        assertTrue(ApiClient.mapRangeToTgjuParams("day").second == "60")
    }
}
