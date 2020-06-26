package com.symeonchen.wakeupscreen.services.notification

import android.app.Application
import android.os.PowerManager
import android.service.notification.StatusBarNotification


/**
 * Created by SymeonChen on 2020/6/25.
 */
sealed class LimitedCondition {
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
}

