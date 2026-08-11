package com.musyfy.nativeapp.core.ui.haptics

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Musyfy Haptic Design System.
 * Standardizes physical tactile feedback across the app with consistent semantic levels:
 * - Light: Action taps, navigation buttons, "+" creation buttons
 * - Confirmation: Single medium confirmation haptic for high-value action completions (such as successful "Add to Library")
 */
object MusyfyHaptics {
    /** Subtle light tactile haptic for action taps and creation buttons. */
    fun performLight(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    /** Subtle light tactile haptic using Compose HapticFeedback interface. */
    fun performLight(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Single confirmation haptic for high-value action completions (e.g. Add to Library success). */
    fun performConfirmation(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /** Single confirmation haptic using Compose HapticFeedback interface. */
    fun performConfirmation(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}
