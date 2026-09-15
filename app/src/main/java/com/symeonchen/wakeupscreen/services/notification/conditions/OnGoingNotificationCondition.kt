package com.symeonchen.wakeupscreen.services.notification.conditions

import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.services.notification.OngoingNotificationPolicy
import com.symeonchen.wakeupscreen.services.notification.PlaybackNotification
import com.symeonchen.wakeupscreen.utils.DataInjection


/**
 * Created by SymeonChen on 2020/6/25.
 */
class OnGoingNotificationCondition : LimitedCondition.AbstractSbnCondition() {

    override val key = BlockReason.ONGOING

    override fun provideResult(sbn: StatusBarNotification?): ConditionState {
        val notification = sbn?.notification
        val blocked = OngoingNotificationPolicy.shouldBlock(
            isOngoing = sbn?.isOngoing == true,
            isClearable = sbn?.isClearable != false,
            isPlayback = PlaybackNotification.isPlayback(notification),
            blockOngoing = DataInjection.ongoingOptimize,
            blockNonClearable = DataInjection.radicalOngoingOptimize,
        )
        return if (blocked) ConditionState.BLOCK else ConditionState.SUCCESS
    }

    override fun isArmed(): Boolean =
        DataInjection.ongoingOptimize || DataInjection.radicalOngoingOptimize

    // wouldBlockNow stays null: the verdict depends on the notification's own
    // ongoing / clearable flags and whether it is a now-playing card.
}
