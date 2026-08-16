package com.symeonchen.wakeupscreen.services.notification

import android.app.Notification
import android.content.Context
import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.services.notification.conditions.ImportanceCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.OnGoingNotificationCondition
import com.symeonchen.wakeupscreen.utils.ChannelLogInfo
import com.symeonchen.wakeupscreen.utils.DataInjection
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The silent filter against real [StatusBarNotification]s on a real device.
 *
 * The unit tests cover the rule in isolation; what needs a device is everything
 * around it — that the flags and fields the conditions read mean what they are
 * assumed to mean, and that the chain reports the right blocker once the
 * preferences are stored where the service reads them.
 *
 * Runs against the installed app's own preferences, so every value it touches
 * is saved and put back afterwards.
 */
@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
class NotificationDecisionInstrumentedTest {

    private val context: Context get() = InstrumentationRegistry.getTargetContext()

    private val importance = ImportanceCondition()
    private val ongoing = OnGoingNotificationCondition()

    private val sender = "com.example.app"

    private lateinit var saved: Map<String, Any>

    @Before
    fun setUp() {
        saved = mapOf(
            "proximity" to DataInjection.switchOfProximity,
            "ongoing" to DataInjection.ongoingOptimize,
            "radical" to DataInjection.radicalOngoingOptimize,
            "silent" to DataInjection.ignoreSilentNotificationSwitch,
            "sleep" to DataInjection.sleepModeBoolean,
            "dnd" to DataInjection.dndDetectSwitch,
            "charging" to DataInjection.chargingOnlySwitch,
            "mode" to DataInjection.modeOfCurrent,
        )

        // A neutral chain: every gate that is not under test is disarmed, so a
        // BLOCK can only come from the one the test is about.
        DataInjection.switchOfProximity = false
        DataInjection.sleepModeBoolean = false
        DataInjection.dndDetectSwitch = false
        DataInjection.chargingOnlySwitch = false
        DataInjection.modeOfCurrent = CurrentMode.MODE_ALL_NOTIFY
        DataInjection.ongoingOptimize = true
        DataInjection.radicalOngoingOptimize = true
        DataInjection.ignoreSilentNotificationSwitch = false
    }

    @After
    fun tearDown() {
        DataInjection.switchOfProximity = saved["proximity"] as Boolean
        DataInjection.ongoingOptimize = saved["ongoing"] as Boolean
        DataInjection.radicalOngoingOptimize = saved["radical"] as Boolean
        DataInjection.ignoreSilentNotificationSwitch = saved["silent"] as Boolean
        DataInjection.sleepModeBoolean = saved["sleep"] as Boolean
        DataInjection.dndDetectSwitch = saved["dnd"] as Boolean
        DataInjection.chargingOnlySwitch = saved["charging"] as Boolean
        DataInjection.modeOfCurrent = saved["mode"] as CurrentMode
    }

    // region issue 74 — silent notifications

    @Test
    fun silentNotificationPassesWhileTheSwitchIsOff() {
        val param = param(chat("Hello"), ChannelLogInfo(importance = 2 /* LOW */))
        assertEquals(ConditionState.SUCCESS, importance.provideResult(param))
        assertFalse(importance.isArmed())
    }

    @Test
    fun silentNotificationIsBlockedWhileTheSwitchIsOn() {
        DataInjection.ignoreSilentNotificationSwitch = true
        assertTrue(importance.isArmed())
        listOf(0 /* NONE */, 1 /* MIN */, 2 /* LOW */).forEach {
            assertEquals(
                "importance $it",
                ConditionState.BLOCK,
                importance.provideResult(param(chat("Hello"), ChannelLogInfo(importance = it))),
            )
        }
    }

    @Test
    fun anAudibleNotificationIsNeverBlocked() {
        DataInjection.ignoreSilentNotificationSwitch = true
        listOf(3 /* DEFAULT */, 4 /* HIGH */, 5 /* MAX */).forEach {
            assertEquals(
                "importance $it",
                ConditionState.SUCCESS,
                importance.provideResult(param(chat("Hello"), ChannelLogInfo(importance = it))),
            )
        }
    }

