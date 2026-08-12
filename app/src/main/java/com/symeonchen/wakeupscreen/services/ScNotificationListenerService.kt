package com.symeonchen.wakeupscreen.services

import android.app.NotificationChannel
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

        val channel = channelOf(sbn)

        // A new message restarts the reminder streak whatever the outcome
        // below: if the screen does not light up now, the reminder is the only
        // thing that will bring it up later.
        ReminderEngine.onNotificationPosted(applicationContext, sbn)

        //Pre check for better performance
        if (ConditionState.BLOCK == preCheckStatusOpen()) {
            logNotification(sbn.packageName, LogStatus.BLOCKED, BlockReason.APP_SWITCH_OFF, channel)
            return
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager

        val result = ListenerManager.provideState(
            ConditionParam(sbn, pm, application)
        )

        if (result.state == ConditionState.BLOCK) {
            val conditionName = result.blockingCondition ?: ""
            if (conditionName == BlockReason.INTERACTIVE) {
                logNotification(sbn.packageName, LogStatus.SCREEN_ALREADY_ON, conditionName, channel)
            } else {
                logNotification(sbn.packageName, LogStatus.BLOCKED, conditionName, channel)
            }
            return
        }

        ScreenWakeUtils.wakeUpScreen(applicationContext, pm)

        logNotification(sbn.packageName, LogStatus.WAKED_UP, "", channel)
    }

    private fun safeActiveNotifications(): Array<StatusBarNotification>? {
        return try {
            activeNotifications
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** Resolves the channel a notification was posted to, for the log. */
    fun channelOf(sbn: StatusBarNotification): NotificationChannel? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null
        return try {
            val ranking = Ranking()
            currentRanking.getRanking(sbn.key, ranking)
            ranking.channel
        } catch (_: Exception) {
            null
        }
    }

    private fun logNotification(
        packageName: String,
        status: LogStatus,
        blockReason: String,
        channel: NotificationChannel?,
    ) {
        try {
            val channelInfo = channel.toLogInfo()
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