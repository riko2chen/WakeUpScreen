package com.symeonchen.wakeupscreen.services

import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.utils.DataInjection

@Suppress("DEPRECATION")
class ScNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG_WAKE = "symeonchen:wakeupscreen"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        LogUtils.d("Received notification from: " + sbn?.packageName)
        val status = DataInjection.switchOfApp
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

        val proximityStatus = DataInjection.statueOfProximity
        val proximitySwitch = DataInjection.switchOfProximity

        if (proximitySwitch && proximityStatus == 0) {
            return
        }
        if (DataInjection.switchOfDebugMode) {
            // todo add behavior of debug record
        }

        val wl = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            TAG_WAKE
        )
        val sec = DataInjection.milliSecondOfWakeUpScreen
        wl.acquire((sec * 1000))
        wl.release()

    }

}