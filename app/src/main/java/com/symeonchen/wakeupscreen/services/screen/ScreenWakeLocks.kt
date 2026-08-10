package com.symeonchen.wakeupscreen.services.screen

import android.os.PowerManager
import com.symeonchen.wakeupscreen.utils.ScLog
import com.symeonchen.wakeupscreen.utils.ScreenOnWindowCalculator

/**
 * The wake locks a precise screen-on window runs on.
 *
 * Every lock is acquired with a timeout. That is deliberate: if the process is
 * killed between two refreshes the locks expire by themselves instead of
 * pinning the display on until the next reboot.
 */
@Suppress("DEPRECATION")
class ScreenWakeLocks(powerManager: PowerManager) {

    private companion object {
        const val MODULE = "WakeLock"

        /**
         * `SCREEN_BRIGHT_WAKE_LOCK` lights the display and `ACQUIRE_CAUSES_WAKEUP`
         * turns it on from a fully asleep state.
         *
         * `ON_AFTER_RELEASE` is deliberately not set, even though the reference
         * implementation uses it. It pokes the user-activity timer on release,
         * handing the display a fresh full system timeout at the exact moment
         * the window is trying to end — and the swap in [refresh] already means
         * the lock is never actually let go mid-window.
         */
        const val SCREEN_FLAGS = PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                PowerManager.ACQUIRE_CAUSES_WAKEUP

        /** Enough to outlive one screen-off attempt. */
        const val CPU_HOLD_MS = 3000L
    }

    private val screenLock: PowerManager.WakeLock =
        powerManager.newWakeLock(SCREEN_FLAGS, "symeonchen:precise_screen")
            .apply { setReferenceCounted(false) }

    /**
     * Held only across the instant the main lock is released and re-acquired.
     * Without it the display briefly has no screen lock at all and can start to
     * dim on some builds.
     */
    private val swapLock: PowerManager.WakeLock =
        powerManager.newWakeLock(SCREEN_FLAGS, "symeonchen:precise_screen_swap")
            .apply { setReferenceCounted(false) }

    private val cpuLock: PowerManager.WakeLock =
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "symeonchen:precise_cpu")
            .apply { setReferenceCounted(false) }

    /**
     * Renews the screen lock without ever letting go of the display.
     *
     * A plain `release()` + `acquire()` leaves a gap the compositor can notice;
     * handing the display to a second lock for the duration of the swap does not.
     */
    fun refresh() {
        val timeout = ScreenOnWindowCalculator.wakeLockTimeoutMs()
        try {
            if (!screenLock.isHeld) {
                screenLock.acquire(timeout)
                ScLog.d(MODULE, "screen lock acquired, timeout=${timeout}ms")
                return
            }
            swapLock.acquire(timeout)
            screenLock.release()
            screenLock.acquire(timeout)
            swapLock.release()
            ScLog.d(MODULE, "screen lock refreshed, timeout=${timeout}ms")
        } catch (e: Exception) {
            ScLog.w(MODULE, "failed to refresh screen lock", e)
        }
    }

    /** Keeps the CPU awake long enough for a screen-off request to be delivered. */
    fun holdCpu() {
        try {
            cpuLock.acquire(CPU_HOLD_MS)
        } catch (e: Exception) {
            ScLog.w(MODULE, "failed to acquire cpu lock", e)
        }
    }

    /** Lets the display go. Safe to call when nothing is held. */
    fun releaseScreen() {
        releaseQuietly(swapLock, "swap")
        releaseQuietly(screenLock, "screen")
    }

    fun releaseAll() {
        releaseScreen()
        releaseQuietly(cpuLock, "cpu")
    }

    private fun releaseQuietly(lock: PowerManager.WakeLock, name: String) {
        try {
            if (lock.isHeld) {
                lock.release()
                ScLog.d(MODULE, "$name lock released")
            }
        } catch (e: Exception) {
            ScLog.w(MODULE, "failed to release $name lock", e)
        }
    }
}
