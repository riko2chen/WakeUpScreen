package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.data.LogTrigger
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
            "bluetooth" to DataInjection.bluetoothWakeSwitch,
            "mode" to DataInjection.modeOfCurrent,
            "faceDown" to DataInjection.switchOfFaceDown,
            "battery" to DataInjection.batteryLevelSwitch,
            "reminder" to DataInjection.repeatReminderSwitch,
            "precise" to DataInjection.preciseScreenOnSwitch,
        )
        rebindListener()
        DataInjection.switchOfFaceDown = false
        DataInjection.batteryLevelSwitch = false
        DataInjection.repeatReminderSwitch = false
        DataInjection.preciseScreenOnSwitch = false
        DataInjection.switchOfApp = true
        DataInjection.notificationGracePeriodMs = GRACE_PERIOD_MS
        DataInjection.switchOfProximity = false
        DataInjection.ongoingOptimize = false
        DataInjection.radicalOngoingOptimize = false
        DataInjection.ignoreSilentNotificationSwitch = false
        DataInjection.sleepModeBoolean = false
        DataInjection.dndDetectSwitch = false
        DataInjection.chargingOnlySwitch = false
        DataInjection.bluetoothWakeSwitch = false
        DataInjection.modeOfCurrent = CurrentMode.MODE_ALL_NOTIFY
        NotificationLogStore.clearLogs()
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "WUS short lived test", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    @After
    fun tearDown() {
        if (!::saved.isInitialized) return
        manager.cancel(NOTIFICATION_ID)
        val service = ScNotificationListenerService.instance
        await {
            if (service?.activeNotifications?.none { it.id == NOTIFICATION_ID &&
                    it.packageName == context.packageName } != false) true else null
        }
        // Let the listener consume removal before restoring preferences.
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        manager.deleteNotificationChannel(CHANNEL_ID)
        shell("input keyevent 224")
        DataInjection.switchOfFaceDown = saved["faceDown"] as Boolean
        DataInjection.batteryLevelSwitch = saved["battery"] as Boolean
        DataInjection.repeatReminderSwitch = saved["reminder"] as Boolean
        DataInjection.preciseScreenOnSwitch = saved["precise"] as Boolean
        DataInjection.switchOfApp = saved["app"] as Boolean
        DataInjection.notificationGracePeriodMs = saved["grace"] as Long
        DataInjection.switchOfProximity = saved["proximity"] as Boolean
        DataInjection.ongoingOptimize = saved["ongoing"] as Boolean
        DataInjection.radicalOngoingOptimize = saved["radical"] as Boolean
        DataInjection.ignoreSilentNotificationSwitch = saved["silent"] as Boolean
        DataInjection.sleepModeBoolean = saved["sleep"] as Boolean
        DataInjection.dndDetectSwitch = saved["dnd"] as Boolean
        DataInjection.chargingOnlySwitch = saved["charging"] as Boolean
        DataInjection.bluetoothWakeSwitch = saved["bluetooth"] as Boolean
        DataInjection.modeOfCurrent = saved["mode"] as CurrentMode
    }

    @Test
    fun notificationCancelledInsideGracePeriodIsLoggedAsDismissed() {
        val service = await { ScNotificationListenerService.instance }
        assertNotNull("notification listener is not bound", service)
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
        assertNotNull("notification listener is not bound", service)

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
        assertNotNull("notification listener is not bound", service)

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

    @Test
    fun continuousSameKeyUpdatesAreProcessedBeforeUpdatesStop() {
        val service = await { ScNotificationListenerService.instance }
        assertNotNull("notification listener is not bound", service)
        val started = SystemClock.elapsedRealtime()
        postNotification("update 0")
        val first = await {
            service!!.activeNotifications?.firstOrNull {
                it.packageName == context.packageName && it.id == NOTIFICATION_ID
            }
        }
        assertNotNull(first)

        var update = 0
        while (ownLogs().isEmpty() && SystemClock.elapsedRealtime() - started < 6000L) {
            Thread.sleep(250L)
            postNotification("update ${++update}")
            val current = service!!.activeNotifications?.firstOrNull {
                it.packageName == context.packageName && it.id == NOTIFICATION_ID
            }
            assertEquals("updates must reuse the real Android key", first!!.key, current?.key)
        }

        val elapsed = SystemClock.elapsedRealtime() - started
        assertTrue("continuous updates starved notification processing", ownLogs().isNotEmpty())
        assertTrue("processed before the grace period", elapsed >= GRACE_PERIOD_MS - 100L)
        assertTrue(ownLogs().first().blockReason != BlockReason.NOTIFICATION_DISMISSED)
        Log.i("WUS_GRACE_TEST", "updates=$update first processing after ${elapsed}ms key=${first!!.key}")
    }

    @Test
    fun sameKeyCanBeProcessedAgainAfterTheFirstWaitCompletes() {
        assertNotNull(await { ScNotificationListenerService.instance })
        postNotification("first message")
        assertNotNull(await { ownLogs().firstOrNull() })
        val started = SystemClock.elapsedRealtime()
        postNotification("second message")
        assertNotNull(await { ownLogs().takeIf { it.size >= 2 } })
        assertTrue(SystemClock.elapsedRealtime() - started >= GRACE_PERIOD_MS - 100L)
        assertEquals(2, ownLogs().size)
        assertTrue(ownLogs().none { it.blockReason == BlockReason.NOTIFICATION_DISMISSED })
    }

    @Test
    fun removalLogsImmediatelyAndSameKeyRepostGetsANewWait() {
        val service = await { ScNotificationListenerService.instance }
        assertNotNull(service)
        postNotification("will be removed")
        assertNotNull(await {
            service!!.activeNotifications?.firstOrNull {
                it.packageName == context.packageName && it.id == NOTIFICATION_ID
            }
        })
        Thread.sleep(CANCEL_AFTER_ACTIVE_MS)
        val removedAt = SystemClock.elapsedRealtime()
        manager.cancel(NOTIFICATION_ID)
        val removedLog = await { ownLogs().firstOrNull() }
        assertNotNull(removedLog)
        assertEquals(BlockReason.NOTIFICATION_DISMISSED, removedLog!!.blockReason)
        assertTrue("removal waited for the old deadline",
            SystemClock.elapsedRealtime() - removedAt < 1000L)

        val repostedAt = SystemClock.elapsedRealtime()
        postNotification("reposted with same id")
        assertNotNull(await { ownLogs().takeIf { it.size >= 2 } })
        assertTrue("repost inherited the removed notification's deadline",
            SystemClock.elapsedRealtime() - repostedAt >= GRACE_PERIOD_MS - 100L)
        assertEquals(2, ownLogs().size)
        assertTrue(ownLogs().first().blockReason != BlockReason.NOTIFICATION_DISMISSED)
    }

    @Test
    fun shortNotificationKeepsScreenOffAndSurvivingNotificationWakesIt() {
        val service = await { ScNotificationListenerService.instance }
        assertNotNull(service)
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        shell("input keyevent 223")
        assertNotNull("screen did not turn off", await { if (!power.isInteractive) true else null })

        postNotification("short while screen off")
        // Cancellation before Android enqueues the post produces no listener event.
        // Start the short-lived interval only once the notification is visible.
        assertNotNull("short notification was never delivered", await {
            service!!.activeNotifications?.firstOrNull {
                it.packageName == context.packageName && it.id == NOTIFICATION_ID
            }
        })
        Thread.sleep(CANCEL_AFTER_ACTIVE_MS)
        manager.cancel(NOTIFICATION_ID)
        assertNotNull(await { ownLogs().firstOrNull() })
        assertEquals(BlockReason.NOTIFICATION_DISMISSED, ownLogs().first().blockReason)
        Thread.sleep(GRACE_PERIOD_MS)
        assertTrue("short notification woke the screen", !power.isInteractive)
        assertEquals(1, ownLogs().size)

        postNotification("survives while screen off")
        assertNotNull(await { ownLogs().takeIf { it.size >= 2 } })
        assertEquals(LogStatus.WAKED_UP, ownLogs().first().status)
        assertNotNull("surviving notification did not wake the display",
            await { if (power.isInteractive) true else null })
        Log.i("WUS_GRACE_TEST", "screen-off short=blocked surviving=waked_up displayInteractive=true")
    }

    private fun ownLogs() = NotificationLogStore.loadLogs().filter {
        it.packageName == context.packageName && it.trigger == LogTrigger.NOTIFICATION
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
