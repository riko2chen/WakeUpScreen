package com.symeonchen.wakeupscreen.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximityDetectorTest {

    private val binaryMaxRange = 5.0f

    @Test
    fun `binary sensors treat zero as near and max range as far`() {
        assertTrue(ProximityDetector.isNear(0f, binaryMaxRange))
        assertFalse(ProximityDetector.isNear(binaryMaxRange, binaryMaxRange))
    }

    @Test
    fun `a short distance below max range is near`() {
        // The previous listener required exactly 0, so a sensor that reports
        // centimetres never counted as covered.
        assertTrue(ProximityDetector.isNear(0.5f, binaryMaxRange))
        assertTrue(ProximityDetector.isNear(1.0f, 8.0f))
        assertFalse(ProximityDetector.isNear(8.0f, 8.0f))
    }

    @Test
    fun `the first far reading is trusted immediately`() {
        val next = ProximityDetector.onReading(
            current = ProximityVerdict.none(),
            distance = binaryMaxRange,
            maxRange = binaryMaxRange,
            nowElapsedMs = 0L,
        )
        assertTrue(next.hasReading)
        assertFalse(next.covered)
        assertNull(next.pendingUncoverAtElapsedMs)
    }

    @Test
    fun `a near reading covers immediately`() {
        val next = ProximityDetector.onReading(
            current = ProximityVerdict.none(),
            distance = 0f,
            maxRange = binaryMaxRange,
            nowElapsedMs = 0L,
        )
        assertTrue(next.covered)
        assertTrue(next.hasReading)
        assertNull(next.pendingUncoverAtElapsedMs)
    }

    @Test
    fun `a far spike while covered does not uncover until the hold elapses`() {
        val covered = ProximityVerdict(covered = true, hasReading = true)
        val spiked = ProximityDetector.onReading(
            current = covered,
            distance = binaryMaxRange,
            maxRange = binaryMaxRange,
            nowElapsedMs = 10_000L,
        )
        assertTrue(spiked.covered)
        assertEquals(10_000L + ProximityDetector.UNCOVER_HOLD_MS, spiked.pendingUncoverAtElapsedMs)

        val stillCovered = ProximityDetector.onHoldElapsed(
            spiked,
            10_000L + ProximityDetector.UNCOVER_HOLD_MS - 1,
        )
        assertTrue(stillCovered.covered)
        assertNotNull(stillCovered.pendingUncoverAtElapsedMs)

        val uncovered = ProximityDetector.onHoldElapsed(
            spiked,
            10_000L + ProximityDetector.UNCOVER_HOLD_MS,
        )
        assertFalse(uncovered.covered)
        assertNull(uncovered.pendingUncoverAtElapsedMs)
    }

    @Test
    fun `a near reading during the hold cancels uncovering`() {
        val covered = ProximityVerdict(covered = true, hasReading = true)
        val spiked = ProximityDetector.onReading(
            current = covered,
            distance = binaryMaxRange,
            maxRange = binaryMaxRange,
            nowElapsedMs = 0L,
        )
        val nearAgain = ProximityDetector.onReading(
            current = spiked,
            distance = 0f,
            maxRange = binaryMaxRange,
            nowElapsedMs = 400L,
        )
        assertTrue(nearAgain.covered)
        assertNull(nearAgain.pendingUncoverAtElapsedMs)
    }
}
