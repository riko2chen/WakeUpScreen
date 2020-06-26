package com.symeonchen.wakeupscreen.services.notification.conditions

import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection


/**
 * Created by SymeonChen on 2020/6/24.
 */
class PocketModeCondition : LimitedCondition.NoParamCondition() {

    /**
     * Check if pocket mode is enable and active
     */
    override fun provideResult(): ConditionState {
        val proximityStatus = DataInjection.statueOfProximity
        val proximitySwitch = DataInjection.switchOfProximity

        if (proximitySwitch && proximityStatus == 0) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }


}