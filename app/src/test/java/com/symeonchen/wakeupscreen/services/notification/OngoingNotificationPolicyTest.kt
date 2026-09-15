package com.symeonchen.wakeupscreen.services.notification

import com.symeonchen.wakeupscreen.services.notification.conditions.OngoingNotificationPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OngoingNotificationPolicyTest {

    @Test
    fun `default filters block ordinary ongoing notifications`() {
        assertTrue(
            OngoingNotificationPolicy.shouldBlock(
                ongoingFilterEnabled = true,
                radicalFilterEnabled = true,
                isOngoing = true,
                isClearable = false,
                isMedia = false,
            )
        )
    }

    @Test
    fun `media updates stay blocked while their flags are transiently clearable`() {
        assertTrue(
            OngoingNotificationPolicy.shouldBlock(
                ongoingFilterEnabled = true,
                radicalFilterEnabled = true,
                isOngoing = false,
                isClearable = true,
                isMedia = true,
            )
        )
    }

    @Test
    fun `turning off ongoing filtering allows clearable media updates`() {
        assertFalse(
            OngoingNotificationPolicy.shouldBlock(
                ongoingFilterEnabled = false,
                radicalFilterEnabled = true,
                isOngoing = false,
                isClearable = true,
                isMedia = true,
            )
        )
    }

    @Test
    fun `radical filtering remains independent`() {
        assertTrue(
            OngoingNotificationPolicy.shouldBlock(
                ongoingFilterEnabled = false,
                radicalFilterEnabled = true,
                isOngoing = false,
                isClearable = false,
                isMedia = false,
            )
        )
        assertFalse(
            OngoingNotificationPolicy.shouldBlock(
                ongoingFilterEnabled = false,
                radicalFilterEnabled = false,
                isOngoing = true,
                isClearable = false,
                isMedia = true,
            )
        )
    }
}
