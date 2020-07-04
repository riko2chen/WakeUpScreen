package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.NotificationUtils


/**
 * Created by SymeonChen on 2020/6/25.
 */
class DndCondition : LimitedCondition.AppContextCondition() {

    override fun provideResult(application: Application?): ConditionState {
        if (DataInjection.dndDetectSwitch) {
            if (application != null && true != NotificationUtils(application).detectDnd()) {
                return ConditionState.BLOCK
            }
        }
        return ConditionState.SUCCESS
    }

}