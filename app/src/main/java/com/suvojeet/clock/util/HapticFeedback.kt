package com.suvojeet.clock.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * Utility object for providing haptic feedback throughout the app.
 * Provides consistent haptic patterns for different user interactions.
 */
object HapticFeedback {
    
    /**
     * Performs a light tap haptic feedback, suitable for button presses.
     */
    fun performClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * Performs a heavier haptic feedback, suitable for confirmations or important actions.
     */
    fun performConfirm(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    /**
     * Performs a rejection haptic feedback, suitable for errors or rejected inputs.
     */
    fun performReject(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    /**
     * Performs haptic feedback for toggle switches.
     */
    fun performToggle(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
    
    /**
     * Performs a custom vibration pattern.
     * @param context The context
     * @param durationMs The duration in milliseconds
     */
    fun vibrate(context: Context, durationMs: Long = 50) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }
}

/**
 * Composable function to get the current View for haptic feedback.
 */
@Composable
fun rememberHapticFeedback(): androidx.compose.ui.hapticfeedback.HapticFeedback {
    return LocalHapticFeedback.current
}

/**
 * Extension function to get the view from a Composable context for haptic operations.
 */
@Composable
fun rememberView(): View {
    return LocalView.current
}
