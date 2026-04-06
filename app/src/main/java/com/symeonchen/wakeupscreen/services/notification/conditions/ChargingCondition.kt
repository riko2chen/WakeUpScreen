package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.symeonchen.wakeupscreen.services.notification.ConditionState
import com.symeonchen.wakeupscreen.services.notification.LimitedCondition
import com.symeonchen.wakeupscreen.utils.DataInjection


class ChargingCondition : LimitedCondition.AppContextCondition() {

    override fun provideResult(application: Application?): ConditionState {
        if (!DataInjection.chargingOnlySwitch) {
            return ConditionState.SUCCESS
        }
        val batteryStatus = application?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL
        return if (isCharging) ConditionState.SUCCESS else ConditionState.BLOCK
    }

}
