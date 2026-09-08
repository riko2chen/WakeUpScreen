package com.symeonchen.wakeupscreen.services.reminder

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderVibrationPolicyTest {
    @Test fun everyRestrictionIndependentlyPreventsVibration() {
        for (enabled in listOf(false, true)) {
            for (dndOff in listOf(false, true)) {
                for (ringerAllows in listOf(false, true)) {
                    for (hardware in listOf(false, true)) {
                        assertEquals(
                            "enabled=$enabled, dndOff=$dndOff, ringerAllows=$ringerAllows, hardware=$hardware",
                            enabled && dndOff && ringerAllows && hardware,
                            ReminderVibrationPolicy.allows(enabled, dndOff, ringerAllows, hardware),
                        )
                    }
                }
            }
        }
    }
}
