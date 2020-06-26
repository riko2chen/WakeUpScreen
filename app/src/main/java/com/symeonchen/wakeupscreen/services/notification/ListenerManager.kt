package com.symeonchen.wakeupscreen.services.notification


/**
 * Created by SymeonChen on 2020/6/24.
 */
object ListenerManager {

    private var mConditionList = mutableListOf<LimitedCondition>()

    @Synchronized
    fun register(condition: LimitedCondition): ListenerManager {
        if (this.mConditionList.indexOf(condition) > 0) {
            return this
        }
        this.mConditionList.add(condition)
        return this
    }

    @Synchronized
    fun provideState(param: ConditionParam): ConditionState {
        for (condition in mConditionList) {
            if (calculateCondition(condition, param) == ConditionState.BLOCK) {
                return ConditionState.BLOCK
            }
        }
        return ConditionState.SUCCESS
    }

    private fun calculateCondition(
        condition: LimitedCondition, param: ConditionParam
    ): ConditionState {
        return when (condition) {
            is LimitedCondition.AbstractPmCondition -> {
                condition.provideResult(param.pm)
            }
            is LimitedCondition.AbstractSbnCondition -> {
                condition.provideResult(param.sbn)
            }

            is LimitedCondition.NoParamCondition -> {
                condition.provideResult()
            }
            is LimitedCondition.AppContextCondition -> {
                condition.provideResult(param.appContext)
            }
        }
    }

}