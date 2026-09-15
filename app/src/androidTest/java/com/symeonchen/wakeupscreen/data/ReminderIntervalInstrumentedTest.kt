package com.symeonchen.wakeupscreen.data

import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderIntervalInstrumentedTest {
    @Test fun persistenceAcceptsCustomValuesAndRejectsInvalidWrites() {
        val key = ScConstant.REPEAT_REMINDER_INTERVAL_MINUTES
        val saved = ScStore.getInt(key, ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES)
        try {
            for (minutes in listOf(5, 90, 1440)) {
                DataInjection.repeatReminderIntervalMinutes = minutes
                assertEquals(minutes, ScStore.getInt(key, -1))
                assertEquals(minutes, DataInjection.repeatReminderIntervalMinutes)
            }
            for (minutes in listOf(Int.MIN_VALUE, 0, 4, 1441, Int.MAX_VALUE)) {
                DataInjection.repeatReminderIntervalMinutes = minutes
                assertEquals(1440, DataInjection.repeatReminderIntervalMinutes)
            }
            // Restore/import writes this same key directly, bypassing the setter.
            for (minutes in listOf(4, 1441, Int.MAX_VALUE)) {
                ScStore.putInt(key, minutes)
                assertEquals(ScConstant.DEFAULT_REPEAT_REMINDER_INTERVAL_MINUTES, DataInjection.repeatReminderIntervalMinutes)
            }
            ScStore.putInt(key, 90)
            assertEquals(90, DataInjection.repeatReminderIntervalMinutes)
        } finally {
            ScStore.putInt(key, saved)
        }
    }
}
