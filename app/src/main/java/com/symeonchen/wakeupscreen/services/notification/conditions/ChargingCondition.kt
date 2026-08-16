package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.symeonchen.wakeupscreen.services.notification.BlockReason
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection


class ChargingCondition : LimitedCondition.AppContextCondition() {

    override val key = BlockReason.CHARGING

    override fun provideResult(application: Application?): ConditionState {
        if (!DataInjection.chargingOnlySwitch) {
            return ConditionState.SUCCESS
        }
        return if (isCharging(application)) ConditionState.SUCCESS else ConditionState.BLOCK
    }

    override fun isArmed(): Boolean = DataInjection.chargingOnlySwitch

    override fun wouldBlockNow(application: Application?): Boolean? {
        if (!DataInjection.chargingOnlySwitch) {
            return false
        }
        application ?: return null
        return !isCharging(application)
    }

    private fun isCharging(application: Application?): Boolean {
        val batteryStatus = application?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL
    }

}
