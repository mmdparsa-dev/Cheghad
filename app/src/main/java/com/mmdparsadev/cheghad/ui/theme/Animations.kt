package com.mmdparsadev.cheghad.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

object ExpressiveAnimations {
    // Spatial (Layout changes, movement, sliding)
    val FastSpatialEasing = CubicBezierEasing(0.42f, 1.67f, 0.21f, 0.90f)
    val DefaultSpatialEasing = CubicBezierEasing(0.38f, 1.21f, 0.22f, 1.00f)
    val SlowSpatialEasing = CubicBezierEasing(0.39f, 1.29f, 0.35f, 0.98f)

    val FastSpatialDuration = 350
    val DefaultSpatialDuration = 500
    val SlowSpatialDuration = 650

    // Effects (Fade, Scale, Color)
    val FastEffectsEasing = CubicBezierEasing(0.31f, 0.94f, 0.34f, 1.00f)
    val DefaultEffectsEasing = CubicBezierEasing(0.34f, 0.80f, 0.34f, 1.00f)
    val SlowEffectsEasing = CubicBezierEasing(0.34f, 0.88f, 0.34f, 1.00f)

    val FastEffectsDuration = 150
    val DefaultEffectsDuration = 200
    val SlowEffectsDuration = 300

    // Spatial Specs
    fun <T> fastSpatial() = tween<T>(durationMillis = FastSpatialDuration, easing = FastSpatialEasing)
    fun <T> defaultSpatial() = tween<T>(durationMillis = DefaultSpatialDuration, easing = DefaultSpatialEasing)
    fun <T> slowSpatial() = tween<T>(durationMillis = SlowSpatialDuration, easing = SlowSpatialEasing)

    // Effects Specs
    fun <T> fastEffects() = tween<T>(durationMillis = FastEffectsDuration, easing = FastEffectsEasing)
    fun <T> defaultEffects() = tween<T>(durationMillis = DefaultEffectsDuration, easing = DefaultEffectsEasing)
    fun <T> slowEffects() = tween<T>(durationMillis = SlowEffectsDuration, easing = SlowEffectsEasing)
}
