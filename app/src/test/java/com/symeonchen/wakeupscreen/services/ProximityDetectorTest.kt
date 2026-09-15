package com.symeonchen.wakeupscreen.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximityDetectorTest {

    @Test
    fun `zero is near for a binary sensor`() {
        assertEquals(ProximityReading.NEAR, ProximityDetector.classify(0f, 5f))
    }

    @Test
    fun `non-zero values below maximum range are also near`() {
        assertEquals(ProximityReading.NEAR, ProximityDetector.classify(1f, 5f))
        assertEquals(ProximityReading.NEAR, ProximityDetector.classify(4.9f, 5f))
    }

    @Test
    fun `maximum range and values above it are far`() {
        assertEquals(ProximityReading.FAR, ProximityDetector.classify(5f, 5f))
        assertEquals(ProximityReading.FAR, ProximityDetector.classify(8f, 5f))
    }

    @Test
    fun `invalid readings stay unknown`() {
        assertEquals(ProximityReading.UNKNOWN, ProximityDetector.classify(Float.NaN, 5f))
        assertEquals(ProximityReading.UNKNOWN, ProximityDetector.classify(0f, 0f))
        assertEquals(ProximityReading.UNKNOWN, ProximityDetector.classify(-1f, 5f))
    }

    @Test
    fun `enabled pocket mode fails safe until the first reading`() {
        assertTrue(PocketModePolicy.shouldBlock(true, ProximityReading.UNKNOWN))
        assertTrue(PocketModePolicy.shouldBlock(true, ProximityReading.NEAR))
        assertFalse(PocketModePolicy.shouldBlock(true, ProximityReading.FAR))
    }

    @Test
    fun `a missing sensor and a disabled setting do not block`() {
        assertFalse(PocketModePolicy.shouldBlock(true, ProximityReading.UNAVAILABLE))
        ProximityReading.values().forEach { reading ->
            assertFalse(PocketModePolicy.shouldBlock(false, reading))
        }
    }
}
