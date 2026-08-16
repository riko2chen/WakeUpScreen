package com.symeonchen.wakeupscreen.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceDownDetectorTest {

    @Test
    fun `screen down enters the face-down state`() {
        assertTrue(FaceDownDetector.next(currentlyFaceDown = false, zAcceleration = -9.8f))
        assertTrue(FaceDownDetector.next(currentlyFaceDown = false, zAcceleration = -7.0f))
    }

    @Test
    fun `screen up leaves it`() {
        assertFalse(FaceDownDetector.next(currentlyFaceDown = true, zAcceleration = 9.8f))
        assertFalse(FaceDownDetector.next(currentlyFaceDown = true, zAcceleration = -5.0f))
    }

    @Test
    fun `the gap between the thresholds keeps the previous state`() {
        assertTrue(FaceDownDetector.next(currentlyFaceDown = true, zAcceleration = -6.0f))
        assertFalse(FaceDownDetector.next(currentlyFaceDown = false, zAcceleration = -6.0f))
    }

    @Test
    fun `an upright phone never counts as face down`() {
        // Gravity mostly on x/y, z near zero — standing in a cup holder.
        assertFalse(FaceDownDetector.next(currentlyFaceDown = false, zAcceleration = 0.3f))
    }
}
