package com.symeonchen.wakeupscreen.services.screen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.Display
import androidx.core.content.ContextCompat
import com.symeonchen.wakeupscreen.data.ScreenOffMethod
import com.symeonchen.wakeupscreen.utils.ScLog
import com.symeonchen.wakeupscreen.utils.ScreenOnWindowCalculator

/**
 * Keeps the display on for an exact number of seconds and then turns it off.
 *
 * Only reached when the precise screen-on switch is enabled; with the switch off
 * nothing in this file runs and the app wakes the screen the way it always did.
 *
 * The window is anchored to an absolute deadline on [SystemClock.uptimeMillis],
 * a clock that neither the user nor the network can move, and the wake lock is
 * renewed on a short tick until that deadline passes. Handler jitter therefore
 * shows up as a slightly late final tick, not as an ever-growing window.
 */
object PreciseScreenOnManager {

    private const val MODULE = "PreciseWake"

    /** How long after the wake request the display is checked, for diagnostics only. */
    private const val WAKE_PROBE_DELAY_MS = 500L

    private val handler = Handler(Looper.getMainLooper())

    private var appContext: Context? = null
    private var locks: ScreenWakeLocks? = null

    private var running = false
    private var deadlineUptimeMs = 0L
    private var startUptimeMs = 0L
    private var targetSeconds = 0L

    private val tick = Runnable { onTick() }

