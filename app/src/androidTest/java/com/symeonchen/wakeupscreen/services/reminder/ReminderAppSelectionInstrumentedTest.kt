package com.symeonchen.wakeupscreen.services.reminder

import android.app.Notification
import android.content.Context
import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.*
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.UnreadNotificationUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class ReminderAppSelectionInstrumentedTest {
    private val context get() = InstrumentationRegistry.getTargetContext()
    private val prefs get() = context.getSharedPreferences("wake_up_screen_settings", Context.MODE_PRIVATE)
    private lateinit var saved: Map<String, *>
    private val keys = listOf(ScConstant.REMINDER_CUSTOM_APPS, ScConstant.REMINDER_APP_PACKAGES,
        ScConstant.APP_NOTIFY_MODE, ScConstant.APP_FILTER_WHITE_LIST_STRING, ScConstant.APP_FILTER_BLACK_LIST_STRING)

    @Before fun setUp() {
        saved = prefs.all.filterKeys { it in keys }
        DataInjection.modeOfCurrent = CurrentMode.MODE_ALL_NOTIFY
        ReminderAppSelection().save()
    }

    @After fun tearDown() {
        prefs.edit().apply {
            keys.forEach { remove(it) }
            saved.forEach { (key, value) -> when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
            } }
        }.commit()
    }

    @Test fun selectedSubsetIsAppliedToSnapshotsWithoutBypassingInitialFilters() {
        DataInjection.modeOfCurrent = CurrentMode.MODE_WHITE_LIST
        DataInjection.appWhiteListStringOfNotify = "a,b,c"
        val active = arrayOf(notification("a"), notification("b"), notification("c"))
        assertEquals(3, UnreadNotificationUtils.snapshot(active).count)
        ReminderAppSelection(true, setOf("a")).save()
        assertEquals(1, UnreadNotificationUtils.snapshot(active).count)
        assertEquals("a", UnreadNotificationUtils.snapshot(active).representative?.packageName)
        DataInjection.appWhiteListStringOfNotify = "b,c"
        assertFalse(UnreadNotificationUtils.hasUnread(active))
        DataInjection.modeOfCurrent = CurrentMode.MODE_BLACK_LIST
        DataInjection.appBlackListStringOfNotify = "a"
        assertFalse(UnreadNotificationUtils.hasUnread(active))
    }

    @Test fun customEmptyListSurvivesBackupRestoreAndCanReturnToInheritedMode() {
        ReminderAppSelection(true).save()
        val backup = SettingsBackup.export()
        ReminderAppSelection(false, setOf("a")).save()
        assertTrue(SettingsBackup.import(backup) is SettingsBackup.ImportResult.Success)
        assertEquals(ReminderAppSelection(true), ReminderAppSelection.load())
        assertFalse(UnreadNotificationUtils.isUnread(notification("a")))
        ReminderAppSelection.load().copy(custom = false).save()
        assertTrue(UnreadNotificationUtils.isUnread(notification("a")))
    }

    @Test fun unavailablePackageSelectionSurvivesBackupRestore() {
        val selection = ReminderAppSelection(true, setOf("com.example.notinstalled"))
        selection.save()
        val backup = SettingsBackup.export()
        ReminderAppSelection().save()
        SettingsBackup.import(backup)
        assertEquals(selection, ReminderAppSelection.load())
    }

    @Test fun ongoingAndSummaryNotificationsRemainExcludedForSelectedApps() {
        ReminderAppSelection(true, setOf("a")).save()
        assertFalse(UnreadNotificationUtils.isUnread(notification("a", Notification.FLAG_ONGOING_EVENT)))
        assertFalse(UnreadNotificationUtils.isUnread(notification("a", Notification.FLAG_GROUP_SUMMARY)))
        assertFalse(UnreadNotificationUtils.isUnread(notification("a", Notification.FLAG_NO_CLEAR)))
        val transientMedia = notification("a").apply {
            notification.category = Notification.CATEGORY_TRANSPORT
        }
        assertTrue(transientMedia.isClearable)
        assertFalse(transientMedia.isOngoing)
        assertFalse(UnreadNotificationUtils.isUnread(transientMedia))
    }

    private fun notification(packageName: String, flags: Int = 0) = StatusBarNotification(
        packageName, packageName, 1, null, Process.myUid(), Process.myPid(), 0,
        Notification().apply { this.flags = flags }, Process.myUserHandle(), System.currentTimeMillis(),
    )
}
