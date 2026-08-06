package com.symeonchen.wakeupscreen.utils

import android.os.PowerManager

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("DEPRECATION")
object ScreenWakeUtils {

    private const val TAG_WAKE = "symeonchen:wakeupscreen"

    /**
     * Turns the display on for the duration configured in settings. Shared by
     * the notification path and the repeat reminder so both light the screen
     * for the same time and with the same wake lock.
     */
    fun wakeUpScreen(pm: PowerManager?) {
        pm ?: return
        try {
            val wakeLock = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                TAG_WAKE
            )
            wakeLock.acquire(DataInjection.milliSecondOfWakeUpScreen)
            wakeLock.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
