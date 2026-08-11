package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.ScConstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenOnWindowCalculatorTest {

    private val tick = ScConstant.PRECISE_SCREEN_ON_TICK_INTERVAL_MS
    private val min = ScConstant.MIN_PRECISE_SCREEN_ON_SECOND
    private val max = ScConstant.MAX_PRECISE_SCREEN_ON_SECOND

    @Test
    fun clampKeepsValuesInsideRange() {
        assertEquals(5L, ScreenOnWindowCalculator.clampSeconds(5L))
        assertEquals(min, ScreenOnWindowCalculator.clampSeconds(min))
        assertEquals(max, ScreenOnWindowCalculator.clampSeconds(max))
    }

    @Test
    fun clampRejectsValuesOutsideRange() {
        assertEquals(min, ScreenOnWindowCalculator.clampSeconds(0L))
        assertEquals(min, ScreenOnWindowCalculator.clampSeconds(-30L))
        assertEquals(max, ScreenOnWindowCalculator.clampSeconds(max + 1))
        // A leftover value from the old, unvalidated setting.
        assertEquals(max, ScreenOnWindowCalculator.clampSeconds(86_400L))
    }

    @Test
    fun deadlineIsTheStartPlusTheClampedDuration() {
        assertEquals(11_000L, ScreenOnWindowCalculator.deadlineOf(1_000L, 10L))
        // Out-of-range input is clamped before it becomes a deadline.
        assertEquals(1_000L + max * 1000L, ScreenOnWindowCalculator.deadlineOf(1_000L, max * 10))
    }

    @Test
    fun remainingNeverGoesNegative() {
        assertEquals(400L, ScreenOnWindowCalculator.remainingMs(1_000L, 600L))
        assertEquals(0L, ScreenOnWindowCalculator.remainingMs(1_000L, 1_000L))
        assertEquals(0L, ScreenOnWindowCalculator.remainingMs(1_000L, 9_000L))
    }

    @Test
    fun expiryIsInclusiveOfTheDeadline() {
        assertFalse(ScreenOnWindowCalculator.isExpired(1_000L, 999L))
        assertTrue(ScreenOnWindowCalculator.isExpired(1_000L, 1_000L))
        assertTrue(ScreenOnWindowCalculator.isExpired(1_000L, 1_001L))
    }

    @Test
    fun tickIsCappedWhileFarFromTheDeadline() {
        val deadline = 100_000L
        assertEquals(tick, ScreenOnWindowCalculator.nextTickDelayMs(deadline, 0L))
        assertEquals(tick, ScreenOnWindowCalculator.nextTickDelayMs(deadline, deadline - tick))
    }

    @Test
    fun lastTickLandsExactlyOnTheDeadline() {
        val deadline = 10_000L
        // 300ms left is less than a full tick, so the final sleep is shortened
        // rather than overshooting the window by up to a whole interval.
        assertEquals(300L, ScreenOnWindowCalculator.nextTickDelayMs(deadline, deadline - 300L))
        assertEquals(1L, ScreenOnWindowCalculator.nextTickDelayMs(deadline, deadline - 1L))
        assertEquals(0L, ScreenOnWindowCalculator.nextTickDelayMs(deadline, deadline))
    }

    /**
     * The whole point of an absolute deadline: ticks that arrive late eat into
     * the following sleep instead of pushing the end of the window out.
     */
    @Test
    fun lateTicksDoNotStretchTheWindow() {
        val start = 0L
        val deadline = ScreenOnWindowCalculator.deadlineOf(start, 5L)

        var now = start
        var ticks = 0
        while (!ScreenOnWindowCalculator.isExpired(deadline, now)) {
            // Every tick fires 350ms later than it was asked to.
            now += ScreenOnWindowCalculator.nextTickDelayMs(deadline, now) + 350L
            ticks++
            assertTrue("timer ran away", ticks < 20)
        }

        // Ends within one late tick of the target, never before it.
        assertTrue(now >= deadline)
        assertTrue("overshot by ${now - deadline}ms", now - deadline <= 350L)
    }

    @Test
    fun wakeLockOutlivesASingleTick() {
        assertTrue(ScreenOnWindowCalculator.wakeLockTimeoutMs() > tick)
    }
}
