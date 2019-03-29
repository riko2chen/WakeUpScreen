package com.symeonchen.wakeupscreen.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.symeonchen.wakeupscreen.services.SCNotifivationListenerService

object NotificationStateHelper {
    fun isNotificationServiceOpen(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val appContext = context.applicationContext
        val pm = appContext.packageManager
        val state = pm.getComponentEnabledSetting(
            ComponentName(
                appContext, SCNotifivationListenerService::class.java
            )
        )
        if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return true
        }
        return false
    }

    fun closeNotificationService(context: Context?) {
        if (context == null) {
            return
        }
        val appContext = context
        val pm = appContext.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                appContext, SCNotifivationListenerService::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
    }

    fun openNotificationService(context: Context?) {
        if (context == null) {
            return
        }
        val appContext = context
        val pm = appContext.packageManager
        pm.setComponentEnabledSetting(
            ComponentName(
                appContext, SCNotifivationListenerService::class.java
            ),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }

}