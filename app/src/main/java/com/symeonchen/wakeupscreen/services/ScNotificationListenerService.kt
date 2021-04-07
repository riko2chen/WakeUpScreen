package com.symeonchen.wakeupscreen.services

import android.content.ComponentName
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.ListenerManager
import com.symeonchen.wakeupscreen.services.notification.conditions.*
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.NotificationUtils

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
        val notificationBuilder = NotificationUtils(this).getNotification(
            resources.getString(R.string.app_name),
            resources.getString(R.string.running_to_prevent_kill_app)
        )
        startForeground(1, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }


    init {
        ListenerManager.register(PocketModeCondition())
            .register(InteractiveCondition())
            .register(FilterListCondition())
            .register(OnGoingNotificationCondition())
            .register(SleepModeCondition())
            .register(DndCondition())
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

        //Pre check for better performance
        if (ConditionState.BLOCK == preCheckStatusOpen()) return

        val pm = getSystemService(POWER_SERVICE) as PowerManager


        if (ConditionState.BLOCK == ListenerManager.provideState(
                ConditionParam(
                    sbn,
                    pm,
                    application
                )
            )
        ) return

        val wl = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            TAG_WAKE
        )
        val sec = DataInjection.milliSecondOfWakeUpScreen

        wl.acquire((sec))
        wl.release()

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