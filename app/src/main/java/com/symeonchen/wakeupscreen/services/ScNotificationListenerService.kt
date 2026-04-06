package com.symeonchen.wakeupscreen.services

import android.app.NotificationChannel
import android.content.ComponentName
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ListenerManager
import com.symeonchen.wakeupscreen.data.LogStatus
import com.symeonchen.wakeupscreen.data.NotificationLogEntry
import com.symeonchen.wakeupscreen.data.NotificationLogStore
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.conditions.*
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.NotificationUtils
import kotlinx.coroutines.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("DEPRECATION")
class ScNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG_WAKE = "symeonchen:wakeupscreen"
        private val TAG = this::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        if (!DataInjection.persistentNotification) {
            return
        }
        val notificationBuilder = NotificationUtils(this).getNotification(
            resources.getString(R.string.app_name),
            resources.getString(R.string.running_to_prevent_kill_app)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notificationBuilder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }


    init {
        ListenerManager.register(PocketModeCondition())
            .register(InteractiveCondition())
            .register(FilterListCondition())
            .register(OnGoingNotificationCondition())
            .register(SleepModeCondition())
            .register(DndCondition())
            .register(ChargingCondition())
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

        val channel = getNotificationChannel(sbn)

        //Pre check for better performance
        if (ConditionState.BLOCK == preCheckStatusOpen()) {
            logNotification(sbn.packageName, LogStatus.BLOCKED, "app_switch_off", channel)
            return
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager

        val result = ListenerManager.provideState(
            ConditionParam(sbn, pm, application)
        )

        if (result.state == ConditionState.BLOCK) {
            val conditionName = result.blockingCondition ?: ""
            if (conditionName == InteractiveCondition::class.java.simpleName) {
                logNotification(sbn.packageName, LogStatus.SCREEN_ALREADY_ON, conditionName, channel)
            } else {
                logNotification(sbn.packageName, LogStatus.BLOCKED, conditionName, channel)
            }
            return
        }

        val wl = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            TAG_WAKE
        )
        val sec = DataInjection.milliSecondOfWakeUpScreen

        wl.acquire(sec)
        wl.release()

        logNotification(sbn.packageName, LogStatus.WAKED_UP, "", channel)
    }

    private fun getNotificationChannel(sbn: StatusBarNotification): NotificationChannel? {
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
            val importance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel != null) {
                channel.importance
            } else {
                -1
            }
            val hasSound = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel != null) {
                channel.sound != null && channel.sound.toString().isNotEmpty()
            } else {
                null
            }
            val hasVibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && channel != null) {
                channel.shouldVibrate()
            } else {
                null
            }
            NotificationLogStore.addLog(
                NotificationLogEntry(
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    status = status,
                    blockReason = blockReason,
                    importance = importance,
                    hasSound = hasSound,
                    hasVibration = hasVibration,
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