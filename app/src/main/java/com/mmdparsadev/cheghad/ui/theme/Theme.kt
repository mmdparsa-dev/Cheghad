package com.mmdparsadev.cheghad.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = CheghadPrimary,
    onPrimary = Color.White,
    primaryContainer = CheghadPrimaryContainer,
    onPrimaryContainer = CheghadOnPrimaryContainer,
    secondaryContainer = CheghadSecondaryContainer,
    onSecondaryContainer = CheghadOnSecondaryContainer,
    tertiaryContainer = CheghadTertiaryContainer,
    background = CheghadBackground,
    onBackground = CheghadOnBackground,
    surface = CheghadBackground,
    onSurface = CheghadOnBackground
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondaryContainer = Color(0xFF332D41),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiaryContainer = Color(0xFF2D253A),
    background = Color(0xFF1E1C24),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1C24),
    onSurface = Color(0xFFE6E1E5)
  )

@Composable
fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
  val motionScheme = MaterialTheme.motionScheme
  val primary = animateColorAsState(targetColorScheme.primary, motionScheme.defaultEffectsSpec(), label = "primary")
  val onPrimary = animateColorAsState(targetColorScheme.onPrimary, motionScheme.defaultEffectsSpec(), label = "onPrimary")
  val primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, motionScheme.defaultEffectsSpec(), label = "primaryContainer")
  val onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, motionScheme.defaultEffectsSpec(), label = "onPrimaryContainer")
  val secondary = animateColorAsState(targetColorScheme.secondary, motionScheme.defaultEffectsSpec(), label = "secondary")
  val onSecondary = animateColorAsState(targetColorScheme.onSecondary, motionScheme.defaultEffectsSpec(), label = "onSecondary")
  val secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, motionScheme.defaultEffectsSpec(), label = "secondaryContainer")
  val onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, motionScheme.defaultEffectsSpec(), label = "onSecondaryContainer")
  val background = animateColorAsState(targetColorScheme.background, motionScheme.defaultEffectsSpec(), label = "background")
  val onBackground = animateColorAsState(targetColorScheme.onBackground, motionScheme.defaultEffectsSpec(), label = "onBackground")
  val surface = animateColorAsState(targetColorScheme.surface, motionScheme.defaultEffectsSpec(), label = "surface")
  val onSurface = animateColorAsState(targetColorScheme.onSurface, motionScheme.defaultEffectsSpec(), label = "onSurface")
  val outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, motionScheme.defaultEffectsSpec(), label = "outlineVariant")
  val error = animateColorAsState(targetColorScheme.error, motionScheme.defaultEffectsSpec(), label = "error")
  val errorContainer = animateColorAsState(targetColorScheme.errorContainer, motionScheme.defaultEffectsSpec(), label = "errorContainer")

  return targetColorScheme.copy(
    primary = primary.value,
    onPrimary = onPrimary.value,
    primaryContainer = primaryContainer.value,
    onPrimaryContainer = onPrimaryContainer.value,
    secondary = secondary.value,
    onSecondary = onSecondary.value,
    secondaryContainer = secondaryContainer.value,
    onSecondaryContainer = onSecondaryContainer.value,
    background = background.value,
    onBackground = onBackground.value,
    surface = surface.value,
    onSurface = onSurface.value,
    outlineVariant = outlineVariant.value,
    error = error.value,
    errorContainer = errorContainer.value
  )
}

@Composable
fun MyApplicationTheme(
  themeMode: String = "system",
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  seedColor: Color? = null,
  animate: Boolean = true,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  initializeVazirFont(context)

  val darkTheme = when (themeMode) {
    "light" -> false
    "dark" -> true
    else -> isSystemInDarkTheme()
  }

  val baseColorScheme =
    when {
      seedColor != null -> {
        val onColor = if (seedColor.red * 0.299 + seedColor.green * 0.587 + seedColor.blue * 0.114 > 0.6) Color.Black else Color.White
        if (darkTheme) {
          darkColorScheme(
            primary = seedColor,
            onPrimary = onColor,
            primaryContainer = seedColor.copy(alpha = 0.3f),
            onPrimaryContainer = seedColor,
            secondary = seedColor,
            onSecondary = onColor,
            secondaryContainer = seedColor.copy(alpha = 0.2f),
            onSecondaryContainer = seedColor,
            tertiary = seedColor,
            onTertiary = onColor,
            tertiaryContainer = seedColor.copy(alpha = 0.15f),
            onTertiaryContainer = seedColor,
            background = seedColor.copy(alpha = 0.05f).compositeOver(Color(0xFF1E1C24)),
            surface = seedColor.copy(alpha = 0.05f).compositeOver(Color(0xFF1E1C24)),
            surfaceVariant = seedColor.copy(alpha = 0.08f).compositeOver(Color(0xFF2D253A)),
            onSurface = Color(0xFFE6E1E5),
            onSurfaceVariant = Color(0xFFCAC4D0),
            outline = seedColor.copy(alpha = 0.5f),
            outlineVariant = seedColor.copy(alpha = 0.2f)
          )
        } else {
          lightColorScheme(
            primary = seedColor,
            onPrimary = onColor,
            primaryContainer = seedColor.copy(alpha = 0.12f),
            onPrimaryContainer = seedColor,
            secondary = seedColor,
            onSecondary = onColor,
            secondaryContainer = seedColor.copy(alpha = 0.08f),
            onSecondaryContainer = seedColor,
            tertiary = seedColor,
            onTertiary = onColor,
            tertiaryContainer = seedColor.copy(alpha = 0.05f),
            onTertiaryContainer = seedColor,
            background = seedColor.copy(alpha = 0.02f).compositeOver(Color(0xFFFEF7FF)),
            surface = seedColor.copy(alpha = 0.02f).compositeOver(Color(0xFFFEF7FF)),
            surfaceVariant = seedColor.copy(alpha = 0.05f).compositeOver(Color(0xFFF4F0F7)),
            onSurface = Color(0xFF1D1B20),
            onSurfaceVariant = Color(0xFF49454F),
            outline = seedColor.copy(alpha = 0.5f),
            outlineVariant = seedColor.copy(alpha = 0.15f)
          )
        }
      }

      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val colorScheme = if (animate) animateColorScheme(baseColorScheme) else baseColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    motionScheme = MotionScheme.expressive(),
    content = content
  )
}

