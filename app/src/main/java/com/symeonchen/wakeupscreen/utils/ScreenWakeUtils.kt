package com.symeonchen.wakeupscreen.utils

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.symeonchen.wakeupscreen.services.screen.ScreenOffController
import com.symeonchen.wakeupscreen.services.screen.PreciseScreenOnManager

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("DEPRECATION")
object ScreenWakeUtils {

    private const val MODULE = "Wake"
    private const val TAG_WAKE = "symeonchen:wakeupscreen"

    /**
     * How long the legacy path asks for. Only the wake-up matters — the lock is
     * released on the next line — so this is just a ceiling in case the release
     * somehow does not run.
     */
    private const val LEGACY_WAKE_TIMEOUT_MS = 2000L

    /**
     * Lights the display for a new notification or a reminder inheriting its duration.
     *
     * Which of the two paths runs is decided entirely by the precise screen-on
     * switch, and the switch defaults to off, so the behaviour an existing
     * install sees after an update is unchanged.
     */
    fun wakeUpScreen(context: Context, pm: PowerManager?) {
        pm ?: return
        if (!DataInjection.preciseScreenOnSwitch) {
            legacyWakeUpScreen(pm)
            return
        }
        PreciseScreenOnManager.startWindow(context, DataInjection.preciseScreenOnSecond)
    }

    /** Custom reminder windows are independent of the initial-notification switch. */
    fun wakeUpScreenForReminder(context: Context, pm: PowerManager?) {
        pm ?: return
        when (val wake = ReminderDurationPolicy.resolve(
            DataInjection.repeatReminderScreenOnSeconds,
            accessibilityRequired = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
            accessibilityAvailable = ScreenOffController.isAccessibilityAvailable(),
        )) {
            ReminderDurationPolicy.Wake.Inherit -> wakeUpScreen(context, pm)
            ReminderDurationPolicy.Wake.SystemTimeout -> {
                ScLog.w(MODULE, "reminder duration unavailable: accessibility is not connected; using system timeout")
                legacyWakeUpScreen(pm)
            }
            is ReminderDurationPolicy.Wake.Precise -> PreciseScreenOnManager.startWindow(context, wake.seconds)
        }
    }

    /**
     * The original behaviour: nudge the display awake and hand it straight back
     * to the system, which keeps it on for whatever the system timeout is. The
     * duration configured in settings is deliberately not consulted here — the
     * lock is released immediately, so it never had any effect.
     */
    private fun legacyWakeUpScreen(pm: PowerManager) {
        try {
            val wakeLock = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                TAG_WAKE
            )
            wakeLock.acquire(LEGACY_WAKE_TIMEOUT_MS)
            wakeLock.release()
            ScLog.i(MODULE, "screen woken (system timeout)")
        } catch (e: Exception) {
            ScLog.w(MODULE, "failed to wake screen", e)
        }
    }
}
