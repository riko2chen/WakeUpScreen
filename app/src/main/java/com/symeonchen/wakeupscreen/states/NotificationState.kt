package com.symeonchen.wakeupscreen.states

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.symeonchen.wakeupscreen.services.ScNotificationListenerService

class NotificationState {
    companion object {

        fun isNotificationServiceOpen(context: Context?): Boolean {
            context ?: return false
            val appContext = context.applicationContext
            val pm = appContext.packageManager
            val state = pm.getComponentEnabledSetting(
                ComponentName(
                    appContext, ScNotificationListenerService::class.java
                )
            )
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return true
            }
            return false
        }

        fun closeNotificationService(context: Context?) {
            context ?: return
            val appContext = context.applicationContext
            val pm = appContext.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    appContext, ScNotificationListenerService::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        }

        fun openNotificationService(context: Context?) {
            context ?: return
            val appContext = context.applicationContext
            val pm = appContext.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    appContext, ScNotificationListenerService::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        }

        fun restartNotificationService(context: Context?) {
            context ?: return
            closeNotificationService(context)
            openNotificationService(context)
        }
    }
}