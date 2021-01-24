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
    override fun provideResult(t: PowerManager?): ConditionState {
        if (true == t?.isInteractive) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }
}