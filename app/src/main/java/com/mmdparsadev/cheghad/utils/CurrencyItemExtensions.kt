package com.mmdparsadev.cheghad.utils

import android.content.Context
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.CurrencyItem

/**
 * Checks if the price was updated recently (e.g., within the last 5 minutes).
 */
fun CurrencyItem.IsUpdatedRecently(): Boolean {
    val currentTime = System.currentTimeMillis()
    val fiveMinutesInMillis = 5 * 60 * 1000L
    return (currentTime - this.LastUpdatedTimestamp) <= fiveMinutesInMillis
}

/**
 * Returns formatted string output for the current price using Android string resources.
 */
fun CurrencyItem.GetFormattedPrice(context: Context): String {
    return context.getString(R.string.format_price, this.CurrentPrice)
}

/**
 * Returns formatted string output for the change percentage using Android string resources.
 */
fun CurrencyItem.GetFormattedChangePercentage(context: Context): String {
    return context.getString(R.string.format_percentage, this.ChangePercentage)
}
