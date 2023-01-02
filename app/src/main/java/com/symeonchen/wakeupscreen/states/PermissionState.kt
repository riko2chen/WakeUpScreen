package com.symeonchen.wakeupscreen.states

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.blankj.utilcode.util.PermissionUtils
import com.symeonchen.wakeupscreen.model.SettingViewModel

/**
 * Created by SymeonChen on 2019-10-27.
 */
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

        fun openSendNotificationSetting(context: Context?, settingModel: SettingViewModel) {
            if (context == null) {
                return
            }
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                settingModel.permissionOfSendNotification.postValue(true)
                return
            }
            // Request Post Notification Permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionUtils.permission(
                    Manifest.permission.POST_NOTIFICATIONS
                )
                    .callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            settingModel.permissionOfSendNotification.postValue(true)
                        }

                        override fun onDenied() {
                            settingModel.permissionOfSendNotification.postValue(false)
                        }
                    })
                    .request()
            }
        }

        fun hasSendNotificationPermission(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
            return true
        }
    }
}