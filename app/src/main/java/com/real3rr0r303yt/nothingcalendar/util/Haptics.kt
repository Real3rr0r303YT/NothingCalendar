package com.real3rr0r303yt.nothingcalendar.util

import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Small vocabulary of haptic patterns used throughout the app.
 *
 * Most patterns go through [View.performHapticFeedback], which respects the system
 * "touch feedback" setting. [confirm] uses a richer composed vibration on devices
 * whose motor supports primitives (e.g. Nothing Phones with their X-axis motor).
 */
class Haptics(private val view: View) {

    var enabled: Boolean = true

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            view.context.getSystemService(Vibrator::class.java)
        }

    /** Light tick: paging between months/days, toggles. */
    fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK)

    /** Standard tap on a button or a day. */
    fun click() = perform(HapticFeedbackConstants.VIRTUAL_KEY)

    /** Long-press on a day to create an event. */
    fun longPress() = perform(HapticFeedbackConstants.LONG_PRESS)

    /** Something was refused (validation error, destructive action). */
    fun reject() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT
        else HapticFeedbackConstants.LONG_PRESS,
    )

    /** Something was saved successfully. */
    fun confirm() {
        if (!enabled) return
        val v = vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && v != null &&
            v.areAllPrimitivesSupported(
                VibrationEffect.Composition.PRIMITIVE_CLICK,
                VibrationEffect.Composition.PRIMITIVE_TICK,
            )
        ) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.9f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f, 70)
                .compose()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                v.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            } else {
                v.vibrate(effect)
            }
        } else {
            perform(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.VIRTUAL_KEY,
            )
        }
    }

    private fun perform(constant: Int) {
        if (enabled) view.performHapticFeedback(constant)
    }
}

val LocalHaptics = staticCompositionLocalOf<Haptics> { error("Haptics not provided") }
