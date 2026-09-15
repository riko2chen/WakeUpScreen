package com.symeonchen.wakeupscreen.services.notification.conditions

import android.service.notification.StatusBarNotification
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.hasMediaContent


/**
 * Created by SymeonChen on 2020/6/25.
 */
class OnGoingNotificationCondition : LimitedCondition.AbstractSbnCondition() {

    override val key = BlockReason.ONGOING

    override fun provideResult(sbn: StatusBarNotification?): ConditionState {
        sbn ?: return ConditionState.SUCCESS
        return if (OngoingNotificationPolicy.shouldBlock(
                ongoingFilterEnabled = DataInjection.ongoingOptimize,
                radicalFilterEnabled = DataInjection.radicalOngoingOptimize,
                isOngoing = sbn.isOngoing,
                isClearable = sbn.isClearable,
                isMedia = sbn.notification.hasMediaContent(),
            )) {
            ConditionState.BLOCK
        } else {
            ConditionState.SUCCESS
        }
    }

    override fun isArmed(): Boolean =
        DataInjection.ongoingOptimize || DataInjection.radicalOngoingOptimize

    // wouldBlockNow stays null: the verdict depends on the notification's own
    // ongoing, clearable and media-session fields.
}

/** Notification-type policy separated from Android objects for unit tests. */
object OngoingNotificationPolicy {
    fun shouldBlock(
        ongoingFilterEnabled: Boolean,
        radicalFilterEnabled: Boolean,
        isOngoing: Boolean,
        isClearable: Boolean,
        isMedia: Boolean,
    ): Boolean =
        (radicalFilterEnabled && !isClearable) ||
                (ongoingFilterEnabled && (isOngoing || isMedia))
}
