package com.symeonchen.wakeupscreen.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProximityDetectorTest {
    @Test
    fun `legacy threshold blocks zero and allows every positive distance`() {
        assertEquals(ProximityReading.NEAR, ProximityDetector.classify(0f, 5f))
        for (distance in listOf(0.1f, 1f, 4.9f, 5f, 8f)) {
            assertEquals(ProximityReading.FAR, ProximityDetector.classify(distance, 5f))
        }
    }

    @Test
    fun `legacy detection does not depend on maximum range metadata`() {
        for (range in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertEquals(ProximityReading.NEAR, ProximityDetector.classify(0f, range))
            assertEquals(ProximityReading.FAR, ProximityDetector.classify(1f, range))
        }
    }

    @Test
    fun `experimental detection recognizes non-zero near readings`() {
        for (distance in listOf(0f, 0.1f, 1f, 4.9f)) {
            assertEquals(ProximityReading.NEAR, ProximityDetector.classify(distance, 5f, true))
        }
        for (distance in listOf(5f, 8f)) {
            assertEquals(ProximityReading.FAR, ProximityDetector.classify(distance, 5f, true))
        }
    }

    @Test
    fun `invalid distances allow wake in both modes`() {
        for (experimental in listOf(false, true)) {
            for (distance in listOf(Float.NaN, -1f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
                val reading = ProximityDetector.classify(distance, 5f, experimental)
                assertEquals(ProximityReading.UNKNOWN, reading)
                assertFalse(PocketModePolicy.shouldBlock(true, reading))
            }
        }
    }

    @Test
    fun `experimental detection allows wake when maximum range is invalid`() {
        for (range in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            for (distance in listOf(0f, 1f)) {
                val reading = ProximityDetector.classify(distance, range, true)
                assertEquals(ProximityReading.UNKNOWN, reading)
                assertFalse(PocketModePolicy.shouldBlock(true, reading))
            }
        }
    }

    @Test
    fun `one sample can change policy without another sensor callback`() {
        val readings = ProximityDetector.readings(1f, 5f)
        assertFalse(PocketModePolicy.shouldBlock(true, readings.selected(false)))
        assertTrue(PocketModePolicy.shouldBlock(true, readings.selected(true)))
        assertFalse(PocketModePolicy.shouldBlock(true, readings.selected(false)))
    }

    @Test
    fun `first reading missing or sensor unavailable allows wake`() {
        for (experimental in listOf(false, true)) {
            assertFalse(PocketModePolicy.shouldBlock(true, ProximityReadings().selected(experimental)))
        }
        assertFalse(PocketModePolicy.shouldBlock(true, ProximityReading.UNAVAILABLE))
        assertFalse(PocketModePolicy.shouldBlock(true, ProximityReading.FAR))
        assertTrue(PocketModePolicy.shouldBlock(true, ProximityReading.NEAR))
    }

    @Test
    fun `disabled pocket mode allows all readings`() {
        ProximityReading.values().forEach { reading ->
            assertFalse(PocketModePolicy.shouldBlock(false, reading))
        }
    }
}
