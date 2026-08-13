package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.data.SleepSchedule
import com.symeonchen.wakeupscreen.data.Weekdays
import com.symeonchen.wakeupscreen.utils.DataInjection
import java.util.*


/**
 * Created by SymeonChen on 2020/6/25.
 */
class SleepModeCondition : LimitedCondition.NoParamCondition() {

    override val key = BlockReason.SLEEP_MODE

    override fun provideResult(): ConditionState {
        if (inSleepWindow()) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean = DataInjection.sleepModeBoolean

    override fun wouldBlockNow(application: Application?): Boolean = inSleepWindow()

    /** Armed and the clock currently sits inside one of the configured windows. */
    private fun inSleepWindow(): Boolean {
        if (!DataInjection.sleepModeBoolean) {
            return false
        }
        val now = Calendar.getInstance()
        val isoDay = Weekdays.fromCalendar(now.get(Calendar.DAY_OF_WEEK))
        val minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return SleepSchedule.contains(DataInjection.sleepModeSegments, isoDay, minuteOfDay)
    }
}