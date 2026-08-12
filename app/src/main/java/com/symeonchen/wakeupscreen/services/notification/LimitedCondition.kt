package com.symeonchen.wakeupscreen.services.notification

import android.app.Application
import android.os.PowerManager
import android.service.notification.StatusBarNotification


/**
 * Created by SymeonChen on 2020/6/25.
 */
sealed class LimitedCondition {
    /** Stable identifier ([BlockReason]) reported when this condition blocks. */
    abstract val key: String

    /**
     * Whether the user has this gate switched on at all.
     *
     * A disarmed condition can never block, so the chain view greys it out
     * instead of implying it took part in the decision.
     */
    open fun isArmed(): Boolean = true

    /**
     * Whether this condition would block a notification arriving right now.
     *
     * `null` means the answer depends on the notification itself — which app
     * posted it, whether it is ongoing — and cannot be known without one.
     * Conditions that only read settings and device state answer concretely.
     */
    open fun wouldBlockNow(application: Application?): Boolean? = null

    abstract class AbstractPmCondition : LimitedCondition() {
        abstract fun provideResult(pm: PowerManager?): ConditionState
    }

    abstract class AbstractSbnCondition : LimitedCondition() {
        abstract fun provideResult(sbn: StatusBarNotification?): ConditionState
    }

    abstract class AppContextCondition : LimitedCondition() {
        abstract fun provideResult(application: Application?): ConditionState
    }

    abstract class NoParamCondition : LimitedCondition() {
        abstract fun provideResult(): ConditionState
    }

    /**
     * For gates that need more than one of the delivery facts at once — the
     * notification plus something the caller resolved about it, such as its
     * channel or whether it announces a new track.
     */
    abstract class ParamCondition : LimitedCondition() {
        abstract fun provideResult(param: ConditionParam): ConditionState
    }
}
