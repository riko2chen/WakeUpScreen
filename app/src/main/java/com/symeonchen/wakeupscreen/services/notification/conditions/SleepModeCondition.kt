package com.symeonchen.wakeupscreen.services.notification.conditions

import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.TimeRangeCalculateUtils
import java.util.*


/**
 * Created by SymeonChen on 2020/6/25.
 */
class SleepModeCondition : LimitedCondition.NoParamCondition() {
    override fun provideResult(): ConditionState {
        if (DataInjection.sleepModeBoolean) {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val beginHour = DataInjection.sleepModeTimeBeginHour
            val endHour = DataInjection.sleepModeTimeEndHour
            if (TimeRangeCalculateUtils.hourInRange(currentHour, beginHour, endHour)) {
                return ConditionState.BLOCK
            }
        }
        return ConditionState.SUCCESS
    }
}