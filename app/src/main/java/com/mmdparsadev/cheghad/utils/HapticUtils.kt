package com.mmdparsadev.cheghad.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtils {

    fun vibrate(context: Context, type: HapticType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hasAmplitude = vibrator.hasAmplitudeControl()

            val effect = when (type) {
                HapticType.LIGHT -> VibrationEffect.createOneShot(
                    60,
                    if (hasAmplitude) 50 else VibrationEffect.DEFAULT_AMPLITUDE
                )

                HapticType.MEDIUM -> VibrationEffect.createOneShot(
                    70,
                    if (hasAmplitude) 100 else VibrationEffect.DEFAULT_AMPLITUDE
                )

                HapticType.HEAVY -> VibrationEffect.createOneShot(
                    80,
                    if (hasAmplitude) 200 else VibrationEffect.DEFAULT_AMPLITUDE
                )

                HapticType.SUCCESS -> {
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 60, 50, 60),
                        if (hasAmplitude) intArrayOf(0, 100, 0, 200) else intArrayOf(
                            0,
                            VibrationEffect.DEFAULT_AMPLITUDE,
                            0,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        ),
                        -1
                    )
                }

                HapticType.ERROR -> {
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 60, 50, 60),
                        if (hasAmplitude) intArrayOf(0, 255, 0, 255) else intArrayOf(
                            0,
                            VibrationEffect.DEFAULT_AMPLITUDE,
                            0,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        ),
                        -1
                    )
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val vibrationAttributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_TOUCH)
                    .build()
                vibrator.vibrate(effect, vibrationAttributes)
            } else {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
                @Suppress("DEPRECATION")
                vibrator.vibrate(effect, audioAttributes)
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(type.duration)
        }
    }
}

enum class HapticType(val duration: Long) {
    LIGHT(60),
    MEDIUM(70),
    HEAVY(80),
    SUCCESS(170),
    ERROR(170)
}
