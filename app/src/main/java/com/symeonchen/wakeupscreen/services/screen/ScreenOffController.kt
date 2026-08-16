package com.symeonchen.wakeupscreen.services.screen

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.data.ScreenOffMethod
import com.symeonchen.wakeupscreen.pages.ScreenOffActivity
import com.symeonchen.wakeupscreen.services.ScLockScreenAccessibilityService
import com.symeonchen.wakeupscreen.utils.ScLog

/**
 * Turns the display off at the end of a precise screen-on window, once it is
 * clear that doing so will not interrupt something the user is in the middle of.
 */
object ScreenOffController {

    private const val MODULE = "ScreenOff"

    @Volatile
    private var lastRequestUptimeMs = 0L

    fun requestScreenOff(context: Context) {
        val appContext = context.applicationContext

        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager?.isInteractive == false) {
            ScLog.i(MODULE, "skipped: display is already off")
            return
        }

        if (isCallOngoing(appContext)) {
            // Blanking the screen mid-call would hide the in-call controls.
            ScLog.i(MODULE, "skipped: a call is in progress")
            return
        }

        if (isKeyguardGone(appContext)) {
            // No keyguard showing means the user's own session is on screen:
            // either they unlocked and the USER_PRESENT broadcast never reached
            // the window (it happens), or the lock screen is set to "None" and
            // that broadcast was never coming. Locking now would slam the door
            // on someone mid-use, which is worse than a screen that stays on.
            ScLog.i(MODULE, "skipped: keyguard no longer showing")
            return
        }

        val now = SystemClock.uptimeMillis()
        if (now - lastRequestUptimeMs < ScConstant.SCREEN_OFF_REQUEST_COOLDOWN_MS) {
            // Two windows ending back to back would otherwise stack two lock
            // attempts on top of each other.
            ScLog.i(MODULE, "skipped: another request ${now - lastRequestUptimeMs}ms ago")
            return
        }
        lastRequestUptimeMs = now

        // Not a user setting: below Android 9 the accessibility action does not
        // exist, and from Android 9 up the permission-free fallback is ignored
        // by the platform. There is only ever one sensible answer per device.
        when (val method = ScreenOffMethod.defaultForThisDevice()) {
            ScreenOffMethod.ACCESSIBILITY -> lockWithAccessibility(appContext)
            ScreenOffMethod.ENFORCE -> {
                ScLog.i(MODULE, "turning display off via $method")
                ScreenOffActivity.start(appContext)
            }
        }
    }

    /**
     * Whether [ScreenOffMethod.ACCESSIBILITY] can be used right now: the API
     * exists and the user has actually granted and connected the service.
     */
    fun isAccessibilityAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                ScLockScreenAccessibilityService.isConnected()
    }

    private fun lockWithAccessibility(context: Context) {
        if (!isAccessibilityAvailable()) {
            // The user can revoke accessibility access at any time, and a window
            // that simply never ends is worse than a less exact one.
            ScLog.w(MODULE, "accessibility service unavailable, falling back to ENFORCE")
            ScreenOffActivity.start(context)
            return
        }
        ScLog.i(MODULE, "turning display off via ACCESSIBILITY")
        if (!ScLockScreenAccessibilityService.lockScreen()) {
            ScLog.w(MODULE, "accessibility lock rejected, falling back to ENFORCE")
            ScreenOffActivity.start(context)
        }
    }

    /**
     * True only when the keyguard is positively known to be dismissed.
     *
     * An unreadable KeyguardManager counts as "still locked" so the window
     * keeps its promise to end; the check only ever *skips* the lock, never
     * forces one. One UI's habit of reporting "unlocked" for a moment right
     * after a wake-lock wake is why the minimum window is 5 seconds — every
     * expiry lands after that flaky period has passed.
     */
    private fun isKeyguardGone(context: Context): Boolean {
        val keyguardManager =
            context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                ?: return false
        return !keyguardManager.isKeyguardLocked
    }

    /**
     * Uses the audio mode rather than telephony state so the check costs no
     * permission, and still covers VoIP calls that never touch the dialler.
     */
    private fun isCallOngoing(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return false
        return audioManager.mode == AudioManager.MODE_IN_CALL ||
                audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
    }
}
