package com.symeonchen.wakeupscreen.services.notification


/**
 * Created by SymeonChen on 2020/6/24.
 */
sealed class ConditionState {
    object SUCCESS :
        ConditionState()

    object BLOCK :
        ConditionState()
}