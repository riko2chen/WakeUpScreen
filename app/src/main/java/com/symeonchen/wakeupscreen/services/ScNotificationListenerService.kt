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

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn ?: return

        val channelInfo = channelInfoOf(sbn)

        // A new message restarts the reminder streak whatever the outcome
        // below: if the screen does not light up now, the reminder is the only
        // thing that will bring it up later.
        ReminderEngine.onNotificationPosted(applicationContext, sbn)

        //Pre check for better performance
        if (ConditionState.BLOCK == preCheckStatusOpen()) {
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
            } else {
                logNotification(sbn.packageName, LogStatus.BLOCKED, conditionName, channelInfo)
            }
            return
        }

        ScreenWakeUtils.wakeUpScreen(applicationContext, pm)

        logNotification(sbn.packageName, LogStatus.WAKED_UP, "", channelInfo)
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