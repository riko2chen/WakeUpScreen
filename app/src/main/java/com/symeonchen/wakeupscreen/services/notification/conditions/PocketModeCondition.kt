package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection


/**
 * Created by SymeonChen on 2020/6/24.
 */
class PocketModeCondition : LimitedCondition.NoParamCondition() {

    override val key = BlockReason.POCKET_MODE

    /**
     * Check if pocket mode is enable and active
     */
    override fun provideResult(): ConditionState {
        if (isCovered()) {
            return ConditionState.BLOCK
        }
        return ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean = DataInjection.switchOfProximity

    override fun wouldBlockNow(application: Application?): Boolean = isCovered()

    /** Armed and the proximity sensor currently reads "covered". */
    private fun isCovered(): Boolean =
        DataInjection.switchOfProximity && DataInjection.statueOfProximity == 0


}