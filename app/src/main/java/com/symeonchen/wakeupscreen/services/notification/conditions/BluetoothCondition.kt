package com.symeonchen.wakeupscreen.services.notification.conditions

import android.app.Application
import com.symeonchen.wakeupscreen.services.bluetooth.BluetoothConnectionMonitor
import com.symeonchen.wakeupscreen.services.notification.*
import com.symeonchen.wakeupscreen.utils.DataInjection

class BluetoothCondition : LimitedCondition.AppContextCondition() {
    override val key = BlockReason.BLUETOOTH
    override fun isArmed() = DataInjection.bluetoothWakeSwitch
    override fun wouldBlockNow(application: Application?): Boolean {
        if (!isArmed()) return false
        return application == null || !BluetoothConnectionMonitor.snapshot(application)
            .allows(true, DataInjection.bluetoothWakeDevices)
    }
    override fun provideResult(application: Application?) =
        if (wouldBlockNow(application)) ConditionState.BLOCK else ConditionState.SUCCESS
}