    @Test
    fun withoutAChannelTheNotificationPriorityDecides() {
        // The pre-Android 8 path, and the fallback whenever the ranking cannot
        // be read. ChannelLogInfo() carries the unresolved importance.
        DataInjection.ignoreSilentNotificationSwitch = true
        assertEquals(
            ConditionState.BLOCK,
            importance.provideResult(
                param(chat("Quiet", priority = Notification.PRIORITY_MIN), ChannelLogInfo())
            ),
        )
        assertEquals(
            ConditionState.SUCCESS,
            importance.provideResult(
                param(chat("Loud", priority = Notification.PRIORITY_DEFAULT), ChannelLogInfo())
            ),
        )
    }

    @Test
    fun theChainNamesTheImportanceGateAsTheBlocker() {
        DataInjection.ignoreSilentNotificationSwitch = true
        val result = ListenerManager.provideState(
            param(chat("Hello"), ChannelLogInfo(importance = 1 /* MIN */))
        )
        assertEquals(ConditionState.BLOCK, result.state)
        assertEquals(BlockReason.LOW_IMPORTANCE, result.blockingCondition)
    }

    @Test
    fun theImportanceGateHasItsOwnNodeInTheChainView() {
        assertTrue(BlockReason.LOW_IMPORTANCE in BlockChain.gateKeys())
        assertTrue(BlockReason.LOW_IMPORTANCE in BlockChain.liveGateKeys())
        val nodes = BlockChain.liveSnapshot(null, hasNotificationAccess = true).map { it.key }
        assertTrue(BlockReason.LOW_IMPORTANCE in nodes)
    }

    @Test
    fun theImportanceNodeIsGreyedOutWhileTheSwitchIsOff() {
        val stateOf = { key: String ->
            BlockChain.liveSnapshot(null, hasNotificationAccess = true)
                .first { it.key == key }.state
        }
        assertEquals(ChainNodeState.SKIPPED, stateOf(BlockReason.LOW_IMPORTANCE))

        DataInjection.ignoreSilentNotificationSwitch = true
        // Armed, but it takes a notification to know the verdict — the chain
        // view draws that as an outline rather than a pass or a block.
        assertEquals(ChainNodeState.DEPENDS, stateOf(BlockReason.LOW_IMPORTANCE))
    }

    // endregion

    // region the ongoing filter is unchanged

    @Test
    fun anUnclearableNotificationIsStillFilteredOut() {
        // Pins the behaviour the silent filter was added alongside: nothing about
        // the new gate changes which notifications the ongoing filters drop.
        val sbn = ongoingNotification("Call in progress")
        assertTrue(sbn.isOngoing)
        assertFalse(sbn.isClearable)
        assertEquals(ConditionState.BLOCK, ongoing.provideResult(sbn))

        DataInjection.ongoingOptimize = false
        DataInjection.radicalOngoingOptimize = false
        assertEquals(ConditionState.SUCCESS, ongoing.provideResult(sbn))
        assertFalse(ongoing.isArmed())
    }

    // endregion

    // region helpers

    private fun param(
        sbn: StatusBarNotification,
        channelInfo: ChannelLogInfo? = null,
    ) = ConditionParam(
        sbn = sbn,
        // Left null on purpose: InteractiveCondition cannot answer without a
        // PowerManager and passes, which is what lets the chain be exercised
        // while the test device's screen is obviously on.
        pm = null,
        appContext = null,
        channelInfo = channelInfo,
    )

    /** An ordinary, clearable message notification. */
    private fun chat(title: String, priority: Int = Notification.PRIORITY_DEFAULT) =
        wrap(
            Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setPriority(priority)
                .build()
        )

    /** A call, a download, a sync: ongoing and not clearable. */
    private fun ongoingNotification(title: String): StatusBarNotification {
        val notification = Notification.Builder(context)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .build()
        notification.flags =
            notification.flags or Notification.FLAG_ONGOING_EVENT or Notification.FLAG_NO_CLEAR
        return wrap(notification)
    }

    private fun wrap(notification: Notification) = StatusBarNotification(
        sender,
        sender,
        1,
        "tag",
        Process.myUid(),
        Process.myPid(),
        0,
        notification,
        Process.myUserHandle(),
        System.currentTimeMillis(),
    )

    // endregion
}
