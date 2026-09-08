package com.symeonchen.wakeupscreen.data

import org.junit.Assert.*
import org.junit.Test

class ReminderIntervalPolicyTest {
    @Test fun acceptsEveryMinuteWithinBoundsIncludingNonPresets() {
        for (minutes in 5..1440) {
            assertEquals(minutes, ReminderIntervalPolicy.parse(minutes.toString()))
            assertEquals(minutes, ReminderIntervalPolicy.normalize(minutes))
        }
        assertEquals(90, ReminderIntervalPolicy.parse(" 90 "))
    }

    @Test fun rejectsInvalidInputWithoutCoercingItToAnotherInterval() {
        for (input in listOf("", " ", "abc", "5.5", "-5", "+5", "0", "4", "1441", "2147483648", "9999999999999999999999")) {
            assertNull(input, ReminderIntervalPolicy.parse(input))
        }
    }

    @Test fun normalizesInvalidImportedValuesAndPreservesPresets() {
        for (minutes in listOf(Int.MIN_VALUE, -1, 0, 4, 1441, Int.MAX_VALUE)) {
            assertEquals(ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES, ReminderIntervalPolicy.normalize(minutes))
        }
        ScConstant.REPEAT_REMINDER_INTERVAL_OPTIONS.forEach {
            assertEquals(it, ReminderIntervalPolicy.normalize(it))
        }
    }
}
