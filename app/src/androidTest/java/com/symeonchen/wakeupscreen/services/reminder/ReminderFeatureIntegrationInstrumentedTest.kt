package com.symeonchen.wakeupscreen.services.reminder

import android.app.Notification
import android.content.Context
import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.*
import com.symeonchen.wakeupscreen.services.notification.*
import com.symeonchen.wakeupscreen.services.notification.conditions.FilterListCondition
import com.symeonchen.wakeupscreen.utils.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Cross-feature regressions: apply the app subset before evaluating a shared reminder batch. */
@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class ReminderFeatureIntegrationInstrumentedTest {
    private val context get() = InstrumentationRegistry.getTargetContext()
    private val prefs get() = context.getSharedPreferences("wake_up_screen_settings", Context.MODE_PRIVATE)
    private lateinit var saved: Map<String, *>

    @Before fun setUp() {
        saved = prefs.all
        DataInjection.bluetoothWakeSwitch = false
        DataInjection.repeatReminderVibration = false
        DataInjection.switchOfProximity = false
        DataInjection.switchOfFaceDown = false
        DataInjection.sleepModeBoolean = false
        DataInjection.dndDetectSwitch = false
        DataInjection.chargingOnlySwitch = false
        DataInjection.batteryLevelSwitch = false
        DataInjection.ignoreSilentNotificationSwitch = true
        DataInjection.modeOfCurrent = CurrentMode.MODE_WHITE_LIST
        DataInjection.appWhiteListStringOfNotify = "a,b,c"
        ReminderAppSelection(true, setOf("a")).save()
    }

    @After fun tearDown() {
        prefs.edit().clear().apply {
            saved.forEach { (key, value) -> when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
            } }
        }.commit()
    }

    @Test fun selectedAppCanRemindEvenWithNewerSilentAndUnselectedNotifications() {
        val active = listOf(notification("b", 3), notification("a", 2), notification("a", 1))
        assertEquals(ConditionState.SUCCESS, FilterListCondition().provideResult(active.first()))
        val candidates = active.filter(UnreadNotificationUtils::isUnread)
        assertEquals(2, candidates.size)
        val decision = ReminderPolicy.evaluate(candidates, check = {
            ListenerManager.provideState(ConditionParam(
                sbn = it, channelInfo = ChannelLogInfo(importance = if (it.id == 2) 2 else 4),
            ))
        }, reason = { it.blockingCondition })
        assertEquals(1, decision?.first?.id)
        assertEquals(ConditionState.SUCCESS, decision?.second?.state)
    }

    @Test fun customEmptySelectionCancelsPendingBatchAndClearsItsCount() {
        DataInjection.switchOfApp = true
        DataInjection.repeatReminderSwitch = true
        DataInjection.repeatReminderRoundCount = 2
        ReminderScheduler.scheduleNext(context)
        assertTrue(ReminderScheduler.isScheduled(context))
        ReminderAppSelection(true, emptySet()).save()
        ReminderAppSelectionController.onChanged(context)
        assertFalse(ReminderScheduler.isScheduled(context))
        assertEquals(0, DataInjection.repeatReminderRoundCount)
    }

    @Test fun subsetAndIndependentDurationSurviveTheSameBackupWithoutChangingInitialDuration() {
        DataInjection.preciseScreenOnSwitch = false
        DataInjection.repeatReminderScreenOnSeconds = 12L
        DataInjection.repeatReminderIntervalMinutes = 90
        DataInjection.repeatReminderVibration = true
        DataInjection.bluetoothWakeDevices = setOf("AA:BB:CC:DD:EE:FF")
        val backup = SettingsBackup.export()
        ReminderAppSelection().save()
        DataInjection.repeatReminderScreenOnSeconds = 0L
        DataInjection.repeatReminderIntervalMinutes = 15
        DataInjection.repeatReminderVibration = false
        DataInjection.bluetoothWakeDevices = emptySet()
        assertTrue(SettingsBackup.import(backup) is SettingsBackup.ImportResult.Success)
        assertEquals(ReminderAppSelection(true, setOf("a")), ReminderAppSelection.load())
        assertEquals(12L, DataInjection.repeatReminderScreenOnSeconds)
        assertFalse(DataInjection.preciseScreenOnSwitch)
        assertEquals(90, DataInjection.repeatReminderIntervalMinutes)
        assertTrue(DataInjection.repeatReminderVibration)
        assertEquals(setOf("AA:BB:CC:DD:EE:FF"), DataInjection.bluetoothWakeDevices)
    }

    @Test fun enabledBluetoothGateBlocksTheWholeSelectedBatchWhenNoDeviceIsSelected() {
        DataInjection.bluetoothWakeSwitch = true
        DataInjection.bluetoothWakeDevices = emptySet()
        var checked = 0
        val decision = ReminderPolicy.evaluate(listOf(notification("a", 2), notification("a", 1)), check = {
            checked++
            ListenerManager.provideState(ConditionParam(
                sbn = it,
                appContext = context.applicationContext as android.app.Application,
                channelInfo = ChannelLogInfo(importance = 4),
            ))
        }, reason = { it.blockingCondition })
        assertEquals(BlockReason.BLUETOOTH, decision?.second?.blockingCondition)
        assertEquals(1, checked)
    }

    private fun notification(pkg: String, id: Int) = StatusBarNotification(
        pkg, pkg, id, null, Process.myUid(), Process.myPid(), 0,
        Notification(), Process.myUserHandle(), id.toLong(),
    )
}
