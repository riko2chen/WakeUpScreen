package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.NotificationUtils


/**
 * Created by SymeonChen on 2020/6/25.
 */
class DndCondition : LimitedCondition.AppContextCondition() {

    override val key = BlockReason.DND

    override fun provideResult(application: Application?): ConditionState {
        if (DataInjection.dndDetectSwitch) {
            if (application != null && true != NotificationUtils(application).detectDnd()) {
                return ConditionState.BLOCK
            }
        }
        return ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean = DataInjection.dndDetectSwitch

    override fun wouldBlockNow(application: Application?): Boolean? {
        if (!DataInjection.dndDetectSwitch) {
            return false
        }
        application ?: return null
        return true != NotificationUtils(application).detectDnd()
    }

}