package com.symeonchen.wakeupscreen.services.reminder

import android.app.NotificationManager
import android.media.AudioManager
import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.AudioAttributes
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.data.SettingsBackup
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderVibrationInstrumentedTest {
    private val context get() = InstrumentationRegistry.getTargetContext()

    @Test fun usesNotificationAttributesAndNormalPermission() {
        val attributes = ReminderVibration.notificationAttributes()
        assertEquals(AudioAttributes.USAGE_NOTIFICATION, attributes.usage)
        assertEquals(AudioAttributes.CONTENT_TYPE_SONIFICATION, attributes.contentType)
        assertEquals(PackageManager.PERMISSION_GRANTED,
            context.checkCallingOrSelfPermission(Manifest.permission.VIBRATE))
        assertFalse(ScConstant.DEFAULT_REPEAT_REMINDER_VIBRATION)
    }

    @Test fun actualAndroidModeValuesRespectEveryDndModeAndSilentMode() {
        for (filter in listOf(NotificationManager.INTERRUPTION_FILTER_UNKNOWN,
            NotificationManager.INTERRUPTION_FILTER_NONE, NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            NotificationManager.INTERRUPTION_FILTER_ALARMS, NotificationManager.INTERRUPTION_FILTER_ALL)) {
            for (mode in listOf(AudioManager.RINGER_MODE_NORMAL, AudioManager.RINGER_MODE_VIBRATE,
                AudioManager.RINGER_MODE_SILENT, -1)) {
                assertEquals(filter == NotificationManager.INTERRUPTION_FILTER_ALL &&
                    (mode == AudioManager.RINGER_MODE_NORMAL || mode == AudioManager.RINGER_MODE_VIBRATE),
                    ReminderVibration.systemAllowsVibration(filter, mode))
            }
        }
    }

    @Test fun disabledFeatureNeverAccessesVibratorAndDeniedAccessDoesNotCrash() {
        val saved = DataInjection.repeatReminderVibration
        val app = DataInjection.switchOfApp
        val reminder = DataInjection.repeatReminderSwitch
        val denied = object : ContextWrapper(context) {
            override fun getSystemService(name: String): Any? = throw SecurityException("Test policy denial")
        }
        try {
            DataInjection.switchOfApp = true
            DataInjection.repeatReminderSwitch = true
            DataInjection.repeatReminderVibration = false
            assertFalse(ReminderVibration.vibrateIfAllowed(denied))
            DataInjection.repeatReminderVibration = true
            assertFalse(ReminderVibration.vibrateIfAllowed(denied))
            val backup = SettingsBackup.export()
            DataInjection.repeatReminderVibration = false
            assertTrue(SettingsBackup.import(backup) is SettingsBackup.ImportResult.Success)
            assertTrue(DataInjection.repeatReminderVibration)
            DataInjection.switchOfApp = false
            assertFalse(ReminderVibration.vibrateIfAllowed(denied))
            DataInjection.switchOfApp = true
            DataInjection.repeatReminderSwitch = false
            assertFalse(ReminderVibration.vibrateIfAllowed(denied))
        } finally {
            DataInjection.repeatReminderVibration = saved
            DataInjection.switchOfApp = app
            DataInjection.repeatReminderSwitch = reminder
        }
    }
}
