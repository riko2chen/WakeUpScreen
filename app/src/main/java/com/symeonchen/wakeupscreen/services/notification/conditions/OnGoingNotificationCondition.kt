package com.symeonchen.wakeupscreen.services.notification.conditions

import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection


/**
 * Created by SymeonChen on 2020/6/25.
 */
class OnGoingNotificationCondition : LimitedCondition.AbstractSbnCondition() {

    override val key = BlockReason.ONGOING

    override fun provideResult(sbn: StatusBarNotification?): ConditionState {
        if (DataInjection.radicalOngoingOptimize) {
            if (sbn != null && !sbn.isClearable) {
                return ConditionState.BLOCK
            }
        }
        if (DataInjection.ongoingOptimize) {
            if (sbn != null && sbn.isOngoing) {
                return ConditionState.BLOCK
            }
        }
        return ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean =
        DataInjection.ongoingOptimize || DataInjection.radicalOngoingOptimize

    // wouldBlockNow stays null: the verdict depends on the notification's own
    // ongoing / clearable flags.
}