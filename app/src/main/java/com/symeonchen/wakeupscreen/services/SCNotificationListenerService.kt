package com.symeonchen.wakeupscreen.services

import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.SCConstant
import com.tencent.mmkv.MMKV

@Suppress("DEPRECATION")
class SCNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG_WAKE = "symeonchen:wakeupscreen"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        LogUtils.d("Received notification from: " + sbn?.packageName)
        val status = MMKV.defaultMMKV().getBoolean(SCConstant.CUSTOM_STATUS, false)
        if (!status) {
            return
        }
        if (sbn?.packageName == "com.android.systemui") {
            return
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isInteractive) {
            return
        }
        var wl = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            TAG_WAKE
        )
        val sec = MMKV.defaultMMKV().getInt(SCConstant.WAKE_SCREEN_SECOND, 2)
        wl.acquire((sec * 1000).toLong())
        wl.release()

    }

}