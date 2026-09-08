package com.symeonchen.wakeupscreen.services.reminder

import android.content.Context
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises real preference persistence and AlarmManager cancellation without waking the display. */
@RunWith(AndroidJUnit4::class)
class ReminderSchedulerInstrumentedTest {
    private val context get() = InstrumentationRegistry.getTargetContext()
    private val preferences get() = context.getSharedPreferences("reminder_scheduler", Context.MODE_PRIVATE)
    private var savedApp = false
    private var savedReminder = false
    private var savedInterval = 0
    private var savedMaximum = 0
    private var savedRounds = 0
    private var savedDeadline = 0L

    @Before fun setUp() {
        savedDeadline = preferences.getLong("deadline", 0L)
        savedApp = DataInjection.switchOfApp
        savedReminder = DataInjection.repeatReminderSwitch
        savedInterval = DataInjection.repeatReminderIntervalMinutes
        savedMaximum = DataInjection.repeatReminderMaxRounds
        savedRounds = DataInjection.repeatReminderRoundCount
        ReminderScheduler.cancel(context)
        DataInjection.switchOfApp = true
        DataInjection.repeatReminderSwitch = true
        DataInjection.repeatReminderIntervalMinutes = 5
        DataInjection.repeatReminderMaxRounds = 3
    }

    @After fun tearDown() {
        ReminderScheduler.cancel(context)
        DataInjection.switchOfApp = savedApp
        DataInjection.repeatReminderSwitch = savedReminder
        DataInjection.repeatReminderIntervalMinutes = savedInterval
        DataInjection.repeatReminderMaxRounds = savedMaximum
        DataInjection.repeatReminderRoundCount = savedRounds
        if (savedDeadline > 0) {
            preferences.edit().putLong("deadline", savedDeadline).commit()
            ReminderScheduler.ensureScheduled(context, restore = true)
        }
    }

    @Test fun updatesAndReconnectKeepDeadlineAndCount() {
        ReminderScheduler.ensureScheduled(context)
        val due = preferences.getLong("deadline", 0)
        assertTrue(due > System.currentTimeMillis())
        DataInjection.repeatReminderRoundCount = 2
        repeat(10) { ReminderScheduler.ensureScheduled(context) }
        ReminderScheduler.ensureScheduled(context, restore = true)
        assertEquals(due, preferences.getLong("deadline", 0))
        assertEquals(2, DataInjection.repeatReminderRoundCount)
    }

    @Test fun bluetoothDiscoveryRetryReplacesDeadlineWithoutConsumingRound() {
        ReminderScheduler.ensureScheduled(context)
        val originalDeadline = ReminderScheduler.deadline(context)
        DataInjection.repeatReminderRoundCount = 2
        val before = System.currentTimeMillis()
        ReminderScheduler.scheduleRetry(context, 1500)
        val retryDeadline = ReminderScheduler.deadline(context)
        assertTrue(retryDeadline >= before + 1500)
        assertTrue(retryDeadline <= System.currentTimeMillis() + 1500)
        assertTrue(retryDeadline < originalDeadline)
        assertEquals(2, DataInjection.repeatReminderRoundCount)
        ReminderScheduler.ensureScheduled(context, restore = true)
        assertEquals(retryDeadline, ReminderScheduler.deadline(context))
        assertEquals(2, DataInjection.repeatReminderRoundCount)
    }

    @Test fun exhaustedBatchCannotRestartUntilDismissal() {
        DataInjection.repeatReminderRoundCount = 3
        ReminderScheduler.ensureScheduled(context)
        assertFalse(ReminderScheduler.isScheduled(context))
        ReminderEngine.onNotificationRemoved(context, emptyArray())
        assertEquals(0, DataInjection.repeatReminderRoundCount)
        ReminderScheduler.ensureScheduled(context)
        assertTrue(ReminderScheduler.isScheduled(context))
    }

    @Test fun selectedIntervalIsPersistedBeforeRearming() {
        ReminderScheduler.ensureScheduled(context)
        val before = System.currentTimeMillis()
        ReminderEngine.onIntervalChanged(context, 10)
        assertEquals(10, DataInjection.repeatReminderIntervalMinutes)
        val due = preferences.getLong("deadline", 0)
        assertTrue(due >= before + 600_000)
        assertTrue(due <= System.currentTimeMillis() + 600_000)
    }

    @Test fun staleAndDuplicateAlarmDeliveriesCannotClaimAnotherRound() {
        ReminderScheduler.ensureScheduled(context)
        assertFalse(ReminderScheduler.claimDue(context))
        preferences.edit().putLong("deadline", System.currentTimeMillis() - 1).commit()
        assertTrue(ReminderScheduler.claimDue(context))
        assertFalse(ReminderScheduler.claimDue(context))
        ReminderScheduler.scheduleNext(context)
        assertFalse(ReminderScheduler.claimDue(context))
    }

    @Test fun masterAndReminderSwitchesCancelBatchImmediately() {
        ReminderScheduler.ensureScheduled(context)
        DataInjection.repeatReminderRoundCount = 1
        DataInjection.switchOfApp = false
        ReminderEngine.onSettingsChanged(context)
        assertFalse(ReminderScheduler.isScheduled(context))
        assertEquals(0, DataInjection.repeatReminderRoundCount)
        DataInjection.switchOfApp = true
        ReminderScheduler.ensureScheduled(context)
        ReminderEngine.onSwitchChanged(context, false)
        assertFalse(ReminderScheduler.isScheduled(context))
        assertFalse(DataInjection.repeatReminderSwitch)
    }
}
