package com.mmdparsadev.cheghad.ui.theme

import androidx.compose.ui.graphics.Color

val CheghadBackground = Color(0xFFFEF7FF)
val CheghadOnBackground = Color(0xFF1D1B20)
val CheghadPrimary = Color(0xFF6750A4)
val CheghadPrimaryContainer = Color(0xFFEADDFF)
val CheghadOnPrimaryContainer = Color(0xFF21005D)
val CheghadSecondaryContainer = Color(0xFFE8DEF8)
val CheghadOnSecondaryContainer = Color(0xFF1D192B)
val CheghadTertiaryContainer = Color(0xFFF3E7FF)

val CheghadPositive = Color(0xFF1D6C40)
val CheghadNegative = Color(0xFFB3261E)
val CheghadNegativeContainer = Color(0xFFF9DEDC)
val CheghadBorder = Color(0xFFEADDFF)
val CheghadLightGray = Color(0xFFF1F5F9) // For crypto/euro icons

enum class AppThemeColor(val seedColor: Color, val stringRes: Int) {
    DEFAULT(Color(0xFF6750A4), com.mmdparsadev.cheghad.R.string.color_default),
    BLUE(Color(0xFF2196F3), com.mmdparsadev.cheghad.R.string.color_blue),
    GREEN(Color(0xFF4CAF50), com.mmdparsadev.cheghad.R.string.color_green),
    RED(Color(0xFFF44336), com.mmdparsadev.cheghad.R.string.color_red),
    ORANGE(Color(0xFFFF9800), com.mmdparsadev.cheghad.R.string.color_orange),
    PINK(Color(0xFFE91E63), com.mmdparsadev.cheghad.R.string.color_pink)
}
