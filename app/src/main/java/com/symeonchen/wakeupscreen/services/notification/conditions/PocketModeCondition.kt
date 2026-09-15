package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.states.ProximitySensorState
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

    /**
     * Armed and either the proximity sensor currently reads "covered", or it
     * has not spoken yet. The second case is fail-closed on purpose: a
     * streaming notification that races the first wakeup-sensor event would
     * otherwise light the lock screen inside a pocket.
     *
     * Devices with no proximity hardware never register a listener, so they
     * keep the historical fail-open path (the stored default is "far").
     */
    private fun isCovered(): Boolean {
        if (!DataInjection.switchOfProximity) {
            return false
        }
        if (ProximitySensorState.isRegistered() && !ProximitySensorState.hasReading()) {
            return true
        }
        return DataInjection.statueOfProximity == 0
    }
}
