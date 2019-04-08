package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.SCConstant.CUSTOM_STATUS
import com.symeonchen.wakeupscreen.data.SCConstant.DEFAULT_SWITCH_OF_APP
import com.symeonchen.wakeupscreen.data.SCConstant.DEFAULT_SWITCH_OF_PROXIMITY
import com.symeonchen.wakeupscreen.data.SCConstant.DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS
import com.symeonchen.wakeupscreen.data.SCConstant.DEFAULT_VALUE_OF_PROXIMITY
import com.symeonchen.wakeupscreen.data.SCConstant.PROXIMITY_STATUS
import com.symeonchen.wakeupscreen.data.SCConstant.PROXIMITY_SWITCH
import com.symeonchen.wakeupscreen.data.SCConstant.WAKE_SCREEN_SECOND
import com.tencent.mmkv.MMKV

object DataInjection {

    fun getSwitchOfApp(): Boolean {
        return MMKV.defaultMMKV().getBoolean(CUSTOM_STATUS, DEFAULT_SWITCH_OF_APP)
    }

    fun setSwitchOfCApp(it: Boolean?) {
        MMKV.defaultMMKV().putBoolean(CUSTOM_STATUS, it == true)
    }


    fun getMilliSecondOfWakeUpScreen(): Long {
        return MMKV.defaultMMKV().getLong(WAKE_SCREEN_SECOND, DEFAULT_TIME_OF_WAKE_UP_SCREEN_MILLISECONDS)
    }

    fun setSecondOfWakeUpScreen(millisSec: Long?) {
        if (millisSec == null || millisSec < 0) {
            return
        }
        MMKV.defaultMMKV().putLong(WAKE_SCREEN_SECOND, millisSec)
    }

    fun getStatueOfProximity(): Int {
        return MMKV.defaultMMKV().getInt(PROXIMITY_STATUS, DEFAULT_VALUE_OF_PROXIMITY)
    }

    fun setStateOfProximitySwitch(state: Int?) {
        if (state == null) {
            return
        }
        MMKV.defaultMMKV().putInt(PROXIMITY_STATUS, state)
    }

    fun getSwitchOfProximity(): Boolean {
        return MMKV.defaultMMKV().getBoolean(PROXIMITY_SWITCH, DEFAULT_SWITCH_OF_PROXIMITY)
    }

    fun setSwitchOfProximity(switch: Boolean?) {
        MMKV.defaultMMKV().putBoolean(PROXIMITY_SWITCH, switch == true)
    }

}