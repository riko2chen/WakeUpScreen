package com.symeonchen.wakeupscreen.utils

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

class PermissionState {
    companion object {
        fun hasNotificationListenerServiceEnabled(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            val appContext = context.applicationContext
            val packageNames = NotificationManagerCompat.getEnabledListenerPackages(appContext)
            if (packageNames.contains(appContext.packageName)) {
                return true
            }
            return false
        }


        fun openReadNotificationSetting(context: Context?) {
            if (context == null) {
                return
            }
            val appContext = context.applicationContext
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}