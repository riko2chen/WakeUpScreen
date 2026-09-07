package com.symeonchen.wakeupscreen.services

import android.content.ComponentName
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ListenerManager
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.pages.NightGlowActivity
import com.symeonchen.wakeupscreen.services.reminder.ReminderEngine
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
    private val pendingNotificationJobs = mutableMapOf<String, Job>()

    override fun onCreate() {
        super.onCreate()
        instance = this
        // The attention statistic needs the unlock broadcast in the same
        // process that records the wakes; this service is that process.
        AttentionTracker.register(applicationContext)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        pendingNotificationJobs.clear()
        super.onDestroy()
        instance = null
        AttentionTracker.unregister(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Also covers the post-reboot case: the system rebinds the listener and
        // any reminder alarm that was lost with the restart is re-armed here.
        ReminderEngine.onListenerConnected(applicationContext, safeActiveNotifications())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        ReminderEngine.onNotificationRemoved(applicationContext, safeActiveNotifications())
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
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

        val gracePeriodMs = DataInjection.notificationGracePeriodMs
        if (gracePeriodMs <= 0L) {
            // Keep the historical path byte-for-byte in spirit: no coroutine,
            // no activeNotifications query, and no added latency.
            processNotification(sbn)
            return
        }

        val channelInfo = channelInfoOf(sbn)

        // The master switch stays the first gate. There is no reason to keep a
        // delayed job alive when the app is disabled already.
        if (ConditionState.BLOCK == preCheckStatusOpen()) {
            logNotification(
                sbn.packageName, LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF, channelInfo
            )
            return
        }

        val key = sbn.key
        val job = serviceScope.launch {
            try {
                delay(gracePeriodMs)

                // Re-check at execution time as well. The user may have
                // disabled the app while this notification was waiting, and
                // the master switch must remain the first gate in the chain.
                if (ConditionState.BLOCK == preCheckStatusOpen()) {
                    logNotification(
                        sbn.packageName,
                        LogStatus.BLOCKED,
                        BlockReason.APP_SWITCH_OFF,
                        channelInfo,
                    )
                    return@launch
                }

                val active = safeActiveNotifications()
                if (active == null) {
                    // Binder/OEM failures should not silently swallow a real
                    // notification. Fall back to the original posted object.
                    processNotification(sbn)
                    return@launch
                }

                val current = active.firstOrNull { it.key == key }
                if (current == null) {
                    logNotification(
                        sbn.packageName,
                        LogStatus.BLOCKED,
                        BlockReason.NOTIFICATION_DISMISSED,
                        channelInfo,
                    )
                    return@launch
                }

                // A notification may have been updated while the grace period
                // was running. Process the latest StatusBarNotification.
                processNotification(current, checkAppSwitch = false)
            } finally {
                if (pendingNotificationJobs[key] === coroutineContext[Job]) {
                    pendingNotificationJobs.remove(key)
                }
            }
        }

        // Re-posts for the same notification key replace the older pending
        // check so one logical notification can wake/log at most once.
        pendingNotificationJobs.put(key, job)?.cancel()
    }

    private fun processNotification(
        sbn: StatusBarNotification,
        checkAppSwitch: Boolean = true,
    ) {
        val channelInfo = channelInfoOf(sbn)

        // A new message restarts the reminder streak whatever the outcome
        // below: if the screen does not light up now, the reminder is the only
        // thing that will bring it up later.
        ReminderEngine.onNotificationPosted(applicationContext, sbn)

        //Pre check for better performance
        if (checkAppSwitch && ConditionState.BLOCK == preCheckStatusOpen()) {
            logNotification(
                sbn.packageName, LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF, channelInfo
            )
            return
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
            } else {
                logNotification(sbn.packageName, LogStatus.BLOCKED, conditionName, channelInfo)
            }
            return
        }

        ScreenWakeUtils.wakeUpScreen(applicationContext, pm)

        logNotification(sbn.packageName, LogStatus.WAKED_UP, "", channelInfo)
        AttentionTracker.onScreenWoken(sbn.packageName)
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
