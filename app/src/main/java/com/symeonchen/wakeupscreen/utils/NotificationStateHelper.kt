package com.symeonchen.wakeupscreen.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.symeonchen.wakeupscreen.NotificationMonitor

class NotificationStateHelper {
    companion object {
        fun isNotificationServiceOpen(context: Context): Boolean {
            val pm = context.packageManager
            val state = pm.getComponentEnabledSetting(
                ComponentName(
                    context, NotificationMonitor::class.java
                )
            )
            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return true
            }
            return false
        }

        fun closeNotificationService(context: Context) {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    context, NotificationMonitor::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
        }

        fun openNotificationService(context: Context) {
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(
                    context, NotificationMonitor::class.java
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
        }

    }
}