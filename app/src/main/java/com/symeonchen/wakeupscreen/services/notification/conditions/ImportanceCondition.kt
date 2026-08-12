package com.symeonchen.wakeupscreen.services.notification.conditions

import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionParam
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.ImportancePolicy
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection

/**
 * Drops notifications the system itself treats as silent.
 *
 * A silent notification lights up the screen and shows nothing: no sound, no
 * heads-up, often no lock-screen content either. Filtering by app cannot fix
 * that, because the same app sends both kinds — the chatty channel and the
 * "sync complete" one. Filtering by importance can.
 *
 * The rule lives in [ImportancePolicy]; this only decides whether to apply it.
 */
class ImportanceCondition : LimitedCondition.ParamCondition() {

    override val key = BlockReason.LOW_IMPORTANCE

    @Suppress("DEPRECATION") // Notification.priority: the only signal before Android 8.
    override fun provideResult(param: ConditionParam): ConditionState {
        if (!DataInjection.ignoreSilentNotificationSwitch) {
            return ConditionState.SUCCESS
        }
        val importance = param.channelInfo?.importance ?: ImportancePolicy.IMPORTANCE_UNRESOLVED
        val priority = try {
            param.sbn?.notification?.priority
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return if (ImportancePolicy.isSilent(importance, priority)) {
            ConditionState.BLOCK
        } else {
            ConditionState.SUCCESS
        }
    }

    override fun isArmed(): Boolean = DataInjection.ignoreSilentNotificationSwitch

    // wouldBlockNow stays null: the verdict depends on the channel the
    // notification was posted to, which no amount of device state reveals.
}
