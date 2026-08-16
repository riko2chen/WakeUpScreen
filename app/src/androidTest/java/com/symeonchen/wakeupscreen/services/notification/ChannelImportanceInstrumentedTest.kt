package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import android.service.notification.StatusBarNotification
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService
import com.symeonchen.wakeupscreen.services.notification.conditions.ImportanceCondition
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

/**
 * The one link the other tests cannot reach: what the app reads back for a
 * notification the *system* has ranked.
 *
 * Everything else about the silent filter is decided from a number, and a number
 * can be handed to a unit test. This is about where that number comes from — and
 * getting it from the wrong place is a real mistake with no visible symptom
 * except the feature quietly doing nothing. So the test posts real notifications
 * on channels of known importance, and asserts on what the live listener resolves
 * for them.
 */
@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class ChannelImportanceInstrumentedTest {

    private val context: Context get() = InstrumentationRegistry.getTargetContext()

    private val manager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val condition = ImportanceCondition()

    private var savedSwitch = false

    private companion object {
        const val CHANNEL_SILENT = "wus_test_silent"
        const val CHANNEL_LOUD = "wus_test_loud"
        const val ID_SILENT = 90001
        const val ID_LOUD = 90002

        /** How long to let the system bind the listener and deliver a post. */
        const val WAIT_MS = 15_000L
        const val POLL_MS = 250L
    }

    @Before
    fun setUp() {
        assumeTrue("channels need Android 8+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        // Installing the test APK unbinds the listener, so the instance the
        // service publishes is gone by the time the test runs. Re-granting the
        // permission is what makes the system bind it again.
        rebindListener()
        savedSwitch = DataInjection.ignoreSilentNotificationSwitch
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_SILENT, "WUS test silent", NotificationManager.IMPORTANCE_LOW)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_LOUD, "WUS test loud", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    @After
    fun tearDown() {
        DataInjection.ignoreSilentNotificationSwitch = savedSwitch
        try {
            manager.cancel(ID_SILENT)
            manager.cancel(ID_LOUD)
            manager.deleteNotificationChannel(CHANNEL_SILENT)
            manager.deleteNotificationChannel(CHANNEL_LOUD)
        } catch (_: Exception) {
        }
    }

    @Test
    fun theAppReadsBackTheImportanceTheSystemRanked() {
        val service = awaitListener()
        assumeNotNull("notification listener is not bound", service)
        service!!

        val silent = post(service, CHANNEL_SILENT, ID_SILENT, "silent one")
        val loud = post(service, CHANNEL_LOUD, ID_LOUD, "loud one")
        assertNotNull("silent notification was never delivered", silent)
        assertNotNull("loud notification was never delivered", loud)

        val silentImportance = service.channelInfoOf(silent!!).importance
        val loudImportance = service.channelInfoOf(loud!!).importance

        assertEquals(
            "a LOW channel must read back as LOW",
            NotificationManager.IMPORTANCE_LOW,
            silentImportance,
        )
        assertEquals(
            "a DEFAULT channel must read back as DEFAULT",
            NotificationManager.IMPORTANCE_DEFAULT,
            loudImportance,
        )
        assertTrue(ImportancePolicy.isSilent(silentImportance))
        assertTrue(!ImportancePolicy.isSilent(loudImportance))
    }

    @Test
    fun theGateBlocksTheSilentOneAndOnlyThat() {
        val service = awaitListener()
        assumeNotNull("notification listener is not bound", service)
        service!!

        val silent = post(service, CHANNEL_SILENT, ID_SILENT, "silent one")
        val loud = post(service, CHANNEL_LOUD, ID_LOUD, "loud one")
        assertNotNull(silent)
        assertNotNull(loud)

        DataInjection.ignoreSilentNotificationSwitch = false
        assertEquals(
            ConditionState.SUCCESS,
            condition.provideResult(paramFor(service, silent!!)),
        )

        DataInjection.ignoreSilentNotificationSwitch = true
        assertEquals(
            ConditionState.BLOCK,
            condition.provideResult(paramFor(service, silent)),
        )
        assertEquals(
            ConditionState.SUCCESS,
            condition.provideResult(paramFor(service, loud!!)),
        )
    }

    private fun paramFor(
        service: ScNotificationListenerService,
        sbn: StatusBarNotification,
    ) = ConditionParam(sbn = sbn, channelInfo = service.channelInfoOf(sbn))

    /**
     * Posts on [channelId] and waits for the listener to see it, returning the
     * delivered notification.
     */
    private fun post(
        service: ScNotificationListenerService,
        channelId: String,
        id: Int,
        title: String,
    ): StatusBarNotification? {
        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .build()
        manager.notify(id, notification)
        return await {
            try {
                service.activeNotifications?.firstOrNull {
                    it.packageName == context.packageName && it.id == id
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun awaitListener(): ScNotificationListenerService? =
        await { ScNotificationListenerService.instance }

    /**
     * Asks the system to bind the listener again, and grants the notification
     * permission the posts below need. Both go through the shell, because the
     * test process cannot grant itself either one.
     *
     * Then waits for the permission to actually be in effect. Without that wait
     * the first post races the grant and is dropped in silence — which surfaces
     * much later as "the notification was never delivered", blaming the listener
     * for something the test did to itself.
     */
    private fun rebindListener() {
        val component = "${context.packageName}/" +
            "${ScNotificationListenerService::class.java.name}"
        shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        shell("cmd notification allow_listener $component")
        await { if (manager.areNotificationsEnabled()) true else null }
    }

    /**
     * Runs a shell command and waits for it to finish.
     *
     * Draining the output is what provides the wait: the descriptor reaches EOF
     * when the command exits, so a caller that closes it early — as this did —
     * carries on while the command is still running.
     */
    private fun shell(command: String) {
        try {
            val fd = InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .executeShellCommand(command)
            ParcelFileDescriptor.AutoCloseInputStream(fd).use { it.readBytes() }
        } catch (_: Exception) {
            // Best effort: the assumption in the test body reports the outcome.
        }
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
