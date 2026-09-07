package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeNotNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortLivedNotificationInstrumentedTest {

    private val context: Context get() = InstrumentationRegistry.getTargetContext()

    private val manager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private lateinit var saved: Map<String, Any>

    private companion object {
        const val CHANNEL_ID = "wus_short_lived_test"
        const val NOTIFICATION_ID = 90102
        const val GRACE_PERIOD_MS = 2000L
        const val CANCEL_AFTER_ACTIVE_MS = 100L
        const val WAIT_MS = 10_000L
        const val POLL_MS = 25L
    }

    @Before
    fun setUp() {
        assumeTrue("notification channels need Android 8+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        rebindListener()
        saved = mapOf(
            "app" to DataInjection.switchOfApp,
            "grace" to DataInjection.notificationGracePeriodMs,
            "proximity" to DataInjection.switchOfProximity,
            "ongoing" to DataInjection.ongoingOptimize,
            "radical" to DataInjection.radicalOngoingOptimize,
            "silent" to DataInjection.ignoreSilentNotificationSwitch,
            "sleep" to DataInjection.sleepModeBoolean,
            "dnd" to DataInjection.dndDetectSwitch,
            "charging" to DataInjection.chargingOnlySwitch,
            "mode" to DataInjection.modeOfCurrent,
        )
        DataInjection.switchOfApp = true
        DataInjection.notificationGracePeriodMs = GRACE_PERIOD_MS
        DataInjection.switchOfProximity = false
        DataInjection.ongoingOptimize = false
        DataInjection.radicalOngoingOptimize = false
        DataInjection.ignoreSilentNotificationSwitch = false
        DataInjection.sleepModeBoolean = false
        DataInjection.dndDetectSwitch = false
        DataInjection.chargingOnlySwitch = false
        DataInjection.modeOfCurrent = CurrentMode.MODE_ALL_NOTIFY
        NotificationLogStore.clearLogs()
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "WUS short lived test", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    @After
    fun tearDown() {
        manager.cancel(NOTIFICATION_ID)
        manager.deleteNotificationChannel(CHANNEL_ID)
        DataInjection.switchOfApp = saved["app"] as Boolean
        DataInjection.notificationGracePeriodMs = saved["grace"] as Long
        DataInjection.switchOfProximity = saved["proximity"] as Boolean
        DataInjection.ongoingOptimize = saved["ongoing"] as Boolean
        DataInjection.radicalOngoingOptimize = saved["radical"] as Boolean
        DataInjection.ignoreSilentNotificationSwitch = saved["silent"] as Boolean
        DataInjection.sleepModeBoolean = saved["sleep"] as Boolean
        DataInjection.dndDetectSwitch = saved["dnd"] as Boolean
        DataInjection.chargingOnlySwitch = saved["charging"] as Boolean
        DataInjection.modeOfCurrent = saved["mode"] as CurrentMode
    }

    @Test
    fun notificationCancelledInsideGracePeriodIsLoggedAsDismissed() {
        val service = await { ScNotificationListenerService.instance }
        assumeNotNull("notification listener is not bound", service)
        service!!

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("short lived notification")
            .build()
        manager.notify(NOTIFICATION_ID, notification)

        val posted = await {
            service.activeNotifications?.firstOrNull {
                it.packageName == context.packageName && it.id == NOTIFICATION_ID
            }
        }
        assertNotNull("notification was never visible to the listener", posted)

        Thread.sleep(CANCEL_AFTER_ACTIVE_MS)
        manager.cancel(NOTIFICATION_ID)

        val log = await {
            NotificationLogStore.loadLogs().firstOrNull {
                it.packageName == context.packageName
            }
        }
        assertNotNull("dismissed notification was never logged", log)
        assertEquals(LogStatus.BLOCKED, log!!.status)
        assertEquals(BlockReason.NOTIFICATION_DISMISSED, log.blockReason)
        Log.i("WUS_GRACE_TEST", "short status=${log.status.key} reason=${log.blockReason}")
    }

    @Test
    fun notificationThatSurvivesGracePeriodContinuesNormalProcessing() {
        val service = await { ScNotificationListenerService.instance }
        assumeNotNull("notification listener is not bound", service)

        postNotification("long lived notification")

        val log = await {
            NotificationLogStore.loadLogs().firstOrNull {
                it.packageName == context.packageName
            }
        }
        assertNotNull("surviving notification was never processed", log)
        assertTrue(log!!.blockReason != BlockReason.NOTIFICATION_DISMISSED)
        Log.i("WUS_GRACE_TEST", "surviving status=${log.status.key} reason=${log.blockReason}")
    }

    @Test
    fun zeroGracePreservesImmediateProcessing() {
        DataInjection.notificationGracePeriodMs = 0L
        val service = await { ScNotificationListenerService.instance }
        assumeNotNull("notification listener is not bound", service)

        postNotification("zero grace notification")

        val log = await {
            NotificationLogStore.loadLogs().firstOrNull {
                it.packageName == context.packageName
            }
        }
        assertNotNull("zero-grace notification was never processed", log)
        assertTrue(log!!.blockReason != BlockReason.NOTIFICATION_DISMISSED)
        Log.i("WUS_GRACE_TEST", "zero status=${log.status.key} reason=${log.blockReason}")
    }

    private fun postNotification(title: String) {
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun rebindListener() {
        val component = "${context.packageName}/${ScNotificationListenerService::class.java.name}"
        shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        shell("cmd notification allow_listener $component")
        await { if (manager.areNotificationsEnabled()) true else null }
    }

    private fun shell(command: String) {
        val fd = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
    }

    private fun <T> await(block: () -> T?): T? {
        val deadline = System.currentTimeMillis() + WAIT_MS
        while (System.currentTimeMillis() < deadline) {
            block()?.let { return it }
            Thread.sleep(POLL_MS)
        }
        return null
    }
}
