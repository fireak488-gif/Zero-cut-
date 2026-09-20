package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundHapticManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var vibrationEnabled: Boolean = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {}
    }

    fun playHeadshotFeedback() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 90)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrate(80, VibrationEffect.DEFAULT_AMPLITUDE)
        }
    }

    fun playHitFeedback() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                } catch (_: Exception) {}
            }
        }
        if (vibrationEnabled) {
            vibrate(30, 100)
        }
    }

    fun playMissFeedback() {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
                } catch (_: Exception) {}
            }
        }
    }

    fun playCountdownBeep(isFinal: Boolean = false) {
        if (soundEnabled) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val tone = if (isFinal) ToneGenerator.TONE_DTMF_A else ToneGenerator.TONE_PROP_BEEP
                    val duration = if (isFinal) 250 else 100
                    toneGenerator?.startTone(tone, duration)
                } catch (_: Exception) {}
            }
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
