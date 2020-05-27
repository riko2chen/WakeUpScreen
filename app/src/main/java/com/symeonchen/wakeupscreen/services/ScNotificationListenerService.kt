package com.symeonchen.wakeupscreen.services

import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.blankj.utilcode.util.LogUtils
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by SymeonChen on 2019-10-27.
 */
@Suppress("DEPRECATION")
class ScNotificationListenerService : NotificationListenerService() {

    private var currentWhiteListFlag: Long = 0L
    private var map = HashMap<String, Int>()

    private var lastNotificationId = 0
    private var lastNotificationOngoing = false

    companion object {
        private const val TAG_WAKE = "symeonchen:wakeupscreen"
        private const val PASS = true
        private val BLOCK = null

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
        checkIfRadicalOnGoingNotificationFilterOpen(sbn) ?: return
        checkIfInSleepModeTime() ?: return
        checkIfInDnd() ?: return

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
            return BLOCK
        }
        return PASS
    }

    /**
     * Check if pocket mode is enable and active
     */
    private fun checkPocketMode(): Boolean? {
        val proximityStatus = DataInjection.statueOfProximity
        val proximitySwitch = DataInjection.switchOfProximity

        if (proximitySwitch && proximityStatus == 0) {
            return BLOCK
        }
        return PASS
    }

    /**
     * Check if open debug mode
     */
    private fun checkDebugMode(sbn: StatusBarNotification): Boolean? {
        if (DataInjection.switchOfDebugMode) {
            CoroutineScope(Dispatchers.IO).launch {
                val time = System.currentTimeMillis()
                val notifyItem = NotifyItem()
                notifyItem.id = time
                notifyItem.time = time
                notifyItem.appName = sbn.packageName ?: ""
                NotifyDataUtils.addData(notifyItem, application)
            }
        }
        return PASS
    }

    /**
     * Check if screen is interactive
     */
    private fun checkIfInteractive(pm: PowerManager): Boolean? {
        if (pm.isInteractive) {
            return BLOCK
        }
        return PASS
    }

    /**
     * Check if open whitelist mode
     */
    private fun checkFilterListMode(sbn: StatusBarNotification): Boolean? {
        when (DataInjection.modeOfCurrent) {
            CurrentMode.MODE_ALL_NOTIFY -> return PASS
            CurrentMode.MODE_WHITE_LIST -> {
                if (currentWhiteListFlag != DataInjection.appListUpdateFlag) {
                    currentWhiteListFlag = DataInjection.appListUpdateFlag
                    map = FilterListUtils.getMapFromString(DataInjection.appWhiteListStringOfNotify)
                }
                LogUtils.d(TAG_WAKE, sbn.packageName)
                LogUtils.d(DataInjection.appWhiteListStringOfNotify)
                if (!map.containsKey(sbn.packageName)) {
                    return BLOCK
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
                    return BLOCK
                }
            }
        }
        return PASS

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
                    return BLOCK
                }
            }
            lastNotificationId = sbn.id
            lastNotificationOngoing = sbn.isOngoing
        }
        return PASS
    }

    private fun checkIfRadicalOnGoingNotificationFilterOpen(sbn: StatusBarNotification): Boolean? {
        if (DataInjection.radicalOngoingOptimize) {
            if (sbn.isOngoing && !sbn.isClearable) {
                return BLOCK
            }
        }
        return PASS
    }

    private fun checkIfInSleepModeTime(): Boolean? {
        if (DataInjection.sleepModeBoolean) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val beginHour = DataInjection.sleepModeTimeBeginHour
            val endHour = DataInjection.sleepModeTimeEndHour
            if (TimeRangeCalculateUtils.hourInRange(currentHour, beginHour, endHour)) {
                return BLOCK
            }
        }
        return PASS
    }

    private fun checkIfInDnd(): Boolean? {
        if (DataInjection.dndDetectSwitch) {
            if (true != NotificationUtils(applicationContext).detectDnd()) {
                return BLOCK
            }
        }
        return PASS
    }
}