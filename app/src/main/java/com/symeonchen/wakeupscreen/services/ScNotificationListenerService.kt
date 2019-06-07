package com.symeonchen.wakeupscreen.services

import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.NotifyDataUtils
import com.symeonchen.wakeupscreen.utils.WhiteListUtils

@Suppress("DEPRECATION")
class ScNotificationListenerService : NotificationListenerService() {

    private var currentWhiteListFlag: Long = 0L
    private var map = HashMap<String, Int>()


    companion object {
        private const val TAG_WAKE = "symeonchen:wakeupscreen"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
//        LogUtils.d("Received notification from: " + sbn?.packageName)
        sbn ?: return
        val status = DataInjection.switchOfApp
        if (!status) {
            return
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager


        val proximityStatus = DataInjection.statueOfProximity
        val proximitySwitch = DataInjection.switchOfProximity

        if (proximitySwitch && proximityStatus == 0) {
            return
        }

        if (DataInjection.switchOfDebugMode) {
            val time = System.currentTimeMillis()
            val notifyItem = NotifyItem()
            notifyItem.id = time
            notifyItem.time = time
            notifyItem.appName = sbn.packageName ?: ""
            NotifyDataUtils.addData(notifyItem)
        }

        if (pm.isInteractive) {
            return
        }

        if (DataInjection.modeOfCurrent == CurrentMode.MODE_WHITE_LIST) {
            if (currentWhiteListFlag != DataInjection.appListUpdateFlag) {
                currentWhiteListFlag = DataInjection.appListUpdateFlag
                map = WhiteListUtils.getMapFromString(DataInjection.appListStringOfNotify)
            }
            LogUtils.d(TAG_WAKE, sbn.packageName)
            LogUtils.d(DataInjection.appListStringOfNotify)
            if (!map.containsKey(sbn.packageName)) {
                return
            }

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