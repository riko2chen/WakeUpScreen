package com.symeonchen.wakeupscreen.services.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OngoingNotificationPolicyTest {

    @Test
    fun `an ongoing flag is blocked when the switch is on`() {
        assertTrue(
            OngoingNotificationPolicy.shouldBlock(
                isOngoing = true,
                isClearable = true,
                isPlayback = false,
                blockOngoing = true,
                blockNonClearable = false,
            )
        )
    }

    @Test
    fun `a media session update is blocked even without the ongoing flag`() {
        assertTrue(
            OngoingNotificationPolicy.shouldBlock(
                isOngoing = false,
                isClearable = true,
                isPlayback = true,
                blockOngoing = true,
                blockNonClearable = false,
            )
        )
    }

    @Test
    fun `a media session update passes when ongoing detection is off`() {
        assertFalse(
            OngoingNotificationPolicy.shouldBlock(
                isOngoing = false,
                isClearable = true,
                isPlayback = true,
                blockOngoing = false,
                blockNonClearable = false,
            )
        )
    }

    @Test
    fun `a clearable ordinary message is never blocked by these gates`() {
        assertFalse(
            OngoingNotificationPolicy.shouldBlock(
                isOngoing = false,
                isClearable = true,
                isPlayback = false,
                blockOngoing = true,
                blockNonClearable = true,
            )
        )
    }

    @Test
    fun `a non-clearable notification is blocked by the radical switch`() {
        assertTrue(
            OngoingNotificationPolicy.shouldBlock(
                isOngoing = false,
                isClearable = false,
                isPlayback = false,
                blockOngoing = false,
                blockNonClearable = true,
            )
        )
    }
}
