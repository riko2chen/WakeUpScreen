package com.symeonchen.wakeupscreen.services.notification.conditions

import android.os.PowerManager
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition


/**
 * Created by SymeonChen on 2020/6/25.
 */
class InteractiveCondition : LimitedCondition.AbstractPmCondition() {

    /**
     * Check if screen is interactive
     */
    override fun provideResult(pm: PowerManager?): ConditionState {
        if (true == pm?.isInteractive) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }
}