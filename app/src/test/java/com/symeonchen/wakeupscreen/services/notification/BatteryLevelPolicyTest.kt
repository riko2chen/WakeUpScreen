package com.symeonchen.wakeupscreen.services.notification

import com.symeonchen.wakeupscreen.services.notification.conditions.BatteryLevelPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryLevelPolicyTest {

    @Test
    fun `blocks below the threshold while draining`() {
        assertTrue(BatteryLevelPolicy.shouldBlock(enabled = true, levelPercent = 19, isCharging = false, thresholdPercent = 20))
        assertTrue(BatteryLevelPolicy.shouldBlock(enabled = true, levelPercent = 0, isCharging = false, thresholdPercent = 20))
    }

    @Test
    fun `the threshold itself passes`() {
        assertFalse(BatteryLevelPolicy.shouldBlock(enabled = true, levelPercent = 20, isCharging = false, thresholdPercent = 20))
        assertFalse(BatteryLevelPolicy.shouldBlock(enabled = true, levelPercent = 90, isCharging = false, thresholdPercent = 20))
    }

    @Test
    fun `charging always passes`() {
        assertFalse(BatteryLevelPolicy.shouldBlock(enabled = true, levelPercent = 5, isCharging = true, thresholdPercent = 20))
    }

    @Test
    fun `disabled always passes`() {
        assertFalse(BatteryLevelPolicy.shouldBlock(enabled = false, levelPercent = 5, isCharging = false, thresholdPercent = 20))
    }

    @Test
    fun `an unreadable level passes rather than silencing everything`() {
        assertFalse(BatteryLevelPolicy.shouldBlock(enabled = true, levelPercent = null, isCharging = false, thresholdPercent = 20))
    }
}
