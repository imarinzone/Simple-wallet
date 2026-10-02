package com.example.security

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.roundToInt

enum class HapticSensitivity(val amplitudeScale: Float, val label: String, val description: String) {
    LIGHT(0.45f, "Light", "Subtle, gentle micro-ticks"),
    MEDIUM(1.0f, "Medium", "Balanced tactile feedback"),
    STRONG(1.6f, "Strong", "Pronounced physical feel")
}

class HapticsHelper(private val context: Context) {

    var sensitivity: HapticSensitivity = HapticSensitivity.MEDIUM

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun scaleAmplitude(amp: Int): Int {
        if (amp <= 0) return 0
        return (amp * sensitivity.amplitudeScale).roundToInt().coerceIn(1, 255)
    }

    private fun scaleDuration(ms: Long): Long {
        return when (sensitivity) {
            HapticSensitivity.LIGHT -> (ms * 0.7f).roundToInt().toLong().coerceAtLeast(4L)
            HapticSensitivity.MEDIUM -> ms
            HapticSensitivity.STRONG -> (ms * 1.3f).roundToInt().toLong()
        }
    }

    fun cardSlide() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = scaleAmplitude(90)
                vibrator.vibrate(VibrationEffect.createOneShot(scaleDuration(10), amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(12))
            }
        }
    }

    /**
     * Subtle micro-tick during active finger dragging to simulate physical resistance
     */
    fun cardDragTick() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = scaleAmplitude(60)
                vibrator.vibrate(VibrationEffect.createOneShot(scaleDuration(8), amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(8))
            }
        }
    }

    /**
     * Tactile cascading cards-ruffle effect when cards fan out into the stack
     */
    fun stackExpand() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, scaleDuration(10), 22, scaleDuration(14), 25, scaleDuration(20))
                val amplitudes = intArrayOf(0, scaleAmplitude(75), 0, scaleAmplitude(140), 0, scaleAmplitude(220))
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, scaleDuration(12), 20, scaleDuration(18)), -1)
            }
        }
    }

    /**
     * Firm, damped thud when the card stack collapses flush back into the slot
     */
    fun stackCollapse() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, scaleDuration(14), 26, scaleDuration(30))
                val amplitudes = intArrayOf(0, scaleAmplitude(110), 0, scaleAmplitude(190))
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(35))
            }
        }
    }

    /**
     * Snappy "drawn from wallet" sensation when picking an individual card
     */
    fun cardDraw() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, scaleDuration(10), 20, scaleDuration(22))
                val amplitudes = intArrayOf(0, scaleAmplitude(95), 0, scaleAmplitude(210))
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(25))
            }
        }
    }

    fun cardSelect() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = scaleAmplitude(115)
                vibrator.vibrate(VibrationEffect.createOneShot(scaleDuration(18), amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(20))
            }
        }
    }

    fun walletOpen() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, scaleDuration(15), 40, scaleDuration(35))
                val amplitudes = intArrayOf(0, scaleAmplitude(80), 0, scaleAmplitude(180))
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(40))
            }
        }
    }

    fun walletClose() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = scaleAmplitude(180)
                vibrator.vibrate(VibrationEffect.createOneShot(scaleDuration(35), amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(50))
            }
        }
    }

    fun cardFlip() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = scaleAmplitude(120)
                vibrator.vibrate(VibrationEffect.createOneShot(scaleDuration(18), amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(scaleDuration(18))
            }
        }
    }

    fun success() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, scaleDuration(20), 40, scaleDuration(25))
                val amplitudes = intArrayOf(0, scaleAmplitude(140), 0, scaleAmplitude(220))
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, scaleDuration(30), 60, scaleDuration(30)), -1)
            }
        }
    }

    fun error() {
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, scaleDuration(40), 50, scaleDuration(40), 50, scaleDuration(40))
                val amplitudes = intArrayOf(0, scaleAmplitude(220), 0, scaleAmplitude(220), 0, scaleAmplitude(220))
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, scaleDuration(40), 50, scaleDuration(40)), -1)
            }
        }
    }
}