    /**
     * Cancels the window as soon as the situation it was started for is over:
     * the display went off by other means, or the user unlocked the device and
     * is plainly using it.
     *
     * `ACTION_USER_PRESENT` is the signal for the unlock rather than polling
     * `KeyguardManager.isKeyguardLocked`. That flag was tried first and turned
     * out to report "unlocked" for a second or two right after a wake-lock wake
     * on One UI, which cut every window short on a locked phone.
     */
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> stop(EndReason.SCREEN_OFF)
                Intent.ACTION_USER_PRESENT -> stop(EndReason.UNLOCKED)
            }
        }
    }
    private var receiverRegistered = false

    private enum class EndReason {
        /** The configured duration elapsed; the display is turned off from here. */
        EXPIRED,

        /** Something else switched the display off first. */
        SCREEN_OFF,

        /** The user unlocked the device — leave the screen alone. */
        UNLOCKED,

        /** The feature was switched off while a window was running. */
        SWITCH_OFF,
    }

    /**
     * Wakes the display and starts — or restarts — the window.
     *
     * A second notification arriving mid-window pushes the deadline to a full
     * duration from now rather than adding to what is left, so two messages in
     * quick succession do not double the time the screen stays on.
     */
    @Synchronized
    fun startWindow(context: Context, seconds: Long) {
        val ctx = context.applicationContext
        val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager == null) {
            ScLog.w(MODULE, "no PowerManager, cannot start window")
            return
        }

        appContext = ctx
        val wakeLocks = locks ?: ScreenWakeLocks(powerManager).also { locks = it }

        val now = SystemClock.uptimeMillis()
        val clamped = ScreenOnWindowCalculator.clampSeconds(seconds)
        val extending = running

        deadlineUptimeMs = ScreenOnWindowCalculator.deadlineOf(now, clamped)
        targetSeconds = clamped

        wakeLocks.holdCpu()
        wakeLocks.refresh()

        if (extending) {
            ScLog.i(MODULE, "window extended to ${clamped}s, ${elapsedMs(now)}ms into the previous one")
            // Rebased so the drift reported at the end measures the window that
            // is actually running, not the one this call replaced.
            startUptimeMs = now
        } else {
            startUptimeMs = now
            running = true
            registerReceiver(ctx)
            ScLog.i(
                MODULE,
                "window started, target=${clamped}s deadline=$deadlineUptimeMs " +
                        "keyguardLocked=${isKeyguardLocked(ctx)} " +
                        "method=${ScreenOffMethod.defaultForThisDevice()}"
            )
            handler.postDelayed({ probeDisplay(ctx) }, WAKE_PROBE_DELAY_MS)
        }

        scheduleTick(now)
    }

    /** Called when the user turns the feature off, so an in-flight window stops immediately. */
    fun onSwitchDisabled() {
        stop(EndReason.SWITCH_OFF)
    }

    @Synchronized
    private fun onTick() {
        if (!running) {
            return
        }
        val ctx = appContext ?: run {
            stopLocked(EndReason.SCREEN_OFF)
            return
        }

        val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager?.isInteractive == false) {
            // The display went off without a broadcast reaching us. Nothing left
            // to hold on to, and certainly nothing to turn off.
            ScLog.i(MODULE, "display already off at tick")
            stopLocked(EndReason.SCREEN_OFF)
            return
        }

        val now = SystemClock.uptimeMillis()
        if (!ScreenOnWindowCalculator.isExpired(deadlineUptimeMs, now)) {
            locks?.refresh()
            scheduleTick(now)
            ScLog.d(
                MODULE,
                "tick, ${ScreenOnWindowCalculator.remainingMs(deadlineUptimeMs, now)}ms remaining"
            )
            return
        }

        val elapsed = elapsedMs(now)
        ScLog.i(
            MODULE,
            "window finished, elapsed=${elapsed}ms target=${targetSeconds * 1000}ms " +
                    "drift=${elapsed - targetSeconds * 1000}ms"
        )
        stopLocked(EndReason.EXPIRED)
    }

    @Synchronized
    private fun stop(reason: EndReason) {
        stopLocked(reason)
    }

    private fun stopLocked(reason: EndReason) {
        if (!running) {
            return
        }
        running = false
        handler.removeCallbacks(tick)
        unregisterReceiver()

        val ctx = appContext
        if (reason == EndReason.EXPIRED && ctx != null) {
            // The display can only go dark once nothing is holding it bright, so
            // the release has to come before the request, not after it.
            locks?.releaseScreen()
            locks?.holdCpu()
            ScreenOffController.requestScreenOff(ctx)
        } else {
            ScLog.i(MODULE, "window ended early: $reason")
            locks?.releaseAll()
        }
    }

    private fun scheduleTick(nowUptimeMs: Long) {
        handler.removeCallbacks(tick)
        handler.postDelayed(
            tick,
            ScreenOnWindowCalculator.nextTickDelayMs(deadlineUptimeMs, nowUptimeMs)
        )
    }

    private fun elapsedMs(nowUptimeMs: Long) = nowUptimeMs - startUptimeMs

    /** Logged when a window starts; not used as a cancellation signal. See [screenStateReceiver]. */
    private fun isKeyguardLocked(context: Context): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguard?.isKeyguardLocked == true
    }

    /**
     * Reports whether the wake lock actually lit the panel. Purely diagnostic —
     * on the vendor builds that refuse background wake-ups this is the line in
     * logcat that says so.
     */
    private fun probeDisplay(context: Context) {
        val displayManager =
            context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return
        val state = displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.state
        ScLog.i(
            MODULE,
            "display state ${WAKE_PROBE_DELAY_MS}ms after wake: " +
                    if (state == Display.STATE_ON) "ON" else "NOT ON ($state)"
        )
    }

    private fun registerReceiver(context: Context) {
        if (receiverRegistered) {
            return
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        try {
            // Both actions are protected system broadcasts, so nothing outside
            // the platform could deliver them anyway.
            ContextCompat.registerReceiver(
                context, screenStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        } catch (e: Exception) {
            ScLog.w(MODULE, "failed to register screen state receiver", e)
        }
    }

    private fun unregisterReceiver() {
        if (!receiverRegistered) {
            return
        }
        try {
            appContext?.unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            ScLog.w(MODULE, "failed to unregister screen state receiver", e)
        }
        receiverRegistered = false
    }
}
