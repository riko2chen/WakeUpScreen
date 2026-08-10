package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.ScConstant

/**
 * The arithmetic behind a precise screen-on window, kept free of Android types
 * so it can be unit tested.
 *
 * The window is driven by an absolute deadline rather than by counting ticks.
 * A tick that arrives late — and under Doze they routinely do — therefore
 * shortens the next sleep instead of pushing the end of the window back, so
 * scheduling jitter cannot accumulate over a long window.
 */
object ScreenOnWindowCalculator {

    /** Restricts a user-entered duration to the range the engine supports. */
    fun clampSeconds(seconds: Long): Long {
        return seconds.coerceIn(
            ScConstant.MIN_PRECISE_SCREEN_ON_SECOND,
            ScConstant.MAX_PRECISE_SCREEN_ON_SECOND,
        )
    }

    /** Absolute end of a window that starts at [nowUptimeMs], on the monotonic clock. */
    fun deadlineOf(nowUptimeMs: Long, seconds: Long): Long {
        return nowUptimeMs + clampSeconds(seconds) * 1000L
    }

    /** Milliseconds left in the window; never negative. */
    fun remainingMs(deadlineUptimeMs: Long, nowUptimeMs: Long): Long {
        return (deadlineUptimeMs - nowUptimeMs).coerceAtLeast(0L)
    }

    fun isExpired(deadlineUptimeMs: Long, nowUptimeMs: Long): Boolean {
        return nowUptimeMs >= deadlineUptimeMs
    }

    /**
     * How long to sleep before the next wake-lock refresh.
     *
     * Capped at [ScConstant.PRECISE_SCREEN_ON_TICK_INTERVAL_MS] so the wake lock
     * is renewed often enough to survive a system-imposed timeout, and shortened
     * to exactly the remaining time near the end so the final tick lands on the
     * deadline instead of up to two seconds past it.
     */
    fun nextTickDelayMs(deadlineUptimeMs: Long, nowUptimeMs: Long): Long {
        val remaining = remainingMs(deadlineUptimeMs, nowUptimeMs)
        return remaining.coerceAtMost(ScConstant.PRECISE_SCREEN_ON_TICK_INTERVAL_MS)
    }

    /**
     * Timeout handed to `WakeLock.acquire`. It outlives one tick by a margin so
     * a refresh that runs slightly late does not let the display drop, while
     * still guaranteeing the lock expires on its own if the process is killed
     * mid-window.
     */
    fun wakeLockTimeoutMs(): Long {
        return ScConstant.PRECISE_SCREEN_ON_TICK_INTERVAL_MS * 3
    }
}
