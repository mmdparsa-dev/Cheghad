package com.mmdparsadev.cheghad.utils

import android.content.Context
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.CurrencyItem

/**
 * Checks if the price was updated recently (e.g., within the last 5 minutes).
 */
fun CurrencyItem.isUpdatedRecently(): Boolean {
    val currentTime = System.currentTimeMillis()
    val fiveMinutesInMillis = 5 * 60 * 1000L
    return (currentTime - this.lastUpdatedTimestamp) <= fiveMinutesInMillis
}

/**
 * Returns formatted string output for the current price using Android string resources.
 */
fun CurrencyItem.getFormattedPrice(context: Context): String {
    return context.getString(R.string.format_price, this.currentPrice)
}

/**
 * Returns formatted string output for the change percentage using Android string resources.
 */
fun CurrencyItem.getFormattedChangePercentage(context: Context): String {
    return context.getString(R.string.format_percentage, this.changePercentage)
}
