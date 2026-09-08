package com.symeonchen.wakeupscreen.utils

import org.junit.Assert.*
import org.junit.Test

class ReminderDurationPolicyTest {
    @Test fun defaultAlwaysDelegatesToExistingWakePath() {
        for (required in listOf(false, true)) {
            for (available in listOf(false, true)) {
                assertEquals(ReminderDurationPolicy.Wake.Inherit,
                    ReminderDurationPolicy.resolve(0, required, available))
                assertFalse(ReminderDurationPolicy.needsPermissionWarning(0, required, available))
            }
        }
    }

    @Test fun independentFiveSecondWindowUsesExistingPreciseDeadlineMath() {
        val custom = ReminderDurationPolicy.resolve(5, true, true) as ReminderDurationPolicy.Wake.Precise
        assertEquals(5L, custom.seconds)
        assertEquals(6_000L, ScreenOnWindowCalculator.deadlineOf(1_000L, custom.seconds))
        assertEquals(11_000L, ScreenOnWindowCalculator.deadlineOf(1_000L, 10))
    }

    @Test fun revokedOrDisconnectedServiceFallsBackWithoutStartingPreciseWindow() {
        assertEquals(ReminderDurationPolicy.Wake.SystemTimeout,
            ReminderDurationPolicy.resolve(5, true, false))
        assertTrue(ReminderDurationPolicy.needsPermissionWarning(5, true, false))
        assertEquals(ReminderDurationPolicy.Wake.Precise(5),
            ReminderDurationPolicy.resolve(5, true, true))
        assertFalse(ReminderDurationPolicy.needsPermissionWarning(5, true, true))
    }

    @Test fun olderAndroidUsesExistingPermissionFreeScreenOffMechanism() {
        assertEquals(ReminderDurationPolicy.Wake.Precise(30),
            ReminderDurationPolicy.resolve(30, false, false))
        assertFalse(ReminderDurationPolicy.needsPermissionWarning(30, false, false))
    }

    @Test fun corruptAndExtremeBackupDurationsCannotCreateUnsafeWindows() {
        assertEquals(0L, ReminderDurationPolicy.normalize(Long.MIN_VALUE))
        assertEquals(0L, ReminderDurationPolicy.normalize(-1))
        assertEquals(5L, ReminderDurationPolicy.normalize(1))
        assertEquals(7L, ReminderDurationPolicy.normalize(7))
        assertEquals(30L, ReminderDurationPolicy.normalize(Long.MAX_VALUE))
        assertEquals(ReminderDurationPolicy.Wake.Precise(30),
            ReminderDurationPolicy.resolve(Long.MAX_VALUE, true, true))
    }
}
