package com.symeonchen.wakeupscreen.services.notification.conditions

import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.FilterListUtils


/**
 * Created by SymeonChen on 2020/6/25.
 * modified by balkce on 2024-04-23.
 */
class FilterListCondition : LimitedCondition.AbstractSbnCondition() {

    override fun provideResult(sbn: StatusBarNotification?): ConditionState {
        sbn ?: return ConditionState.BLOCK
        val map: HashMap<String, Int>
        when (DataInjection.modeOfCurrent) {
            CurrentMode.MODE_ALL_NOTIFY -> return ConditionState.SUCCESS
            CurrentMode.MODE_WHITE_LIST -> {
                map = FilterListUtils.getMapFromString(DataInjection.appWhiteListStringOfNotify)
                if (!map.containsKey(sbn.packageName)) {
                    return ConditionState.BLOCK
                }
            }
            CurrentMode.MODE_BLACK_LIST -> {
                map = FilterListUtils.getMapFromString(DataInjection.appBlackListStringOfNotify)
                if (map.containsKey(sbn.packageName)) {
                    return ConditionState.BLOCK
                }
            }
            CurrentMode.MODE_IGNORE_SILENT -> {
                int priority = sbn.getNotification().priority;
                if (priority == PRIORITY_LOW || priority == PRIORITY_MIN) {
                    return ConditionState.BLOCK
                }
            }
        }
        return ConditionState.SUCCESS
    }
}
