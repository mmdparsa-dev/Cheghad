package com.mmdparsadev.cheghad.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
  val primary = animateColorAsState(targetColorScheme.primary, ExpressiveAnimations.defaultEffects(), label = "primary")
  val onPrimary = animateColorAsState(targetColorScheme.onPrimary, ExpressiveAnimations.defaultEffects(), label = "onPrimary")
  val primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, ExpressiveAnimations.defaultEffects(), label = "primaryContainer")
  val onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, ExpressiveAnimations.defaultEffects(), label = "onPrimaryContainer")
  val secondary = animateColorAsState(targetColorScheme.secondary, ExpressiveAnimations.defaultEffects(), label = "secondary")
  val onSecondary = animateColorAsState(targetColorScheme.onSecondary, ExpressiveAnimations.defaultEffects(), label = "onSecondary")
  val secondaryContainer = animateColorAsState(targetColorScheme.secondaryContainer, ExpressiveAnimations.defaultEffects(), label = "secondaryContainer")
  val onSecondaryContainer = animateColorAsState(targetColorScheme.onSecondaryContainer, ExpressiveAnimations.defaultEffects(), label = "onSecondaryContainer")
  val background = animateColorAsState(targetColorScheme.background, ExpressiveAnimations.defaultEffects(), label = "background")
  val onBackground = animateColorAsState(targetColorScheme.onBackground, ExpressiveAnimations.defaultEffects(), label = "onBackground")
  val surface = animateColorAsState(targetColorScheme.surface, ExpressiveAnimations.defaultEffects(), label = "surface")
  val onSurface = animateColorAsState(targetColorScheme.onSurface, ExpressiveAnimations.defaultEffects(), label = "onSurface")
  val outlineVariant = animateColorAsState(targetColorScheme.outlineVariant, ExpressiveAnimations.defaultEffects(), label = "outlineVariant")
  val error = animateColorAsState(targetColorScheme.error, ExpressiveAnimations.defaultEffects(), label = "error")
  val errorContainer = animateColorAsState(targetColorScheme.errorContainer, ExpressiveAnimations.defaultEffects(), label = "errorContainer")

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
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val colorScheme = animateColorScheme(baseColorScheme)

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

