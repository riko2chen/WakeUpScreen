package com.symeonchen.wakeupscreen.services

import android.content.ComponentName
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor
import com.symeonchen.wakeupscreen.services.bluetooth.BluetoothStartupBatch
import com.symeonchen.wakeupscreen.services.bluetooth.BluetoothStartupQueue
import com.symeonchen.wakeupscreen.services.notification.NotificationGraceQueue
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ListenerManager
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.pages.NightGlowActivity
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine
import com.symeonchen.wakeupscreen.states.FaceDownSensorState
import com.symeonchen.wakeupscreen.states.ProximitySensorState
import com.symeonchen.wakeupscreen.utils.ChannelLogInfo
import com.symeonchen.wakeupscreen.utils.ScreenWakeUtils
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.toLogInfo
import kotlinx.coroutines.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("DEPRECATION")
class ScNotificationListenerService : NotificationListenerService() {

    companion object {
        private val TAG = this::class.java.simpleName
        @Volatile var instance: ScNotificationListenerService? = null
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private data class PendingNotification(
        val sbn: StatusBarNotification,
        val channelInfo: ChannelLogInfo,
    )

    // All queue operations, including expiry, run on Main (also on API 23).
    private val pendingNotifications = NotificationGraceQueue<PendingNotification>(
        serviceScope,
        ::processAfterGracePeriod,
    )

    private val bluetoothStartupQueue = BluetoothStartupQueue<PendingNotification>(
        serviceScope,
        { BluetoothConnectionMonitor.startupRetryDelayMillis(applicationContext) > 0L },
        ::processAfterBluetoothStartup,
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor.acquire(applicationContext, this)
        // The attention statistic needs the unlock broadcast in the same
        // process that records the wakes; this service is that process.
        AttentionTracker.register(applicationContext)
        syncPostureSensors()
    }

    override fun onDestroy() {
        ProximitySensorState.unRegisterListener(applicationContext)
        FaceDownSensorState.unRegisterListener(applicationContext)
        com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor.release(this)
        bluetoothStartupQueue.clear()
        serviceScope.cancel()
        pendingNotifications.clear()
        super.onDestroy()
        instance = null
        AttentionTracker.unregister(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // The system can recreate and rebind this service without opening an
        // activity. Restore both posture guards before accepting notifications.
        syncPostureSensors()
        com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor.acquire(applicationContext, this)
        // Also covers the post-reboot case: the system rebinds the listener and
        // any reminder alarm that was lost with the restart is re-armed here.
        ReminderEngine.onListenerConnected(applicationContext, safeActiveNotifications())
    }

    private fun syncPostureSensors() {
        if (DataInjection.switchOfProximity) {
            ProximitySensorState.registerListener(applicationContext)
        } else if (ProximitySensorState.isRegistered()) {
            ProximitySensorState.unRegisterListener(applicationContext)
        }

        if (DataInjection.switchOfFaceDown) {
            FaceDownSensorState.registerListener(applicationContext)
        } else if (FaceDownSensorState.isRegistered()) {
            FaceDownSensorState.unRegisterListener(applicationContext)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        serviceScope.launch {
            sbn?.let { removed ->
                pendingNotifications.remove(removed.key)?.let(::logDismissedNotification)
                bluetoothStartupQueue.remove(removed.key)?.let(::logDismissedNotification)
            }
            ReminderEngine.onNotificationRemoved(applicationContext, safeActiveNotifications())
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor.release(this)
        serviceScope.launch {
            pendingNotifications.clear()
            bluetoothStartupQueue.clear()
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requestRebind(
                    ComponentName(
                        applicationContext, ScNotificationListenerService::class.java
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        serviceScope.launch { handleNotificationPosted(sbn) }
    }

    private fun handleNotificationPosted(sbn: StatusBarNotification) {
        val gracePeriodMs = DataInjection.notificationGracePeriodMs
        if (gracePeriodMs <= 0L) {
            // No wait or active-list query. Cancel an older check if the user
            // disabled the grace period before this update arrived.
            pendingNotifications.remove(sbn.key)
            processNotification(sbn)
            return
        }

        val channelInfo = channelInfoOf(sbn)
        if (ConditionState.BLOCK == preCheckStatusOpen()) {
            pendingNotifications.remove(sbn.key)
            logNotification(
                sbn.packageName, LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF, channelInfo
            )
            return
        }

        // Updates replace the payload only; the first post owns the deadline.
        // Once processed or removed, the same key can start a fresh wait.
        pendingNotifications.post(
            sbn.key, PendingNotification(sbn, channelInfo), gracePeriodMs
        )
    }

    private fun processAfterGracePeriod(pending: PendingNotification) {
        val sbn = pending.sbn
        if (ConditionState.BLOCK == preCheckStatusOpen()) {
            logNotification(
                sbn.packageName, LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF, pending.channelInfo
            )
            return
        }

        val active = safeActiveNotifications()
        if (active == null) {
            // Preserve fail-open behaviour, using the latest delivered update.
            processNotification(sbn)
            return
        }

        val current = active.firstOrNull { it.key == sbn.key }
        if (current == null) {
            logDismissedNotification(pending)
            return
        }
        processNotification(current, checkAppSwitch = false)
    }

    private fun logDismissedNotification(pending: PendingNotification) {
        // Keep the master switch first even if it changed during the wait.
        val reason = if (ConditionState.BLOCK == preCheckStatusOpen()) {
            BlockReason.APP_SWITCH_OFF
        } else {
            BlockReason.NOTIFICATION_DISMISSED
        }
        logNotification(
            pending.sbn.packageName, LogStatus.BLOCKED, reason, pending.channelInfo
        )
    }

    private fun processAfterBluetoothStartup(pending: List<PendingNotification>) {
        // The wait is not permission to wake. A dismissed notification, unavailable
        // listener, changed setting, or timed-out discovery must still fail closed.
        val active = safeActiveNotifications()
        if (active == null) {
            pending.forEach {
                logNotification(it.sbn.packageName, LogStatus.BLOCKED,
                    if (DataInjection.switchOfApp) BlockReason.BLUETOOTH else BlockReason.APP_SWITCH_OFF,
                    it.channelInfo)
            }
            return
        }
        // A startup burst produces at most one wake/glow. Older still-active
        // notifications remain available to the regular reminder policy.
        BluetoothStartupBatch.recheck(
            pending,
            current = { item -> active.firstOrNull { it.key == item.sbn.key }?.let { item.copy(sbn = it) } },
            dismissed = ::logDismissedNotification,
            tryWake = { processNotification(it.sbn, allowBluetoothWait = false) },
        )
    }

    private fun processNotification(
        sbn: StatusBarNotification,
        checkAppSwitch: Boolean = true,
        allowBluetoothWait: Boolean = true,
    ): Boolean {
        val channelInfo = channelInfoOf(sbn)
        if (allowBluetoothWait) {
            if (DataInjection.switchOfApp &&
                (bluetoothStartupQueue.hasPending ||
                    BluetoothConnectionMonitor.startupRetryDelayMillis(applicationContext) > 0L)) {
                bluetoothStartupQueue.post(sbn.key, PendingNotification(sbn, channelInfo))
                return false
            }
            // A fresh update may arrive between profile discovery and the queue tick.
            bluetoothStartupQueue.remove(sbn.key)
        }

        // Join the shared reminder batch without postponing its pending deadline.
        ReminderEngine.onNotificationPosted(applicationContext, sbn)

        //Pre check for better performance
        if (checkAppSwitch && ConditionState.BLOCK == preCheckStatusOpen()) {
            logNotification(
                sbn.packageName, LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF, channelInfo
            )
            return false
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager

        val result = ListenerManager.provideState(
            ConditionParam(sbn, pm, application, channelInfo)
        )

        if (result.state == ConditionState.BLOCK) {
            val conditionName = result.blockingCondition ?: ""
            if (conditionName == BlockReason.INTERACTIVE) {
                logNotification(
                    sbn.packageName, LogStatus.SCREEN_ALREADY_ON, conditionName, channelInfo
                )
            } else if (conditionName == BlockReason.SLEEP_MODE && DataInjection.nightGlowSwitch) {
                // Sleep window plus night glow: not a full wake, not silence —
                // the dim red pulse, logged under its own status.
                NightGlowActivity.start(applicationContext)
                logNotification(sbn.packageName, LogStatus.NIGHT_GLOW, conditionName, channelInfo)
                return true
            } else {
                logNotification(sbn.packageName, LogStatus.BLOCKED, conditionName, channelInfo)
            }
            return false
        }

        ScreenWakeUtils.wakeUpScreen(applicationContext, pm)

        logNotification(sbn.packageName, LogStatus.WAKED_UP, "", channelInfo)
        AttentionTracker.onScreenWoken(sbn.packageName)
        return true
    }

    private fun safeActiveNotifications(): Array<StatusBarNotification>? {
        return try {
            activeNotifications
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * What the system decided about this notification: the importance it was
     * ranked with, plus the sound and vibration settings of the channel behind
     * it.
     *
     * The importance comes from the ranking rather than from `channel.importance`
     * because the ranking is the value the system applied to *this* notification,
     * which is the question the silent filter is asking. In the ordinary case the
     * two agree; where they differ the ranking is the one that decided how the
     * notification was actually treated. It also answers on Android 7, where
     * there is no channel to read at all.
     *
     * Worth knowing what this still cannot see: an OEM per-app "silent" switch
     * may lower a notification's effective importance without that showing up in
     * either the ranking or the channel — measured on One UI 5, where `dumpsys
     * notification` reports the record at LOW while both APIs a listener can
     * reach still say DEFAULT. Notifications silenced that way are invisible to
     * this filter, and no amount of reading the channel would have helped.
     *
     * The channel is still what says whether a sound or vibration is configured,
     * so both are read from the same [Ranking].
     */
    fun channelInfoOf(sbn: StatusBarNotification): ChannelLogInfo {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return ChannelLogInfo()
        return try {
            val ranking = Ranking()
            if (!currentRanking.getRanking(sbn.key, ranking)) {
                return ChannelLogInfo()
            }
            val channelInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ranking.channel.toLogInfo()
            } else {
                ChannelLogInfo()
            }
            channelInfo.copy(importance = ranking.importance)
        } catch (_: Exception) {
            ChannelLogInfo()
        }
    }

    private fun logNotification(
        packageName: String,
        status: LogStatus,
        blockReason: String,
        channelInfo: ChannelLogInfo,
    ) {
        try {
            NotificationLogStore.addLog(
                NotificationLogEntry(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    status = status,
                    blockReason = blockReason,
                    importance = channelInfo.importance,
                    hasSound = channelInfo.hasSound,
                    hasVibration = channelInfo.hasVibration,
                )
            )
        } catch (_: Exception) {
        }
    }

    /**
     * Check if service switch is open
     */
    private fun preCheckStatusOpen(): ConditionState? {
        val status = DataInjection.switchOfApp
        if (!status) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }

}
