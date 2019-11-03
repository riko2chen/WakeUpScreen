package com.symeonchen.wakeupscreen.utils


/**
 * Created by SymeonChen on 2019-11-03.
 */
class TimeRangeCalculateUtils {

    companion object {
        fun hourInRange(currentHour: Int, beginHour: Int, endHour: Int): Boolean {
            if (beginHour > endHour) {
                if (currentHour in beginHour..23) {
                    return true
                }
                if (currentHour in 0..endHour) {
                    return true
                }
            }
            if (beginHour < endHour) {
                if (currentHour in beginHour..endHour) {
                    return true
                }
            }
            if (beginHour == endHour) {
                return true
            }
            return false
        }
    }
}