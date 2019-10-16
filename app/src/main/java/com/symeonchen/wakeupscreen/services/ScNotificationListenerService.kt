package com.symeonchen.wakeupscreen.services

import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.FilterListUtils
import com.symeonchen.wakeupscreen.utils.NotifyDataUtils
import java.util.*
import kotlin.collections.HashMap

@Suppress("DEPRECATION")
class ScNotificationListenerService : NotificationListenerService() {

    private var currentWhiteListFlag: Long = 0L
    private var map = HashMap<String, Int>()

    private var lastNotificationId = 0
    private var lastNotificationOngoing = false


    companion object {
        private const val TAG_WAKE = "symeonchen:wakeupscreen"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
//        LogUtils.d("Received notification from: " + sbn?.packageName)
        sbn ?: return

        checkStatus()
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        checkPocketMode() ?: return
        checkDebugMode(sbn) ?: return
        checkIfInteractive(pm) ?: return
        checkFilterListMode(sbn) ?: return
        checkIfUpdateOnGoingNotification(sbn) ?: return
        checkIfInSleepModeTime() ?: return

        val wl = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            TAG_WAKE
        )
        val sec = DataInjection.milliSecondOfWakeUpScreen
        LogUtils.d("sec is $sec")
        wl.acquire((sec))
        wl.release()

    }

    /**
     * Check if service switch is open
     */
    private fun checkStatus(): Boolean? {
        val status = DataInjection.switchOfApp
        if (!status) {
            return null
        }
        return true
    }

    /**
     * Check if pocket mode is enable and active
     */
    private fun checkPocketMode(): Boolean? {
        val proximityStatus = DataInjection.statueOfProximity
        val proximitySwitch = DataInjection.switchOfProximity

        if (proximitySwitch && proximityStatus == 0) {
            return null
        }
        return true
    }

    /**
     * Check if open debug mode
     */
    private fun checkDebugMode(sbn: StatusBarNotification): Boolean? {
        if (DataInjection.switchOfDebugMode) {
            val time = System.currentTimeMillis()
            val notifyItem = NotifyItem()
            notifyItem.id = time
            notifyItem.time = time
            notifyItem.appName = sbn.packageName ?: ""
            NotifyDataUtils.addData(notifyItem)
        }
        return true
    }

    /**
     * Check if screen is interactive
     */
    private fun checkIfInteractive(pm: PowerManager): Boolean? {
        if (pm.isInteractive) {
            return null
        }
        return true
    }

    /**
     * Check if open whitelist mode
     */
    private fun checkFilterListMode(sbn: StatusBarNotification): Boolean? {
        when (DataInjection.modeOfCurrent) {
            CurrentMode.MODE_ALL_NOTIFY -> return true
            CurrentMode.MODE_WHITE_LIST -> {
                if (currentWhiteListFlag != DataInjection.appListUpdateFlag) {
                    currentWhiteListFlag = DataInjection.appListUpdateFlag
                    map = FilterListUtils.getMapFromString(DataInjection.appWhiteListStringOfNotify)
                }
                LogUtils.d(TAG_WAKE, sbn.packageName)
                LogUtils.d(DataInjection.appWhiteListStringOfNotify)
                if (!map.containsKey(sbn.packageName)) {
                    return null
                }
            }
            CurrentMode.MODE_BLACK_LIST -> {
                if (currentWhiteListFlag != DataInjection.appListUpdateFlag) {
                    currentWhiteListFlag = DataInjection.appListUpdateFlag
                    map = FilterListUtils.getMapFromString(DataInjection.appBlackListStringOfNotify)
                }
                LogUtils.d(TAG_WAKE, sbn.packageName)
                LogUtils.d(DataInjection.appBlackListStringOfNotify)
                if (map.containsKey(sbn.packageName)) {
                    return null
                }
            }
        }
        return true

    }

    /**
     *  Check if filter ongoing notification
     */
    private fun checkIfUpdateOnGoingNotification(sbn: StatusBarNotification): Boolean? {
        if (DataInjection.ongoingOptimize) {
            if (lastNotificationId == sbn.id) {
                if (lastNotificationOngoing && sbn.isOngoing) {
                    lastNotificationId = sbn.id
                    lastNotificationOngoing = sbn.isOngoing
                    return null
                }
            }
            lastNotificationId = sbn.id
            lastNotificationOngoing = sbn.isOngoing
        }
        return true
    }

    private fun checkIfInSleepModeTime(): Boolean? {
        if (DataInjection.sleepModeBoolean) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            LogUtils.d("currentHour is $currentHour")
            if (currentHour in 2..4) {
                return null
            }
        }
        return true
    }

}