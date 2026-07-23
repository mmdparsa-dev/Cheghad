package com.mmdparsadev.cheghad.data.models

import androidx.compose.ui.graphics.Color

enum class NewsCategory {
    All,
    Economic,
    CurrencyGold,
    Bourse,
    Crypto,
    World
}

data class NewsAgency(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val rssUrl: String,
    val websiteUrl: String,
    val brandColorHex: Long
) {
    val brandColor: Color
        get() = Color(brandColorHex)
}

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val agency: NewsAgency,
    val pubDate: String,
    val timeAgo: String,
    val category: NewsCategory,
    val link: String,
    val imageUrl: String? = null,
    val isBookmarked: Boolean = false,
    val pubTimestamp: Long = System.currentTimeMillis()
)
