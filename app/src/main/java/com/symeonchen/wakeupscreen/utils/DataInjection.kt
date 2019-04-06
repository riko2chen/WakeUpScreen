package com.symeonchen.wakeupscreen.utils

import com.symeonchen.wakeupscreen.data.SCConstant
import com.tencent.mmkv.MMKV

object DataInjection {

    fun getSwitchOfCustom(): Boolean {
        return MMKV.defaultMMKV().getBoolean(SCConstant.CUSTOM_STATUS, false)
    }

    fun setSwitchOfCustom(it: Boolean?) {
        MMKV.defaultMMKV().putBoolean(SCConstant.CUSTOM_STATUS, it == true)
    }


    fun getSecondOfWakeUpScreen(): Int {
        return MMKV.defaultMMKV().getInt(SCConstant.WAKE_SCREEN_SECOND, 2)
    }

    fun setSecondOfWakeUpScreen(sec: Int?) {
        if (sec == null || sec < 0) {
            return
        }
        MMKV.defaultMMKV().putInt(SCConstant.WAKE_SCREEN_SECOND, sec)
    }

    fun getStatueOfProximity(): Int {
        return MMKV.defaultMMKV().getInt(SCConstant.PROXIMITY_STATUS, 1)
    }

    fun setStateOfProximitySwitch(state: Int?) {
        if (state == null) {
            return
        }
        MMKV.defaultMMKV().putInt(SCConstant.PROXIMITY_STATUS, state)
    }

    fun getSwitchOfProximity(): Boolean {
        return MMKV.defaultMMKV().getBoolean(SCConstant.PROXIMITY_SWITCH, true)
    }

    fun setSwitchOfProximity(switch: Boolean?) {
        MMKV.defaultMMKV().putBoolean(SCConstant.PROXIMITY_SWITCH, switch == true)
    }

}