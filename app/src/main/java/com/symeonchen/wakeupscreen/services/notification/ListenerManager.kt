package com.symeonchen.wakeupscreen.services.notification

import com.symeonchen.wakeupscreen.services.notification.conditions.ChargingCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.DndCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.FilterListCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.InteractiveCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.OnGoingNotificationCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.PocketModeCondition
import com.symeonchen.wakeupscreen.services.notification.conditions.SleepModeCondition


data class ConditionCheckResult(
    val state: ConditionState,
    val blockingCondition: String? = null,
)

/**
 * Created by SymeonChen on 2020/6/24.
 */
object ListenerManager {

    /**
     * The gates a notification passes through, in evaluation order.
     *
     * This list is the single definition of that order: the chain view reads
     * it through [orderedKeys] rather than repeating it, so a condition added
     * here cannot silently go missing from the diagram. Order is meaningful —
     * [InteractiveCondition] sitting this early means nothing below it is ever
     * evaluated while the screen is on.
     *
     * It is built here rather than registered by the listener service so that
     * the UI can describe the chain even in a process where the service has
     * never been constructed.
     */
    private val mConditionList = mutableListOf<LimitedCondition>(
        PocketModeCondition(),
        InteractiveCondition(),
        FilterListCondition(),
        OnGoingNotificationCondition(),
        SleepModeCondition(),
        DndCondition(),
        ChargingCondition(),
    )

    /** The chain in evaluation order. */
    @Synchronized
    fun conditions(): List<LimitedCondition> = mConditionList.toList()

    /** The [BlockReason] keys of [conditions], in evaluation order. */
    @Synchronized
    fun orderedKeys(): List<String> = mConditionList.map { it.key }

    @Synchronized
    fun provideState(param: ConditionParam): ConditionCheckResult {
        for (condition in mConditionList) {
            if (calculateCondition(condition, param) == ConditionState.BLOCK) {
                return ConditionCheckResult(
                    ConditionState.BLOCK,
                    condition.key,
                )
            }
        }
        return ConditionCheckResult(ConditionState.SUCCESS)
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
