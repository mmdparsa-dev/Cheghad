package com.mmdparsadev.cheghad

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.*
import org.junit.Test

class PriceFormattingTest {

    @Test
    fun testFormatTargetPriceWithCommasAndDigitSystems() {
        assertEquals("1,234,567", formatTargetPrice(1234567.0, "en"))
        assertEquals("۱,۲۳۴,۵۶۷", formatTargetPrice(1234567.0, "fa"))
        assertEquals("١,٢٣٤,٥٦٧", formatTargetPrice(1234567.0, "ar"))

        assertEquals("1,234.5", formatTargetPrice(1234.5, "en"))
        assertEquals("۱,۲۳۴.۵", formatTargetPrice(1234.5, "fa"))
        assertEquals("١,٢٣٤.٥", formatTargetPrice(1234.5, "ar"))

        assertEquals("", formatTargetPrice(0.0, "fa"))
    }

    @Test
    fun testFormatPriceInput() {
        assertEquals("1,000,000", formatPriceInput("1000000", "en"))
        assertEquals("۱,۰۰۰,۰۰۰", formatPriceInput("1000000", "fa"))
        assertEquals("١,٠٠٠,٠٠٠", formatPriceInput("1000000", "ar"))

        assertEquals("۱,۰۰۰,۰۰۰", formatPriceInput("۱۰۰۰۰۰۰", "fa"))
        assertEquals("١,٠٠٠,٠٠٠", formatPriceInput("١٠٠٠٠٠٠", "ar"))

        assertEquals("1,234.5", formatPriceInput("1234.5", "en"))
        assertEquals("۱,۲۳۴.۵", formatPriceInput("1234.5", "fa"))
        assertEquals("۱,۲۳۴.", formatPriceInput("1234.", "fa"))

        assertEquals("", formatPriceInput("", "fa"))
    }

    @Test
    fun testParseTargetPrice() {
        assertEquals(1234567.0, parseTargetPrice("1,234,567")!!, 0.001)
        assertEquals(1234567.0, parseTargetPrice("۱,۲۳۴,۵۶۷")!!, 0.001)
        assertEquals(1234567.0, parseTargetPrice("١,٢٣٤,٥٦٧")!!, 0.001)

        assertEquals(1234.5, parseTargetPrice("1,234.5")!!, 0.001)
        assertEquals(1234.5, parseTargetPrice("۱,۲۳۴.۵")!!, 0.001)
        assertEquals(1234.5, parseTargetPrice("١,٢٣٤.٥")!!, 0.001)

        assertNull(parseTargetPrice(""))
        assertNull(parseTargetPrice("abc"))
    }

    @Test
    fun testFormatPriceTextFieldValueTypingAndCursor() {
        val oldVal = TextFieldValue("۱,۲۳۴", TextRange(5))
        val newVal = TextFieldValue("۱,۲۳۴۵", TextRange(6))
        val formatted = formatPriceTextFieldValue(oldVal, newVal, "fa")

        assertEquals("۱۲,۳۴۵", formatted.text)
        assertEquals(6, formatted.selection.end)
    }

    @Test
    fun testFormatPriceTextFieldValueBackspaceComma() {
        // When cursor is after comma "12,|345" (index 3) and user deletes comma
        val oldVal = TextFieldValue("12,345", TextRange(3))
        val newVal = TextFieldValue("12345", TextRange(2))
        val formatted = formatPriceTextFieldValue(oldVal, newVal, "en")

        // Preceding digit '2' should be removed as well, resulting in "1,345"
        assertEquals("1,345", formatted.text)
    }

    @Test
    fun testCalculatorOverflowDisplaysOverflow() {
        assertEquals("Overflow", formatPrice(Double.POSITIVE_INFINITY, "fa"))
        assertEquals("Overflow", formatPrice(Double.NEGATIVE_INFINITY, "en"))
        assertEquals("Overflow", formatPrice(Double.NaN, "en"))
        assertEquals("Overflow", formatCalculatorToman(Double.POSITIVE_INFINITY, "en", "Toman"))
        assertEquals("Overflow", formatCalculatorAssetAmount(Double.POSITIVE_INFINITY, "USD", "fa"))
        assertEquals("Overflow", formatCalculatorAssetAmount(0.0, "USD", "en", inputOverflow = true))
        assertEquals("100,000 Toman", formatCalculatorToman(100_000.0, "en", "Toman"))
        assertEquals("1.5000 USD", formatCalculatorAssetAmount(1.5, "USD", "en"))
    }
}
